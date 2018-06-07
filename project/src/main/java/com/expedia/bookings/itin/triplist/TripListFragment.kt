package com.expedia.bookings.itin.triplist

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.tracking.ITripsTracking
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.DisableableViewPager

class TripListFragment : Fragment(), TabLayout.OnTabSelectedListener {
    private val tripTabLayout: TabLayout by bindView(R.id.trip_list_tabs)
    private val viewPager: DisableableViewPager by bindView(R.id.trip_list_viewpager)

    var tripsTracking: ITripsTracking = TripsTracking
    lateinit var viewModel: ITripListFragmentViewModel

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        viewModel = TripListFragmentViewModel(tripsTracking)
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

        viewPager.offscreenPageLimit = Integer.MAX_VALUE
        viewPager.setPageSwipingEnabled(false)
        viewPager.adapter = TripListAdapter(context)
        tripTabLayout.setupWithViewPager(viewPager)
        tripTabLayout.addOnTabSelectedListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        tripTabLayout.removeOnTabSelectedListener(this)
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        viewModel.tabSelectedSubject.onNext(tab.position)
    }

    fun trackTripListVisit() {
        viewModel.tripListVisitTrackingSubject.onNext(tripTabLayout.selectedTabPosition)
    }

    interface TripListFragmentListener {
        fun onTripListFragmentAttached(frag: TripListFragment)
    }
}
