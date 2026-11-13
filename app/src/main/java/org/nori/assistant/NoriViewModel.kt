package org.nori.assistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.nori.assistant.data.AuditRecord
import org.nori.assistant.data.DailyCheckIn
import org.nori.assistant.data.NoriDatabase
import org.nori.assistant.data.NoriRepository
import org.nori.assistant.data.NoriTask
import org.nori.assistant.brain.AssistantResponseEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoriViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NoriRepository(NoriDatabase.getInstance(application).noriDao())
    private val responseEngine = AssistantResponseEngine()

    fun reply(message: String): String = responseEngine.respond(message)

    val checkIns = repository.checkIns.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val tasks = repository.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val audits = repository.audits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
        viewModelScope.launch { repository.addTask(NoriTask(title = title.trim())) }
    }

    fun setTaskComplete(task: NoriTask, complete: Boolean) {
        viewModelScope.launch { repository.updateTask(task.copy(isComplete = complete)) }
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
        viewModelScope.launch { repository.updateAudit(item.copy(isVerified = verified)) }
    }
}
