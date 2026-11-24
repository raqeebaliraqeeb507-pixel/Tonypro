package com.tony.pro
import android.app.*
import android.content.Intent
import android.net.Uri
import android.os.*
import android.speech.*
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import java.util.Locale

class TonyForegroundService : Service(), TextToSpeech.OnInitListener {
    private var sr: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private val h = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
        h.post {
            sr = SpeechRecognizer.createSpeechRecognizer(this)
            sr?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(p: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(r: Float) {}
                override fun onBufferReceived(b: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(p: Bundle?) {}
                override fun onEvent(e: Int, p: Bundle?) {}
                override fun onError(e: Int) { h.postDelayed({ listen() }, 600) }
                override fun onResults(r: Bundle?) {
                    val m = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!m.isNullOrEmpty()) runCmd(m.lowercase())
                    h.postDelayed({ listen() }, 1000)
                }
            })
        }
    }

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("TonyCh", "Tony Service", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val n = NotificationCompat.Builder(this, "TonyCh")
            .setContentTitle("TonyPro فعال ہے")
            .setContentText("آواز سن رہا ہوں...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now).build()
        startForeground(101, n)
        listen()
        return START_STICKY
    }

    override fun onInit(s: Int) { if (s == TextToSpeech.SUCCESS) tts?.language = Locale("ur", "PK") }
    private fun speak(t: String) { tts?.speak(t, TextToSpeech.QUEUE_FLUSH, null, null) }

    private fun listen() {
        h.post {
            try {
                val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur-PK")
                }
                sr?.startListening(i)
            } catch (e: Exception) {}
        }
    }

    private fun runCmd(cmd: String) {
        when {
            cmd.contains("whatsapp") || cmd.contains("واٹس ایپ") -> {
                val i = packageManager.getLaunchIntentForPackage("com.whatsapp")
                if (i != null) { i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); speak("واٹس ایپ کھول دیا") }
                else speak("واٹس ایپ نہیں ملا")
            }
            cmd.contains("youtube") || cmd.contains("یوٹیوب") -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                speak("یوٹیوب کھول رہا ہوں")
            }
            cmd.contains("click") || cmd.contains("کلک") -> {
                val t = cmd.replace("click", "").replace("کلک", "").trim()
                if (TonyAccessibilityService.instance?.clickByText(t) == true) speak("کلک کر دیا") else speak("نہیں ملا")
            }
            cmd.contains("type") || cmd.contains("لکھو") -> {
                val t = cmd.replace("type", "").replace("لکھو", "").trim()
                if (TonyAccessibilityService.instance?.typeText(t) == true) speak("لکھ دیا") else speak("جگہ نہیں ملی")
            }
            else -> speak("آپ نے کہا: $cmd")
        }
    }

    override fun onDestroy() { sr?.destroy(); tts?.shutdown(); super.onDestroy() }
    override fun onBind(i: Intent?): IBinder? = null
}
