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

The current code has Room entities and DAO flows for daily check-ins, tasks and audit records. The UI observes those flows and writes through the ViewModel. The baseline intent classifier is a transparent keyword router; it is not machine learning and must not be represented as an LLM.

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
- No INTERNET or RECORD_AUDIO permission is declared in the initial manifest.
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

Voice is a separate adapter:
- Push-to-talk first.
- Speech-to-text adapter.
- Shared message pipeline.
- Text-to-speech adapter.
- Explicit listening state and microphone permission.
- Later evaluation of wake-word behavior.

Voice errors must not stop typed chat, records or other modules. No always-on microphone is planned for the first voice milestone.
