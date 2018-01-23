package com.expedia.account.presenter

import android.content.Context
import android.graphics.Point
import android.view.View
import android.view.WindowManager
import com.expedia.account.util.PresenterUtils

class SlideUpTransition(val targetView: View) : Presenter.Transition() {

    private var initialTosTranslationY: Float = 0.toFloat()

    private var tosStartTranslationY: Float = 0.toFloat()
    private var tosEndTranslationY: Float = 0.toFloat()

    override fun startTransition(forward: Boolean) {
        targetView.visibility = View.VISIBLE
        val size = Point()
        val wm = targetView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getSize(size)
        val screenHeight = size.y
        if (forward) {
            initialTosTranslationY = targetView.translationY
        }
        tosStartTranslationY = if (forward) initialTosTranslationY else -1 * screenHeight * 0.1f
        tosEndTranslationY = if (forward) -1 * screenHeight * 0.1f else initialTosTranslationY
    }

    override fun updateTransition(f: Float, forward: Boolean) {
        targetView.translationY = PresenterUtils.calculateStep(tosStartTranslationY, tosEndTranslationY, f)
    }

    override fun finalizeTransition(forward: Boolean) {
        if (forward) {
            targetView.translationY = 0f
        }
    }
}
