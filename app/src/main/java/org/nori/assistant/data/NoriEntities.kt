package org.nori.assistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_checkins")
data class DailyCheckIn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val sleepHours: Float,
    val energy: String,
    val condition: String,
    val note: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class NoriTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "Personal",
    val dueDate: String = "",
    val isComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_records")
data class AuditRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val claim: String,
    val evidence: String,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)


@Entity(tableName = "merit_awards", indices = [androidx.room.Index(value = ["awardKey"], unique = true)])
data class MeritAward(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val awardKey: String,
    val title: String,
    val description: String,
    val points: Int,
    val earnedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_completions", indices = [androidx.room.Index(value = ["date"], unique = true)])
data class DailyCompletion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "nori_schedules", indices = [androidx.room.Index(value = ["kind"], unique = true)])
data class NoriSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val title: String,
    val hour: Int,
    val minute: Int,
    val dayOfWeek: Int,
    val enabled: Boolean = false
)
