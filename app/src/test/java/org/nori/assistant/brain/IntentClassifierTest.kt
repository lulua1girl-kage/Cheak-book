package org.nori.assistant.brain

import org.junit.Assert.assertEquals
import org.junit.Test

class IntentClassifierTest {
    private val classifier = IntentClassifier()

    @Test fun recognizesGreeting() {
        assertEquals(NoriIntent.GREETING, classifier.classify("hello").intent)
    }

    @Test fun routesStudyRequest() {
        assertEquals(NoriIntent.STUDY_HELP, classifier.classify("help me revise chemistry").intent)
    }

    @Test fun ambiguousInputStaysUnknown() {
        assertEquals(NoriIntent.UNKNOWN, classifier.classify("hello, help me study").intent)
    }

    @Test fun blankInputStaysUnknown() {
        assertEquals(NoriIntent.UNKNOWN, classifier.classify("   ").intent)
    }

    @Test fun shortKeywordDoesNotMatchInsideAnotherWord() {
        assertEquals(NoriIntent.UNKNOWN, classifier.classify("do something unusual").intent)
    }

    @Test fun multipleTermsFromSameIntentRemainValid() {
        assertEquals(NoriIntent.STUDY_HELP, classifier.classify("help me study chemistry").intent)
    }
}
