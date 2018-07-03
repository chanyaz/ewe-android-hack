package com.expedia.layouttestandroid.extension

import android.view.View
import android.view.ViewGroup
import com.expedia.layouttestandroid.viewsize.LayoutViewSize
import com.facebook.testing.screenshot.ViewHelpers

fun View.setSize(size: LayoutViewSize?): View {
    val setupViewHelper = ViewHelpers.setupView(this)
    size?.computedWidth?.let {
        setupViewHelper.setExactWidthPx(it)
    }
    size?.computedHeight?.let {
        setupViewHelper.setExactHeightPx(it)
    }
    setupViewHelper.layout()
    return this
}

fun View.toDisplayString(): String {
    return "${javaClass.canonicalName} : ${hashCodeString()}"
}

fun View.hashCodeString(): String {
    return Integer.toHexString(hashCode())
}

fun ViewGroup.getVisibleChildViews(): List<View> {
    return (0 until childCount)
            .map { getChildAt(it) }
            .filter { it.visibility == View.VISIBLE }
}
