package com.superai.screenassistant

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class ScreenAIAccessibilityService : AccessibilityService(), TextToSpeech.OnInitListener {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private val client = OkHttpClient()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    // সম্পূর্ণ ফ্রি Google Gemini API Key (বাধ্যতামূলক নয়, না দিলেও অফলাইনে চলবে)
    private val FREE_GEMINI_API_KEY = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        tts = TextToSpeech(this, this)
        setupSpeechRecognizer()
        createFloatingAssistantWidget()
        Toast.makeText(this, "AI স্ক্রিন বাবল চালু হয়েছে!", Toast.LENGTH_SHORT).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("bn", "BD")
        }
    }

    // স্ক্রিনের উপর ভেসে থাকার বাবল উইজেট
    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingAssistantWidget() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 60
            y = 300
        }

        val container = FrameLayout(this)
        val button = Button(this).apply {
            text = "🎙️ AI"
            setBackgroundColor(Color.parseColor("#8B5CF6"))
            setTextColor(Color.WHITE)
        }

        button.setOnClickListener {
            speak("আমি স্ক্রিন দেখছি, কী জানতে চান বলুন...")
            startVoiceListening()
        }

        // স্ক্রিনে টেনে নিয়ে ইচ্ছামতো রাখার সুবিধা (Drag & Drop)
        button.setOnTouchListener(object : View.OnTouchListener {
            var initX = 0
            var initY = 0
            var touchX = 0f
            var touchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initX = params.x
                        initY = params.y
                        touchX = event.rawX
                        touchY = event.rawY
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initX + (event.rawX - touchX).toInt()
                        params.y = initY + (event.rawY - touchY).toInt()
                        windowManager.updateViewLayout(container, params)
                        return true
                    }
                }
                return false
            }
        })

        container.addView(button)
        floatingView = container
        windowManager.addView(floatingView, params)
    }

    // কোনো স্ক্রিন শেয়ার বা স্ক্রিন রেকর্ডার ছাড়াই স্ক্রিনের সমস্ত লেখা তুলে আনার মেথড
    private fun extractCurrentScreenText(): String {
        val rootNode = rootInActiveWindow ?: return ""
        val sb = StringBuilder()

        fun traverse(node: AccessibilityNodeInfo?) {
            if (node == null) return
            if (!node.text.isNullOrEmpty()) {
                sb.append(node.text.toString()).append(" | ")
            }
            if (!node.contentDescription.isNullOrEmpty()) {
                sb.append(node.contentDescription.toString()).append(" | ")
            }
            for (i in 0 until node.childCount) {
                traverse(node.getChild(i))
            }
        }

        traverse(rootNode)
        return sb.toString().trim()
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val userVoiceText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                    ?: "এখানে কি করতে হবে বলো"

                val onScreenData = extractCurrentScreenText()
                processUserQuery(userVoiceText, onScreenData)
            }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                speak("কথা স্পষ্ট শোনা যায়নি, আবার বাটনে চাপুন।")
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startVoiceListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
        }
        speechRecognizer.startListening(intent)
    }

    private fun processUserQuery(query: String, screenText: String) {
        if (screenText.isEmpty()) {
            speak("স্ক্রিনে কোনো পড়ার মতো লেখা দেখতে পাচ্ছি না।")
            return
        }

        val q = query.lowercase()
        val isDraftQuery = q.contains("লিখব") || q.contains("লিখবো") || q.contains("রিপ্লাই") || q.contains("ড্রাফট") || q.contains("কী লিখি")

        // যদি ফ্রি জেমিনি এপিআই কী থাকে তবে এআই এনালাইসিস করবে, না থাকলে বিল্ট-ইন অফলাইন ইঞ্জিন বলবে
        if (FREE_GEMINI_API_KEY.isNotEmpty()) {
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val prompt = """
                        তুমি ইউজারের বিজ্ঞ ও বিশ্বস্ত "Wise Personal Screen Guide" (ডিজিটাল অভিভাবক)।
                        মোবাইল স্ক্রিনের বর্তমান টেক্সট: "$screenText"
                        ইউজারের মুখে বলা প্রশ্ন: "$query"
                        
                        তোমার দায়িত্ব:
                        ১. ইউজার যদি জানতে চায় "এখানে কী লিখব?" -> স্ক্রিনের চ্যাট বা মেসেজটি পড়ে সবচেয়ে মার্জিত, ভদ্র ও নির্ভুল উত্তর তৈরি করো।
                        ২. ইউজার যদি জানতে চায় "এখানে কী আছে?" -> কোনো টেকনিক্যাল বুলি ছাড়া অত্যন্ত সহজ কথ্য বাংলায় স্ক্রিনটি বুঝিয়ে দাও।
                        ৩. ইউজার যদি জানতে চায় "এটা চাপলে কী হবে?" -> বাটনটি চাপলে ঠিক কী ঘটবে তা আগেই স্পষ্ট করে আশ্বস্ত বা সতর্ক করো।
                        ৪. স্ক্রিনে পিন, পাসওয়ার্ড, ওটিপি বা সন্দেহজনক পারমিশন চাওয়া হলে অবিলম্বে সতর্ক করো।
                        ৫. অত্যন্ত উষ্ণ ও আন্তরিক কথ্য বাংলায় মাত্র ২-৩ বাক্যে উত্তর দাও।
                    """.trimIndent()

                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$FREE_GEMINI_API_KEY"
                    val json = JSONObject().apply {
                        put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))))
                    }

                    val req = Request.Builder().url(url).post(json.toString().toRequestBody("application/json".toMediaType())).build()
                    val res = client.newCall(req).execute()
                    val resBody = res.body?.string()

                    if (res.isSuccessful && resBody != null) {
                        val reply = JSONObject(resBody)
                            .getJSONArray("candidates").getJSONObject(0)
                            .getJSONObject("content").getJSONArray("parts")
                            .getJSONObject(0).getString("text")

                        if (isDraftQuery) {
                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("AI Drafted Reply", reply)
                            clipboard.setPrimaryClip(clip)
                            withContext(Dispatchers.Main) { 
                                Toast.makeText(this@ScreenAIAccessibilityService, "উত্তর ক্লিপবোর্ডে কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                                speak("আমি সুন্দর করে উত্তরটি লিখে কপি করে দিয়েছি, আপনি শুধু পেস্ট করে দিন।") 
                            }
                        } else {
                            withContext(Dispatchers.Main) { speak(reply) }
                        }
                    } else {
                        offlineFallback(screenText, query)
                    }
                } catch (e: Exception) {
                    offlineFallback(screenText, query)
                }
            }
        } else {
            offlineFallback(screenText, query)
        }
    }

    private fun offlineFallback(screenText: String, query: String) {
        val s = screenText.lowercase()
        val q = query.lowercase()

        if (q.contains("লিখব") || q.contains("লিখবো") || q.contains("রিপ্লাই") || q.contains("ড্রাফট")) {
            val draft = if (s.contains("chat") || s.contains("whatsapp") || s.contains("কাজ")) {
                "ধন্যবাদ আপনার বার্তার জন্য। আমি কাজটির আপডেট গুছিয়ে নিয়েছি, আজকের মধ্যেই আপনাকে পাঠিয়ে দেব।"
            } else {
                "ধন্যবাদ! বিষয়টি বুঝতে পেরেছি, আমি দ্রুতই এ ব্যাপারে পরবর্তী আপডেট জানাচ্ছি।"
            }
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("AI Drafted Reply", draft)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "উত্তর ক্লিপবোর্ডে কপি হয়েছে!", Toast.LENGTH_SHORT).show()
            speak("আমি সুন্দর করে উত্তরটি লিখে কপি করে দিয়েছি, আপনি শুধু পেস্ট করে দিন।")
            return
        }

        val reply = when {
            s.contains("bkash") || s.contains("বিকাশ") -> {
                if (q.contains("হবে") || q.contains("চাপলে")) {
                    "এটি চেপে ধরে রাখলে আপনার একাউন্ট থেকে টাকা কেটে প্রাপককে পাঠানো হবে। পিন নম্বর গোপন রাখুন এবং নিশ্চিত হয়ে টাকা পাঠান।"
                } else {
                    "এখানে টাকা পাঠানোর সামারি দেখাচ্ছে। গোপন পিন দিয়ে ট্যাপ করে ধরে রাখুন। মনে রাখবেন, পিন কখনো কাউকে বলবেন না।"
                }
            }
            s.contains("otp") || s.contains("verification") -> {
                "এসএমএসের ৬ সংখ্যার ওটিপি কোডটি বক্সে দিয়ে ভেরিফাই বাটনে চাপুন। সাবধান, এই ওটিপি কোড কখনো কাউকে মুখে বলবেন না।"
            }
            s.contains("permission") || s.contains("allow") -> {
                "অ্যাপটি আপনার পারমিশন চাইছে। ক্ষতিকর বা অপ্রয়োজনীয় মনে হলে অবিলম্বে লাল 'Don't Allow' বাটনে চাপ দিন।"
            }
            else -> {
                val summary = if (screenText.length > 80) screenText.substring(0, 80) else screenText
                "স্ক্রিনে দেখতে পাচ্ছি: $summary । সাবধানে এগিয়ে যেতে নির্দেশিত অ্যাকশন বাটনে চাপুন।"
            }
        }
        speak(reply)
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SCREEN_REPLY")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null) windowManager.removeView(floatingView)
        tts.stop()
        tts.shutdown()
        speechRecognizer.destroy()
        serviceScope.cancel()
    }
}
