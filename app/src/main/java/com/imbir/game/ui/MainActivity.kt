package com.imbir.game.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.imbir.game.R
import com.imbir.game.logic.GameManager
import com.imbir.game.logic.NotificationHelper
import com.imbir.game.util.PreferencesManager

class MainActivity : AppCompatActivity() {

    lateinit var gameManager: GameManager
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameManager = GameManager(PreferencesManager(this))

        NotificationHelper.createChannel(this)
        requestNotificationPermission()

        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHost.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)
    }

    override fun onPause() {
        super.onPause()
        gameManager.save()
        val state = gameManager.state
        NotificationHelper.scheduleReminder(this, state.hunger, state.happiness)
    }

    override fun onResume() {
        super.onResume()
        NotificationHelper.cancelReminder(this)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }
}