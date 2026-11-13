package org.nori.assistant.data

class NoriRepository(private val dao: NoriDao) {
    val checkIns = dao.observeCheckIns()
    val tasks = dao.observeTasks()
    val audits = dao.observeAudits()

    suspend fun saveCheckIn(item: DailyCheckIn) = dao.addCheckIn(item)
    suspend fun addTask(item: NoriTask) = dao.addTask(item)
    suspend fun updateTask(item: NoriTask) = dao.updateTask(item)
    suspend fun deleteTask(id: Long) = dao.deleteTask(id)
    suspend fun addAudit(item: AuditRecord) = dao.addAudit(item)
    suspend fun updateAudit(item: AuditRecord) = dao.updateAudit(item)

    val chatMessages = dao.observeChatMessages()
    suspend fun addChatMessage(item: ChatMessage) = dao.addChatMessage(item)
    suspend fun clearChatMessages() = dao.clearChatMessages()

    suspend fun tasksForDate(date: String) = dao.getTasksForDate(date)
    suspend fun addDailyCompletion(item: DailyCompletion) = dao.addDailyCompletion(item)
    suspend fun completionDates() = dao.getCompletionDates()
    suspend fun addMeritAward(item: MeritAward) = dao.addMeritAward(item)
    val meritAwards = dao.observeMeritAwards()
    val schedules = dao.observeSchedules()
    val dailyCompletions = dao.observeDailyCompletions()
    suspend fun schedule(kind: String) = dao.getSchedule(kind)
    suspend fun saveSchedule(item: NoriSchedule) = dao.saveSchedule(item)
}
