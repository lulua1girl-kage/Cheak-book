# Nori architecture

## Current implemented foundation

```text
Compose UI
   |
NoriViewModel
   |
NoriRepository
   |
Room DAO
   |
nori-local.db (device-local)
```

The current code has Room entities and DAO flows for daily check-ins, tasks, audit records and chat history. Conversation history is stored locally and can be cleared from the chat screen. The baseline intent classifier is a transparent keyword router; it is not machine learning and must not be represented as an LLM.

## Planned intelligence pipeline

```text
User message
   |
Normalization and language detection
   |
Intent + entity extraction
   |
Conversation state / relevant local memory
   |
Confidence and permission gate
   |
Task-specific engine
   |
Response or confirmed action
```

The system should ask a clarification when intent or entities are ambiguous. A classifier may suggest an intent; it cannot authorize a consequential action.

## Local data boundaries

- Room is the source of truth for structured records.
- Core check-in, task and audit flows do not require network access.
- No INTERNET permission is declared. RECORD_AUDIO is requested only when the user taps Speak; microphone hardware is optional.
- Model weights, if added, are separate optional assets and are not bundled into the base APK by default.
- Backups should be user-initiated and clearly explain what is exported.

## AI runtime decision

Candidate runtime: Google's LiteRT-LM Android/Kotlin API. This remains a candidate, not an integrated dependency. Before selection, compare at least one alternative runtime and benchmark on the actual target phone.

Benchmark dimensions:
- Model file size and download cost.
- Peak memory and process stability.
- Prompt processing and token generation latency.
- Battery and thermal behavior.
- Quality on Nori's real task set.
- Offline operation and data flow.
- Runtime compatibility with the device's Android version and chipset.

A small on-device model is optional assistance, not the authority for academic records or task completion.

## Voice design

Voice is a separate Android adapter:
- User-initiated push-to-talk.
- Android on-device SpeechRecognizer only; no cloud-recognition fallback.
- Transcript returns to the same typed-message field and requires the user to press Send.
- Android TextToSpeech for optional spoken replies.
- Explicit microphone permission request and listening status.
- Availability and offline language/voice data depend on installed Android speech services.

Voice errors must not stop typed chat, records or other modules. No always-on microphone or wake-word listener is enabled.
