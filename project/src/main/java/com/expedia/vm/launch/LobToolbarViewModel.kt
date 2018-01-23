package com.expedia.vm.launch

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.NavigationHelper
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

class LobToolbarViewModel(context: Context, val defaultLob: LineOfBusiness) {

    val lobSelectedSubject = PublishSubject.create<LineOfBusiness>()

    val supportedLobs: ArrayList<LobInfo> = getLobs()
    private val nav = NavigationHelper(context)

    init {
        lobSelectedSubject.subscribe { lineOfBusiness ->
            if (lineOfBusiness != defaultLob) {
                launchLineOfBusiness(lineOfBusiness)
            }
        }
    }

    //TODO re-using LobInfo for now, but once we change the toolbar per Art's new design, we'll probably need something different here
    private fun getLobs(): ArrayList<LobInfo> {
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

    fun getLobPosition(lob: LineOfBusiness): Int {
        for ((index, lobInfo) in supportedLobs.withIndex()) {
            if (lobInfo.lineOfBusiness == lob)
                return index
        }
        return 0
    }

    private fun launchLineOfBusiness(lob: LineOfBusiness) {
        when (lob) {
            LineOfBusiness.HOTELS -> nav.goToHotels(null, finishCallingActivity = true)
            LineOfBusiness.FLIGHTS -> nav.goToFlights(null, finishCallingActivity = true)
            LineOfBusiness.TRANSPORT -> nav.goToTransport(null, finishCallingActivity = true)
            LineOfBusiness.LX -> nav.goToActivities(null, finishCallingActivity = true)
            LineOfBusiness.CARS -> nav.goToCars(null, finishCallingActivity = true)
            LineOfBusiness.PACKAGES -> nav.goToPackages(null, null, finishCallingActivity = true)
            LineOfBusiness.RAILS -> nav.goToRail(null, finishCallingActivity = true)
            else -> {
                //Add other lobs navigation in future
            }
        }
    }
}
