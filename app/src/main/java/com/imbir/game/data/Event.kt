package com.imbir.game.data

enum class EventType { SICKNESS, GIFT, RUNAWAY, HOLIDAY, GUEST_ANIMAL, FOOD_THEFT }

data class GameEvent(
    val id: String,
    val title: String,
    val description: String,
    val type: EventType,
    val durationMinutes: Int = 0,
    val effectDescription: String = "",
    val emoji: String = "❓"
)

object EventList {
    val SICK = GameEvent(
        id = "sick",
        title = "Imbir is sick! 🤒",
        description = "Your cat caught a cold and needs treatment.",
        type = EventType.SICKNESS,
        durationMinutes = 360,
        effectDescription = "Health is draining. Use the medicine button to treat.",
        emoji = "🤒"
    )
    val FOUND_GIFT = GameEvent(
        id = "gift",
        title = "Imbir found something! 🎁",
        description = "Your cat dragged in a mysterious gift for you!",
        type = EventType.GIFT,
        durationMinutes = 0,
        effectDescription = "+30 XP bonus!",
        emoji = "🎁"
    )
    val RAN_AWAY = GameEvent(
        id = "runaway",
        title = "Imbir ran away! 😿",
        description = "Your cat is upset and ran off. Wait for them to return.",
        type = EventType.RUNAWAY,
        durationMinutes = 120,
        effectDescription = "Imbir will return in 2 hours.",
        emoji = "😿"
    )
    val HOLIDAY_BONUS = GameEvent(
        id = "holiday",
        title = "It's a special day! 🎉",
        description = "A festive mood is in the air — Imbir is excited!",
        type = EventType.HOLIDAY,
        durationMinutes = 1440,
        effectDescription = "All XP gains doubled for today!",
        emoji = "🎉"
    )
    val GUEST_ANIMAL = GameEvent(
        id = "guest",
        title = "A visitor! 🐾",
        description = "A friendly neighbourhood animal came to visit Imbir!",
        type = EventType.GUEST_ANIMAL,
        durationMinutes = 0,
        effectDescription = "Happiness +20!",
        emoji = "🐾"
    )
    val FOOD_THEFT = GameEvent(
        id = "food_theft",
        title = "Imbir stole food! 😼",
        description = "Your hungry cat raided the pantry while you weren't looking.",
        type = EventType.FOOD_THEFT,
        durationMinutes = 0,
        effectDescription = "Hunger +20, Cleanliness -10",
        emoji = "😼"
    )

    val all = listOf(SICK, FOUND_GIFT, RAN_AWAY, HOLIDAY_BONUS, GUEST_ANIMAL, FOOD_THEFT)
}