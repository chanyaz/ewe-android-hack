package com.expedia.bookings.itin.triplist

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.tracking.ITripsTracking
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.DisableableViewPager
import javax.inject.Inject

class TripListFragment : Fragment(), TabLayout.OnTabSelectedListener {
    private val tripTabLayout: TabLayout by bindView(R.id.trip_list_tabs)
    private val viewPager: DisableableViewPager by bindView(R.id.trip_list_viewpager)

    var tripsTracking: ITripsTracking = TripsTracking
    val tripListAdapterViewModel: ITripListAdapterViewModel = TripListAdapterViewModel()
    lateinit var tripListRepository: ITripListRepository
        @Inject set
    lateinit var viewModel: ITripListFragmentViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        Ui.getApplication(context).defaultTripComponents()
        Ui.getApplication(context).tripComponent().inject(this)

        val viewModelFactory = TripListFragmentViewModelFactory(tripsTracking, tripListRepository)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TripListFragmentViewModel::class.java)
        if (context is TripListFragmentListener) {
            val listener: TripListFragmentListener = context
            listener.onTripListFragmentAttached(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_trip_folders_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            viewPager.adapter = TripListAdapter(it, tripListAdapterViewModel)
        }
        viewPager.offscreenPageLimit = Integer.MAX_VALUE
        viewPager.setPageSwipingEnabled(false)
        tripTabLayout.setupWithViewPager(viewPager)
        tripTabLayout.addOnTabSelectedListener(this)

        setupLiveDataObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        tripTabLayout.removeOnTabSelectedListener(this)
    }

    fun setupLiveDataObservers() {
        viewModel.upcomingFoldersLiveData.observe(this, Observer<List<TripFolder>> { folders ->
            folders?.let {
                tripListAdapterViewModel.upcomingTripFoldersSubject.onNext(folders)
            }
        })
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
