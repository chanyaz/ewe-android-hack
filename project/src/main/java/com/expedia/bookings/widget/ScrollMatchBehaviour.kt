package com.expedia.bookings.widget

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.utils.Ui


class ScrollMatchBehaviour(val context: Context, attrs: AttributeSet?) : CoordinatorLayout.Behavior<View>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is AppBarLayout

    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View, dependency: View): Boolean {
        val maxScroll = (dependency as AppBarLayout).totalScrollRange;
        if (maxScroll != 0) {
            child.translationY = dependency.getY() - Ui.getToolbarSize(context)
        }
        return true
    }
}

