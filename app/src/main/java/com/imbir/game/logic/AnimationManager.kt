package com.imbir.game.logic

import android.content.Context
import android.widget.ImageView
import com.imbir.game.R
import com.imbir.game.data.CatAnimation
import com.imbir.game.data.GrowthStage

class AnimationManager(
    private val context: Context,
    private val catSprite: ImageView,
    private val getGrowthStage: () -> GrowthStage
) {
    private val returnToIdleRunnable = Runnable {
        updateAnimationForAction(CatAnimation.IDLE_BLINK, getGrowthStage(), temporary = false)
    }

    fun updateAnimationForAction(
        action: CatAnimation,
        stage: GrowthStage,
        temporary: Boolean = true
    ) {
        val resId = resolveDrawable(action, stage)
        catSprite.setImageResource(resId)

        if (temporary) {
            catSprite.removeCallbacks(returnToIdleRunnable)
            catSprite.postDelayed(returnToIdleRunnable, 3000)
        }
    }

    fun showMoodIdle(stage: GrowthStage, happiness: Float, isSick: Boolean) {
        val anim = when {
            isSick         -> CatAnimation.SICK
            happiness > 80 -> CatAnimation.HAPPY
            happiness > 50 -> CatAnimation.IDLE_BLINK
            happiness > 25 -> CatAnimation.SAD
            else           -> CatAnimation.ANGRY
        }
        catSprite.setImageResource(resolveDrawable(anim, stage))
    }

    private fun resolveDrawable(action: CatAnimation, stage: GrowthStage): Int {
        val stageKey = stage.name.lowercase()
        val animKey  = action.key

        val stageSpecific = context.resources.getIdentifier(
            "${stageKey}_${animKey}", "drawable", context.packageName
        )
        if (stageSpecific != 0) return stageSpecific

        val generic = context.resources.getIdentifier(
            "cat_${animKey}", "drawable", context.packageName
        )
        if (generic != 0) return generic

        return R.drawable.cat_idle
    }
}