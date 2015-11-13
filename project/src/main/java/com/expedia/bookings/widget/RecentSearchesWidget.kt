package com.expedia.bookings.widget;

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.RecentSearchesAdapterViewModel


public class RecentSearchesWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val recentSearchesAdapter: RecyclerView  by bindView(R.id.recent_searches_adapter)

    init {
        View.inflate(context, R.layout.recent_search_view, this)
        orientation = VERTICAL
        recentSearchesAdapter.layoutManager = LinearLayoutManager(context)
    }

    var recentSearchesAdapterViewModel: RecentSearchesAdapterViewModel by notNullAndObservable { vm ->
        recentSearchesAdapter.adapter = RecentSearchesAdapter(vm)
    }
}

