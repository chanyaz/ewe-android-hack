package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoScrollListener

public class HotelListRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {
    val PICASSO_TAG = "HOTEL_RESULTS_LIST"
    val layoutManager = LinearLayoutManager(getContext())

    init {
        setLayoutManager(layoutManager)
        addOnScrollListener(PicassoScrollListener(getContext(), PICASSO_TAG))
        addItemDecoration(RecyclerDividerDecoration(getContext(), 0, 0, 0, 0, 0, resources.getDimensionPixelSize(R.dimen.hotel_filter_height), false))
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val adapter = adapter as HotelListAdapter

        if (adapter.isLoading())
            return true;

        return super.dispatchTouchEvent(ev);
    }
}
