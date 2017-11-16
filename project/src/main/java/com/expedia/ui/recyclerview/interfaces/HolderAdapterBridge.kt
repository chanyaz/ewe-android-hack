package com.expedia.ui.recyclerview.interfaces

import android.view.View
import android.widget.AdapterView
import com.expedia.ui.recyclerview.ItemVH



/**
 * Created by nbirla on 15/11/17.
 */
interface HolderAdapterBridge {
    fun notifyVHChanged(position: Int)
}