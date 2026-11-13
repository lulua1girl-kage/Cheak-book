package org.nori.assistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.nori.assistant.data.AuditRecord
import org.nori.assistant.data.DailyCheckIn
import org.nori.assistant.data.NoriDatabase
import org.nori.assistant.data.NoriRepository
import org.nori.assistant.data.NoriTask
import org.nori.assistant.data.ChatMessage
import org.nori.assistant.data.MeritAward
import org.nori.assistant.data.DailyCompletion
import org.nori.assistant.data.NoriSchedule
import org.nori.assistant.notifications.NoriAlarmScheduler
import org.nori.assistant.brain.AssistantResponseEngine
import org.nori.assistant.brain.IntentClassifier
import org.nori.assistant.brain.TrainedIntentModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.LocalDate

class NoriViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NoriRepository(NoriDatabase.getInstance(application).noriDao())
    private val trainedIntentModel = runCatching {
        application.assets.open("intent_model.json").bufferedReader().use { reader ->
            TrainedIntentModel.fromJson(reader.readText())
        }
    }.getOrNull()
    private val responseEngine = AssistantResponseEngine(IntentClassifier(trainedIntentModel))

    fun reply(message: String): String = responseEngine.respond(message)

    val checkIns = repository.checkIns.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val tasks = repository.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val audits = repository.audits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val chatMessages = repository.chatMessages.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val meritAwards = repository.meritAwards.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val schedules = repository.schedules.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val dailyCompletions = repository.dailyCompletions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun ensureDefaultSchedules() {
        viewModelScope.launch {
            val defaults = listOf(
                NoriSchedule(kind = "daily", title = "Daily check-in", hour = 19, minute = 0, dayOfWeek = 0),
                NoriSchedule(kind = "weekly", title = "Weekly review", hour = 18, minute = 0, dayOfWeek = 7),
                NoriSchedule(kind = "workout", title = "Workout reminder", hour = 16, minute = 0, dayOfWeek = 0)
            )
            defaults.forEach { if (repository.schedule(it.kind) == null) repository.saveSchedule(it) }
        }
    }

    fun saveSchedule(item: NoriSchedule) {
        viewModelScope.launch {
            repository.saveSchedule(item)
            if (item.enabled) NoriAlarmScheduler.schedule(getApplication(), item)
            else NoriAlarmScheduler.cancel(getApplication(), item.id.toInt())
        }
    }

    private fun evaluateDailyCompletion() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val todaysTasks = repository.tasksForDate(today)
            if (todaysTasks.isEmpty() || todaysTasks.any { !it.isComplete }) return@launch
            val inserted = repository.addDailyCompletion(DailyCompletion(date = today))
            if (inserted == -1L) return@launch
            repository.addMeritAward(
                MeritAward(
                    awardKey = "daily-completion:$today",
                    title = "Daily Mission Complete",
                    description = "Every required task on today's list was completed.",
                    points = 10
                )
            )
            val dates = repository.completionDates().toSet()
            var streak = 0
            var cursor = LocalDate.now()
            while (dates.contains(cursor.toString())) {
                streak++
                cursor = cursor.minusDays(1)
            }
            if (streak >= 7 && streak % 7 == 0) {
                repository.addMeritAward(
                    MeritAward(
                        awardKey = "streak-$streak:$today",
                        title = "$streak-Day Consistency",
                        description = "Maintained a $streak-day daily completion streak.",
                        points = 25
                    )
                )
            }
        }
    }

    fun saveCheckIn(sleep: Float, energy: String, condition: String, note: String) {
        viewModelScope.launch {
            repository.saveCheckIn(
                DailyCheckIn(
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                    sleepHours = sleep.coerceIn(0f, 24f),
                    energy = energy,
                    condition = condition,
                    note = note.trim()
                )
            )
        }
    }

    fun addTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { repository.addTask(NoriTask(title = title.trim(), dueDate = LocalDate.now().toString())) }
    }

    fun setTaskComplete(task: NoriTask, complete: Boolean) {
        viewModelScope.launch { repository.updateTask(task.copy(isComplete = complete)); evaluateDailyCompletion() }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch { repository.deleteTask(id) }
    }

    fun addAudit(claim: String, evidence: String) {
        if (claim.isBlank()) return
        viewModelScope.launch {
            repository.addAudit(AuditRecord(claim = claim.trim(), evidence = evidence.trim()))
        }
    }

    fun verifyAudit(item: AuditRecord, verified: Boolean) {
        if (verified && item.evidence.isBlank()) return
        viewModelScope.launch { repository.updateAudit(item.copy(isVerified = verified)) }
    }

    fun sendMessage(message: String) {
        val clean = message.trim()
        if (clean.isBlank()) return
        viewModelScope.launch {
            repository.addChatMessage(ChatMessage(role = "You", content = clean))
            val response = responseEngine.respond(clean)
            repository.addChatMessage(ChatMessage(role = "Nori", content = response))
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch { repository.clearChatMessages() }
    }
}
