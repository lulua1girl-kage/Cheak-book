package org.nori.assistant.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.nori.assistant.data.NoriSchedule
import java.time.LocalDateTime
import java.time.ZoneId

object NoriAlarmScheduler {
    fun schedule(context: Context, item: NoriSchedule) {
        if (!item.enabled) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NoriReminderReceiver::class.java).apply {
            putExtra("schedule_id", item.id.toInt())
            putExtra("kind", item.kind)
            putExtra("title", item.title)
            putExtra("hour", item.hour)
            putExtra("minute", item.minute)
            putExtra("day_of_week", item.dayOfWeek)
        }
        val pending = PendingIntent.getBroadcast(context, item.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val now = LocalDateTime.now()
        var next = now.withHour(item.hour).withMinute(item.minute).withSecond(0).withNano(0)
        if (item.dayOfWeek in 1..7) {
            val delta = (item.dayOfWeek - next.dayOfWeek.value + 7) % 7
            next = next.plusDays(delta.toLong())
        }
        if (!next.isAfter(now)) next = next.plusDays(if (item.dayOfWeek in 1..7) 7 else 1)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), pending)
    }

    fun cancel(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = PendingIntent.getBroadcast(context, requestCode,
            Intent(context, NoriReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (pending != null) alarmManager.cancel(pending)
    }
}
