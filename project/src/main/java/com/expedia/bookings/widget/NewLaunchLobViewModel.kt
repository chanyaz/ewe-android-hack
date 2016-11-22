package com.expedia.bookings.widget

import android.content.Context
import android.view.View
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

class NewLaunchLobViewModel(val context: Context, val hasInternetConnectionChangeSubject: BehaviorSubject<Boolean>, val posChangeSubject: BehaviorSubject<Unit>) {

    val lobsSubject = PublishSubject.create<ArrayList<LobInfo>>()

    val navigationSubject = PublishSubject.create<Pair<LineOfBusiness, View>>()

    val refreshLobsObserver = endlessObserver<Unit> {
        lobsSubject.onNext(getSupportedLinesOfBusiness(PointOfSale.getPointOfSale()))
    }

    init {
        posChangeSubject.subscribe(refreshLobsObserver)
        navigationSubject.subscribe {
            trackLobNavigation(it.first)
        }
    }

    private fun getSupportedLinesOfBusiness(pos: PointOfSale): ArrayList<LobInfo> {
        val lobs = ArrayList<LobInfo>()

        lobs.add(LobInfo.HOTELS)
        lobs.add(LobInfo.FLIGHTS)

        if (pos.supports(LineOfBusiness.CARS)) {
            lobs.add(LobInfo.CARS)
        }

        if (pos.supports(LineOfBusiness.LX)) {
            lobs.add(LobInfo.ACTIVITIES)
        }

        if (pos.supports(LineOfBusiness.TRANSPORT)) {
            lobs.add(LobInfo.TRANSPORT)
        }

        if (pos.supports(LineOfBusiness.RAILS) && Db.getAbacusResponse()
                .isUserBucketedForTest(AbacusUtils.EBAndroidAppRailLineOfBusinessEnabledTest)) {
            lobs.add(LobInfo.RAILS)
        }

        if (pos.supports(LineOfBusiness.PACKAGES)) {
            // if we have odd lob then we should add Packages in the end other 3 index
            if (lobs.size % 2 == 0) {
                lobs.add(LobInfo.PACKAGES)
            } else {
                lobs.add(2, LobInfo.PACKAGES)
            }
        }

        return lobs
    }

    private fun trackLobNavigation(lineOfBusiness: LineOfBusiness) {
        OmnitureTracking.trackNewLaunchScreenLobNavigation(lineOfBusiness)
    }
}
