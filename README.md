# Nori

A local-first Android personal assistant focused on academic planning, daily check-ins, evidence-based audits, and user-controlled memory.

## Project status

**Stage:** Foundation build  
**Current release:** No APK yet  
**Architecture:** Native Android / Kotlin / Jetpack Compose / Room  
**Privacy default:** Core records stay on-device. No AI API key or cloud account is required.

This repository is being built in verified stages. The first milestone is a working offline foundation; language intelligence and voice are separate milestones.

## Current milestone

- [x] Repository initialized
- [ ] Android build scaffold
- [ ] Persistent local records
- [ ] Daily check-in and task screens
- [ ] Evidence-based audit
- [ ] Tests and CI build
- [ ] Language understanding
- [ ] Speech input/output
- [ ] On-device language model evaluation

## Build

Open the project in Android Studio (Hedgehog or newer) and sync Gradle. The project uses JDK 17.

A GitHub Actions workflow will build the debug APK and attach it to successful workflow runs once the Android project scaffold is committed.

## Principles

1. Local-first: core records work offline.
2. Evidence before claims: planned work is not completed work.
3. Human Override: recommendations never replace user judgment.
4. Independent modules: a voice or model failure must not disable records or planning.
5. Transparent AI: distinguish deterministic commands, trained classifiers, and generative models.
6. No secrets in source control: no API keys, passwords, signing keys, or private data.

See `docs/ARCHITECTURE.md` and `docs/ROADMAP.md`.
