package com.expedia.bookings.utils

import android.content.Context
import android.support.annotation.StringRes
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.ImageButton
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp

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
                    v.clearFocus()
                    v.requestFocus()
                    v.setBackgroundColor(android.R.color.transparent)
                    v.setAccessibilityHoverFocus()
                    break
                }
            }
        }
    }

    @JvmStatic fun delayFocusToToolbarNavigationIcon(toolbar: Toolbar, delayMillis: Long) {
        toolbar.postDelayed(Runnable { setFocusToToolbarNavigationIcon(toolbar) }, delayMillis)
    }

    @JvmStatic fun setMenuItemContentDescription(toolbar: Toolbar, contentDescription: String) {
        if (AccessibilityUtil.isTalkBackEnabled(toolbar.context)) {
            for (i in 0..toolbar.childCount - 1) {
                val v = toolbar.getChildAt(i)
                if (v is ActionMenuView) {
                    v.getChildAt(0)?.contentDescription = contentDescription
                    break
                }
            }
        }
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
}

fun View.setFocusForView() {
    AccessibilityUtil.setFocusForView(this)
}

fun View.setAccessibilityHoverFocus() {
    this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
}

fun View.setAccessibilityHoverFocus(delayMillis: Long) {
    postDelayed({ this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER) }, delayMillis)
}

fun TextInputLayout.hideErrorTextViewFromHoverFocus() {
    val errorText = this.findViewById<AppCompatTextView>(R.id.textinput_error)
    errorText.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
}
