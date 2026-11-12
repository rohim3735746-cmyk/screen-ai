package com.superai.screenassistant

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.Manifest

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // পারমিশন চেক ও রিকোয়েস্ট
        requestAudioPermission()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0B0D13")) // Midnight Black
            setPadding(60, 60, 60, 60)
        }

        val title = TextView(this).apply {
            text = "⚡ Universal Screen AI\n(১০০% ফ্রি অ্যাসিস্ট্যান্ট)"
            textSize = 22f
            setTextColor(Color.parseColor("#06B6D4")) // Neon Cyan
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "\nকোনো স্ক্রিন শেয়ার বা রেকর্ডিং লাগবে না।\nচালু করে যেকোনো অ্যাপ বা গেমে ঢুকুন, মাইকে জিজ্ঞেস করলেই সে পড়ে বুঝিয়ে দেবে।\n"
            textSize = 14f
            setTextColor(Color.parseColor("#94A3B8"))
            gravity = Gravity.CENTER
        }

        val btnActivate = Button(this).apply {
            text = "সার্ভিসটি চালু করুন (Start)"
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#8B5CF6")) // Electric Purple
            setPadding(30, 20, 30, 20)
            setOnClickListener {
                setupPermissions()
            }
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(btnActivate)
        setContentView(layout)
    }

    private fun requestAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        }
    }

    private fun setupPermissions() {
        // ১. Overlay Permission চেক
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Display Over Other Apps পারমিশনটি অন করুন", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
            return
        }

        // ২. Accessibility Service চালু করার পেইজ ওপেন করা
        Toast.makeText(this, "'Universal Screen AI' সার্ভিসটি অন করুন", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}
