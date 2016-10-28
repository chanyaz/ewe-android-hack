package com.expedia.bookings.animation

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.animation.AccelerateDecelerateInterpolator

class HotelFavoriteBurstAnimation(val color: Int,val radius: Int,val duration: Long) : Drawable() {

    private var scale = 0f
    private var alphaTransparency = 255
    private val burstPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val animatorSet = AnimatorSet()
    private var animator: Animator? = null

    override fun draw(canvas: Canvas) {
        burstPaint.style = Paint.Style.FILL
        burstPaint.color = color
        burstPaint.alpha = alphaTransparency
        canvas.drawCircle(bounds.centerX().toFloat(), bounds.centerY().toFloat(),
                radius.toFloat() * scale, burstPaint)
    }

    fun startAnimation() {
        animator = generateAnimation()
        animator?.start()
    }

    fun isAnimationRunning(): Boolean {
        return animator?.isRunning ?: false
    }

    private fun generateAnimation() : Animator {
        val burstAnimator = ObjectAnimator.ofFloat(this, "scale", 0f, 1f)
        burstAnimator.duration = duration
        burstAnimator.interpolator = AccelerateDecelerateInterpolator()
        burstAnimator.repeatCount = 0

        val alphaAnimator = ObjectAnimator.ofInt(this, "alpha", 255, 0)
        alphaAnimator.duration = duration
        alphaAnimator.interpolator = AccelerateDecelerateInterpolator()
        alphaAnimator.repeatCount = 0

        animatorSet.playTogether(burstAnimator, alphaAnimator)
        return animatorSet
    }

    override fun getOpacity(): Int {
        return burstPaint.alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        burstPaint.colorFilter = cf
    }

    override fun setAlpha(alpha: Int) {
        this.alphaTransparency = alpha
        invalidateSelf()
        burstPaint.alpha = alpha
    }

}
