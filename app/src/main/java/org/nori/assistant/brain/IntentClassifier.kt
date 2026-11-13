package org.nori.assistant.brain

/**
 * Uses the trained local model when it is available; otherwise uses a transparent rules fallback.
 */
enum class NoriIntent { GREETING, STUDY_HELP, TASK_MANAGEMENT, CHECK_IN, MEMORY, UNKNOWN }

data class IntentResult(val intent: NoriIntent, val confidence: Float, val matchedTerm: String? = null)

class IntentClassifier(
    private val trainedModel: TrainedIntentModel? = null
) {
    private val patterns = linkedMapOf(
        NoriIntent.GREETING to listOf("hello", "hi", "good morning", "good evening"),
        NoriIntent.STUDY_HELP to listOf("study", "revise", "revision", "chemistry", "math", "physics", "biology", "explain"),
        NoriIntent.TASK_MANAGEMENT to listOf("task", "homework", "deadline", "plan", "schedule", "remind"),
        NoriIntent.CHECK_IN to listOf("check in", "check-in", "sleep", "energy", "tired", "feeling"),
        NoriIntent.MEMORY to listOf("remember", "memory", "saved", "forget")
    )

    fun classify(message: String): IntentResult {
        if (trainedModel != null) return trainedModel.classify(message)

        val normalized = message.trim().lowercase()
        if (normalized.isBlank()) return IntentResult(NoriIntent.UNKNOWN, 0f)
        val matches = patterns.flatMap { (intent, terms) ->
            terms.filter { term ->
                Regex("(^|[^A-Za-z0-9])" + Regex.escape(term) + "($|[^A-Za-z0-9])")
                    .containsMatchIn(normalized)
            }.map { intent to it }
        }
        val distinctIntents = matches.map { it.first }.distinct()
        if (distinctIntents.size != 1) return IntentResult(NoriIntent.UNKNOWN, 0.25f)
        val intent = distinctIntents.single()
        val term = matches.first { it.first == intent }.second
        val confidence = if (normalized == term) 0.95f else 0.72f
        return IntentResult(intent, confidence, term)
    }
}
