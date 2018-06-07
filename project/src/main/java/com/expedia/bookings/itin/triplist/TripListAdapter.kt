package com.expedia.bookings.itin.triplist

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R

class TripListAdapter(val context: Context) : PagerAdapter() {
    val upcomingTripListView = UpcomingTripListView(context, null)
    val pastTripListView = PastTripListView(context, null)
    val cancelledTripListView = CancelledTripListView(context, null)

    override fun isViewFromObject(view: View?, obj: Any?): Boolean {
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
            TripListTabs.UPCOMING_TAB.value -> container.removeView(obj as UpcomingTripListView)
            TripListTabs.PAST_TAB.value -> container.removeView(obj as PastTripListView)
            TripListTabs.CANCELLED_TAB.value -> container.removeView(obj as CancelledTripListView)
        }
    }
}
