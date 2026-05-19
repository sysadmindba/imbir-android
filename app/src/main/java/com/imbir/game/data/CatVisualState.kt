package com.imbir.game.data

/**
 * Every named visual state the cat can display.
 *
 * [priority]          — lower number wins when multiple states compete.
 * [animKey]           — used by AnimationController to resolve a drawable:
 *                       tries "{stage}_{animKey}" → "cat_{animKey}" → "cat_idle"
 * [defaultDurationMs] — how long a forced action state holds before releasing
 *                       back to the passive state. 0 = held until explicitly cleared.
 */
enum class CatVisualState(
    val priority: Int,
    val animKey: String,
    val defaultDurationMs: Long = 0L,
) {
    // ── Critical (always override) ───────────────────────────────────────
    DEAD        (0,  "death",     0L),
    SICK        (1,  "sick",      0L),

    // ── Timed action states (player-triggered, expire after duration) ─────
    SLEEPING    (2,  "sleep",     0L),           // held until Wake pressed
    EATING      (3,  "eat",    8_000L),
    PLAYING     (4,  "ball",   9_000L),
    CLEANING    (5,  "bath",   7_000L),

    // ── Passive emotional states (computed every tick from stats) ─────────
    ANGRY       (6,  "angry",     0L),
    SAD         (7,  "sad",       0L),
    AFFECTIONATE(8,  "idle_blink",0L),
    HAPPY       (9,  "happy",     0L),
    IDLE        (10, "idle_blink",0L),
}