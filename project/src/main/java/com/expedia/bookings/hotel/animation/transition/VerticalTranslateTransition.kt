package com.expedia.bookings.hotel.animation.transition

import android.view.View
import rx.subjects.PublishSubject

class VerticalTranslateTransition(view: View, origin: Int, target: Int) : TranslateTransition(view, origin, target) {
    val reachedTargetSubject = PublishSubject.create<Unit>()

    override fun toOrigin(progress: Float) {
        view.translationY = target + (progress * (origin - target))
    }

    override fun toTarget(progress: Float) {
        view.translationY = origin + (progress * (target - origin))
    }

    fun jumpToOrigin() {
        view.translationY = origin.toFloat()
    }

    fun jumpToTarget() {
        view.translationY = target.toFloat()
        reachedTargetSubject.onNext(Unit)
    }
}