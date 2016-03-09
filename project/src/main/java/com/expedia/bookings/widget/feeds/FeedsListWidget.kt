package com.expedia.bookings.widget.feeds

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

class FeedsListWidget(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    override fun onFinishInflate() {
        super.onFinishInflate()

        adapter = FeedsListAdapter()
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        adapter.notifyDataSetChanged()
    }
}
