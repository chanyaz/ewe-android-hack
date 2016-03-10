package com.expedia.bookings.widget

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.packages.CheckoutOverviewHeader

class CheckoutHeaderBehavior(val context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<CheckoutOverviewHeader>(context, attrs) {
    var toolBarRightX = 0
    var textViewLeftX = 0
    var textViewWidth = 0

    override fun layoutDependsOn(parent: CoordinatorLayout, child: CheckoutOverviewHeader, dependency: View): Boolean {
        return dependency is AppBarLayout;
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: CheckoutOverviewHeader, dependency: View): Boolean {
        val maxScroll = (dependency as AppBarLayout).totalScrollRange;
        if (maxScroll != 0) {
            val percentage = Math.abs(dependency.getY()) / maxScroll;
            var childHeight = dependency.height + dependency.getY() - Ui.getStatusBarHeight(context)

            child.checkInOutDates.alpha = 1 - (percentage * 2)
            child.travelers.alpha = 1 - (percentage * 2)

            if (textViewLeftX == 0 || textViewWidth == 0 || toolBarRightX == 0) {
                setupValues(child, dependency)
            } else {
                val toolbarText = dependency.findViewById(R.id.checkout_overview_header_toolbar) as CheckoutOverviewHeader
                val lp = child.layoutParams as CoordinatorLayout.LayoutParams
                lp.height = childHeight.toInt()
                lp.setMargins(lp.leftMargin, toolbarText.top, lp.rightMargin, lp.bottomMargin)
                child.layoutParams = lp
                child.destinationText.x = Math.max(toolBarRightX.toFloat(), textViewLeftX * (1 - percentage))
                val scale = .9f + (1 - percentage) * (1f - .9f)
                child.destinationText.pivotX = 0f
                child.destinationText.pivotY = 0f
                child.destinationText.scaleX = scale
                child.destinationText.scaleY = scale
            }
        }
        return true;
    }

    private fun setupValues(child: CheckoutOverviewHeader, dependency: View) {
        val toolbar = dependency.findViewById(R.id.checkout_toolbar) as CheckoutToolbar
        textViewLeftX = getLocation(child.destinationText)[0]
        textViewWidth = child.destinationText.width
        toolBarRightX = getLocation(toolbar.getChildAt(0))[0] + toolbar.getChildAt(0).width
        child.destinationText.maxWidth = child.width - (toolBarRightX * 2)
    }

    private fun getLocation(view: View): IntArray {
        val location = IntArray(2)
        view.getLocationInWindow(location);
        return location;
    }
}