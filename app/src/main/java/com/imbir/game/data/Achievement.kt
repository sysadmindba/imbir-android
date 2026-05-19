package com.imbir.game.data

enum class AchievementTier { BASIC, INTERMEDIATE, ADVANCED }

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val tier: AchievementTier,
    var unlocked: Boolean = false,
    var progress: Int = 0,
    val goal: Int = 1
)

object AchievementList {
    fun getAll(): List<Achievement> = listOf(
        // Basic
        Achievement("first_day",    "First Day Together", "Open the app for the first time",       "🐱", AchievementTier.BASIC),
        Achievement("full_belly",   "Full Belly",         "Feed the cat 10 times",                  "🍗", AchievementTier.BASIC,  goal = 10),
        Achievement("clean_cat",    "Clean Cat",          "Wash the cat 5 times",                   "🛁", AchievementTier.BASIC,  goal = 5),
        Achievement("first_play",   "Playtime!",          "Play with the cat 5 times",              "🎾", AchievementTier.BASIC,  goal = 5),
        Achievement("first_pet",    "Gentle Touch",       "Pet the cat 10 times",                   "✋", AchievementTier.BASIC,  goal = 10),
        Achievement("survive_week", "One Week In",        "Keep your cat alive for 7 days",         "📅", AchievementTier.BASIC,  goal = 7),
        Achievement("first_sleep",  "Sweet Dreams",       "Put the cat to sleep 3 times",           "😴", AchievementTier.BASIC,  goal = 3),
        // Intermediate
        Achievement("best_friend",   "Best Friend",    "Reach 75 trust",                     "💛", AchievementTier.INTERMEDIATE),
        Achievement("trainer",       "Trainer",        "Teach the cat 3 tricks",             "🎩", AchievementTier.INTERMEDIATE, goal = 3),
        Achievement("play_master",   "Play Master",    "Play 50 times total",                "⭐", AchievementTier.INTERMEDIATE, goal = 50),
        Achievement("laser_ace",     "Laser Ace",      "Win 5 laser chase games",            "🔦", AchievementTier.INTERMEDIATE, goal = 5),
        Achievement("mouse_hunter",  "Mouse Hunter",   "Catch 20 mice total",                "🐭", AchievementTier.INTERMEDIATE, goal = 20),
        Achievement("daily_5",       "Devoted Owner",  "Play 5 consecutive days",            "🏅", AchievementTier.INTERMEDIATE, goal = 5),
        Achievement("come_back_cat", "Come Back Cat",  "Cat ran away but returned",          "🏃", AchievementTier.INTERMEDIATE),
        Achievement("healed",        "On the Mend",    "Nurse a sick cat back to health",    "💊", AchievementTier.INTERMEDIATE),
        // Advanced
        Achievement("perfect_owner",   "Perfect Owner",   "All stats above 80 at once",        "👑", AchievementTier.ADVANCED),
        Achievement("max_trust",       "Maximum Trust",   "Reach 100 trust",                   "💖", AchievementTier.ADVANCED),
        Achievement("emotional_bond",  "Emotional Bond",  "Reach Adult stage",                 "🌟", AchievementTier.ADVANCED),
        Achievement("senior_sage",     "Senior Sage",     "Reach Senior stage",                "🎭", AchievementTier.ADVANCED),
        Achievement("second_life",     "Second Life",     "Start a new cat after losing one",  "🌱", AchievementTier.ADVANCED),
    )
}