package com.expedia.vm.launch

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.NavigationHelper
import rx.subjects.PublishSubject
import java.util.ArrayList

class LobToolbarViewModel(context: Context, val defaultLob: LineOfBusiness) {

    val lobSelectedSubject = PublishSubject.create<LineOfBusiness>()

    private val nav = NavigationHelper(context)

    init {
        lobSelectedSubject.subscribe { lineOfBusiness -> launchLineOfBusiness(lineOfBusiness) }
    }

    //TODO re-using LobInfo for now, but once we change the toolbar per Art's new design, we'll probably need something different here
    fun getSupportedLobs(): ArrayList<LobInfo> {
        val lobs = ArrayList<LobInfo>()
        val pos = PointOfSale.getPointOfSale()

        lobs.add(LobInfo.HOTELS)

        if (pos.supports(LineOfBusiness.FLIGHTS)) {
            lobs.add(LobInfo.FLIGHTS)
        }

        if (pos.supports(LineOfBusiness.PACKAGES)) {
            lobs.add(LobInfo.PACKAGES)
        }

        if (pos.supports(LineOfBusiness.CARS)) {
            lobs.add(LobInfo.CARS)
        }

        if (pos.supports(LineOfBusiness.LX)) {
            lobs.add(LobInfo.ACTIVITIES)
        }

        if (pos.supports(LineOfBusiness.TRANSPORT)) {
            lobs.add(LobInfo.TRANSPORT)
        }

        if (pos.supports(LineOfBusiness.RAILS)) {
            lobs.add(LobInfo.RAILS)
        }

        return lobs
    }

    private fun launchLineOfBusiness(lob: LineOfBusiness) {
        when (lob) {
            LineOfBusiness.HOTELS -> nav.goToHotels(null)
            LineOfBusiness.FLIGHTS -> nav.goToFlights(null)
            LineOfBusiness.TRANSPORT -> nav.goToTransport(null)
            LineOfBusiness.LX -> nav.goToActivities(null)
            LineOfBusiness.CARS -> nav.goToCars(null)
            LineOfBusiness.PACKAGES -> nav.goToPackages(null, null)
            LineOfBusiness.RAILS -> nav.goToRail(null)
            else -> {
                //Add other lobs navigation in future
            }
        }
    }
}