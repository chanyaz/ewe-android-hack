package com.expedia.bookings.extensions

import android.view.View
import android.view.accessibility.AccessibilityEvent
import com.expedia.bookings.utils.AccessibilityUtil
import io.reactivex.Observer

fun View.setFocusForView() {
    AccessibilityUtil.setFocusForView(this)
}

fun View.setAccessibilityHoverFocus() {
    this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
}

fun View.setAccessibilityHoverFocus(delayMillis: Long) {
    postDelayed({ this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER) }, delayMillis)
}

fun View.setInverseVisibility(forward: Boolean) {
    this.visibility = if (forward) View.GONE else View.VISIBLE
}

fun View.setVisibility(show: Boolean) {
    this.visibility = if (show) View.VISIBLE else View.GONE
}

fun View.subscribeOnClick(observer: Observer<Unit>) {
    this.setOnClickListener {
        observer.onNext(Unit)
    }
}

fun View.unsubscribeOnClick() {
    this.setOnClickListener(null)
}
