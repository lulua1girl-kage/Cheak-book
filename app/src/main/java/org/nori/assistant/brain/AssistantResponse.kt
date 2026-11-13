package org.nori.assistant.brain

/**
 * Safe, transparent response routing for the first build.
 * This is intentionally modest: it does not pretend to be a generative AI.
 */
class AssistantResponseEngine(
    private val classifier: IntentClassifier = IntentClassifier()
) {
    fun respond(message: String): String {
        return when (classifier.classify(message).intent) {
            NoriIntent.GREETING -> "Hello. What would you like to work on?"
            NoriIntent.STUDY_HELP -> "Let's identify the subject, exact topic and what you have tried. I can then guide the next step."
            NoriIntent.TASK_MANAGEMENT -> "I can help organize tasks. Tell me the task and its deadline; I will not mark it complete without evidence."
            NoriIntent.CHECK_IN -> "A check-in should reflect your actual sleep, energy and condition. You can record it in the Check-in screen."
            NoriIntent.MEMORY -> "Memory controls are not implemented yet. This build stores only the structured records shown in the app."
            NoriIntent.UNKNOWN -> "I am not confident I understood that. Please rephrase or choose a screen. I will not guess an action."
        }
    }
}
