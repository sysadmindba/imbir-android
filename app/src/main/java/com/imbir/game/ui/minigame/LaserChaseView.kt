package com.imbir.game.ui.minigame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class LaserChaseView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : View(ctx, attrs) {

    var onHit: (() -> Unit)? = null
    var speedMultiplier = 1f

    private var dotX = 0f
    private var dotY = 0f
    private var velX = 0f
    private var velY = 0f
    private val dotRadius = 28f
    private val hitRadius = 60f

    private var isRunning = false
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x44FF0000
    }

    private val frameRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            update()
            invalidate()
            postDelayed(this, 16L)
        }
    }

    fun startGame() {
        if (width == 0 || height == 0) {
            post { startGame() }
            return
        }
        dotX = width / 2f
        dotY = height / 2f
        val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
        velX = cos(angle) * 6f
        velY = sin(angle) * 6f
        isRunning = true
        post(frameRunnable)
    }

    fun stopGame() {
        isRunning = false
        removeCallbacks(frameRunnable)
        invalidate()
    }

    private fun update() {
        val speed = speedMultiplier
        dotX += velX * speed
        dotY += velY * speed

        if (dotX - dotRadius < 0) { dotX = dotRadius; velX = -velX }
        if (dotX + dotRadius > width) { dotX = width - dotRadius; velX = -velX }
        if (dotY - dotRadius < 0) { dotY = dotRadius; velY = -velY }
        if (dotY + dotRadius > height) { dotY = height - dotRadius; velY = -velY }

        // Randomly change direction occasionally
        if (Random.nextInt(120) == 0) {
            val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
            val currentSpeed = sqrt(velX * velX + velY * velY)
            velX = cos(angle) * currentSpeed
            velY = sin(angle) * currentSpeed
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(0xFF1A1A2E.toInt())

        if (!isRunning) {
            paint.color = Color.WHITE
            paint.textSize = 48f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Tap Start to play!", width / 2f, height / 2f, paint)
            return
        }

        // Shadow
        canvas.drawCircle(dotX + 4, dotY + 4, dotRadius + 10, shadowPaint)

        // Glow effect
        glowPaint.shader = RadialGradient(
            dotX, dotY, dotRadius * 3,
            intArrayOf(0xFFFF4444.toInt(), 0x44FF0000, 0x00000000),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(dotX, dotY, dotRadius * 3, glowPaint)

        // Main dot
        paint.color = 0xFFFF2222.toInt()
        canvas.drawCircle(dotX, dotY, dotRadius, paint)

        // Bright center
        paint.color = 0xFFFFAAAA.toInt()
        canvas.drawCircle(dotX - dotRadius * 0.3f, dotY - dotRadius * 0.3f, dotRadius * 0.3f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isRunning) return false
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val dx = event.x - dotX
            val dy = event.y - dotY
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < hitRadius) {
                onHit?.invoke()
                // Scatter the dot on hit
                val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
                val currentSpeed = sqrt(velX * velX + velY * velY).coerceAtLeast(5f)
                velX = cos(angle) * (currentSpeed + 2f)
                velY = sin(angle) * (currentSpeed + 2f)
            }
        }
        return true
    }
}