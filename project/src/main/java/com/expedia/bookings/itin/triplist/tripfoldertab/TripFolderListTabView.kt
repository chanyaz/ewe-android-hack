package com.expedia.bookings.itin.triplist.tripfoldertab

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.RecyclerDividerDecoration
import com.expedia.util.notNullAndObservable

class TripFolderListTabView(context: Context, attributeSet: AttributeSet?) : LinearLayout(context, attributeSet) {

    private val LIST_VIEW_PADDING = 12
    val tripListRecyclerView: RecyclerView by bindView(R.id.trip_list_recycler_view)
    private val recyclerViewAdapter = TripFolderListRecyclerViewAdapter()

    var viewModel: ITripFolderListTabViewModel by notNullAndObservable { vm ->
        vm.foldersSubject.subscribe { folders ->
            recyclerViewAdapter.updateTripListItems(folders)
        }
    }

    init {
        View.inflate(context, R.layout.trip_folders_trip_list_tab, this)
        tripListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerViewAdapter
            addItemDecoration(RecyclerDividerDecoration(context, LIST_VIEW_PADDING, 0, 0, false))
        }
    }
}
