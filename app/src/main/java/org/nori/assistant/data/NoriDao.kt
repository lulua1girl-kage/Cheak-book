package org.nori.assistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoriDao {
    @Query("SELECT * FROM daily_checkins ORDER BY createdAt DESC")
    fun observeCheckIns(): Flow<List<DailyCheckIn>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCheckIn(item: DailyCheckIn)

    @Query("SELECT * FROM tasks ORDER BY isComplete ASC, createdAt DESC")
    fun observeTasks(): Flow<List<NoriTask>>

    @Insert
    suspend fun addTask(item: NoriTask)

    @Update
    suspend fun updateTask(item: NoriTask)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)

    @Query("SELECT * FROM audit_records ORDER BY createdAt DESC")
    fun observeAudits(): Flow<List<AuditRecord>>

    @Insert
    suspend fun addAudit(item: AuditRecord)

    @Update
    suspend fun updateAudit(item: AuditRecord)
}
