package com.expedia.bookings.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller

class ScrollOverlayController(var scrollTouchZone: View,
                              var overlayZone: View,
                              var recyclerView: RecyclerView,
                              var darkness: View,
                              var listener: OverlayListener) {

    interface OverlayListener {
        fun onOverlayScrollVisibilityChanged(overlayVisible: Boolean)

        fun onOverlayStateChanged(overlayVisible: Boolean)
    }

    internal val detector: GestureDetector = GestureDetector(recyclerView.context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            scroller.forceFinished(true)
            matchScroll()
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // Ensure that we're responding only to y scrolling and ignoring x scrolling
            // The gesture detector is ensuring that we ignore things below touch slop, so we just want
            // to ensure we're moving at all here.
            if (distanceY != 0f) {
                scroller.forceFinished(true)
                scrollBy(distanceY)
                return true
            } else {
                return false
            }
        }

        override fun onLongPress(e: MotionEvent) {
            scroller.forceFinished(true)
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var adjustedVelocityY = velocityY
            if (Math.abs(adjustedVelocityY) > recyclerView.maxFlingVelocity) {
                if (adjustedVelocityY < 0) {
                    adjustedVelocityY = (-recyclerView.maxFlingVelocity).toFloat()
                } else {
                    adjustedVelocityY = recyclerView.maxFlingVelocity.toFloat()
                }
            }
            if (Math.abs(adjustedVelocityY) > recyclerView.minFlingVelocity) {
                scroller.fling(0, Math.round(currentScroll),
                        0, Math.round(adjustedVelocityY),
                        0, 0,
                        // If we end up with an extremely long list and a bug happens where we can't scroll
                        // through the entire thing, this is the spot where you'll need to be creative with the scroller
                        // and it's minimum value.
                        Integer.MIN_VALUE, 0)
                matchScroll()
                return true
            } else {
                return false
            }
        }
    })

    val scroller: Scroller = Scroller(recyclerView.context)
    val overlayHeight: Float = overlayZone.height.toFloat()
    var currentScroll: Float = 0f
    var inOverlay = false

    init {
        recyclerView.translationY = overlayHeight

        scrollTouchZone.setOnTouchListener { v, event ->
            if (inOverlay && event.y > overlayZone.getHeight()) {
                toggleOverlay()
                true
            } else {
                detector.onTouchEvent(event)
            }
        }
    }

    fun scrollTo(locationY: Float) {
        val scrollDistance = currentScroll - locationY
        val overscroll: Float
        if (lobIsVisible()) {
            overscroll = scrollLobTo(locationY)
            if (overscroll > 0) {
                // If we want to do anything with edge boundary overscroll for the top of the list, here is the place
                // Since we don't for now, just kill any scrolling that's happening, we've reached the end
                scroller.forceFinished(true)
                currentScroll = 0f
            } else {
                if (overscroll < 0) {
                    scrollListBy(-overscroll)
                }
                currentScroll = locationY
            }
            // We crossed the lob threshhold, so we've switched states

            if (locationY < -overlayHeight) {
                listener.onOverlayScrollVisibilityChanged(false)
            }
        } else {
            overscroll = scrollListBy(scrollDistance)
            if (overscroll > 0) {
                //TODO: Known concern - this code is never called because of the way we generate overscroll from the
                // scrolllistby, apparently. Doesn't affect the current expected use case, so we're willing to forgo
                // fixing it unless we really need to know this at some point.
                // The lastScroll is recalculated on a regular scroll, which undoes any overscrolling that may occur.
                currentScroll = locationY + overscroll
                // If we want to do anything with edge boundary overscroll for the bottom of the list, here is the place
                // Since we don't for now, just kill any scrolling that's happening, we've reached the end
                scroller.forceFinished(true)
            } else {
                if (overscroll < 0) {
                    scrollLobTo(locationY - overscroll)
                    currentScroll = locationY
                } else {
                    currentScroll = -(recyclerView.computeVerticalScrollOffset() + overlayHeight)
                }
            }

            // We crossed the lob threshhold, so we've switched states
            if (locationY > -overlayHeight) {
                listener.onOverlayScrollVisibilityChanged(true)
            }
        }
    }

    fun scrollBy(scrollDistance: Float) {
        scrollTo(currentScroll - scrollDistance)
    }

    fun scrollToTop() {
        if (!inOverlay) {
            scroller.forceFinished(true)
            scroller.startScroll(0, Math.round(currentScroll), 0, Math.round((-currentScroll)), 300)
            matchScroll()
        }
    }

    fun toggleOverlay(showOverlay: Boolean = !inOverlay) {
        if (showOverlay) {
            // If we're going to show the overlay, just scroll to the top if it is partially visible
            if (lobIsVisible()) {
                scrollToTop()
            } else if (!inOverlay) {
                // As long as we aren't already showing the overlay we want to show it
                overlay()
            }
        } else if (inOverlay) {
            // Only try to remove the overlay if we're in the overlay
            unoverlay()
        }
    }

    /**
     * @param newTransY The total new amount they should be moved to
     * *
     * @return The remainder that could not be moved (overscroll, basically)
     */
    private fun scrollLobTo(newTransY: Float): Float {
        // We haven't moved past the final upward point,
        val remainder: Float
        val finalTransY: Float
        if (newTransY >= 0) {
            remainder = newTransY
            finalTransY = 0f
        } else if (newTransY > -overlayZone.height) {
            finalTransY = newTransY
            remainder = 0f
        } else {
            remainder = newTransY - -overlayZone.height
            finalTransY = (-overlayZone.height).toFloat()
        }
        overlayZone.translationY = finalTransY
        recyclerView.translationY = finalTransY + overlayHeight
        return remainder
    }

    private fun scrollListBy(scrollAmount: Float): Float {
        val scrollAvailable: Float
        if (scrollAmount > 0) {
            scrollAvailable = recyclerView.computeVerticalScrollRange().toFloat()
        } else {
            scrollAvailable = recyclerView.computeVerticalScrollOffset().toFloat()
        }
        recyclerView.scrollBy(0, Math.round(scrollAmount))
        return Math.min(0f, scrollAvailable - Math.abs(scrollAmount)) * if (scrollAmount < 0) 1 else -1
    }

    private fun matchScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currY.toFloat())
            recyclerView.post { matchScroll() }
        }
    }

    private fun lobIsVisible(): Boolean {
        return currentScroll > -overlayHeight
    }

    //states

    private fun overlay() {
        inOverlay = true
        scroller.forceFinished(true);
        val animator = ObjectAnimator.ofFloat(overlayZone, "translationY", overlayZone.translationY, 0f)
        val dark = ObjectAnimator.ofFloat(darkness, "alpha", 0f, .7f)
        val set = AnimatorSet()
        set.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                darkness.visibility = View.VISIBLE
            }
        })
        set.duration = 400
        set.playTogether(animator, dark)
        set.start()
        listener.onOverlayStateChanged(true)
    }

    private fun unoverlay() {
        scroller.forceFinished(true);
        val animator = ObjectAnimator.ofFloat(overlayZone, "translationY", 0f, -overlayZone.height.toFloat())
        val dark = ObjectAnimator.ofFloat(darkness, "alpha", .7f, 0f)
        val set = AnimatorSet()
        set.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                darkness.visibility = View.INVISIBLE
                inOverlay = false
            }
        })
        set.duration = 400
        set.playTogether(animator, dark)
        set.start()
        listener.onOverlayStateChanged(false)
    }
}
