package com.expedia.bookings.itin.triplist

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.tracking.ITripsTracking
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBottomNavigationBarEnabled
import com.expedia.bookings.utils.isBrandColorEnabled

class TripListFragment : Fragment() {
    private val tripToolbar: Toolbar by bindView(R.id.trip_list_toolbar)
    private val tripTabLayout: TabLayout by bindView(R.id.trip_list_tabs)

    //TODO inject dependency from dagger
    var tripsTracking: ITripsTracking = TripsTracking

    companion object {
        val UPCOMING_TAB = 0
        val PAST_TAB = 1
        val CANCELLED_TAB = 2
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is TripListFragmentListener) {
            val listener: TripListFragmentListener = context
            listener.onTripListFragmentAttached(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_trip_folders_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //toolbar and colors ab tests
        handleToolbarAndTabColorForBrandColorsTest()
        handleToolbarVisibilityAndTabColorForBottomNavTest()
    }

    private fun handleToolbarAndTabColorForBrandColorsTest() {
        if (isBrandColorEnabled(context)) {
            val brandColor = ContextCompat.getColor(context, R.color.brand_primary)
            tripToolbar.setBackgroundColor(brandColor)
            tripTabLayout.setBackgroundColor(brandColor)
            tripTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(context, R.color.brand_secondary))
        }
    }

    private fun handleToolbarVisibilityAndTabColorForBottomNavTest() {
        if (isBottomNavigationBarEnabled(context)) {
            tripToolbar.visibility = View.VISIBLE
            tripTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(context, R.color.white))
        } else {
            tripToolbar.visibility = View.GONE
        }
    }

    fun trackTripListVisit() {
        tripsTracking.trackTripListVisit(tripTabLayout.selectedTabPosition)
    }

    interface TripListFragmentListener {
        fun onTripListFragmentAttached(frag: TripListFragment)
    }
}
