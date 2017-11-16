package com.expedia.ui.recyclerview.interfaces

import android.view.View
import com.expedia.ui.recyclerview.ItemVH

/**
 * Created by nbirla on 15/11/17.
 */

interface VHClickable {
    fun onViewHolderClicked(holder: ItemVH<*>, view: View)
}