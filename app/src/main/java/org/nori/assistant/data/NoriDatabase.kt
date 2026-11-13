package org.nori.assistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DailyCheckIn::class, NoriTask::class, AuditRecord::class, ChatMessage::class, MeritAward::class, DailyCompletion::class, NoriSchedule::class],
    version = 3,
    exportSchema = true
)
abstract class NoriDatabase : RoomDatabase() {
    abstract fun noriDao(): NoriDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS chat_messages (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "role TEXT NOT NULL, " +
                        "content TEXT NOT NULL, " +
                        "createdAt INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS merit_awards (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, awardKey TEXT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, points INTEGER NOT NULL, earnedAt INTEGER NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_merit_awards_awardKey ON merit_awards(awardKey)")
                database.execSQL("CREATE TABLE IF NOT EXISTS daily_completions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date TEXT NOT NULL, completedAt INTEGER NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daily_completions_date ON daily_completions(date)")
                database.execSQL("CREATE TABLE IF NOT EXISTS nori_schedules (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, kind TEXT NOT NULL, title TEXT NOT NULL, hour INTEGER NOT NULL, minute INTEGER NOT NULL, dayOfWeek INTEGER NOT NULL, enabled INTEGER NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_nori_schedules_kind ON nori_schedules(kind)")
            }
        }

        @Volatile private var INSTANCE: NoriDatabase? = null

        fun getInstance(context: Context): NoriDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NoriDatabase::class.java,
                    "nori-local.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { INSTANCE = it }
            }
    }
}
