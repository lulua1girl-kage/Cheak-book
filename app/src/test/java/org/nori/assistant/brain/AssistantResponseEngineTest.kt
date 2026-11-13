package org.nori.assistant.brain

import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantResponseEngineTest {
    private val engine = AssistantResponseEngine()

    @Test fun unknownRequestsAskForClarification() {
        assertTrue(engine.respond("do something unusual").contains("not confident"))
    }

    @Test fun studyRequestsUseAttemptFirstGuidance() {
        assertTrue(engine.respond("help me revise chemistry").contains("what you have tried"))
    }
}
