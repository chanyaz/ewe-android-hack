package com.expedia.bookings.widget

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.packages.CheckoutOverviewHeader

class CheckoutHeaderBehavior(val context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<CheckoutOverviewHeader>(context, attrs) {
    var toolBarRightX = 0
    var textViewLeftX = 0
    var textViewWidth = 0
    val toolbarHeight = Ui.getStatusBarHeight(context)

    override fun layoutDependsOn(parent: CoordinatorLayout, child: CheckoutOverviewHeader, dependency: View): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: CheckoutOverviewHeader, dependency: View): Boolean {
        val appBarLayout = (dependency as AppBarLayout)
        val maxScroll = appBarLayout.totalScrollRange
        if (maxScroll != 0) {
            val percentage = Math.abs(dependency.getY()) / maxScroll

            child.checkInOutDates.alpha = 1 - (percentage * 2)
            child.travelers.alpha = 1 - (percentage * 2)
            if (percentage == 0f || textViewLeftX == 0 || textViewWidth == 0 || toolBarRightX == 0) {
                setupValues(child, dependency)
            }

            if (textViewLeftX != 0 && textViewWidth != 0 && toolBarRightX != 0) {
                translateText(percentage, child, dependency)
            }

            child.visibility = if (percentage == 1f || !appBarLayout.isActivated) View.GONE else View.VISIBLE
        }
        return true
    }

    fun translateText(percentage: Float, child: CheckoutOverviewHeader, dependency: View) {
        val childHeight = dependency.height + dependency.y - toolbarHeight
        val lp = child.layoutParams as CoordinatorLayout.LayoutParams
        lp.height = childHeight.toInt()
        child.layoutParams = lp

        child.destinationText.x = Math.max(toolBarRightX.toFloat(), textViewLeftX * (1 - percentage))
        val scale = .75f + (1 - percentage) * (1f - .75f)
        child.destinationText.pivotX = 0f
        child.destinationText.pivotY = 0f
        child.destinationText.scaleX = scale
        child.destinationText.scaleY = scale
    }

    private fun setupValues(child: CheckoutOverviewHeader, dependency: View) {
        val toolbar = dependency.findViewById(R.id.checkout_toolbar) as CheckoutToolbar
        textViewWidth = child.destinationText.width
        val fullWidth = child.width
        child.destinationText.x = (fullWidth / 2f) - (textViewWidth/2f)
        textViewLeftX = getLocation(child.destinationText)[0]
        toolBarRightX = getLocation(toolbar.getChildAt(0))[0] + toolbar.getChildAt(0).width
        child.destinationText.maxWidth = child.width - (toolBarRightX * 2)
        AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar)
    }

    private fun getLocation(view: View): IntArray {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return location
    }
}