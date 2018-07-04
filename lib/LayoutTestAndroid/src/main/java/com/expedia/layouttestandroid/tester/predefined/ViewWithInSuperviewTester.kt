package com.expedia.layouttestandroid.tester.predefined

import android.graphics.Rect
import android.support.annotation.IdRes
import android.view.View
import android.view.ViewGroup
import com.expedia.layouttestandroid.extension.getVisibleChildViews
import com.expedia.layouttestandroid.extension.toDisplayString
import com.expedia.layouttestandroid.tester.LayoutTestException
import com.expedia.layouttestandroid.tester.LayoutTester
import com.expedia.layouttestandroid.viewsize.LayoutViewSize

class ViewWithInSuperviewTester : LayoutTester {
    private val viewsToIgnore = arrayListOf<View>()
    private val viewIdsToIgnore = arrayListOf<@IdRes Int>()

    fun ignoreViews(vararg views: View) {
        viewsToIgnore += views
    }

    fun ignoreViewIds(@IdRes vararg viewIds: Int) {
        viewIdsToIgnore += viewIds.toList()
    }

    override fun runTest(view: View, dataSpec: Map<String, Any?>, size: LayoutViewSize) {
        if (view.width > (size.computedWidth ?: Int.MAX_VALUE) || view.height > (size.computedHeight
                        ?: Int.MAX_VALUE)) {
            throw LayoutTestException(createRootViewException(view), arrayListOf(view))
        } else if (view.visibility == View.VISIBLE) {
            testIfViewWithInSuperview(view)
        }
    }

    private fun testIfViewWithInSuperview(parentView: View) {
        if (parentView is ViewGroup) {
            val childViews = parentView.getVisibleChildViews()

            (0 until childViews.size)
                    .map { childViews[it] }
                    .filter { !shouldIgnoreView(it) }
                    .forEach { childView ->
                        var errorString: String? = null
                        if (isWidthOrHeightZero(childView)) {
                            errorString = createChildViewWidthOrHeightZeroException(childView, parentView)
                        } else if (!isViewWithinSuperview(parentView, childView)) {
                            errorString = createException(childView, parentView)
                        }
                        errorString?.let {
                            throw LayoutTestException(it, arrayListOf(childView, parentView))
                        }
                    }

            childViews.forEach { childView ->
                testIfViewWithInSuperview(childView)
            }
        }
    }

    private fun shouldIgnoreView(view: View): Boolean {
        return viewsToIgnore.any { view == it } || viewIdsToIgnore.any { view.id == it }
    }

    private fun createRootViewException(rootView: View): String {
        return "View: ${rootView.toDisplayString()} is not within screen"
    }

    private fun createException(childView: View, parentView: View): String {
        return "View: ${childView.toDisplayString()} is not within superview i.e. ${parentView.toDisplayString()}"
    }

    private fun createChildViewWidthOrHeightZeroException(childView: View, parentView: View): String {
        return "View: ${childView.toDisplayString()} is not occupying space in superview i.e. ${parentView.toDisplayString()} where width: ${childView.width} & height: ${childView.height}"
    }

    private fun isWidthOrHeightZero(childView: View): Boolean {
        return childView.width == 0 || childView.height == 0
    }

    private fun isViewWithinSuperview(parentView: View, childView: View): Boolean {
        val parentRect = Rect()
        val childRect = Rect()

        parentView.getGlobalVisibleRect(parentRect)
        childView.getGlobalVisibleRect(childRect)

        return parentRect.contains(childRect)
    }
}
