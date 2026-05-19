package com.imbir.game.data

enum class RoomItemType { WALLPAPER, FURNITURE, TOY, DECORATION }

data class RoomItem(
    val id: String,
    val name: String,
    val emoji: String,
    val type: RoomItemType,
    val moodBonus: Int = 0,
    val energyBonus: Int = 0,
    val unlockLevel: Int = 1,
    val description: String = ""
)

data class RoomState(
    var activeWallpaper: String = "plain",
    var placedItems: ArrayList<String> = arrayListOf(),
    var unlockedItems: ArrayList<String> = arrayListOf("plain", "cushion", "ball"),
) {
    fun getMoodBonus(): Int =
        RoomCatalog.items.filter { it.id in placedItems }.sumOf { it.moodBonus }

    fun getEnergyBonus(): Int =
        RoomCatalog.items.filter { it.id in placedItems }.sumOf { it.energyBonus }

    fun getActiveWallpaperItem(): RoomItem =
        RoomCatalog.items.find { it.id == activeWallpaper } ?: RoomCatalog.items.first()
}

object RoomCatalog {
    val items = listOf(
        // Wallpapers
        RoomItem("plain",   "Plain Wall",   "⬜", RoomItemType.WALLPAPER, unlockLevel = 1,  description = "Simple and clean"),
        RoomItem("sunset",  "Sunset Wall",  "🌅", RoomItemType.WALLPAPER, moodBonus = 5,  unlockLevel = 5,  description = "Warm evening glow"),
        RoomItem("stars",   "Starry Night", "⭐", RoomItemType.WALLPAPER, moodBonus = 8, energyBonus = 3, unlockLevel = 10, description = "Peaceful night sky"),
        RoomItem("garden",  "Garden View",  "🌿", RoomItemType.WALLPAPER, moodBonus = 10, unlockLevel = 15, description = "Lush outdoor scenery"),
        RoomItem("ocean",   "Ocean Waves",  "🌊", RoomItemType.WALLPAPER, moodBonus = 12, energyBonus = 5, unlockLevel = 20, description = "Calming ocean view"),
        // Furniture
        RoomItem("cushion",     "Cozy Cushion",  "🛋️", RoomItemType.FURNITURE, energyBonus = 5, unlockLevel = 1,  description = "A soft spot to rest"),
        RoomItem("scratcher",   "Cat Scratcher", "🪵", RoomItemType.FURNITURE, moodBonus = 5,  unlockLevel = 3,  description = "Keeps claws sharp"),
        RoomItem("cat_tree",    "Cat Tree",      "🌲", RoomItemType.FURNITURE, moodBonus = 10, energyBonus = 5, unlockLevel = 8,  description = "Multi-level climbing fun"),
        RoomItem("window_perch","Window Perch",  "🪟", RoomItemType.FURNITURE, moodBonus = 8,  unlockLevel = 12, description = "Watch the world go by"),
        RoomItem("hammock",     "Cat Hammock",   "🛏️", RoomItemType.FURNITURE, energyBonus = 10, unlockLevel = 18, description = "Swing and snooze"),
        // Toys
        RoomItem("ball",       "Yarn Ball",    "🧶", RoomItemType.TOY, moodBonus = 3, unlockLevel = 1,  description = "Classic cat toy"),
        RoomItem("mouse_toy",  "Toy Mouse",    "🐭", RoomItemType.TOY, moodBonus = 5, unlockLevel = 5,  description = "Tiny prey to hunt"),
        RoomItem("feather",    "Feather Wand", "🪶", RoomItemType.TOY, moodBonus = 7, unlockLevel = 10, description = "Irresistible feathers"),
        RoomItem("laser_toy",  "Auto Laser",   "🔴", RoomItemType.TOY, moodBonus = 8, unlockLevel = 15, description = "Infinite laser fun"),
        // Decorations
        RoomItem("plant",     "Indoor Plant", "🌱", RoomItemType.DECORATION, moodBonus = 3,  unlockLevel = 2,  description = "A little green friend"),
        RoomItem("fish_tank", "Fish Tank",    "🐠", RoomItemType.DECORATION, moodBonus = 8, energyBonus = 3, unlockLevel = 15, description = "Fascinating to watch"),
        RoomItem("clock",     "Cat Clock",    "🕐", RoomItemType.DECORATION, moodBonus = 2,  unlockLevel = 3,  description = "Tick tock"),
        RoomItem("photo",     "Cat Photo",    "📷", RoomItemType.DECORATION, moodBonus = 5,  unlockLevel = 8,  description = "Memories on the wall"),
    )

    val wallpaperColors = mapOf(
        "plain"   to 0xFFFDF5E6.toInt(),
        "sunset"  to 0xFFFFD6A5.toInt(),
        "stars"   to 0xFF1A1A3E.toInt(),
        "garden"  to 0xFFD4EDDA.toInt(),
        "ocean"   to 0xFFCCE5FF.toInt(),
    )
}