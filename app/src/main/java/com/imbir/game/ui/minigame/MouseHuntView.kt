package com.imbir.game.ui.minigame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt
import kotlin.random.Random

class MouseHuntView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : View(ctx, attrs) {

    var onCatch: (() -> Unit)? = null
    var spawnRate: Int = 600  // ms between spawns

    private data class Mouse(
        val x: Float,
        val y: Float,
        var alpha: Float = 1f,
        var timeAlive: Long = 0L,
        val lifespan: Long = Random.nextLong(1200, 2200),
        var caught: Boolean = false,
        var fadingOut: Boolean = false
    )

    private val mice = mutableListOf<Mouse>()
    private var isRunning = false
    private var lastSpawn = 0L
    private val mouseRadius = 40f
    private val hitRadius   = 70f

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFAAAAAA.toInt() }
    private val eyePaint  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
    private val earPaint  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF888888.toInt() }
    private val caughtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFD700.toInt()
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }
    private val tailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF888888.toInt()
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val frameRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            val now = System.currentTimeMillis()

            if (now - lastSpawn > spawnRate && mice.size < 5) {
                spawnMouse()
                lastSpawn = now
            }

            val iter = mice.iterator()
            while (iter.hasNext()) {
                val m = iter.next()
                m.timeAlive += 16
                if (m.caught) {
                    m.alpha = (m.alpha - 0.08f).coerceAtLeast(0f)
                    if (m.alpha <= 0f) iter.remove()
                } else if (m.timeAlive >= m.lifespan) {
                    m.fadingOut = true
                    m.alpha = (m.alpha - 0.05f).coerceAtLeast(0f)
                    if (m.alpha <= 0f) iter.remove()
                }
            }

            invalidate()
            postDelayed(this, 16L)
        }
    }

    fun startGame() {
        if (width == 0 || height == 0) { post { startGame() }; return }
        mice.clear()
        isRunning = true
        lastSpawn = System.currentTimeMillis()
        post(frameRunnable)
    }

    fun stopGame() {
        isRunning = false
        removeCallbacks(frameRunnable)
        mice.clear()
        invalidate()
    }

    private fun spawnMouse() {
        val margin = mouseRadius * 2
        val x = Random.nextFloat() * (width - margin * 2) + margin
        val y = Random.nextFloat() * (height - margin * 2) + margin
        mice.add(Mouse(x, y))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(0xFF2D5016.toInt())

        if (!isRunning && mice.isEmpty()) {
            bodyPaint.color = Color.WHITE
            bodyPaint.textSize = 48f
            bodyPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Tap Start to hunt! 🐭", width / 2f, height / 2f, bodyPaint)
            bodyPaint.color = 0xFFAAAAAA.toInt()
            bodyPaint.textSize = 48f
            return
        }

        for (mouse in mice) {
            if (mouse.caught) {
                val a = (mouse.alpha * 255).toInt()
                caughtPaint.alpha = a
                canvas.drawText("⭐", mouse.x, mouse.y, caughtPaint)
            } else {
                bodyPaint.alpha = (mouse.alpha * 255).toInt()
                earPaint.alpha  = (mouse.alpha * 255).toInt()
                eyePaint.alpha  = (mouse.alpha * 255).toInt()
                tailPaint.alpha = (mouse.alpha * 255).toInt()
                drawMouse(canvas, mouse)
            }
        }
    }

    private fun drawMouse(canvas: Canvas, mouse: Mouse) {
        val r = mouseRadius * mouse.alpha.coerceIn(0.3f, 1f)

        // Ears
        earPaint.color = 0xFF888888.toInt()
        canvas.drawCircle(mouse.x - r * 0.6f, mouse.y - r * 0.8f, r * 0.4f, earPaint)
        canvas.drawCircle(mouse.x + r * 0.6f, mouse.y - r * 0.8f, r * 0.4f, earPaint)

        // Body
        bodyPaint.color = 0xFFAAAAAA.toInt()
        canvas.drawOval(RectF(mouse.x - r, mouse.y - r * 0.7f, mouse.x + r, mouse.y + r * 0.7f), bodyPaint)
        canvas.drawCircle(mouse.x, mouse.y - r * 0.3f, r * 0.7f, bodyPaint)

        // Eyes
        canvas.drawCircle(mouse.x - r * 0.3f, mouse.y - r * 0.2f, r * 0.12f, eyePaint)
        canvas.drawCircle(mouse.x + r * 0.3f, mouse.y - r * 0.2f, r * 0.12f, eyePaint)

        // Tail
        canvas.drawLine(mouse.x + r, mouse.y, mouse.x + r * 2f, mouse.y + r * 0.5f, tailPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isRunning) return false
        if (event.action != MotionEvent.ACTION_DOWN) return true

        val iter = mice.iterator()
        while (iter.hasNext()) {
            val m = iter.next()
            if (m.caught || m.fadingOut) continue
            val dx = event.x - m.x
            val dy = event.y - m.y
            if (sqrt(dx * dx + dy * dy) < hitRadius) {
                m.caught = true
                onCatch?.invoke()
                break
            }
        }
        return true
    }
}