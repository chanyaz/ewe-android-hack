package com.expedia.bookings.presenter

import android.view.View
import kotlin.properties.Delegates

open class ScaleTransition(val presenter: Presenter, val left: Class<*>, val right: Class<*>): Presenter.Transition(left, right) {

    val xScale = 0.75f
    val yScale = 0.75f
    var rightView: View by Delegates.notNull()
    var leftView: View by Delegates.notNull()

    override fun startTransition(forward: Boolean) {
        rightView = getRightChildView()
        leftView = getLeftChildView()

        leftView.alpha = 1f
        leftView.visibility = View.VISIBLE

        rightView.alpha = (if (forward) 0f else 1f)
        rightView.visibility = View.VISIBLE
        rightView.scaleX = (if (forward) xScale else 1f)
        rightView.scaleY = (if (forward) yScale else 1f)
    }

    override fun updateTransition(f: Float, forward: Boolean) {
        rightView.alpha = (if (forward) f else (1-f))
        rightView.scaleX = (if (forward) (1 - (1-xScale) * -(f-1)) else (xScale + (1-xScale) * -(f-1)))
        rightView.scaleY = (if (forward) (1 - (1-yScale) * -(f-1)) else (yScale + (1-yScale) * -(f-1)))
    }

    override fun endTransition(forward: Boolean) {
        // do nothing
    }

    override fun finalizeTransition(forward: Boolean) {
        leftView.visibility = (if (forward) View.GONE else View.VISIBLE)

        rightView.alpha = 1f
        rightView.visibility = (if (forward) View.VISIBLE else View.GONE)
        rightView.scaleX = 1f
        rightView.scaleY = 1f
    }

    private fun getRightChildView(): View {
        return getChildInPresenter(right)
    }

    private fun getLeftChildView(): View {
        return getChildInPresenter(left)
    }

    private fun getChildInPresenter(childClass: Class<*>): View {
        var child: View? = null
        for (i in 0..presenter.childCount - 1) {
            val c = presenter.getChildAt(i)
            if (c.javaClass.equals(childClass)) {
                if (child != null) {
                    throw RuntimeException("Found duplicate child view in this presenter (class: " + childClass.getName() + ")")
                }
                child = c
            }
        }

        if (child != null) {
            return child
        } else {
            throw RuntimeException("Could not find child with class:" + childClass.getName() + " in this presenter")
        }
    }
}
