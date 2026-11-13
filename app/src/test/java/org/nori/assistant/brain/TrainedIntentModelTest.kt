package org.nori.assistant.brain

import org.junit.Assert.assertEquals
import org.junit.Test

class TrainedIntentModelTest {
    private val model = TrainedIntentModel(
        classes = listOf(NoriIntent.GREETING, NoriIntent.STUDY_HELP),
        vocabulary = setOf("hello", "study", "chemistry"),
        logPriors = mapOf(
            NoriIntent.GREETING to -0.69,
            NoriIntent.STUDY_HELP to -0.69
        ),
        logLikelihoods = mapOf(
            NoriIntent.GREETING to mapOf("hello" to -0.1, "study" to -2.0, "chemistry" to -2.0),
            NoriIntent.STUDY_HELP to mapOf("hello" to -2.0, "study" to -0.1, "chemistry" to -0.1)
        ),
        singleTokenOverrides = mapOf("hello" to NoriIntent.GREETING),
        minKnownTokens = 2,
        confidenceThreshold = 0.5,
        marginThreshold = 0.1
    )

    @Test fun exactSingleWordUsesExplicitOverride() {
        assertEquals(NoriIntent.GREETING, model.classify("hello").intent)
    }

    @Test fun multiwordStudyRequestUsesTrainedProbabilities() {
        assertEquals(NoriIntent.STUDY_HELP, model.classify("study chemistry").intent)
    }

    @Test fun unknownInputDoesNotForceAClass() {
        assertEquals(NoriIntent.UNKNOWN, model.classify("do something unusual").intent)
    }
}
