# Nori

A local-first Android personal assistant focused on academic planning, daily check-ins, evidence-based audits, and user-controlled memory.

## Project status

**Stage:** Foundation build  
**Current release:** No APK yet  
**Architecture:** Native Android / Kotlin / Jetpack Compose / Room  
**Privacy default:** Core records and conversation history stay on-device. No AI API key or cloud account is required.

This repository is being built in verified stages. Structured records, local chat history and the first optional voice adapter are in the foundation build. A trained classifier and generative language model remain separate milestones.

## Current milestone

- [x] Repository initialized
- [x] Android build scaffold
- [x] Persistent local records
- [x] Daily check-in and task screens
- [x] Evidence-based audit
- [ ] Full CI build and tests passing
- [x] Trained small Naive Bayes intent router with deterministic fallback (not an LLM)
- [x] Optional push-to-talk input and spoken replies (device support dependent)
- [ ] On-device generative language model evaluation

## Build

Open the project in Android Studio (Hedgehog or newer) and sync Gradle. The project uses JDK 17. Before a local Android build, generate the small intent model with `python ml/train_intent_model.py` (Python 3.9+; standard library only). GitHub Actions performs this step automatically.

A GitHub Actions workflow will build the debug APK and attach it to successful workflow runs once the Android project scaffold is committed.

## Voice limitations\n\nSpeech input uses Android's on-device recognizer only; availability and language packs depend on the device. Speech output uses the installed Android TTS engine; offline voices depend on installed voice data. No network speech fallback is enabled.\n\n## Principles

1. Local-first: core records work offline.
2. Evidence before claims: planned work is not completed work.
3. Human Override: recommendations never replace user judgment.
4. Independent modules: a voice or model failure must not disable records or planning.
5. Transparent AI: distinguish deterministic commands, trained classifiers, and generative models.
6. No secrets in source control: no API keys, passwords, signing keys, or private data.

See `docs/ARCHITECTURE.md` and `docs/ROADMAP.md`.
