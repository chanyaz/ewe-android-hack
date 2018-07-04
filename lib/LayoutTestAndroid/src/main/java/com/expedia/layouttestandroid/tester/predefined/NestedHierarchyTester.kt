package com.expedia.layouttestandroid.tester.predefined

import android.support.annotation.IdRes
import android.support.v7.widget.CardView
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import com.expedia.layouttestandroid.extension.getVisibleChildViews
import com.expedia.layouttestandroid.extension.toDisplayString
import com.expedia.layouttestandroid.tester.LayoutTestException
import com.expedia.layouttestandroid.tester.LayoutTester
import com.expedia.layouttestandroid.viewsize.LayoutViewSize

class NestedHierarchyTester : LayoutTester {
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
            testNestedHierarchyTester(view)
        }
    }

    private fun testNestedHierarchyTester(view: View) {
        if (view is ViewGroup) {
            val visibleChildViews = view.getVisibleChildViews()
            if (!isSingleChildContainer(view) && visibleChildViews.size == 1) {
                val childView = visibleChildViews[0]
                if (!shouldIgnoreView(childView) && childView is ViewGroup) {
                    throw LayoutTestException("View: ${childView.toDisplayString()} is nested in ${view.toDisplayString()} which can be merged",
                            arrayListOf(childView, view))
                }
            }
            visibleChildViews.forEach { childView ->
                testNestedHierarchyTester(childView)
            }
        }
    }

    private fun isSingleChildContainer(view: ViewGroup): Boolean {
        return view is ScrollView ||
                view is CardView
    }

    private fun shouldIgnoreView(view: View?): Boolean {
        return viewsToIgnore.any { view == it } || viewIdsToIgnore.any { view?.id == it }
    }
}
