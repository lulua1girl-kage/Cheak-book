package org.nori.assistant.brain

import org.junit.Assert.assertEquals
import org.junit.Test

class TrainedIntentModelTest {
    private val model = TrainedIntentModel.fromJson(
        """
        {
          "model_version": 1,
          "algorithm": "multinomial_naive_bayes",
          "language": "en",
          "classes": ["GREETING", "STUDY_HELP"],
          "vocabulary": ["hello", "study", "chemistry"],
          "log_priors": {"GREETING": -0.69, "STUDY_HELP": -0.69},
          "log_likelihoods": {
            "GREETING": {"hello": -0.1, "study": -2.0, "chemistry": -2.0},
            "STUDY_HELP": {"hello": -2.0, "study": -0.1, "chemistry": -0.1}
          },
          "single_token_overrides": {"hello": "GREETING"},
          "min_known_tokens": 2,
          "confidence_threshold": 0.5,
          "margin_threshold": 0.1
        }
        """.trimIndent()
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
