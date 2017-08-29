package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Point
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.WindowManager
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoScrollListener
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils

class HotelListRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {
    val PICASSO_TAG = "HOTEL_RESULTS_LIST"
    val layoutManager = PreCachingLayoutManager(getContext())

    init {
        layoutManager.setExtraLayoutSpace(getScreenHeight())
        setLayoutManager(layoutManager)
        addOnScrollListener(PicassoScrollListener(getContext(), PICASSO_TAG))
        addItemDecoration(RecyclerDividerDecoration(getContext(), 0, 0, 0, 0, 0, resources.getDimensionPixelSize(R.dimen.hotel_filter_height), false))
    }

    private fun getScreenHeight(): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.y
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val adapter = adapter as BaseHotelListAdapter

        if (adapter.isLoading())
            return true

        return super.dispatchTouchEvent(ev)
    }

    class PreCachingLayoutManager(context: Context) : LinearLayoutManager(context) {
        private var extraLayoutSpace = -1

        fun setExtraLayoutSpace(extraLayoutSpace: Int) {
            this.extraLayoutSpace = extraLayoutSpace
        }

        override fun getExtraLayoutSpace(state: RecyclerView.State): Int {
            if (extraLayoutSpace > 0) {
                return extraLayoutSpace
            } else {
                return super.getExtraLayoutSpace(state)
            }
        }
    }
}
