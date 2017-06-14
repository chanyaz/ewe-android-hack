package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet

class HotelDetailRecyclerGallery(context: Context, attrs: AttributeSet) : RecyclerGallery(context, attrs) {
    private var collapse = false

    override fun useCollapsedGalleryContDesc(): Boolean {
        return collapse
    }

    fun prepareCollapseState(collapse: Boolean) {
        this.collapse = collapse
        val first = (layoutManager as A11yLinearLayoutManager).findFirstVisibleItemPosition()
        val last = (layoutManager as A11yLinearLayoutManager).findLastVisibleItemPosition()

        (findViewHolderForAdapterPosition(first) as? RecyclerGallery.RecyclerAdapter.GalleryViewHolder)?.updateContDesc()

        if (first != last) {
            (findViewHolderForAdapterPosition(last) as? RecyclerGallery.RecyclerAdapter.GalleryViewHolder)?.updateContDesc()
        }
    }
}
