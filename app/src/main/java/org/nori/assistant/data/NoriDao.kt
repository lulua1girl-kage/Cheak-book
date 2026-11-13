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

    @Query("SELECT * FROM chat_messages ORDER BY createdAt ASC, id ASC")
    fun observeChatMessages(): Flow<List<ChatMessage>>

    @Insert
    suspend fun addChatMessage(item: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()
}


    // Merit, streak and schedule persistence
    @Query("SELECT * FROM tasks WHERE dueDate = :date ORDER BY createdAt ASC")
    suspend fun getTasksForDate(date: String): List<NoriTask>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addDailyCompletion(item: DailyCompletion): Long

    @Query("SELECT date FROM daily_completions ORDER BY date DESC")
    suspend fun getCompletionDates(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMeritAward(item: MeritAward): Long

    @Query("SELECT * FROM merit_awards ORDER BY earnedAt DESC")
    fun observeMeritAwards(): Flow<List<MeritAward>>

    @Query("SELECT * FROM nori_schedules ORDER BY id ASC")
    fun observeSchedules(): Flow<List<NoriSchedule>>

    @Query("SELECT * FROM nori_schedules WHERE kind = :kind LIMIT 1")
    suspend fun getSchedule(kind: String): NoriSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSchedule(item: NoriSchedule)

    @Query("SELECT * FROM daily_completions ORDER BY date DESC")
    fun observeDailyCompletions(): Flow<List<DailyCompletion>>
}
