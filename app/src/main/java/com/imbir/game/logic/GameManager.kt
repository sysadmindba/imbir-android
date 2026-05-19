package com.imbir.game.logic

import com.imbir.game.data.Achievement
import com.imbir.game.data.AchievementList
import com.imbir.game.data.CatState
import com.imbir.game.data.EventList
import com.imbir.game.data.EventType
import com.imbir.game.data.GameEvent
import com.imbir.game.data.GrowthStage
import com.imbir.game.data.Personality
import com.imbir.game.data.RoomCatalog
import com.imbir.game.data.RoomItemType
import com.imbir.game.data.RoomState
import com.imbir.game.util.PreferencesManager
import java.util.Calendar
import java.util.Random

class GameManager(private val prefs: PreferencesManager) {

    var state: CatState = prefs.loadCatState() ?: CatState()
        private set

    var roomState: RoomState = prefs.loadRoomState() ?: RoomState()
        private set

    var achievements: MutableList<Achievement> =
        (prefs.loadAchievements() ?: AchievementList.getAll()).toMutableList()
        private set

    private val pendingAchievements = mutableListOf<Achievement>()
    private val pendingEvents = mutableListOf<GameEvent>()

    init {
        applyTimeDecay()
        checkDailyLogin()
        resolveExpiredEvents()
        maybeCheckGrowth()
        maybeCheckDeath()
        if (state.isAlive) maybeFireEvent()
        checkAllAchievements()
        unlockRoomItemsForLevel()
        save()
    }

    // ── Public event/achievement drain ──────────────────────────────────────

    fun drainPendingAchievements(): List<Achievement> {
        val result = pendingAchievements.toList()
        pendingAchievements.clear()
        return result
    }

    fun drainPendingEvents(): List<GameEvent> {
        val result = pendingEvents.toList()
        pendingEvents.clear()
        return result
    }

    // ── Time-based decay ────────────────────────────────────────────────────

    private fun applyTimeDecay() {
        val now = System.currentTimeMillis()
        val secs = (now - state.lastUpdateTimestamp) / 1000f
        // 1 unit per 10 min base
        val base = 1f / 600f

        if (!state.isAlive) return

        // Running away cats don't decay and can't be interacted with
        if (state.isRunningAway) {
            state.lastUpdateTimestamp = now
            return
        }

        state.hunger      = (state.hunger      - secs * base * 1.0f).coerceIn(0f, 100f)
        state.happiness   = (state.happiness   - secs * base * 0.8f).coerceIn(0f, 100f)
        state.energy      = (state.energy      - secs * base * 0.5f).coerceIn(0f, 100f)
        state.cleanliness = (state.cleanliness - secs * base * 0.3f).coerceIn(0f, 100f)

        // Sickness drains health rapidly
        val sickMult = if (state.isSick) 4f else 1f
        when {
            state.hunger < 10 || state.cleanliness < 10 || state.isSick ->
                state.health = (state.health - secs * base * 2f * sickMult).coerceIn(0f, 100f)
            state.hunger > 70 && state.cleanliness > 70 && !state.isSick ->
                state.health = (state.health + secs * base * 0.5f).coerceIn(0f, 100f)
        }

        // Room bonus reduces happiness decay slightly
        val roomMoodBonus = roomState.getMoodBonus() / 2000f
        val roomEnergyBonus = roomState.getEnergyBonus() / 2000f
        state.happiness = (state.happiness + secs * roomMoodBonus).coerceIn(0f, 100f)
        state.energy    = (state.energy    + secs * roomEnergyBonus).coerceIn(0f, 100f)

        // Neglect tracking
        if (state.isNeglected()) state.neglectTimeMinutes += secs / 60f

        state.lastUpdateTimestamp = now
        updatePersonality()
    }

    private fun checkDailyLogin() {
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_YEAR)
        if (state.lastOpenedDay == today) return

        val yesterday = if (today == 1) 365 else today - 1
        state.consecutiveDaysPlayed =
            if (state.lastOpenedDay == yesterday) state.consecutiveDaysPlayed + 1 else 1

        state.lastOpenedDay = today
        if (state.isAlive) state.totalDaysAlive++

        if (state.consecutiveDaysPlayed == 1) checkAchievement("first_day")
    }

    private fun resolveExpiredEvents() {
        val now = System.currentTimeMillis()
        if (state.activeEventId == null) return

        if (now > state.eventEndsAt) {
            when (state.activeEventId) {
                "runaway" -> {
                    state.isRunningAway = false
                    val comeBack = achievements.find { it.id == "come_back_cat" }
                    if (comeBack != null && !comeBack.unlocked) unlockAchievement(comeBack)
                }
                "sick" -> state.isSick = false
            }
            state.activeEventId = null
        }
    }

    private fun maybeFireEvent() {
        if (state.activeEventId != null) return
        val rng = Random()
        val roll = rng.nextInt(100)

        val event: GameEvent? = when {
            state.hunger < 15 && state.personality == Personality.STUBBORN && roll < 35 -> EventList.FOOD_THEFT
            state.happiness < 25 && state.trust > 30 && roll < 25 -> EventList.RAN_AWAY
            state.health < 30 && roll < 30 -> EventList.SICK
            roll < 5 -> EventList.FOUND_GIFT
            roll < 12 -> EventList.GUEST_ANIMAL
            else -> null
        }
        event?.let { triggerEvent(it) }
    }

    private fun triggerEvent(event: GameEvent) {
        state.activeEventId = event.id
        state.eventEndsAt = System.currentTimeMillis() + event.durationMinutes * 60_000L

        when (event.type) {
            EventType.SICKNESS -> state.isSick = true
            EventType.GIFT -> {
                state.xp += xpBoosted(30)
                state.activeEventId = null
            }
            EventType.RUNAWAY -> {
                state.isRunningAway = true
                state.runAwayEndsAt = state.eventEndsAt
            }
            EventType.HOLIDAY -> { /* XP boost applied in xpBoosted() */ }
            EventType.GUEST_ANIMAL -> {
                state.happiness = (state.happiness + 20f).coerceIn(0f, 100f)
                state.activeEventId = null
            }
            EventType.FOOD_THEFT -> {
                state.hunger      = (state.hunger      + 20f).coerceIn(0f, 100f)
                state.cleanliness = (state.cleanliness - 10f).coerceIn(0f, 100f)
                state.activeEventId = null
            }
        }
        pendingEvents += event
    }

    private fun updatePersonality() {
        state.personality = when {
            state.playCount > 50 && state.trust > 70 -> Personality.PLAYFUL
            state.neglectTimeMinutes > 180 -> Personality.INDEPENDENT
            state.trust > 80 -> Personality.AFFECTIONATE
            state.overplayCount > 10 && state.petCount < state.playCount / 3 -> Personality.STUBBORN
            state.sleepCount > state.playCount * 2 + 5 -> Personality.LAZY
            state.petCount > 30 && state.trust > 50 -> Personality.AFFECTIONATE
            else -> Personality.NEUTRAL
        }
    }

    private fun maybeCheckGrowth() {
        val level = state.getLevel()
        state.growthStage = when {
            level < 5  -> GrowthStage.KITTEN
            level < 15 -> GrowthStage.TEEN
            level < 30 -> GrowthStage.ADULT
            else       -> GrowthStage.SENIOR
        }
    }

    private fun maybeCheckDeath() {
        if (!state.isAlive) return
        val starved = state.hunger <= 0 && state.neglectTimeMinutes > 360
        val healthDead = state.health <= 0
        if (starved || healthDead) handleDeath()
    }

    private fun handleDeath() {
        state.deathCount++
        val oldName = state.name
        val oldPersonality = state.personality
        val oldTrust = state.trust

        val newState = CatState(
            deathCount = state.deathCount,
            prevCatName = oldName,
            prevCatPersonality = oldPersonality.name,
            trust = (oldTrust * 0.15f).coerceIn(0f, 15f),
            inheritedTrait = when (oldPersonality) {
                Personality.AFFECTIONATE -> "affectionate"
                Personality.PLAYFUL -> "playful"
                Personality.LAZY -> "lazy"
                else -> ""
            }
        )
        state = newState
        checkAchievement("second_life")
    }

    // ── Foreground tick (called every few seconds while app is open) ────────

    fun tick() {
        if (!state.isAlive) return
        applyTimeDecay()
        resolveExpiredEvents()
        maybeCheckDeath()
        checkAllAchievements()
        prefs.saveCatState(state)
    }

    // ── Player actions ──────────────────────────────────────────────────────

    fun feed() {
        if (!canAct()) return
        state.hunger      = (state.hunger      + 25f).coerceIn(0f, 100f)
        state.cleanliness = (state.cleanliness -  3f).coerceIn(0f, 100f)
        state.feedCount++
        state.xp += xpBoosted(10)
        state.trust = (state.trust + 0.5f).coerceIn(0f, 100f)
        state.lastFedTimestamp = System.currentTimeMillis()
        afterAction()
    }

    fun play() {
        if (!canAct()) return
        if (state.energy < 10f) state.overplayCount++
        state.happiness = (state.happiness + 20f).coerceIn(0f, 100f)
        state.energy    = (state.energy    - 15f).coerceIn(0f, 100f)
        state.hunger    = (state.hunger    -  5f).coerceIn(0f, 100f)
        state.playCount++
        state.xp += xpBoosted(15)
        state.trust = (state.trust + 1f).coerceIn(0f, 100f)
        afterAction()
    }

    fun pet() {
        if (!canAct()) return
        val mult = when {
            state.trust > 80 -> 1.5f
            state.trust > 40 -> 1.0f
            else -> 0.5f
        }
        state.happiness = (state.happiness + 15f * mult).coerceIn(0f, 100f)
        state.petCount++
        state.xp += xpBoosted(8)
        state.trust = (state.trust + 1.5f).coerceIn(0f, 100f)
        afterAction()
    }

    fun sleep() {
        if (!state.isAlive) return
        state.energy     = (state.energy     + 40f).coerceIn(0f, 100f)
        state.happiness  = (state.happiness  +  5f).coerceIn(0f, 100f)
        state.sleepCount++
        state.xp += xpBoosted(5)
        afterAction()
    }

    fun wash() {
        if (!canAct()) return
        state.cleanliness = (state.cleanliness + 35f).coerceIn(0f, 100f)
        val moodHit = if (state.trust > 50) -5f else -15f
        state.happiness = (state.happiness + moodHit).coerceIn(0f, 100f)
        state.washCount++
        state.xp += xpBoosted(5)
        state.trust = (state.trust + 0.2f).coerceIn(0f, 100f)
        afterAction()
    }

    fun teachTrick(trickName: String) {
        if (!canAct()) return
        if (state.growthStage == GrowthStage.KITTEN) return
        if (state.trust < 30f) return
        if (state.learnedTricks.contains(trickName)) return
        state.learnedTricks.add(trickName)
        state.xp += xpBoosted(30)
        state.trust     = (state.trust     + 3f ).coerceIn(0f, 100f)
        state.happiness = (state.happiness + 10f).coerceIn(0f, 100f)
        afterAction()
    }

    fun treatSickness() {
        if (!state.isSick) return
        state.isSick = false
        state.activeEventId = null
        state.health = (state.health + 30f).coerceIn(0f, 100f)
        state.xp += xpBoosted(20)
        checkAchievement("healed")
        afterAction()
    }

    fun laserGameCompleted(score: Int) {
        if (!state.isAlive) return
        val xp = (score * 2).coerceAtMost(40)
        state.xp += xpBoosted(xp)
        state.happiness = (state.happiness + 25f).coerceIn(0f, 100f)
        state.energy    = (state.energy    - 10f).coerceIn(0f, 100f)
        state.hunger    = (state.hunger    -  5f).coerceIn(0f, 100f)
        state.playCount++
        state.trust = (state.trust + 0.5f).coerceIn(0f, 100f)
        if (score >= 5) {
            state.laserWins++
            updateAchievementProgress("laser_ace", state.laserWins)
        }
        afterAction()
    }

    fun mouseGameCompleted(caught: Int) {
        if (!state.isAlive) return
        val xp = (caught * 3).coerceAtMost(40)
        state.xp += xpBoosted(xp)
        state.happiness = (state.happiness + 20f).coerceIn(0f, 100f)
        state.energy    = (state.energy    - 10f).coerceIn(0f, 100f)
        state.hunger    = (state.hunger    -  5f).coerceIn(0f, 100f)
        state.playCount++
        state.trust = (state.trust + 0.5f).coerceIn(0f, 100f)
        state.miceCaught += caught
        updateAchievementProgress("mouse_hunter", state.miceCaught)
        afterAction()
    }

    // ── Room ────────────────────────────────────────────────────────────────

    fun placeRoomItem(itemId: String): Boolean {
        val item = RoomCatalog.items.find { it.id == itemId } ?: return false
        if (itemId !in roomState.unlockedItems) return false
        return when (item.type) {
            RoomItemType.WALLPAPER -> {
                roomState.activeWallpaper = itemId
                prefs.saveRoomState(roomState)
                true
            }
            else -> {
                if (itemId !in roomState.placedItems) {
                    roomState.placedItems.add(itemId)
                    prefs.saveRoomState(roomState)
                    true
                } else false
            }
        }
    }

    fun removeRoomItem(itemId: String) {
        roomState.placedItems.remove(itemId)
        prefs.saveRoomState(roomState)
    }

    fun unlockRoomItemsForLevel() {
        val level = state.getLevel()
        RoomCatalog.items.forEach { item ->
            if (item.unlockLevel <= level && item.id !in roomState.unlockedItems) {
                roomState.unlockedItems.add(item.id)
            }
        }
    }

    // ── Achievements ────────────────────────────────────────────────────────

    private fun checkAllAchievements() {
        updateAchievementProgress("full_belly",   state.feedCount)
        updateAchievementProgress("clean_cat",    state.washCount)
        updateAchievementProgress("first_play",   state.playCount)
        updateAchievementProgress("first_pet",    state.petCount)
        updateAchievementProgress("first_sleep",  state.sleepCount)
        updateAchievementProgress("play_master",  state.playCount)
        updateAchievementProgress("trainer",      state.learnedTricks.size)
        updateAchievementProgress("survive_week", state.totalDaysAlive)
        updateAchievementProgress("daily_5",      state.consecutiveDaysPlayed)
        updateAchievementProgress("laser_ace",    state.laserWins)
        updateAchievementProgress("mouse_hunter", state.miceCaught)

        if (state.trust >= 75f)  checkAchievement("best_friend")
        if (state.trust >= 100f) checkAchievement("max_trust")
        if (state.growthStage == GrowthStage.ADULT || state.growthStage == GrowthStage.SENIOR)
            checkAchievement("emotional_bond")
        if (state.growthStage == GrowthStage.SENIOR) checkAchievement("senior_sage")
        if (state.hunger > 80 && state.happiness > 80 && state.energy > 80 &&
            state.cleanliness > 80 && state.health > 80)
            checkAchievement("perfect_owner")
    }

    private fun updateAchievementProgress(id: String, current: Int) {
        val ach = achievements.find { it.id == id } ?: return
        if (ach.unlocked) return
        ach.progress = current
        if (ach.progress >= ach.goal) unlockAchievement(ach)
    }

    fun checkAchievement(id: String) {
        val ach = achievements.find { it.id == id } ?: return
        if (ach.unlocked) return
        unlockAchievement(ach)
    }

    private fun unlockAchievement(ach: Achievement) {
        ach.unlocked = true
        ach.progress = ach.goal
        pendingAchievements += ach
        state.xp += 50
        unlockRoomItemsForLevel()
        prefs.saveAchievements(achievements)
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun canAct(): Boolean = state.isAlive && !state.isRunningAway

    private fun xpBoosted(base: Int): Int =
        if (state.activeEventId == "holiday") base * 2 else base

    private fun afterAction() {
        maybeCheckGrowth()
        maybeCheckDeath()
        checkAllAchievements()
        save()
    }

    fun save() {
        state.lastUpdateTimestamp = System.currentTimeMillis()
        prefs.saveCatState(state)
        prefs.saveAchievements(achievements)
        prefs.saveRoomState(roomState)
    }
}