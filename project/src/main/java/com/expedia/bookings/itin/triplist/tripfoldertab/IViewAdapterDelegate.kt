package com.expedia.bookings.itin.triplist.tripfoldertab

import android.view.View
import android.view.ViewGroup

interface IViewAdapterDelegate {
    fun getViewType(): Int
    fun isItemForView(item: Any): Boolean
    fun createView(parent: ViewGroup): View
    fun bindView(view: View, item: Any)
}
