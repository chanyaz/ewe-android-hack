package com.expedia.bookings.onboarding

import android.view.GestureDetector
import android.view.MotionEvent
import com.expedia.bookings.utils.Constants
import io.reactivex.subjects.PublishSubject


class LeftRightFlingListener : GestureDetector.SimpleOnGestureListener() {

    val leftFlingSubject = PublishSubject.create<Unit>()
    val rightFlingSubject = PublishSubject.create<Unit>()

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (e1.x - e2.x > Constants.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > Constants.SWIPE_THRESHOLD_VELOCITY) {
            rightFlingSubject.onNext(Unit)
            return true
        } else if (e2.x - e1.x > Constants.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > Constants.SWIPE_THRESHOLD_VELOCITY) {
            leftFlingSubject.onNext(Unit)
            return true
        }
        return false
    }
    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }
    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return true
    }
}