package com.expedia.bookings.widget.rail

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent

public class RailResultsRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {
    val PICASSO_TAG = "RAIL_RESULTS_LIST"
    val layoutManager = LinearLayoutManager(getContext())

    init {
        setLayoutManager(layoutManager)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val adapter = adapter as RailResultsAdapter

        if (adapter.isLoading()) {
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }
}
