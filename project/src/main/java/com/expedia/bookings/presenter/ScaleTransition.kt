package com.expedia.bookings.presenter

import android.support.annotation.CallSuper
import android.view.View

open class ScaleTransition(open val presenter: Presenter, val left: Class<*>, val right: Class<*>) : Presenter.Transition(left, right) {

    constructor(presenter: Presenter, left: Class<*>, right: Class<*>, leftClassName: Class<*>) : this(presenter, left, right) {
        this.leftClassName = leftClassName
    }

    constructor(presenter: Presenter, left: View, right: View) : this(presenter, left.javaClass, right.javaClass) {
        this.leftView = left
        this.rightView = right
    }

    constructor(presenter: Presenter, left: View, right: View, leftClass: Class<*>, rightClass: Class<*>) : this(presenter, leftClass, rightClass) {
        this.leftView = left
        this.rightView = right
    }

    val xScale = 0.75f
    val yScale = 0.75f
    var rightView: View? = null
    var leftView: View? = null
    var leftClassName: Class<*>? = null

    @CallSuper override fun startTransition(forward: Boolean) {
        if (rightView == null) {
            rightView = getRightChildView()
        }
        if (leftView == null) {
            leftView = getLeftChildView()
        }

        leftView?.alpha = 1f
        leftView?.visibility = View.VISIBLE

        rightView?.alpha = (if (forward) 0f else 1f)
        rightView?.visibility = View.VISIBLE
        rightView?.scaleX = (if (forward) xScale else 1f)
        rightView?.scaleY = (if (forward) yScale else 1f)
    }

    @CallSuper override fun updateTransition(f: Float, forward: Boolean) {
        rightView?.alpha = (if (forward) f else (1 - f))
        rightView?.scaleX = (if (forward) (1 - (1 - xScale) * -(f-1)) else (xScale + (1 - xScale) * -(f-1)))
        rightView?.scaleY = (if (forward) (1 - (1 - yScale) * -(f-1)) else (yScale + (1 - yScale) * -(f-1)))
    }

    @CallSuper override fun endTransition(forward: Boolean) {
        leftView?.visibility = (if (forward) View.GONE else View.VISIBLE)

        rightView?.alpha = 1f
        rightView?.visibility = (if (forward) View.VISIBLE else View.GONE)
        rightView?.scaleX = 1f
        rightView?.scaleY = 1f
    }

    private fun getRightChildView(): View {
        return getChildInPresenter(right)
    }

    private fun getLeftChildView(): View {
        return getChildInPresenter(leftClassName ?: left)
    }

    private fun getChildInPresenter(childClass: Class<*>): View {
        var child: View? = null
        for (i in 0..presenter.childCount - 1) {
            val c = presenter.getChildAt(i)
            if (c.javaClass.equals(childClass)) {
                if (child != null) {
                    throw RuntimeException("Found duplicate child view in this presenter (class: " + childClass.name + ")")
                }
                child = c
            }
        }

        if (child != null) {
            return child
        } else {
            throw RuntimeException("Could not find child with class:" + childClass.name + " in this presenter")
        }
    }
}
