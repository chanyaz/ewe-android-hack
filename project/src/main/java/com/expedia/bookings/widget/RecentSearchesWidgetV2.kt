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


public class RecentSearchesWidgetV2(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val recentSearchesAdapter: RecyclerView  by bindView(R.id.recent_searches_v2_list)

    init {
        View.inflate(context, R.layout.recent_search_view_v2, this)
        orientation = VERTICAL
        recentSearchesAdapter.layoutManager = LinearLayoutManager(context)
    }

    var recentSearchesAdapterViewModel: RecentSearchesAdapterViewModel by notNullAndObservable { vm ->
        recentSearchesAdapter.adapter = RecentSearchesAdapter(vm, false)
       // used to wrap the height of card view which is holding recycler view
        vm.recentSearchesObservable.subscribe {
            val numberOfRows = Math.min(it.size, RecentSearchesAdapter.MAX_ROW_FOR_SEARCH_V2)
            val layoutParams = recentSearchesAdapter.layoutParams
            layoutParams.height = numberOfRows * resources.getDimensionPixelSize(R.dimen.recent_search_item_height)
        }
    }
}

