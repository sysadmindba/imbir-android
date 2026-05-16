package com.imbir.game.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.imbir.game.R

class MainActivity : AppCompatActivity() {

    private var hunger = 100
    private var mood = 100
    private var energy = 100
    private var cleanliness = 100

    private lateinit var hungerBar: ProgressBar
    private lateinit var moodBar: ProgressBar
    private lateinit var energyBar: ProgressBar
    private lateinit var cleanBar: ProgressBar
    private lateinit var catSprite: ImageView

    private val returnToIdleRunnable = Runnable {
        catSprite.setImageResource(R.drawable.cat_idle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupButtons()
        updateStats()
    }

    private fun initViews() {
        hungerBar = findViewById(R.id.hungerBar)
        moodBar = findViewById(R.id.moodBar)
        energyBar = findViewById(R.id.energyBar)
        cleanBar = findViewById(R.id.cleanBar)
        catSprite = findViewById(R.id.catSprite)
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnFeed).setOnClickListener {
            hunger = (hunger + 25).coerceAtMost(100)
            cleanliness = (cleanliness - 5).coerceAtLeast(0)
            updateAction(R.drawable.cat_eat)
            updateStats()
        }

        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            mood = (mood + 20).coerceAtMost(100)
            energy = (energy - 10).coerceAtLeast(0)
            hunger = (hunger - 5).coerceAtLeast(0)
            updateAction(R.drawable.cat_play)
            updateStats()
        }

        findViewById<Button>(R.id.btnSleep).setOnClickListener {
            energy = (energy + 40).coerceAtMost(100)
            updateAction(R.drawable.cat_sleep)
            updateStats()
        }

        findViewById<Button>(R.id.btnWash).setOnClickListener {
            cleanliness = (cleanliness + 35).coerceAtMost(100)
            mood = (mood - 3).coerceAtLeast(0)
            updateAction(R.drawable.cat_bath)
            updateStats()
        }
    }

    private fun updateAction(drawableRes: Int) {
        catSprite.setImageResource(drawableRes)
        // Return to idle after 3 seconds
        catSprite.removeCallbacks(returnToIdleRunnable)
        catSprite.postDelayed(returnToIdleRunnable, 3000)
    }

    private fun updateStats() {
        hungerBar.progress = hunger
        moodBar.progress = mood
        energyBar.progress = energy
        cleanBar.progress = cleanliness
    }
}
