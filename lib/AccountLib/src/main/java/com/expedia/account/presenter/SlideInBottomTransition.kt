package com.expedia.account.presenter

import android.view.View

class SlideInBottomTransition(targetView: View) : OffScreenBottomTransition(targetView) {

    override fun startTransition(forward: Boolean) {
        super.startTransition(!forward)
    }

    override fun updateTransition(f: Float, forward: Boolean) {
        super.updateTransition(f, !forward)
    }

    override fun finalizeTransition(forward: Boolean) {
        super.finalizeTransition(!forward)
    }
}
