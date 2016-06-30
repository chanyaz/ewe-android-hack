package com.expedia.bookings.utils

import android.content.Context
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.ImageButton

object AccessibilityUtil {
    @JvmStatic fun isTalkBackEnabled(context: Context) : Boolean {
        val accessbilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager;
        return accessbilityManager.isEnabled && accessbilityManager.isTouchExplorationEnabled
    }

    @JvmStatic fun setFocusToToolbarNavigationIcon(toolbar: Toolbar) {
        if (AccessibilityUtil.isTalkBackEnabled(toolbar.context)) {
            for (i in 0..toolbar.childCount - 1) {
                val v = toolbar.getChildAt(i)
                if (v is ImageButton) {
                    v.setFocusableInTouchMode(true)
                    v.setFocusable(true)
                    v.clearFocus()
                    v.requestFocus()
                    break
                }
            }
        }
    }

    @JvmStatic fun delayedFocusToView(view: View, delayMillis: Long) {
        if (AccessibilityUtil.isTalkBackEnabled(view.context)) {
            view.postDelayed(Runnable {
                view.isFocusableInTouchMode = true
                view.isFocusable = true
                view.clearFocus()
                view.requestFocus()
            }, delayMillis)
        }
    }
}