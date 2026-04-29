package com.example.syslauncher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LauncherGateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = AppPrefs(this)
        val target = if (prefs.isProvisioned()) {
            MainActivity::class.java
        } else {
            ProvisioningActivity::class.java
        }

        startActivity(Intent(this, target))
        finish()
    }
}
