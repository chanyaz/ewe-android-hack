package com.expedia.bookings.itin.triplist.tripfoldertab

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

class TripFolderListRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var tripListItems = listOf<Any>()
    var tripFolderViewAdapterDelegate: IViewAdapterDelegate = TripFolderViewAdapterDelegate(TripFolderListViewType.TRIP_FOLDER)

    override fun getItemViewType(position: Int): Int {
        //TODO when there are more types, compare using delegate.isItemForView()
        return tripFolderViewAdapterDelegate.getViewType()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //TODO when there more types, compare using viewType
        val view = tripFolderViewAdapterDelegate.createView(parent)
        return TripFolderListRecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return tripListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == tripFolderViewAdapterDelegate.getViewType()) {
            tripFolderViewAdapterDelegate.bindView(holder.itemView, tripListItems[position])
        }
    }

    fun updateTripListItems(items: List<Any>) {
        //TODO diff util and update only changed items
        tripListItems = items
        notifyDataSetChanged()
    }

    class TripFolderListRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

enum class TripFolderListViewType(val value: Int) {
    TRIP_FOLDER(0)
}
