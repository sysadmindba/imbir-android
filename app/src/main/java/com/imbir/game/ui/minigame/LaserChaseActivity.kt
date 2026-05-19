package com.imbir.game.ui.minigame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.imbir.game.R

class LaserChaseActivity : AppCompatActivity() {

    private lateinit var laserView: LaserChaseView
    private lateinit var scoreText: TextView
    private lateinit var timerText: TextView
    private lateinit var btnStart: Button
    private lateinit var statusText: TextView

    private var timer: CountDownTimer? = null
    private var score = 0
    private var isRunning = false
    private var gameFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laser_chase)

        laserView  = findViewById(R.id.laserView)
        scoreText  = findViewById(R.id.laserScoreText)
        timerText  = findViewById(R.id.laserTimerText)
        btnStart   = findViewById(R.id.btnLaserStart)
        statusText = findViewById(R.id.laserStatusText)

        laserView.onHit = {
            score++
            scoreText.text = "Score: $score"
        }

        btnStart.setOnClickListener {
            if (!isRunning) startGame() else endGame()
        }
    }

    private fun startGame() {
        score = 0
        isRunning = true
        gameFinished = false
        scoreText.text = "Score: 0"
        statusText.text = "Catch the laser dot! 🔴"
        btnStart.text = "Give Up"
        laserView.startGame()

        timer = object : CountDownTimer(30_000, 1000) {
            override fun onTick(remaining: Long) {
                timerText.text = "⏱ ${remaining / 1000}s"
                laserView.speedMultiplier = 1f + (30 - remaining / 1000) / 20f
            }
            override fun onFinish() { endGame() }
        }.start()
    }

    private fun endGame() {
        if (gameFinished) return
        gameFinished = true
        timer?.cancel()
        isRunning = false
        laserView.stopGame()
        btnStart.text = "Play Again"

        val message = when {
            score >= 20 -> "Amazing! Imbir had the time of her life! 😻"
            score >= 10 -> "Great job! Imbir is tired but happy! 😺"
            score >= 5  -> "Good effort! Imbir got some exercise! 🐈"
            else        -> "That laser was tricky! Better luck next time! 😾"
        }
        statusText.text = "Game Over! Score: $score\n$message"
        timerText.text = "⏱ 0s"
        btnStart.text = "Done"
        btnStart.setOnClickListener { finishWithScore() }
    }

    private fun finishWithScore() {
        val result = Intent().putExtra(EXTRA_SCORE, score)
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
        const val EXTRA_SCORE = "laser_score"
        const val REQUEST_CODE = 1001
    }
}