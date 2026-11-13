package org.nori.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProgressScreen(vm: NoriViewModel) {
    val awards by vm.meritAwards.collectAsStateWithLifecycle()
    val schedules by vm.schedules.collectAsStateWithLifecycle()
    val completions by vm.dailyCompletions.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val times = remember { mutableStateMapOf<String, Pair<String, String>>() }
    LaunchedEffect(Unit) { vm.ensureDefaultSchedules() }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val points = awards.sumOf { it.points }
    val completedDates = completions.map { it.date }.toSet()
    var status by remember { mutableStateOf("Schedules are disabled until you enable them.") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Progress & awards", style = MaterialTheme.typography.headlineSmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("$points merit points", style = MaterialTheme.typography.headlineMedium)
                Text("${completedDates.size} qualifying days recorded")
                Text("Daily completion earns 10 points. Seven-day milestones earn 25 points.")
            }
        }
        Text("Award cabinet", style = MaterialTheme.typography.titleLarge)
        if (awards.isEmpty()) Text("No awards earned yet. Complete every required task on a day's list to qualify.")
        LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(awards, key = { it.id }) { award ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(award.title, style = MaterialTheme.typography.titleMedium)
                        Text(award.description)
                        Text("+${award.points} merit · ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(award.earnedAt))}")
                    }
                }
            }
        }
        Text("Scheduled messages", style = MaterialTheme.typography.titleLarge)
        Text("Choose a time, then enable a reminder. Android notification permission is required.")
        if (Build.VERSION.SDK_INT >= 33) {
            Button(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else status = "Notification permission is already enabled."
            }) { Text("Enable notification permission") }
        }
        Text(status, style = MaterialTheme.typography.bodySmall)
        schedules.forEach { item ->
            val current = times[item.kind] ?: ("%02d".format(item.hour) to "%02d".format(item.minute))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        Checkbox(checked = item.enabled, onCheckedChange = {
                            vm.saveSchedule(item.copy(enabled = it))
                            status = if (it) "Reminder enabled. Check notification permission." else "Reminder paused."
                        })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(current.first, { times[item.kind] = it to current.second },
                            label = { Text("Hour") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(current.second, { times[item.kind] = current.first to it },
                            label = { Text("Minute") }, modifier = Modifier.weight(1f), singleLine = true)
                        Button(onClick = {
                            val h = current.first.toIntOrNull()
                            val m = current.second.toIntOrNull()
                            if (h != null && m != null && h in 0..23 && m in 0..59) {
                                vm.saveSchedule(item.copy(hour = h, minute = m))
                                status = "Time saved for ${item.title}."
                            } else status = "Enter a valid 24-hour time."
                        }) { Text("Save") }
                    }
                    Text(when (item.kind) {
                        "weekly" -> "Repeats every Sunday"
                        "workout" -> "Daily reminder; enable only on workout days"
                        else -> "Repeats every day"
                    }, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
