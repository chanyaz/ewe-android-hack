package com.expedia.bookings.hotel.animation

import android.view.View
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference

abstract class ScaleRunnable(view: View, private val duration: Long, private val startDelay: Long) : Runnable {
    abstract fun startScale() : Float
    abstract fun endScale() : Float
    abstract fun getYPivot() : Float

    val startSubject = PublishSubject.create<Unit>()
    val endSubject = PublishSubject.create<Unit>()

    protected  val viewRef: WeakReference<View>

    init {
        viewRef = WeakReference(view)
    }

    override fun run() {
        val view = viewRef.get()
        if (view != null) {
            view.pivotY = getYPivot()
            view.scaleY = startScale()
            view.visibility = View.VISIBLE

            view.animate().scaleY(endScale()).setDuration(duration).setStartDelay(startDelay)
                    .withStartAction {
                        startSubject.onNext(Unit)
                    }
                    .withEndAction {
                        endSubject.onNext(Unit)
                    }.start()
        }
    }
}