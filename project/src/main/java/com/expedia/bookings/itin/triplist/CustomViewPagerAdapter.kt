package com.expedia.bookings.itin.triplist

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.itin.triplist.TripListFragment.Companion.TAB_CANCELLED
import com.expedia.bookings.itin.triplist.TripListFragment.Companion.TAB_PAST
import com.expedia.bookings.itin.triplist.TripListFragment.Companion.TAB_UPCOMING

class CustomViewPagerAdapter(val context: Context) : PagerAdapter() {
    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return 3
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val triplistView = TripListView(context)
        container.addView(triplistView)
        return triplistView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as TripListView)
    }

    override fun getPageTitle(i: Int): String {
        return when (i) {
            TAB_UPCOMING -> "UPCOMING"
            TAB_PAST -> "PAST"
            TAB_CANCELLED -> "CANCELLED"
            else -> throw RuntimeException("Position out of bounds position = $i")
        }
    }
}
