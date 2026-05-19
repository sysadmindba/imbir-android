package com.imbir.game.ui.minigame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.imbir.game.R

class MouseHuntActivity : AppCompatActivity() {

    private lateinit var mouseView: MouseHuntView
    private lateinit var scoreText: TextView
    private lateinit var timerText: TextView
    private lateinit var btnStart: Button
    private lateinit var statusText: TextView

    private var timer: CountDownTimer? = null
    private var caught = 0
    private var isRunning = false
    private var gameFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mouse_hunt)

        mouseView  = findViewById(R.id.mouseView)
        scoreText  = findViewById(R.id.mouseScoreText)
        timerText  = findViewById(R.id.mouseTimerText)
        btnStart   = findViewById(R.id.btnMouseStart)
        statusText = findViewById(R.id.mouseStatusText)

        mouseView.onCatch = {
            caught++
            scoreText.text = "Caught: $caught 🐭"
        }

        btnStart.setOnClickListener {
            if (!isRunning) startGame() else endGame()
        }
    }

    private fun startGame() {
        caught = 0
        isRunning = true
        gameFinished = false
        scoreText.text = "Caught: 0 🐭"
        statusText.text = "Catch the mice before they escape! 🐭"
        btnStart.text = "Give Up"
        mouseView.startGame()

        timer = object : CountDownTimer(30_000, 1000) {
            override fun onTick(remaining: Long) {
                timerText.text = "⏱ ${remaining / 1000}s"
                mouseView.spawnRate = (600 - (30 - remaining / 1000) * 15).coerceAtLeast(200).toInt()
            }
            override fun onFinish() { endGame() }
        }.start()
    }

    private fun endGame() {
        if (gameFinished) return
        gameFinished = true
        timer?.cancel()
        isRunning = false
        mouseView.stopGame()

        val message = when {
            caught >= 15 -> "Imbir is an elite hunter! 😻"
            caught >= 8  -> "Great hunting! Imbir is proud! 😺"
            caught >= 3  -> "Nice catch! Keep practicing! 🐈"
            else         -> "The mice were too fast this time! 😾"
        }
        statusText.text = "Game Over! Caught: $caught\n$message"
        timerText.text = "⏱ 0s"
        btnStart.text = "Done"
        btnStart.setOnClickListener { finishWithScore() }
    }

    private fun finishWithScore() {
        val result = Intent().putExtra(EXTRA_CAUGHT, caught)
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    override fun onBackPressed() {
        finishWithScore()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    companion object {
        const val EXTRA_CAUGHT = "mice_caught"
        const val REQUEST_CODE = 1002
    }
}