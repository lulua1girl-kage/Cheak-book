package org.nori.assistant.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.nori.assistant.MainActivity
import org.nori.assistant.data.NoriSchedule

class NoriReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val kind = intent.getStringExtra("kind") ?: "daily"
        val title = intent.getStringExtra("title") ?: "Nori reminder"
        val id = intent.getIntExtra("schedule_id", 0)
        val hour = intent.getIntExtra("hour", 19)
        val minute = intent.getIntExtra("minute", 0)
        val day = intent.getIntExtra("day_of_week", 0)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_screen", if (kind == "weekly") "PROGRESS" else "CHECK_IN")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val content = PendingIntent.getActivity(context, 9000 + id, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(
                "nori_reminders", "Nori reminders", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val allowed = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (allowed) {
            val body = when (kind) {
                "weekly" -> "Your weekly review is ready. Open Nori to review the week."
                "workout" -> "Your planned workout reminder. Check today's workload and energy first."
                else -> "Your daily check-in is ready. Record schoolwork, FutureX and anything extra."
            }
            val notification = NotificationCompat.Builder(context, "nori_reminders")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title).setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(content).setAutoCancel(true).build()
            NotificationManagerCompat.from(context).notify(9000 + id, notification)
        }
        NoriAlarmScheduler.schedule(context, NoriSchedule(
            id = id.toLong(), kind = kind, title = title, hour = hour,
            minute = minute, dayOfWeek = day, enabled = true))
    }
}
