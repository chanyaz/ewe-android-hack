package com.expedia.bookings.itin.triplist

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.expedia.bookings.itin.triplist.TripListFragment.Companion.TAB_CANCELLED
import com.expedia.bookings.itin.triplist.TripListFragment.Companion.TAB_PAST
import com.expedia.bookings.itin.triplist.TripListFragment.Companion.TAB_UPCOMING

class CustomFragmentPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            TAB_UPCOMING -> {
                val frag1 = TestFragment()
                frag1.number = TAB_UPCOMING
                frag1
            }
            TAB_PAST -> {
                val frag2 = TestFragment()
                frag2.number = TAB_PAST
                frag2
            }
            TAB_CANCELLED -> {
                val frag3 = TestFragment()
                frag3.number = TAB_CANCELLED
                frag3
            }
            else -> throw RuntimeException("Position out of bounds position=$position")
        }
    }

    override fun getCount(): Int {
        return 3
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
