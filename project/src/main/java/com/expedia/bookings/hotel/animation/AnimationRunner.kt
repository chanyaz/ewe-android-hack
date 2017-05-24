package com.expedia.bookings.hotel.animation

import android.content.Context
import android.support.annotation.AnimRes
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.expedia.bookings.animation.AnimationListenerAdapter
import java.lang.ref.WeakReference

class AnimationRunner(view: View, private val context: Context) {
    private var animationIn: Animation? = null
    private var animationOut: Animation? = null

    private var beforeAction: () -> Unit = {}
    private var afterAction: () -> Unit = {}

    private var duration = 500L
    private var outDelay = 1000L

    private val viewRef: WeakReference<View>

    init {
        viewRef = WeakReference(view)
    }

    fun animIn(@AnimRes anim: Int) : AnimationRunner {
        animationIn = AnimationUtils.loadAnimation(context, anim)
        return this
    }

    fun animOut(@AnimRes anim: Int) : AnimationRunner {
        animationOut = AnimationUtils.loadAnimation(context, anim)
        return this
    }

    fun duration(duration: Long) : AnimationRunner {
        this.duration = duration
        return this
    }

    fun outDelay(delay: Long) : AnimationRunner {
        outDelay = delay
        return this
    }

    fun beforeAction(beforeAction: () -> Unit) : AnimationRunner {
        this.beforeAction = beforeAction
        return this
    }

    fun afterAction(afterAction: () -> Unit) : AnimationRunner {
        this.afterAction = afterAction
        return this
    }

    fun run() {
        build()
        val view = viewRef.get()
        view?.let { view -> view.startAnimation(animationIn) }
    }

    private fun build() {
        animationOut?.let { animation ->
            animation.duration = duration
            animation.startOffset = outDelay
            animation.setAnimationListener(object: AnimationListenerAdapter() {
                override fun onAnimationEnd(animation: Animation?) {
                    afterAction()
                }
            })
        }

        animationIn?.let { animation ->
            animation.duration = duration
            animation.setAnimationListener(object: AnimationListenerAdapter() {
                override fun onAnimationStart(animation: Animation?) {
                    beforeAction()
                }
                override fun onAnimationEnd(animation: Animation?) {
                    val view = viewRef.get()
                    view?.let { view -> view.startAnimation(animationOut) }
                }
            })
        }
    }
}