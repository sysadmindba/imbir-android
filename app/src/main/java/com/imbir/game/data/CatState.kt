package com.imbir.game.data

enum class GrowthStage { KITTEN, TEEN, ADULT, SENIOR }

enum class Personality { NEUTRAL, AFFECTIONATE, INDEPENDENT, PLAYFUL, STUBBORN, LAZY }

data class CatState(
    // Core stats (0–100)
    var hunger: Float = 80f,
    var happiness: Float = 100f,
    var energy: Float = 100f,
    var cleanliness: Float = 100f,
    var health: Float = 100f,
    var trust: Float = 10f,

    // Progression
    var xp: Int = 0,
    var growthStage: GrowthStage = GrowthStage.KITTEN,
    var personality: Personality = Personality.NEUTRAL,

    // Identity
    var name: String = "Imbir",
    var birthTimestamp: Long = System.currentTimeMillis(),
    var lastUpdateTimestamp: Long = System.currentTimeMillis(),
    var lastFedTimestamp: Long = System.currentTimeMillis(),
    var lastOpenedDay: Int = -1,
    var consecutiveDaysPlayed: Int = 0,
    var totalDaysAlive: Int = 0,

    // Action counters
    var playCount: Int = 0,
    var feedCount: Int = 0,
    var washCount: Int = 0,
    var petCount: Int = 0,
    var sleepCount: Int = 0,
    var overplayCount: Int = 0,

    // Mini-game stats
    var laserWins: Int = 0,
    var miceCaught: Int = 0,

    // Neglect tracking
    var neglectTimeMinutes: Float = 0f,
    var totalNeglectDays: Int = 0,

    // Tricks
    var learnedTricks: ArrayList<String> = arrayListOf(),

    // State flags
    var isAlive: Boolean = true,
    var isSick: Boolean = false,
    var isRunningAway: Boolean = false,
    var runAwayEndsAt: Long = 0L,

    // Death + inheritance
    var deathCount: Int = 0,
    var prevCatName: String = "",
    var prevCatPersonality: String = "",
    var inheritedTrait: String = "",

    // Active event
    var activeEventId: String? = null,
    var eventEndsAt: Long = 0L,
) {
    fun getLevel(): Int = (xp / 100) + 1

    fun isNeglected(): Boolean = hunger < 20f || cleanliness < 20f || health < 20f

    fun isCritical(): Boolean = hunger < 5f || health < 5f

    fun getMoodEmoji(): String = when {
        !isAlive -> "💀"
        isRunningAway -> "🏃"
        isSick -> "🤒"
        happiness > 80 -> "😻"
        happiness > 60 -> "😺"
        happiness > 40 -> "😐"
        happiness > 20 -> "😿"
        else -> "😾"
    }

    fun getPersonalityEmoji(): String = when (personality) {
        Personality.AFFECTIONATE -> "💛"
        Personality.PLAYFUL -> "⚡"
        Personality.INDEPENDENT -> "🌙"
        Personality.STUBBORN -> "😤"
        Personality.LAZY -> "💤"
        Personality.NEUTRAL -> "✨"
    }

    fun getStageEmoji(): String = when (growthStage) {
        GrowthStage.KITTEN -> "🐱"
        GrowthStage.TEEN -> "🐈"
        GrowthStage.ADULT -> "🐈‍⬛"
        GrowthStage.SENIOR -> "🦁"
    }
}