package com.expedia.bookings.packages.widget

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.packages.presenter.PackageFlightPresenter
import com.expedia.bookings.packages.presenter.PackageHotelPresenter
import com.expedia.bookings.tracking.PackagesTracking

class SlidingBundleWidgetListener (val widget: SlidingBundleWidget, val presenter: Presenter) {

    val SWIPE_MIN_DISTANCE = 10
    val SWIPE_THRESHOLD_VELOCITY = 300
    val FAST_ANIMATION_DURATION = 150
    val REGULAR_ANIMATION_DURATION = 400

    val onTouchListener = View.OnTouchListener { _, event ->
        if (widget.isMoving) {
            return@OnTouchListener true
        }
        if (!gestureDetector.onTouchEvent(event)) {
            when (event.action) {
                (MotionEvent.ACTION_DOWN) -> {
                    widget.canMove = true
                }
                (MotionEvent.ACTION_UP) -> {
                    widget.canMove = false
                    val distance = Math.abs(widget.translationY)
                    val distanceMax = presenter.height.toFloat() - widget.bundlePriceWidget.height
                    val upperThreshold = distanceMax / 3
                    val lowerThreshold = (distanceMax / 3) * 2
                    if (distance > Math.abs(lowerThreshold) && !isShowingBundle()) {
                        // currentState !=  BundleWidget, from BOTTOM to TOP but distance moved less than threshold hence close widget.
                        widget.closeBundleOverview()
                    } else if (distance <= Math.abs(lowerThreshold) && !isShowingBundle()) {
                        // currentState !=  BundleWidget, from BOTTOM to TOP and distance moved greater than threshold hence show widget again.
                        presenter.show(widget)
                        PackagesTracking().trackBundleWidgetTap()
                    } else if (distance <= Math.abs(upperThreshold)) {
                        // currentState ==  BundleWidget, from TOP to BOTTOM but distance moved less than threshold hence show widget again.
                        widget.openBundleOverview()
                    } else if (distance > Math.abs(upperThreshold)) {
                        // currentState ==  BundleWidget, from TOP to BOTTOM and distance moved greater than threshold hence close widget i.e. hitBack()
                        presenter.back()
                    }
                }
            }
        }
        true
    }

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (e1 == null || e2 == null) {
                return false
            }
            if (widget.isMoving || !widget.canMove) {
                return true
            }
            if (e1.y.minus(e2.y) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                updateOverviewAnimationDuration(FAST_ANIMATION_DURATION)
                if (!isShowingBundle()) {
                    PackagesTracking().trackBundleWidgetTap()
                    presenter.show(widget)
                } else {
                    presenter.back()
                }
                return true
            } else if (e2.y.minus(e1.y) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                if (isShowingBundle()) {
                    presenter.back()
                } else {
                    widget.closeBundleOverview()
                }
                return true
            }
            return false
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return super.onDown(e)
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (widget.isMoving || !widget.canMove) {
                return true
            }
            updateOverviewAnimationDuration(REGULAR_ANIMATION_DURATION)
            widget.translateBundleOverview(e2.rawY)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            updateOverviewAnimationDuration(REGULAR_ANIMATION_DURATION)
            if (isShowingBundle()) {
                presenter.back()
            } else {
                PackagesTracking().trackBundleWidgetTap()
                presenter.show(widget)
            }
            return true
        }
    }

    private val gestureDetector: GestureDetector = GestureDetector(presenter.context, gestureListener)

    private fun isShowingBundle(): Boolean {
        when (presenter) {
            is PackageFlightPresenter -> return presenter.isShowingBundle()
            is PackageHotelPresenter -> return presenter.isShowingBundle()
            else -> return false
        }
    }

    private fun updateOverviewAnimationDuration(duration: Int) {
        when (presenter) {
            is PackageHotelPresenter -> presenter.updateOverviewAnimationDuration(duration)
            is PackageFlightPresenter -> presenter.updateOverviewAnimationDuration(duration)
        }
    }
}
