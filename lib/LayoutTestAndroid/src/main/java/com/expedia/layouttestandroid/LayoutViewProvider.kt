package com.expedia.layouttestandroid

import android.content.Context
import android.view.View
import com.expedia.layouttestandroid.dataspecs.LayoutDataSpecValues
import com.expedia.layouttestandroid.tester.LayoutTesterConfigurator
import com.expedia.layouttestandroid.viewsize.LayoutViewSize

abstract class LayoutViewProvider {

    abstract fun getView(context: Context,
                         dataSpec: Map<String, Any?>,
                         size: LayoutViewSize,
                         reuseView: View?): View

    open fun dataSpecForTest(): Map<String, LayoutDataSpecValues> {
        return emptyMap()
    }

    open fun sizesForView(): Array<LayoutViewSize> {
        return emptyArray()
    }

    open fun setupLayoutTesters(configurator: LayoutTesterConfigurator,
                                view: View,
                                dataSpec: Map<String, Any?>,
                                size: LayoutViewSize) {

    }
}
