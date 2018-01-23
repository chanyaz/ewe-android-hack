package com.expedia.bookings.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R

class FlightSearchPageAdapter(val context: Context) : PagerAdapter() {

    enum class Tab(val titleResourceId: Int) {
        RETURN(R.string.flights_round_trip_label),
        ONE_WAY(R.string.flights_one_way_label)
    }

    override fun getCount(): Int {
        return Tab.values().size
    }

    override fun isViewFromObject(view: View?, obj: Any?): Boolean {
        return true
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any? {
        // create some mock views to listen for swipes.
        // We're actually not going to show anything here
            return createMockView(container)
    }

    private fun createMockView(container: ViewGroup): View {
        val view = View(context)
        container.addView(view)
        return view
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(Tab.values()[position].titleResourceId)
    }
}
