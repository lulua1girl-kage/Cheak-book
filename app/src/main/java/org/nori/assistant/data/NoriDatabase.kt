package org.nori.assistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailyCheckIn::class, NoriTask::class, AuditRecord::class],
    version = 1,
    exportSchema = true
)
abstract class NoriDatabase : RoomDatabase() {
    abstract fun noriDao(): NoriDao

    companion object {
        @Volatile private var INSTANCE: NoriDatabase? = null

        fun getInstance(context: Context): NoriDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NoriDatabase::class.java,
                    "nori-local.db"
                ).build().also { INSTANCE = it }
            }
    }
}
