# ⚡ Universal Screen AI (১০০% ফ্রি অ্যান্ড্রয়েড স্ক্রিন অ্যাসিস্ট্যান্ট)

এই প্রোজেক্টটি একটি সম্পূর্ণ অ্যান্ড্রয়েড অ্যাপ যা কোনো স্ক্রিন রেকর্ডিং বা স্ক্রিন শেয়ার ছাড়াই সম্পূর্ণ নিজস্ব Accessibility Service দিয়ে স্ক্রিনের টেক্সট তুলে নেয় এবং গুগল জেমিনি (Gemini AI) ও বাংলা ভয়েস দিয়ে ইউজারকে মুখে পড়ে সমাধান দেয়।

## 📁 প্রোজেক্ট স্ট্রাকচার
- `.github/workflows/build_apk.yml`: GitHub Actions অটোমেটিক APK বিল্ডার ওয়ার্কফ্লো
- `settings.gradle.kts`: রিপোজিটরি ও মডিউল কনফিগারেশন
- `build.gradle.kts`: রুট গ্রেডেল বিল্ড স্ক্রিপ্ট
- `app/build.gradle.kts`: অ্যাপ মডিউল ও ডিপেন্ডেন্সিস (OkHttp, Coroutines, Material)
- `app/src/main/AndroidManifest.xml`: SYSTEM_ALERT_WINDOW ও Accessibility Service পারমিশন
- `app/src/main/res/xml/accessibility_service_config.xml`: স্ক্রিন ইভেন্ট কনফিগ
- `app/src/main/java/.../MainActivity.kt`: পারমিশন রিকোয়েস্ট ও অ্যাক্টিভেশন পেজ
- `app/src/main/java/.../ScreenAIAccessibilityService.kt`: ড্র্যাগেবল বাবল, স্ক্রিন রিডার ও এআই ইঞ্জিন

---

## 🤖 GitHub Actions দিয়ে অটোমেটিক APK ডাউনলোড (সবচেয়ে সহজ উপায়!)

আপনার কম্পিউটারে Android Studio ইন্সটল করা না থাকলেও চলবে! 
1. এই প্রোজেক্টের সমস্ত ফাইল আপনার একটি নতুন **GitHub Repository**-তে Push বা Upload করুন।
2. আপনার GitHub রিপোজিটরির **Actions** ট্যাবে যান।
3. স্বয়ংক্রিয়ভাবে `Build Android Debug APK` ওয়ার্কফ্লো চালু হবে।
4. বিল্ড শেষ হলে (১-২ মিনিটের মধ্যে) ওয়ার্কফ্লোর নিচে **Artifacts** সেকশনে যান।
5. **`app-debug`** ফাইলে ক্লিক করলেই সরাসরি ইন্সটলেবল `app-debug.apk` ডাউনলোড হয়ে যাবে!

---

## 🚀 অ্যান্ড্রয়েড স্টুডিওতে সরাসরি APK বিল্ডের ধাপসমূহ

### ধাপ ১: ফাইল আনজিপ করুন
ডাউনলোড করা `UniversalScreenAI.zip` ফাইলটি যেকোনো ফোল্ডারে Extract (Unzip) করুন।

### ধাপ ২: Android Studio-তে ওপেন করুন
1. আপনার কম্পিউটারে **Android Studio** ওপেন করুন।
2. **File > Open** এ ক্লিক করে আনজিপ করা ফোল্ডারটি সিলেক্ট করুন।
3. গ্রেডেল সিঙ্ক (Gradle Sync) স্বয়ংক্রিয়ভাবে শুরু হবে।

### ধাপ ৩: APK জেনারেট করুন
1. Android Studio-এর মেনু থেকে **Build > Build Bundle(s) / APK(s) > Build APK(s)** এ ক্লিক করুন।
2. বিল্ড শেষ হলে "locate" এ ক্লিক করে `app-debug.apk` পেয়ে যাবেন।

### ধাপ ৪: ফোনে ইন্সটল ও পারমিশন অন করুন
1. ফোনে ইন্সটল করে অ্যাপটি ওপেন করুন।
2. **Display Over Other Apps** (Overlay) পারমিশন On করুন।
3. **Accessibility Service** পেজে গিয়ে "Universal Screen AI" সার্ভিসটি চালু (Enable) করুন।
4. ব্যস! সাথে সাথে আপনার স্ক্রিনে বেগুনি রঙের **🎙️ AI** বাবল ভেসে উঠবে।
