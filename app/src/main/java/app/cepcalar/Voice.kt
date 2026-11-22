package app.cepcalar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Telefonun kendi konuşma motoru (TTS) ve ses tanıması. Türkçe çevrimdışı paketler yüklüyse internetsiz çalışır.
 * Tüm çağrılar ana iş parçacığından yapılır.
 */
class Voice(private val app: Context) {
    private val main = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var ttsFailed = false
    private var pending: Pair<String, () -> Unit>? = null
    private var currentId = ""
    private var doneCb: (() -> Unit)? = null

    private var rec: SpeechRecognizer? = null
    private var wantListen = false
    private var active = false
    private var errCount = 0
    var speaking = false
        private set

    var onHeard: (String) -> Unit = {}
    var onListening: (Boolean) -> Unit = {}
    var onSpeaking: (Boolean) -> Unit = {}
    var onNote: (String?) -> Unit = {}
    var onFatal: (String) -> Unit = {}

    fun init() {
        if (tts != null) return
        tts = TextToSpeech(app) { status ->
            main.post {
                if (status == TextToSpeech.SUCCESS) {
                    val r = tts?.setLanguage(Locale("tr", "TR"))
                    ttsReady = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
                    if (!ttsReady) {
                        ttsFailed = true
                        onNote("Telefonda Türkçe konuşma sesi yok. Ayarlar > Metin okuma bölümünden Türkçe sesi indirirsen konuşabilirim.")
                    }
                } else {
                    ttsFailed = true
                    onNote("Telefonda konuşma motoru (metin okuma) bulunamadı.")
                }
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        main.post { if (utteranceId == currentId) finishSpeak() }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        main.post { if (utteranceId == currentId) finishSpeak() }
                    }
                })
                val p = pending
                pending = null
                if (p != null) {
                    if (ttsReady) speak(p.first, p.second) else p.second()
                }
            }
        }
    }

    fun speak(text: String, onDone: () -> Unit = {}) {
        if (tts == null) init()
        if (ttsFailed) { onDone(); return }
        if (!ttsReady) { pending = text to onDone; return }
        speaking = false
        doneCb = onDone
        speaking = true
        onSpeaking(true)
        active = false
        runCatching { rec?.cancel() }
        onListening(false)
        currentId = "y" + System.nanoTime()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, currentId)
    }

    fun stopSpeaking() {
        if (!speaking) return
        currentId = ""
        tts?.stop()
        finishSpeak()
    }

    private fun finishSpeak() {
        if (!speaking) return
        speaking = false
        onSpeaking(false)
        val cb = doneCb
        doneCb = null
        cb?.invoke()
        if (wantListen) main.postDelayed({ listenOnce() }, 600)
    }

    fun startListening() {
        wantListen = true
        errCount = 0
        main.post { listenOnce() }
    }

    fun stopListening() {
        wantListen = false
        active = false
        main.post {
            runCatching { rec?.cancel() }
            runCatching { rec?.destroy() }
            rec = null
            onListening(false)
        }
    }

    fun release() {
        stopListening()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    private fun retry(ms: Long) {
        if (wantListen) main.postDelayed({ listenOnce() }, ms)
    }

    private fun listenOnce() {
        if (!wantListen || speaking || active) return
        if (!SpeechRecognizer.isRecognitionAvailable(app)) {
            wantListen = false
            onFatal("Telefonda ses tanıma servisi (Google) bulunamadı.")
            return
        }
        val r = rec ?: SpeechRecognizer.createSpeechRecognizer(app).also {
            it.setRecognitionListener(listener)
            rec = it
        }
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, app.packageName)
        }
        active = true
        runCatching { r.startListening(i) }.onFailure { active = false; retry(1500) }
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { onListening(true) }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onResults(results: Bundle?) {
            active = false
            onListening(false)
            errCount = 0
            onNote(null)
            val t = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (!t.isNullOrBlank()) onHeard(t)
            retry(400)
        }

        override fun onError(error: Int) {
            active = false
            onListening(false)
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> retry(300)
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                    wantListen = false
                    onFatal("Mikrofon izni verilmemiş.")
                }
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                    runCatching { rec?.destroy() }
                    rec = null
                    retry(1000)
                }
                12, 13 -> {
                    wantListen = false
                    onFatal("Türkçe ses tanıma paketi yüklü değil. Google uygulaması > Ayarlar > Ses > Çevrimdışı konuşma tanıma bölümünden Türkçe'yi indir.")
                }
                else -> {
                    errCount++
                    if (errCount >= 5) {
                        wantListen = false
                        onFatal("Ses tanıma çalışmıyor. İnternet yoksa Türkçe çevrimdışı konuşma tanıma paketi yüklü olmalı.")
                    } else retry(1500)
                }
            }
        }
    }
}
