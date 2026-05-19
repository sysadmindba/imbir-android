package com.imbir.game.logic

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import com.imbir.game.R
import com.imbir.game.data.CatState
import com.imbir.game.data.CatVisualState
import com.imbir.game.data.GrowthStage
import com.imbir.game.data.Personality

/**
 * Drives all cat visuals on the Home screen.
 *
 * Two ImageViews (A / B) sit in a FrameLayout. The controller alternates which one
 * is "front" (fully visible), cross-fading between them on every state change.
 * A separate [AnimatorSet] of [ObjectAnimator]s provides continuous procedural motion
 * (breathing, bobbing, shaking, etc.) so the cat always looks alive.
 *
 * ── Public API ──────────────────────────────────────────────────────────────
 *
 *  [forceState]  — call immediately on a player action (feed, play, etc.).
 *                  Holds for [durationMs] then releases back to passive state.
 *  [update]      — call every tick (5 s) with the latest [CatState].
 *                  Resolves passive emotion from stats; transitions if needed.
 *  [clearForced] — call on Wake button press to clear SLEEPING hold.
 */
class CatAnimationController(
    private val context: Context,
    private val viewA: ImageView,
    private val viewB: ImageView,
) {
    private val dp = context.resources.displayMetrics.density

    // Which view is currently "front" (fully visible)
    private var frontIsA = true
    private val front get() = if (frontIsA) viewA else viewB
    private val back  get() = if (frontIsA) viewB else viewA

    private var currentState: CatVisualState? = null
    private var currentStage: GrowthStage?    = null

    // A forced state (EATING, PLAYING …) overrides passive until it expires
    private var forcedState:  CatVisualState? = null
    private var forcedExpiry: Long            = 0L

    // The looping procedural animator (breathe, bob, shake …)
    private var loopSet: AnimatorSet? = null

    companion object {
        private const val CROSSFADE_MS = 250L
        private const val LOOP_START_DELAY_MS = 280L   // slightly after fade completes
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Immediately transition to [state] and hold for [durationMs].
     * Calling with the same state simply extends the expiry without restarting.
     */
    fun forceState(
        state: CatVisualState,
        durationMs: Long = state.defaultDurationMs,
    ) {
        val extending = state == forcedState && state == currentState
        forcedState  = state
        forcedExpiry = if (durationMs > 0L) System.currentTimeMillis() + durationMs
                       else Long.MAX_VALUE   // hold indefinitely (SLEEPING)
        if (!extending) applyTarget(state)
    }

    /** Remove any forced state (e.g. Wake button pressed). */
    fun clearForced() {
        forcedState  = null
        forcedExpiry = 0L
        // next update() call will resolve passive state and transition
    }

    /** Called every ~5 s by the HomeFragment refresh loop. */
    fun update(catState: CatState) {
        if (forcedState != null && System.currentTimeMillis() > forcedExpiry) {
            forcedState = null
        }
        val target = resolveTarget(catState)

        // Transition if state changed OR growth stage changed (different sprite)
        if (target != currentState || catState.growthStage != currentStage) {
            currentStage = catState.growthStage
            applyTarget(target)
        }
    }

    // ── State resolution ─────────────────────────────────────────────────────

    private fun resolveTarget(s: CatState): CatVisualState {
        val passive = resolvePassive(s)
        val forced  = forcedState
        return if (forced != null && forced.priority <= passive.priority) forced else passive
    }

    private fun resolvePassive(s: CatState): CatVisualState = when {
        !s.isAlive                              -> CatVisualState.DEAD
        s.isSick                                -> CatVisualState.SICK
        s.energy < 15f                          -> CatVisualState.SLEEPING
        s.isRunningAway || s.happiness < 15f    -> CatVisualState.ANGRY
        s.happiness < 35f                       -> CatVisualState.SAD
        s.trust > 80f && s.happiness > 70f      -> CatVisualState.AFFECTIONATE
        s.happiness > 75f                       -> CatVisualState.HAPPY
        else                                    -> CatVisualState.IDLE
    }

    // ── Transition ───────────────────────────────────────────────────────────

    private fun applyTarget(target: CatVisualState) {
        val stage   = currentStage ?: GrowthStage.KITTEN
        val incoming = back
        val outgoing = front

        // Prepare incoming view (invisible, reset to neutral transform)
        incoming.setImageResource(resolveDrawable(stage, target))
        resetTransforms(incoming)
        incoming.alpha = 0f
        incoming.visibility = View.VISIBLE

        // Cross-fade
        incoming.animate()
            .alpha(1f)
            .setDuration(CROSSFADE_MS)
            .start()

        outgoing.animate()
            .alpha(0f)
            .setDuration(CROSSFADE_MS)
            .withEndAction {
                outgoing.visibility = View.INVISIBLE
                resetTransforms(outgoing)
            }
            .start()

        // Swap which view is "front"
        frontIsA = !frontIsA

        // Cancel the current procedural loop
        loopSet?.cancel()
        loopSet = null

        // Start the new loop after the cross-fade settles (avoids alpha conflict)
        val newFront = front  // capture after swap
        newFront.postDelayed({
            if (currentState == target) {   // guard: state might have changed again
                loopSet = buildLoop(target, newFront)
                loopSet?.start()
            }
        }, LOOP_START_DELAY_MS)

        currentState = target
    }

    // ── Drawable resolution ──────────────────────────────────────────────────

    private fun resolveDrawable(stage: GrowthStage, state: CatVisualState): Int {
        val sk = stage.name.lowercase()
        val ak = state.animKey
        return listOf("${sk}_${ak}", "cat_${ak}", "cat_idle")
            .firstNotNullOfOrNull { name ->
                context.resources
                    .getIdentifier(name, "drawable", context.packageName)
                    .takeIf { it != 0 }
            } ?: R.drawable.cat_idle
    }

    // ── Procedural loop builder ──────────────────────────────────────────────

    private fun buildLoop(state: CatVisualState, v: ImageView): AnimatorSet {
        val animators = when (state) {
            CatVisualState.IDLE          -> breathe(v, 3000L, 0.030f) + bob(v, 3200L, -5f)
            CatVisualState.SLEEPING      -> breathe(v, 5000L, 0.018f) + fade(v, 5000L, 0.90f, 1.0f)
            CatVisualState.EATING        -> breathe(v, 450L,  0.018f) + bob(v, 450L,  -5f)
            CatVisualState.PLAYING       -> breathe(v, 540L,  0.050f) + bob(v, 540L, -14f)
            CatVisualState.CLEANING      -> shake(v, 200L, 5.5f)
            CatVisualState.SICK          -> breathe(v, 4000L, 0.012f) + fade(v, 2500L, 0.78f, 1.00f)
            CatVisualState.ANGRY         -> trembleX(v, 170L, 2.5f)   + breathe(v, 1200L, 0.020f)
            CatVisualState.SAD           -> breathe(v, 4500L, 0.018f) + bob(v, 4500L, 3f)
            CatVisualState.HAPPY         -> breathe(v, 1300L, 0.028f) + bob(v, 1300L, -6f)
            CatVisualState.AFFECTIONATE  -> breathe(v, 2800L, 0.022f) + sway(v, 2800L, -3f)
            CatVisualState.DEAD          -> fade(v, 2000L, 0.35f, 0.55f)
        }
        return AnimatorSet().apply { playTogether(*animators.toTypedArray()) }
    }

    // ── Animator primitives ──────────────────────────────────────────────────
    // Each returns List<ObjectAnimator> so callers can flatten with '+'.

    /** Symmetric scale pulse on both axes — simulates breathing. */
    private fun breathe(v: ImageView, dur: Long, amount: Float): List<ObjectAnimator> = listOf(
        oa(v, "scaleX", dur, 1f, 1f + amount),
        oa(v, "scaleY", dur, 1f, 1f + amount),
    )

    /** Vertical translation loop — gentle bob or drop. Positive = moves down. */
    private fun bob(v: ImageView, dur: Long, offsetDp: Float): List<ObjectAnimator> = listOf(
        oa(v, "translationY", dur, 0f, offsetDp * dp),
    )

    /** Horizontal translation — left/right sway. */
    private fun sway(v: ImageView, dur: Long, offsetDp: Float): List<ObjectAnimator> = listOf(
        oa(v, "translationX", dur, 0f, offsetDp * dp),
    )

    /** Rapid left-right micro-tremble on X — agitation / anger. */
    private fun trembleX(v: ImageView, dur: Long, ampDp: Float): List<ObjectAnimator> = listOf(
        oa(v, "translationX", dur, -ampDp * dp, ampDp * dp),
    )

    /** Z-axis rotation oscillation — water shake after cleaning. */
    private fun shake(v: ImageView, dur: Long, degrees: Float): List<ObjectAnimator> = listOf(
        oa(v, "rotation", dur, -degrees, degrees),
    )

    /** Alpha breath — dims then brightens (sick / sleeping). */
    private fun fade(v: ImageView, dur: Long, lo: Float, hi: Float): List<ObjectAnimator> = listOf(
        ObjectAnimator.ofFloat(v, "alpha", lo, hi).apply {
            duration    = dur
            repeatCount = ValueAnimator.INFINITE
            repeatMode  = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        },
    )

    /** Core factory: infinite-reverse ObjectAnimator with ease curve. */
    private fun oa(v: ImageView, prop: String, dur: Long, from: Float, to: Float) =
        ObjectAnimator.ofFloat(v, prop, from, to).apply {
            duration    = dur
            repeatCount = ValueAnimator.INFINITE
            repeatMode  = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun resetTransforms(v: ImageView) {
        v.scaleX = 1f;       v.scaleY = 1f
        v.translationX = 0f; v.translationY = 0f
        v.rotation = 0f;     v.alpha = 1f
    }
}