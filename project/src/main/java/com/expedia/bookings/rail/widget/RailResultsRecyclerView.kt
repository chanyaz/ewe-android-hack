package com.expedia.bookings.rail.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

class RailResultsRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {
    val PICASSO_TAG = "RAIL_RESULTS_LIST"
    val layoutManager = LinearLayoutManager(context)

    init {
        setLayoutManager(layoutManager)
    }
}
