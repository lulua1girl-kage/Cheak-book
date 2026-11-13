package org.nori.assistant.brain

/**
 * A transparent baseline router, not a machine-learning model.
 * Unknown or ambiguous requests stay unknown instead of triggering a guessed action.
 */
enum class NoriIntent { GREETING, STUDY_HELP, TASK_MANAGEMENT, CHECK_IN, MEMORY, UNKNOWN }

data class IntentResult(val intent: NoriIntent, val confidence: Float, val matchedTerm: String? = null)

class IntentClassifier {
    private val patterns = linkedMapOf(
        NoriIntent.GREETING to listOf("hello", "hi", "good morning", "good evening"),
        NoriIntent.STUDY_HELP to listOf("study", "revise", "revision", "chemistry", "math", "physics", "biology", "explain"),
        NoriIntent.TASK_MANAGEMENT to listOf("task", "homework", "deadline", "plan", "schedule", "remind"),
        NoriIntent.CHECK_IN to listOf("check in", "check-in", "sleep", "energy", "tired", "feeling"),
        NoriIntent.MEMORY to listOf("remember", "memory", "saved", "forget")
    )

    fun classify(message: String): IntentResult {
        val normalized = message.trim().lowercase()
        if (normalized.isBlank()) return IntentResult(NoriIntent.UNKNOWN, 0f)
        val matches = patterns.flatMap { (intent, terms) ->
            terms.filter { term -> normalized.contains(term) }.map { intent to it }
        }
        if (matches.size != 1) return IntentResult(NoriIntent.UNKNOWN, 0.25f)
        val (intent, term) = matches.single()
        val confidence = if (normalized == term) 0.95f else 0.72f
        return IntentResult(intent, confidence, term)
    }
}
