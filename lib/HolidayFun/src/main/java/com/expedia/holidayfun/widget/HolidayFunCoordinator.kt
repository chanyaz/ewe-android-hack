package com.expedia.holidayfun.widget

import android.view.View
import com.expedia.holidayfun.R
import com.jetradarmobile.snowfall.SnowfallView

class HolidayFunCoordinator(private val animatedWidget: AnimatedHolidayFunWidget, private val snowfall: SnowfallView) {

    private var fireAnalytics: (() -> Unit)? = null

    private val animatedWidgetClickListener = View.OnClickListener {
        fireAnalytics?.invoke()
        startWinter()
    }

    private val snowfallTouchListener = View.OnTouchListener { view, motionEvent ->
        stopWinter()
        false
    }

    init {
        animatedWidget.setOnClickListener(animatedWidgetClickListener)
    }

    var visibility: Int
        get() = animatedWidget.visibility
        set(value) {
            animatedWidget.visibility = value
            if (value == View.GONE) {
                snowfall.visibility = value
            }
        }

    fun setAnalyticsListener(analyticsListener: () -> Unit) {
        fireAnalytics = analyticsListener
    }

    fun hideCallToAction() {
        val actualHeight = animatedWidget.height.toFloat()
        val estimatedHeight = animatedWidget.resources.getDimension(R.dimen.animated_widget_expected_height)
        animatedWidget.animate().translationY(if (actualHeight > 0) actualHeight else estimatedHeight)
    }

    fun showCallToAction() {
        animatedWidget.animate().translationY(0f)
    }

    private fun stopWinter() {
        snowfall.stopFalling()
        snowfall.setOnTouchListener(null)
        animatedWidget.popDown { animatedWidget.setOnClickListener(animatedWidgetClickListener) }
    }

    private fun startWinter() {
        snowfall.visibility = View.VISIBLE
        snowfall.restartFalling()
        snowfall.setOnTouchListener(snowfallTouchListener)
        animatedWidget.setOnClickListener(null)
        animatedWidget.popUp()
    }


}