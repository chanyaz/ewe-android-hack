package com.expedia.bookings.hotel.widget

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class GalleryGridItemDecoration(private val space: Int, private val columnCount: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        outRect.top = space + space / 2
        outRect.bottom = 0

        val position = parent.getChildAdapterPosition(view)
        val column = position % columnCount

        if (position < columnCount) {
            outRect.top = 0
        }
        if (column == 0) {
            outRect.right = space
        } else if (column == columnCount - 1) {
            outRect.left = space
        } else {
            outRect.left = space / 2
            outRect.right = space / 2
        }
    }
}
