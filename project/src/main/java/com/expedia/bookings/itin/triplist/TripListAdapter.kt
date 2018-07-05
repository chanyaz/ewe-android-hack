package com.expedia.bookings.itin.triplist

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.itin.triplist.tripfoldertab.TripFolderListTabView
import com.expedia.bookings.itin.triplist.tripfoldertab.TripFolderListTabViewModel

class TripListAdapter(val context: Context, val viewModel: ITripListAdapterViewModel) : PagerAdapter() {
    val upcomingTripListView = TripFolderListTabView(context, null)
    val pastTripListView = TripFolderListTabView(context, null)
    val cancelledTripListView = TripFolderListTabView(context, null)

    init {
        upcomingTripListView.viewModel = TripFolderListTabViewModel()
        pastTripListView.viewModel = TripFolderListTabViewModel()
        cancelledTripListView.viewModel = TripFolderListTabViewModel()

        viewModel.upcomingTripFoldersSubject.subscribe {
            upcomingTripListView.viewModel.foldersSubject.onNext(it)
        }
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return TripListTabs.values().size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            TripListTabs.UPCOMING_TAB.value -> context.getString(R.string.trip_folder_upcoming_tab_title)
            TripListTabs.PAST_TAB.value -> context.getString(R.string.trip_folder_past_tab_title)
            else -> context.getString(R.string.trip_folder_cancelled_tab_title)
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        when (position) {
            TripListTabs.UPCOMING_TAB.value -> {
                container.addView(upcomingTripListView)
                return upcomingTripListView
            }
            TripListTabs.PAST_TAB.value -> {
                container.addView(pastTripListView)
                return pastTripListView
            }
            else -> {
                container.addView(cancelledTripListView)
                return cancelledTripListView
            }
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        when (position) {
            TripListTabs.UPCOMING_TAB.value -> container.removeView(obj as TripFolderListTabView)
            TripListTabs.PAST_TAB.value -> container.removeView(obj as TripFolderListTabView)
            TripListTabs.CANCELLED_TAB.value -> container.removeView(obj as TripFolderListTabView)
        }
    }
}
