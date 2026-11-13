# Build roadmap

## Milestone 0 — repository foundation
- [x] Initialize Android project
- [x] Configure Kotlin, Compose, Room and CI workflow
- [x] Add local entities, DAO, repository and ViewModel
- [x] Add first screens for check-ins, tasks and audit
- [x] Add baseline intent router and unit tests
- [ ] Verify CI build and resolve all failures

## Milestone 1 — data integrity
- [ ] Add validation and clear error states
- [ ] Add edit flows and date-aware task fields
- [ ] Add database migration tests
- [ ] Add export/import with explicit user action
- [ ] Add accessibility and small-screen UI tests

## Milestone 2 — language understanding
- [ ] Define a labelled Nori intent dataset from user-approved examples
- [ ] Train and evaluate a small classifier
- [ ] Add entity/date extraction and conversation state
- [ ] Add confidence calibration and clarification behavior
- [ ] Keep rules as a reliable fallback

## Milestone 3 — voice
- [ ] Compare Android speech recognition options and offline availability
- [ ] Add push-to-talk permission flow
- [ ] Add text-to-speech adapter
- [ ] Test pauses, interruptions and recoverable failures
- [ ] Evaluate wake word only after battery/privacy review

## Milestone 4 — on-device LLM
- [ ] Confirm exact target-device RAM, free storage and Android version
- [ ] Compare LiteRT-LM, llama.cpp and another supported mobile runtime
- [ ] Select small candidate models and inspect model licenses
- [ ] Benchmark quality, memory, speed, heat and battery
- [ ] Integrate only if practical; keep the app functional without it

## Milestone 5 — release readiness
- [ ] Automated tests pass
- [ ] Manual device test completed
- [ ] Privacy and permission review
- [ ] User reviews feature set and limitations
- [ ] Build and share APK only after review
- [ ] Production signing key managed outside the repository

## Release rule

A green build is not the same as a tested application. APK delivery requires successful CI, installation on a physical device, core-flow verification, and a user review.
