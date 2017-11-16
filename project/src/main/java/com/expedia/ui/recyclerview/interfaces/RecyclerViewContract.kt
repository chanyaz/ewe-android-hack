package com.expedia.ui.recyclerview.interfaces

import android.view.ViewGroup
import android.view.LayoutInflater
import com.expedia.ui.recyclerview.ItemVH



/**
 * Created by nbirla on 15/11/17.
 */
interface RecyclerViewContract {
    fun createHolder(inflater: LayoutInflater, parent: ViewGroup, type: Int): ItemVH<*>
    fun getViewType(type: String): Int
}