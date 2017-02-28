package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoScrollListener

class FlightListRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {
    val PICASSO_TAG = "FLIGHT_RESULTS_LIST"
    val layoutManager = LinearLayoutManager(getContext())

    init {
        setLayoutManager(layoutManager)
        addOnScrollListener(PicassoScrollListener(getContext(), PICASSO_TAG))
        addItemDecoration(RecyclerDividerDecoration(getContext(), 0, 0, 0, 0, 0, resources.getDimensionPixelSize(R.dimen.footer_button_height), false))
    }

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        return if (isEnabled) super.onInterceptTouchEvent(e) else true
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return if (isEnabled) super.onTouchEvent(e) else false
    }
}
