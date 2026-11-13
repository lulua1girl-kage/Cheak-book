package org.nori.assistant.voice

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Android-native voice adapter.
 *
 * Speech recognition is deliberately on-device only. If the device has no on-device
 * recognition service or language pack, Nori reports that and keeps typed chat usable.
 * Text-to-speech uses the device's configured TTS engine; offline availability depends
 * on the installed engine and voice package.
 */
class AndroidVoiceController(
    context: Context,
    private val onTranscript: (String) -> Unit,
    private val onStatus: (String) -> Unit
) {
    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false

    init {
        textToSpeech = TextToSpeech(appContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                textToSpeech?.language = Locale.getDefault()
                onStatus("Speech output ready")
            } else {
                onStatus("Speech output is unavailable; typed chat still works")
            }
        }
    }

    fun supportsOnDeviceRecognition(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext)

    fun startListening(activity: Activity) {
        if (!supportsOnDeviceRecognition()) {
            onStatus("Offline speech recognition is not available on this device. You can type instead.")
            return
        }

        stopListening()
        recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext).also { service ->
            service.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onStatus("Listening on device…")
                }

                override fun onBeginningOfSpeech() {
                    onStatus("Listening…")
                }

                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    onStatus("Processing speech on device…")
                }

                override fun onError(error: Int) {
                    onStatus("Voice input stopped (code $error). You can type instead.")
                    recognizer?.destroy()
                    recognizer = null
                }

                override fun onResults(results: Bundle?) {
                    val phrase = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()
                    if (phrase.isNotBlank()) {
                        onTranscript(phrase)
                        onStatus("Speech recognized")
                    } else {
                        onStatus("No speech recognized. You can type instead.")
                    }
                    recognizer?.destroy()
                    recognizer = null
                }

                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            recognizer?.startListening(intent)
        } catch (error: RuntimeException) {
            recognizer?.destroy()
            recognizer = null
            onStatus("Could not start voice input. You can type instead.")
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        if (!ttsReady) {
            onStatus("Speech output is not ready. You can read the reply on screen.")
            return
        }
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "nori-reply")
    }

    fun stopListening() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
    }

    fun release() {
        stopListening()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        ttsReady = false
    }
}
