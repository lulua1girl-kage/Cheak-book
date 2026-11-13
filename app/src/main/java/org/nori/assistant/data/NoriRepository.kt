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
}
