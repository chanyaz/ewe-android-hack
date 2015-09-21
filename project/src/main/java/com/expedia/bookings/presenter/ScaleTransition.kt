package com.expedia.bookings.presenter

import android.view.View
import kotlin.properties.Delegates

public class ScaleTransition(val presenter: Presenter, val left: Class<*>, val right: Class<*>): Presenter.Transition(left, right) {

    val xScale = 0.75f
    val yScale = 0.75f
    var rightView: View by Delegates.notNull()
    var leftView: View by Delegates.notNull()

    override fun startTransition(forward: Boolean) {
        rightView = getRightChildView()
        leftView = getLeftChildView()

        leftView.setAlpha(if (forward) 1f else 0f)
        leftView.setVisibility(View.VISIBLE)
        leftView.setScaleX(if (forward) 1f else xScale)
        leftView.setScaleY(if (forward) 1f else yScale)

        rightView.setAlpha(if (forward) 0f else 1f)
        rightView.setVisibility(View.VISIBLE)
        rightView.setScaleX(if (forward) xScale else 1f)
        rightView.setScaleY(if (forward) yScale else 1f)
    }

    override fun updateTransition(f: Float, forward: Boolean) {
        leftView.setAlpha(if (forward) 1f else f)
        leftView.setScaleX(if (forward) 1f else (1 - (1-xScale) * -(f-1)))
        leftView.setScaleY(if (forward) 1f else (1 - (1-yScale) * -(f-1)))

        rightView.setAlpha(if (forward) f else 1f)
        rightView.setScaleX(if (forward) (1 - (1-xScale) * -(f-1)) else 1f)
        rightView.setScaleY(if (forward) (1 - (1-yScale) * -(f-1)) else 1f)
    }

    override fun endTransition(forward: Boolean) {
        // do nothing
    }

    override fun finalizeTransition(forward: Boolean) {
        leftView.setAlpha(if (forward) 0f else 1f)
        leftView.setVisibility(if (forward) View.INVISIBLE else View.VISIBLE)
        leftView.setScaleX(1f)
        leftView.setScaleY(1f)

        rightView.setAlpha(if (forward) 1f else 0f)
        rightView.setVisibility(if (forward) View.VISIBLE else View.INVISIBLE)
        rightView.setScaleX(1f)
        rightView.setScaleY(1f)
    }

    private fun getRightChildView(): View {
        return getChildInPresenter(right)
    }

    private fun getLeftChildView(): View {
        return getChildInPresenter(left)
    }

    private fun getChildInPresenter(childClass: Class<*>): View {
        var child: View? = null
        for (i in 0..presenter.getChildCount() - 1) {
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
