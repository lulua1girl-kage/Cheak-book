package org.nori.assistant.brain

import org.json.JSONObject
import kotlin.math.exp

/**
 * Tiny on-device Multinomial Naive Bayes intent model exported by ml/train_intent_model.py.
 * It is an intent router, not a conversational language model.
 */
class TrainedIntentModel internal constructor(
    private val classes: List<NoriIntent>,
    private val vocabulary: Set<String>,
    private val logPriors: Map<NoriIntent, Double>,
    private val logLikelihoods: Map<NoriIntent, Map<String, Double>>,
    private val singleTokenOverrides: Map<String, NoriIntent>,
    private val minKnownTokens: Int,
    private val confidenceThreshold: Double,
    private val marginThreshold: Double
) {
    fun classify(message: String): IntentResult {
        val tokens = TOKEN_REGEX.findAll(message.lowercase()).map { it.value }.toList()
        if (tokens.isEmpty()) return IntentResult(NoriIntent.UNKNOWN, 0f)

        if (tokens.size == 1) {
            val exact = singleTokenOverrides[tokens.first()]
            return if (exact != null) IntentResult(exact, 0.99f, tokens.first())
            else IntentResult(NoriIntent.UNKNOWN, 0f)
        }

        val knownTokens = tokens.filter { it in vocabulary }
        if (knownTokens.size < minKnownTokens) return IntentResult(NoriIntent.UNKNOWN, 0f)

        val scores = classes.associateWith { intent ->
            (logPriors[intent] ?: 0.0) +
                knownTokens.sumOf { token -> logLikelihoods[intent]?.get(token) ?: 0.0 }
        }
        val maxScore = scores.values.maxOrNull() ?: return IntentResult(NoriIntent.UNKNOWN, 0f)
        val exponentials = scores.mapValues { (_, score) -> exp(score - maxScore) }
        val total = exponentials.values.sum()
        if (total <= 0.0) return IntentResult(NoriIntent.UNKNOWN, 0f)

        val ranked = exponentials.mapValues { (_, value) -> value / total }
            .entries.sortedByDescending { it.value }
        val best = ranked.firstOrNull() ?: return IntentResult(NoriIntent.UNKNOWN, 0f)
        val secondConfidence = ranked.getOrNull(1)?.value ?: 0.0
        val confidence = best.value

        if (confidence < confidenceThreshold || confidence - secondConfidence < marginThreshold) {
            return IntentResult(NoriIntent.UNKNOWN, confidence.toFloat())
        }
        return IntentResult(best.key, confidence.toFloat())
    }

    companion object {
        private val TOKEN_REGEX = Regex("[a-z0-9']+")

        fun fromJson(source: String): TrainedIntentModel {
            val root = JSONObject(source)
            require(root.optInt("model_version") == 1) { "Unsupported intent model version" }

            val classesJson = root.getJSONArray("classes")
            val classes = (0 until classesJson.length()).map {
                NoriIntent.valueOf(classesJson.getString(it))
            }
            val vocabularyJson = root.getJSONArray("vocabulary")
            val vocabulary = (0 until vocabularyJson.length()).map { vocabularyJson.getString(it) }.toSet()

            val priorsJson = root.getJSONObject("log_priors")
            val priors = classes.associateWith { priorsJson.getDouble(it.name) }

            val likelihoodsJson = root.getJSONObject("log_likelihoods")
            val likelihoods = classes.associateWith { intent ->
                val classJson = likelihoodsJson.getJSONObject(intent.name)
                vocabulary.associateWith { token -> classJson.getDouble(token) }
            }

            val overridesJson = root.getJSONObject("single_token_overrides")
            val overrides = mutableMapOf<String, NoriIntent>()
            val keys = overridesJson.keys()
            while (keys.hasNext()) {
                val token = keys.next()
                overrides[token] = NoriIntent.valueOf(overridesJson.getString(token))
            }

            return TrainedIntentModel(
                classes = classes,
                vocabulary = vocabulary,
                logPriors = priors,
                logLikelihoods = likelihoods,
                singleTokenOverrides = overrides,
                minKnownTokens = root.optInt("min_known_tokens", 2),
                confidenceThreshold = root.optDouble("confidence_threshold", 0.42),
                marginThreshold = root.optDouble("margin_threshold", 0.06)
            )
        }
    }
}
