package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.nova.actions.ActionManager
import com.example.nova.actions.ActionRiskLevel
import com.example.nova.actions.ActionType
import com.example.nova.ai.AiConfig
import com.example.nova.ai.AiResponseParser
import com.example.nova.ai.IntentClassifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Nova", appName)
    }

    @Test
    fun `parse structured AI action response`() {
        val rawJson = """
            {
                "response": "Opening YouTube for you.",
                "action": "OPEN_APP",
                "parameters": {
                    "packageName": "com.google.android.youtube"
                }
            }
        """.trimIndent()

        val parsed = AiResponseParser.parse(rawJson)
        assertEquals("Opening YouTube for you.", parsed.displayText)
        assertEquals(ActionType.OPEN_APP, parsed.actionType)
        assertEquals("com.google.android.youtube", parsed.parameters["packageName"])
    }

    @Test
    fun `high risk action requires explicit confirmation`() {
        val eval = ActionManager.evaluateAction(
            actionType = ActionType.MAKE_PHONE_CALL,
            parameters = mapOf("phoneNumber" to "555-1234")
        )
        assertTrue(eval.requiresUserInput)
        assertNotNull(eval.pendingAction)
        assertEquals(ActionRiskLevel.HIGH, eval.pendingAction?.riskLevel)
    }

    @Test
    fun `low risk action executes without modal blocking`() {
        val eval = ActionManager.evaluateAction(
            actionType = ActionType.WEB_SEARCH,
            parameters = mapOf("query" to "Kotlin programming")
        )
        assertEquals(false, eval.requiresUserInput)
    }

    @Test
    fun `intent classifier detects open app commands`() {
        val classified = IntentClassifier.classifyLocal("Open YouTube")
        assertNotNull(classified)
        assertEquals(ActionType.OPEN_APP, classified?.actionType)
        assertEquals("YouTube", classified?.parameters?.get("appName"))
    }

    @Test
    fun `intent classifier detects timer commands`() {
        val classified = IntentClassifier.classifyLocal("Set a timer for 10 minutes")
        assertNotNull(classified)
        assertEquals(ActionType.SET_TIMER, classified?.actionType)
        assertEquals(600, classified?.parameters?.get("durationSeconds"))
    }

    @Test
    fun `intent classifier detects time query`() {
        val classified = IntentClassifier.classifyLocal("What time is it")
        assertNotNull(classified)
        assertEquals(ActionType.GET_CURRENT_TIME, classified?.actionType)
    }

    @Test
    fun `ai config classifies quota error without raw sdk strings`() {
        val ex = Exception("Resource has been exhausted: quota limit reached for Gemini free tier")
        val classified = AiConfig.classifyError(ex)
        assertEquals("Nova has temporarily reached its AI usage limit. Please try again later.", classified)
    }

    @Test
    fun `ai config classifies network error cleanly`() {
        val ex = java.net.UnknownHostException("Unable to resolve host: connect timed out")
        val classified = AiConfig.classifyError(ex)
        assertEquals("Nova can't connect to the AI right now. Check your internet connection.", classified)
    }
}
