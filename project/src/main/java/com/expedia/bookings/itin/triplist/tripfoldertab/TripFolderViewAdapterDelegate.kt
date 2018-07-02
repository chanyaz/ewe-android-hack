package com.expedia.bookings.itin.triplist.tripfoldertab

import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.itin.triplist.tripfolderlistitems.TripFolderItemView
import com.expedia.bookings.utils.Ui

class TripFolderViewAdapterDelegate(private val viewType: TripFolderListViewType) : IViewAdapterDelegate {

    override fun getViewType(): Int {
        return viewType.value
    }

    override fun isItemForView(item: Any): Boolean {
        return item is TripFolder
    }

    override fun createView(parent: ViewGroup): View {
        val view = Ui.inflate(R.layout.trip_folder_list_item, parent, false) as TripFolderItemView
        view.setViewModel()
        return view
    }

    override fun bindView(view: View, item: Any) {
        val tripFolderItemView = view as TripFolderItemView
        val tripFolder = item as TripFolder
        tripFolderItemView.viewModel.bindTripFolderSubject.onNext(tripFolder)
    }
}
