package com.expedia.holidayfun.widget

import android.view.View
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