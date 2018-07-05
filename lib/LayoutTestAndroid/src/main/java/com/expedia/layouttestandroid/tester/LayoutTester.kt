package com.expedia.layouttestandroid.tester

import android.view.View
import com.expedia.layouttestandroid.viewsize.LayoutViewSize

interface LayoutTester {
    fun runTest(view: View,
                dataSpec: Map<String, Any?>,
                size: LayoutViewSize)
}
