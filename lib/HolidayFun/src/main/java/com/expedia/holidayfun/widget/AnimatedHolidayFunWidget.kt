package com.expedia.holidayfun.widget

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.expedia.holidayfun.R

class AnimatedHolidayFunWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val bear: ImageView by lazy { findViewById<ImageView>(R.id.bear) }
    private val deer: ImageView by lazy { findViewById<ImageView>(R.id.deer) }
    private val deerDownY: Float by lazy { resources.getDimension(R.dimen.deer_down) }
    private val deerUpY: Float by lazy { resources.getDimension(R.dimen.deer_up) }
    private val bearDownY: Float by lazy { resources.getDimension(R.dimen.bear_down) }
    private val bearUpY: Float by lazy { resources.getDimension(R.dimen.bear_up) }

    init {
        View.inflate(context, R.layout.widget_animated_holiday_fun, this)

        bear.translationY = bearDownY
        deer.translationY = deerDownY
    }

    fun popDown(completionHandler: () -> Unit) {
        deer.stopDrawableAnimation()
        bear.stopDrawableAnimation()
        deer.animate().translationY(deerDownY).withEndAction {
            completionHandler()
        }
        bear.animate().translationY(bearDownY)
    }

    fun popUp() {
        deer.animate().translationY(deerUpY).withEndAction {
            deer.startDrawableAnimation()
        }
        bear.animate().translationY(bearUpY).setStartDelay(150).withEndAction {
            bear.startDrawableAnimation()
        }
    }

    private fun ImageView.startDrawableAnimation() {
        (drawable as AnimatedVectorDrawable).start()
    }

    private fun ImageView.stopDrawableAnimation() {
        (drawable as AnimatedVectorDrawable).stop()
    }
}