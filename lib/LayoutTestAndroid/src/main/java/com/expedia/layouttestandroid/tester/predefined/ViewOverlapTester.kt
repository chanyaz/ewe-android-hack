package com.expedia.layouttestandroid.tester.predefined

import android.view.View
import android.view.ViewGroup
import com.expedia.layouttestandroid.extension.getVisibleChildViews
import com.expedia.layouttestandroid.extension.toDisplayString
import com.expedia.layouttestandroid.tester.LayoutTestException
import com.expedia.layouttestandroid.tester.LayoutTester
import com.expedia.layouttestandroid.viewsize.LayoutViewSize

class ViewOverlapTester : LayoutTester {
    private val viewsToIgnore = arrayListOf<Pair<View, View>>()
    private val viewIdsToIgnore = arrayListOf<Pair<Int, Int>>()

    fun ignoreViews(vararg views: Pair<View, View>) {
        viewsToIgnore += views
    }

    fun ignoreViewIds(vararg viewIds: Pair<Int, Int>) {
        viewIdsToIgnore += viewIds
    }

    override fun runTest(view: View, dataSpec: Map<String, Any?>, size: LayoutViewSize) {
        if (view.visibility == View.VISIBLE) {
            testIfViewOverlap(view)
        }
    }

    private fun testIfViewOverlap(view: View) {
        if (view is ViewGroup) {
            val childViews = view.getVisibleChildViews()
            (0 until childViews.size).forEach { i ->
                (i + 1 until childViews.size)
                        .forEach { j ->
                            val view1 = childViews[i]
                            val view2 = childViews[j]

                            if (shouldTestOverlap(view1, view2)) {
                                doViewsOverlap(view1, view2)
                            }
                        }
            }
            childViews.forEach { childView ->
                testIfViewOverlap(childView)
            }
        }
    }

    private fun shouldTestOverlap(view1: View, view2: View): Boolean {
        return !(shouldIgnoreViewPair(view1, view2) || shouldIgnoreViewIdPair(view1, view2))
    }

    private fun shouldIgnoreViewIdPair(view1: View, view2: View): Boolean {
        return viewIdsToIgnore.any { (ignoreViewId1, ignoreViewId2) ->
            (view1.id == ignoreViewId1 && view2.id == ignoreViewId2) || (view1.id == ignoreViewId2 && view2.id == ignoreViewId1)
        }
    }

    private fun shouldIgnoreViewPair(view1: View, view2: View): Boolean {
        return viewsToIgnore.any { (ignoreView1, ignoreView2) ->
            (view1 == ignoreView1 && view2 == ignoreView2) || (view1 == ignoreView2 && view2 == ignoreView1)
        }
    }

    private fun doViewsOverlap(view1: View, view2: View) {
        val view1Bounds = Rectangle(view1.x, view1.y, view1.width.toFloat(), view1.height.toFloat())
        val view2Bounds = Rectangle(view2.x, view2.y, view2.width.toFloat(), view2.height.toFloat())

        if (doRectanglesOverlap(view1Bounds, view2Bounds)) {
            throw LayoutTestException(createException(view1, view2), arrayListOf(view1, view2))
        }
    }

    private fun createException(view1: View, view2: View): String {
        return "View: ${view1.toDisplayString()} is overlapping with ${view2.toDisplayString()}"
    }

    private fun doRectanglesOverlap(r1: Rectangle, r2: Rectangle): Boolean {
        return !(r1.left >= r2.right ||
                r1.right <= r2.left ||
                r1.top >= r2.bottom ||
                r1.bottom <= r2.top)
    }

    private data class Rectangle(val left: Float, val top: Float, val right: Float, val bottom: Float)
}
