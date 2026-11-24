package com.tony.pro
import android.Manifest
import android.content.Intent
import android.os.*
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 100, 50, 50) }
        layout.addView(TextView(this).apply { text = "TonyPro Assistant"; textSize = 24f; setPadding(0, 0, 0, 50) })
        layout.addView(Button(this).apply {
            text = "1. اجازت دیں (Permissions)"
            setOnClickListener {
                val p = mutableListOf(Manifest.permission.RECORD_AUDIO)
                if (Build.VERSION.SDK_INT >= 33) p.add(Manifest.permission.POST_NOTIFICATIONS)
                ActivityCompat.requestPermissions(this@MainActivity, p.toTypedArray(), 100)
            }
        })
        layout.addView(Button(this).apply {
            text = "2. اسکرین کنٹرول آن کریں"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        layout.addView(Button(this).apply {
            text = "3. TonyPro شروع کریں"
            setOnClickListener {
                val i = Intent(this@MainActivity, TonyForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
                Toast.makeText(this@MainActivity, "شروع ہو گیا!", Toast.LENGTH_SHORT).show()
            }
        })
        setContentView(layout)
    }
}
