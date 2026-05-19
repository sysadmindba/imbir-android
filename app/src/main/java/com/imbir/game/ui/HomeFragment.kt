package com.imbir.game.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.imbir.game.R
import com.imbir.game.data.CatVisualState
import com.imbir.game.data.GrowthStage
import com.imbir.game.logic.CatAnimationController
import com.imbir.game.logic.GameManager
import com.imbir.game.ui.minigame.LaserChaseActivity
import com.imbir.game.ui.minigame.MouseHuntActivity

class HomeFragment : Fragment() {

    private val gm get() = (requireActivity() as MainActivity).gameManager

    private lateinit var animController: CatAnimationController
    private var isSleeping = false

    // Cat display
    private lateinit var catSprite: ImageView
    private lateinit var catSpriteB: ImageView
    private lateinit var catMoodEmoji: TextView
    private lateinit var catNameText: TextView
    private lateinit var catLevelText: TextView
    private lateinit var personalityText: TextView
    private lateinit var stageText: TextView

    // Stats
    private lateinit var hungerBar: ProgressBar
    private lateinit var happinessBar: ProgressBar
    private lateinit var energyBar: ProgressBar
    private lateinit var cleanBar: ProgressBar
    private lateinit var healthBar: ProgressBar
    private lateinit var trustBar: ProgressBar

    private lateinit var hungerLabel: TextView
    private lateinit var happinessLabel: TextView
    private lateinit var energyLabel: TextView
    private lateinit var cleanLabel: TextView
    private lateinit var healthLabel: TextView
    private lateinit var trustLabel: TextView

    // Buttons
    private lateinit var btnFeed: Button
    private lateinit var btnPet: Button
    private lateinit var btnPlay: Button
    private lateinit var btnSleep: Button
    private lateinit var btnWash: Button
    private lateinit var btnTrick: Button
    private lateinit var btnMedicine: Button

    // Event card
    private lateinit var eventCard: CardView
    private lateinit var eventTitle: TextView
    private lateinit var eventDesc: TextView

    // Mini-game buttons
    private lateinit var btnLaser: Button
    private lateinit var btnMouse: Button

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            gm.tick()
            updateUI()
            showNewAchievements()
            refreshHandler.postDelayed(this, 5000L)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        animController = CatAnimationController(requireContext(), catSprite, catSpriteB)

        setupButtons()
        updateUI()
        showPendingEvents()
        showNewAchievements()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        showPendingEvents()
        showNewAchievements()
        refreshHandler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    private fun bindViews(v: View) {
        catSprite       = v.findViewById(R.id.catSprite)
        catSpriteB      = v.findViewById(R.id.catSpriteB)
        catMoodEmoji    = v.findViewById(R.id.catMoodEmoji)
        catNameText     = v.findViewById(R.id.catNameText)
        catLevelText    = v.findViewById(R.id.catLevelText)
        personalityText = v.findViewById(R.id.personalityText)
        stageText       = v.findViewById(R.id.stageText)

        hungerBar    = v.findViewById(R.id.hungerBar)
        happinessBar = v.findViewById(R.id.happinessBar)
        energyBar    = v.findViewById(R.id.energyBar)
        cleanBar     = v.findViewById(R.id.cleanBar)
        healthBar    = v.findViewById(R.id.healthBar)
        trustBar     = v.findViewById(R.id.trustBar)

        hungerLabel    = v.findViewById(R.id.hungerLabel)
        happinessLabel = v.findViewById(R.id.happinessLabel)
        energyLabel    = v.findViewById(R.id.energyLabel)
        cleanLabel     = v.findViewById(R.id.cleanLabel)
        healthLabel    = v.findViewById(R.id.healthLabel)
        trustLabel     = v.findViewById(R.id.trustLabel)

        btnFeed      = v.findViewById(R.id.btnFeed)
        btnPet       = v.findViewById(R.id.btnPet)
        btnPlay      = v.findViewById(R.id.btnPlay)
        btnSleep     = v.findViewById(R.id.btnSleep)
        btnWash      = v.findViewById(R.id.btnWash)
        btnTrick     = v.findViewById(R.id.btnTrick)
        btnMedicine  = v.findViewById(R.id.btnMedicine)

        eventCard  = v.findViewById(R.id.eventCard)
        eventTitle = v.findViewById(R.id.eventTitle)
        eventDesc  = v.findViewById(R.id.eventDesc)

        btnLaser = v.findViewById(R.id.btnLaser)
        btnMouse = v.findViewById(R.id.btnMouse)
    }

    private fun setupButtons() {
        btnFeed.setOnClickListener {
            gm.feed()
            animController.forceState(CatVisualState.EATING)
            updateUI()
        }
        btnPet.setOnClickListener {
            gm.pet()
            animController.forceState(CatVisualState.AFFECTIONATE, 4000L)
            updateUI()
        }
        btnPlay.setOnClickListener {
            gm.play()
            animController.forceState(CatVisualState.PLAYING)
            updateUI()
        }
        btnSleep.setOnClickListener {
            if (!isSleeping) {
                isSleeping = true
                gm.sleep()
                animController.forceState(CatVisualState.SLEEPING)
                btnSleep.text = getString(R.string.btn_wake)
            } else {
                isSleeping = false
                animController.clearForced()
                btnSleep.text = getString(R.string.btn_sleep)
            }
            updateUI()
        }
        btnWash.setOnClickListener {
            gm.wash()
            animController.forceState(CatVisualState.CLEANING)
            updateUI()
        }
        btnTrick.setOnClickListener { showTeachTrickDialog() }
        btnMedicine.setOnClickListener {
            gm.treatSickness()
            animController.forceState(CatVisualState.HAPPY, 4000L)
            updateUI()
            Snackbar.make(requireView(), "Imbir is feeling better! 💊", Snackbar.LENGTH_SHORT).show()
        }
        btnLaser.setOnClickListener {
            startActivityForResult(
                Intent(requireContext(), LaserChaseActivity::class.java),
                LaserChaseActivity.REQUEST_CODE
            )
        }
        btnMouse.setOnClickListener {
            startActivityForResult(
                Intent(requireContext(), MouseHuntActivity::class.java),
                MouseHuntActivity.REQUEST_CODE
            )
        }
    }

    private fun updateUI() {
        val s = gm.state

        catMoodEmoji.text    = s.getMoodEmoji()
        catNameText.text     = s.name
        catLevelText.text    = "Lvl ${s.getLevel()} · XP ${s.xp}"
        personalityText.text = "${s.getPersonalityEmoji()} ${s.personality.name.lowercase().replaceFirstChar { it.uppercase() }}"
        stageText.text       = "${s.getStageEmoji()} ${s.growthStage.name.lowercase().replaceFirstChar { it.uppercase() }}"

        hungerBar.progress    = s.hunger.toInt()
        happinessBar.progress = s.happiness.toInt()
        energyBar.progress    = s.energy.toInt()
        cleanBar.progress     = s.cleanliness.toInt()
        healthBar.progress    = s.health.toInt()
        trustBar.progress     = s.trust.toInt()

        hungerLabel.text    = "🍗 Hunger ${s.hunger.toInt()}%"
        happinessLabel.text = "😺 Happiness ${s.happiness.toInt()}%"
        energyLabel.text    = "⚡ Energy ${s.energy.toInt()}%"
        cleanLabel.text     = "🛁 Clean ${s.cleanliness.toInt()}%"
        healthLabel.text    = "❤️ Health ${s.health.toInt()}%"
        trustLabel.text     = "💛 Trust ${s.trust.toInt()}%"

        btnMedicine.visibility = if (s.isSick) View.VISIBLE else View.GONE

        btnTrick.isEnabled = s.growthStage != GrowthStage.KITTEN && s.trust >= 30f
        btnTrick.alpha = if (btnTrick.isEnabled) 1f else 0.4f

        val canAct = s.isAlive && !s.isRunningAway
        listOf(btnFeed, btnPet, btnPlay, btnWash, btnMedicine).forEach {
            it.isEnabled = canAct
            it.alpha = if (canAct) 1f else 0.4f
        }

        val activeEvent = s.activeEventId?.let { id ->
            com.imbir.game.data.EventList.all.find { it.id == id }
        }
        if (activeEvent != null) {
            eventCard.visibility = View.VISIBLE
            eventTitle.text = activeEvent.title
            eventDesc.text  = activeEvent.effectDescription
        } else {
            eventCard.visibility = View.GONE
        }

        animController.update(s)
    }

    private fun showPendingEvents() {
        gm.drainPendingEvents().forEach { event ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(event.title)
                .setMessage("${event.description}\n\n${event.effectDescription}")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showNewAchievements() {
        gm.drainPendingAchievements().forEach { ach ->
            Snackbar.make(
                requireView(),
                "🏆 Achievement unlocked: ${ach.name}!",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return
        when (requestCode) {
            LaserChaseActivity.REQUEST_CODE -> {
                val score = data.getIntExtra(LaserChaseActivity.EXTRA_SCORE, 0)
                gm.laserGameCompleted(score)
                animController.forceState(CatVisualState.PLAYING, 3000L)
                updateUI()
                showNewAchievements()
                Snackbar.make(requireView(), "Laser game: score $score! ⚡", Snackbar.LENGTH_SHORT).show()
            }
            MouseHuntActivity.REQUEST_CODE -> {
                val caught = data.getIntExtra(MouseHuntActivity.EXTRA_CAUGHT, 0)
                gm.mouseGameCompleted(caught)
                animController.forceState(CatVisualState.HAPPY, 3000L)
                updateUI()
                showNewAchievements()
                Snackbar.make(requireView(), "Caught $caught mice! 🐭", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTeachTrickDialog() {
        val allTricks = listOf("Sit", "High Five", "Fetch", "Roll Over", "Spin", "Wave")
        val available = allTricks.filter { it !in gm.state.learnedTricks }

        if (available.isEmpty()) {
            Toast.makeText(requireContext(), "Imbir knows all the tricks! 🎩", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Teach a trick")
            .setItems(available.toTypedArray()) { _, idx ->
                val trick = available[idx]
                gm.teachTrick(trick)
                animController.forceState(CatVisualState.HAPPY, 4000L)
                updateUI()
                Snackbar.make(requireView(), "Imbir learned: $trick! 🎩", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}