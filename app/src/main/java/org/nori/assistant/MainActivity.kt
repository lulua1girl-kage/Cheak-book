package org.nori.assistant

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.nori.assistant.data.NoriTask
import org.nori.assistant.voice.AndroidVoiceController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val destination = when (intent.getStringExtra("open_screen")) {
                    "CHECK_IN" -> Screen.CHECK_IN
                    "PROGRESS" -> Screen.PROGRESS
                    else -> Screen.HOME
                }
                NoriRoot(initialScreen = destination)
            }
        }
    }
}

private enum class Screen(val label: String) {
    HOME("Home"), CHECK_IN("Check-in"), TASKS("Tasks"), AUDIT("Audit"), CHAT("Nori"), PROGRESS("Progress")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoriRoot(initialScreen: Screen = Screen.HOME, vm: NoriViewModel = viewModel()) {
    var screen by remember { mutableStateOf(initialScreen) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Nori") }) },
        bottomBar = {
            NavigationBar {
                Screen.entries.forEach { item ->
                    NavigationBarItem(
                        selected = screen == item,
                        onClick = { screen = item },
                        icon = {},
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Surface(Modifier.fillMaxSize().padding(padding)) {
            when (screen) {
                Screen.HOME -> HomeScreen(vm)
                Screen.CHECK_IN -> CheckInScreen(vm)
                Screen.TASKS -> TasksScreen(vm)
                Screen.AUDIT -> AuditScreen(vm)
                Screen.CHAT -> ChatScreen(vm)
                Screen.PROGRESS -> ProgressScreen(vm)
            }
        }
    }
}

@Composable
private fun HomeScreen(vm: NoriViewModel) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val checkIns by vm.checkIns.collectAsStateWithLifecycle()
    val pending = tasks.count { !it.isComplete }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Personal assistant", style = MaterialTheme.typography.headlineMedium)
        Text("Local-first foundation · Core records work offline")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Today at a glance", style = MaterialTheme.typography.titleLarge)
                Text("Open tasks: $pending")
                Text("Saved check-ins: ${checkIns.size}")
                Text("Progress is based on saved records, not assumptions.")
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Human Override", style = MaterialTheme.typography.titleLarge)
                Text("Nori can recommend. You retain the final decision.")
                Text("No AI model or microphone is active in this foundation build.")
            }
        }
    }
}

@Composable
private fun CheckInScreen(vm: NoriViewModel) {
    var sleep by remember { mutableStateOf("8") }
    var energy by remember { mutableStateOf("Medium") }
    var condition by remember { mutableStateOf("Steady") }
    var note by remember { mutableStateOf("") }
    val checkIns by vm.checkIns.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Daily check-in", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(sleep, { sleep = it }, label = { Text("Sleep hours") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(energy, { energy = it }, label = { Text("Energy (Low / Medium / High)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(condition, { condition = it }, label = { Text("Current condition") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(note, { note = it }, label = { Text("Optional notes") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val parsed = sleep.toFloatOrNull()
            if (parsed != null && parsed in 0f..24f) {
                vm.saveCheckIn(parsed, energy, condition, note)
                note = ""
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Save check-in") }
        Text("Recent check-ins", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(checkIns, key = { it.id }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${item.date} · ${item.sleepHours}h sleep")
                        Text("${item.energy} energy · ${item.condition}")
                        if (item.note.isNotBlank()) Text(item.note)
                    }
                }
            }
        }
    }
}

@Composable
private fun TasksScreen(vm: NoriViewModel) {
    var title by remember { mutableStateOf("") }
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Tasks", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(title, { title = it }, label = { Text("New task") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.addTask(title); title = "" }, modifier = Modifier.fillMaxWidth()) { Text("Add task") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(tasks, key = { it.id }) { task -> TaskRow(task, vm) }
        }
    }
}

@Composable
private fun TaskRow(task: NoriTask, vm: NoriViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isComplete, onCheckedChange = { vm.setTaskComplete(task, it) })
            Text(task.title, modifier = Modifier.weight(1f))
            Button(onClick = { vm.deleteTask(task.id) }) { Text("Delete") }
        }
    }
}

@Composable
private fun AuditScreen(vm: NoriViewModel) {
    var claim by remember { mutableStateOf("") }
    var evidence by remember { mutableStateOf("") }
    val audits by vm.audits.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Evidence audit", style = MaterialTheme.typography.headlineSmall)
        Text("Record a claim and the evidence supporting it. Verify it only when the evidence is sufficient.")
        OutlinedTextField(claim, { claim = it }, label = { Text("Claim (e.g. completed a revision set)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(evidence, { evidence = it }, label = { Text("Evidence / source") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.addAudit(claim, evidence); claim = ""; evidence = "" }, modifier = Modifier.fillMaxWidth()) { Text("Save audit record") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(audits, key = { it.id }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.claim, style = MaterialTheme.typography.titleMedium)
                            Text(if (item.evidence.isBlank()) "No evidence supplied" else item.evidence)
                            Text(if (item.isVerified) "Verified" else "Unverified")
                        }
                        Checkbox(
                            checked = item.isVerified,
                            onCheckedChange = { vm.verifyAudit(item, it) },
                            enabled = item.evidence.isNotBlank() || item.isVerified
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatScreen(vm: NoriViewModel) {
    var input by remember { mutableStateOf("") }
    var voiceStatus by remember { mutableStateOf("Voice is optional. Typed chat always remains available.") }
    var showClearConfirmation by remember { mutableStateOf(false) }
    val messages by vm.chatMessages.collectAsStateWithLifecycle()
    val lastReply = messages.lastOrNull { it.role == "Nori" }?.content.orEmpty()
    val context = LocalContext.current
    val voiceController = remember(context) {
        AndroidVoiceController(
            context = context,
            onTranscript = { transcript -> input = transcript },
            onStatus = { status -> voiceStatus = status }
        )
    }
    DisposableEffect(voiceController) {
        onDispose { voiceController.release() }
    }
    val microphonePermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceController.startListening()
        } else {
            voiceStatus = "Microphone permission was not granted. You can continue typing."
        }
    }

    fun beginVoiceInput() {
        if (!voiceController.supportsOnDeviceRecognition()) {
            voiceStatus = "This device has no available on-device speech recognizer. Typed chat still works."
            return
        }
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (permissionGranted) {
            voiceController.startListening()
        } else {
            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear conversation history?") },
            text = { Text("This permanently clears the locally saved Nori chat on this device. It does not delete check-ins, tasks or audit records.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearChatHistory()
                    showClearConfirmation = false
                }) { Text("Clear history") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Talk to Nori", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = { showClearConfirmation = true }, enabled = messages.isNotEmpty()) {
                Text("Clear chat")
            }
        }
        Text("Conversation history is saved locally on this device. You can clear it at any time.")
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (messages.isEmpty()) {
                item { Text("No saved messages yet. Start a conversation below.") }
            }
            items(messages, key = { it.id }) { message ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(message.role, style = MaterialTheme.typography.labelLarge)
                        Text(message.content)
                    }
                }
            }
        }
        Text(voiceStatus, style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Message or recognized speech") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = ::beginVoiceInput,
                modifier = Modifier.weight(1f)
            ) { Text("Speak") }
            Button(
                onClick = { voiceController.speak(lastReply) },
                enabled = lastReply.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) { Text("Read reply") }
        }
        Button(
            onClick = {
                vm.sendMessage(input)
                input = ""
            },
            enabled = input.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Send") }
    }
}
