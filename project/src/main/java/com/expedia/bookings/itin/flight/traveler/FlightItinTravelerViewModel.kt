package com.expedia.bookings.itin.flight.traveler

import android.content.Context
import android.support.design.widget.TabLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.itin.common.ItinToolbarViewModel
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject

open class FlightItinTravelerViewModel(private val context: Context, private val itinId: String) : TabLayout.OnTabSelectedListener {
    override fun onTabReselected(tab: TabLayout.Tab?) {
        return
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        return
    }

    override fun onTabSelected(tab: TabLayout.Tab) = updateCurrentTravelerSubject.onNext(travelers[tab.position])

    lateinit var itinCardDataFlight: ItinCardDataFlight
    lateinit var travelers: List<Traveler>
    var itineraryManager: ItineraryManager = ItineraryManager.getInstance()
    val updateToolbarSubject: PublishSubject<ItinToolbarViewModel.ToolbarParams> = PublishSubject.create()
    val updateTravelerListSubject: PublishSubject<List<Traveler>> = PublishSubject.create()
    val updateCurrentTravelerSubject: PublishSubject<Traveler> = PublishSubject.create()
    val itinCardDataNotValidSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()
    val itinCardUpdatedSubject: PublishSubject<Unit> = PublishSubject.create()

    protected lateinit var userStateManager: UserStateManager

    init {
        itinCardUpdatedSubject.subscribe {
            updateToolbar()
            updateTravelList()
        }
    }

    fun onResume() {
        updateItinCardDataFlight()
    }

    fun updateToolbar() {
        val destinationCity = itinCardDataFlight.flightLeg.lastWaypoint?.airport?.mCity ?: ""
        updateToolbarSubject.onNext(ItinToolbarViewModel.ToolbarParams(
                context.getString(R.string.itin_flight_traveler_info),
                Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE)
                        .put("destination", destinationCity).format().toString(),
                false
        ))
    }

    fun updateTravelList() {
        travelers = (itinCardDataFlight.tripComponent as TripFlight).travelers
        updateTravelerListSubject.onNext(travelers)
    }

    fun updateItinCardDataFlight() {
        val freshItinCardDataFlight = itineraryManager.getItinCardDataFromItinId(itinId)
        if (freshItinCardDataFlight != null && freshItinCardDataFlight is ItinCardDataFlight) {
            itinCardDataFlight = freshItinCardDataFlight
            itinCardUpdatedSubject.onNext(Unit)
        } else {
            itinCardDataNotValidSubject.onNext(Unit)
        }
    }
}
