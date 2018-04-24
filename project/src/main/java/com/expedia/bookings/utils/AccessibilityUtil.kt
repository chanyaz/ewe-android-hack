package com.expedia.bookings.utils

import android.content.Context
import android.support.annotation.StringRes
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.ImageButton
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.extensions.setAccessibilityHoverFocus
import com.squareup.phrase.Phrase

object AccessibilityUtil {
    @JvmStatic fun isTalkBackEnabled(context: Context): Boolean {
        val accessbilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return accessbilityManager.isEnabled && accessbilityManager.isTouchExplorationEnabled
    }

    @JvmStatic fun setFocusToToolbarNavigationIcon(toolbar: Toolbar) {
        if (AccessibilityUtil.isTalkBackEnabled(toolbar.context) || ExpediaBookingApp.isRobolectric()) {
            for (i in 0..toolbar.childCount - 1) {
                val v = toolbar.getChildAt(i)
                if (v is ImageButton) {
                    v.isFocusableInTouchMode = true
                    v.isFocusable = true
                    v.requestFocus()
                    val transparentColor = ContextCompat.getColor(toolbar.context, android.R.color.transparent)
                    v.setBackgroundColor(transparentColor)
                    v.setAccessibilityHoverFocus()
                    break
                }
            }
        }
    }

    @JvmStatic fun delayFocusToToolbarNavigationIcon(toolbar: Toolbar, delayMillis: Long) {
        toolbar.postDelayed(Runnable { setFocusToToolbarNavigationIcon(toolbar) }, delayMillis)
    }

    @JvmStatic fun delayedFocusToView(view: View, delayMillis: Long) {
        if (AccessibilityUtil.isTalkBackEnabled(view.context)) {
            view.postDelayed({
                view.isFocusableInTouchMode = true
                view.isFocusable = true
                view.clearFocus()
                view.requestFocus()
            }, delayMillis)
        }
    }

    @JvmStatic fun appendRoleContDesc(view: View, contentDescription: String, @StringRes roleResId: Int) {
        view.contentDescription = contentDescription + " " + view.context.getString(roleResId)
    }

    @JvmStatic
    fun appendRoleContDesc(view: TextView, @StringRes roleResId: Int) {
        appendRoleContDesc(view, view.text.toString(), roleResId)
    }

    @JvmStatic fun getNumberOfInvalidFields(vararg isValid: Boolean): Int {
        var sum = 0
        for (item in isValid) {
            sum += if (item) 0 else 1
        }
        return sum
    }

    @JvmStatic fun setFocusForView(view: View) {
        view.isFocusable = true
        view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    @JvmStatic fun requestFocusForView(view: View) {
        setFocusForView(view)
        view.requestFocus()
    }
}

fun setContentDescriptionToolbarTabs(context: Context, tabs: TabLayout) {
    for (index in 0..tabs.tabCount) {
        val tab = tabs.getTabAt(index)
        if (tab != null) {
            val tabContDesc = Phrase.from(context, R.string.accessibility_cont_desc_search_type_TEMPLATE)
                    .put("tabname", tab.text).format().toString()
            tab.contentDescription = tabContDesc
        }
    }
}
