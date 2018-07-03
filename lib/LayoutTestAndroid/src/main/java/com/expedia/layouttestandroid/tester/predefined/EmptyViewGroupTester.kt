package com.expedia.layouttestandroid.tester.predefined

import android.support.annotation.IdRes
import android.view.View
import android.view.ViewGroup
import com.expedia.layouttestandroid.extension.getVisibleChildViews
import com.expedia.layouttestandroid.extension.toDisplayString
import com.expedia.layouttestandroid.tester.LayoutTestException
import com.expedia.layouttestandroid.tester.LayoutTester
import com.expedia.layouttestandroid.viewsize.LayoutViewSize

class EmptyViewGroupTester : LayoutTester {
    private val viewsToIgnore = arrayListOf<View>()
    private val viewIdsToIgnore = arrayListOf<@IdRes Int>()

    fun ignoreViews(vararg views: View) {
        viewsToIgnore += views
    }

    fun ignoreViewIds(@IdRes vararg viewIds: Int) {
        viewIdsToIgnore += viewIds.toList()
    }

    override fun runTest(view: View, dataSpec: Map<String, Any?>, size: LayoutViewSize) {
        if (view.visibility == View.VISIBLE) {
            testEmptyViewGroupTester(view)
        }
    }

    private fun testEmptyViewGroupTester(view: View) {
        if (view is ViewGroup) {
            if (!shouldIgnoreView(view) && view.childCount == 0) {
                throw LayoutTestException("ViewGroup: ${view.toDisplayString()} is empty", arrayListOf(view))
            } else {
                view.getVisibleChildViews().forEach { childView ->
                    testEmptyViewGroupTester(childView)
                }
            }
        }
    }

    private fun shouldIgnoreView(view: View): Boolean {
        return viewsToIgnore.any { view == it } || viewIdsToIgnore.any { view.id == it }
    }
}