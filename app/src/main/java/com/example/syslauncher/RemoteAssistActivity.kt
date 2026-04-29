package com.example.syslauncher

import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RemoteAssistActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_assist)

        val tvSession = findViewById<TextView>(R.id.tvSessionState)
        val btnHome = findViewById<Button>(R.id.btnActionGoHome)
        val btnCallSon = findViewById<Button>(R.id.btnActionCallSon)
        val btnCallHelp = findViewById<Button>(R.id.btnActionCallHelp)
        val btnReset = findViewById<Button>(R.id.btnActionResetUi)
        val btnEnd = findViewById<Button>(R.id.btnEndSession)

        val startedAt = intent.getLongExtra("session_started_at", SystemClock.elapsedRealtime())
        val maxDurationMs = 10 * 60 * 1000L
        val expiresAt = startedAt + maxDurationMs
        val remainingSec = ((expiresAt - SystemClock.elapsedRealtime()).coerceAtLeast(0L)) / 1000L

        tvSession.text = "Consent granted. Session active for ${remainingSec}s."

        btnHome.setOnClickListener { finish() }
        btnCallSon.setOnClickListener {
            setResult(RESULT_FIRST_USER, intent.putExtra("assist_action", "CALL_SON"))
            finish()
        }
        btnCallHelp.setOnClickListener {
            setResult(RESULT_FIRST_USER, intent.putExtra("assist_action", "CALL_HELP"))
            finish()
        }
        btnReset.setOnClickListener {
            setResult(RESULT_FIRST_USER, intent.putExtra("assist_action", "RESET_UI"))
            finish()
        }
        btnEnd.setOnClickListener { finish() }
    }
}
