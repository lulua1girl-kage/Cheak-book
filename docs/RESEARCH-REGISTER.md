# Nori research register

Status: initial references recorded; comprehensive application review not complete.

| Candidate | Area | Why it matters | Current assessment |
|---|---|---|---|
| OpenJarvis (Stanford) | Local personal AI | Modular agents, local model backends, tools and memory | Architecture reference; not a ready-made Android app |
| PhonosAssist | Android voice assistant | Speech input/output and Android integration | Useful voice reference; default LLM deployment depends on a reachable server |
| LiteRT-LM | On-device model runtime | Kotlin API for local language models on Android | Strong candidate; benchmark on target device before adoption |
| llama.cpp | Local inference runtime | Broad model support and Android examples | Candidate alternative; native integration complexity to assess |
| Android offline-first guidance | Data architecture | Local source of truth and optional synchronization | Applied to the current Room-first design |

## Screening plan: 200 applications

| Category | Target |
|---|---:|
| AI assistants and agents | 30 |
| Voice assistants | 25 |
| Local AI/model apps | 25 |
| Education and study | 25 |
| Task and project management | 20 |
| Habit and time tracking | 20 |
| Notes and personal memory | 20 |
| Android automation | 15 |
| Dashboards and analytics | 10 |
| Recovery and workload management | 10 |
| **Total** | **200** |

Each candidate must be verified from its official site, repository or store listing. Capture platform support, feature evidence, source availability, license, offline behavior, external service requirements, storage, permissions, hardware requirements, privacy implications and lessons for Nori.

Do not count a project as open source without checking its actual license. Do not infer offline support from a marketing phrase. Distinguish a local speech engine from a fully offline assistant: a local STT/TTS stack may still send the recognized text to a cloud LLM.

## Known reference links

- OpenJarvis: https://github.com/stanford-oval/storm
- llama.cpp: https://github.com/ggml-org/llama.cpp
- LiteRT: https://ai.google.dev/edge/litert
- Android architecture: https://developer.android.com/topic/architecture
- PhonosAssist: https://github.com/PromtEngineer/PhonosAssist

Links are starting points for validation, not endorsements or permission to reuse code.
