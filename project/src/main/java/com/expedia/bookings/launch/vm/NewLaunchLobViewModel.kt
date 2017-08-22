package com.expedia.bookings.launch.vm

import android.content.Context
import android.view.View
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

class NewLaunchLobViewModel(val context: Context, val hasInternetConnectionChangeSubject: BehaviorSubject<Boolean>?, val posChangeSubject: BehaviorSubject<Unit>?) {

    val lobsSubject = PublishSubject.create<ArrayList<LobInfo>>()

    val navigationSubject = PublishSubject.create<Pair<LineOfBusiness, View>>()

    val refreshLobsObserver = endlessObserver<Unit> {
        lobsSubject.onNext(getSupportedLinesOfBusiness(PointOfSale.getPointOfSale()))
    }

    init {
        posChangeSubject?.subscribe(refreshLobsObserver)
        navigationSubject.subscribe {
            trackLobNavigation(it.first)
        }
    }

    private fun getSupportedLinesOfBusiness(pos: PointOfSale): ArrayList<LobInfo> {
        val lobs = ArrayList<LobInfo>()

        lobs.add(LobInfo.HOTELS)
        lobs.add(LobInfo.FLIGHTS)

        val packagesPOSABTest = PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.JAPAN || Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppPackagesEnableJapanPOS)
        if (pos.supports(LineOfBusiness.PACKAGES) && packagesPOSABTest) {
            lobs.add(LobInfo.PACKAGES)
        }

        if (pos.supports(LineOfBusiness.CARS) && carsLOBOnNewPOS()) {
            lobs.add(LobInfo.CARS)
        }

        if (pos.supports(LineOfBusiness.LX)) {
            lobs.add(LobInfo.ACTIVITIES)
        }

        if (pos.supports(LineOfBusiness.TRANSPORT)) {
            lobs.add(LobInfo.TRANSPORT)
        }

        if (pos.supports(LineOfBusiness.RAILS) || (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidRailHybridAppForDEEnabled) && PointOfSale.getPointOfSale().pointOfSaleId == PointOfSaleId.GERMANY)) {
            lobs.add(LobInfo.RAILS)
        }

        return lobs
    }

    private fun trackLobNavigation(lineOfBusiness: LineOfBusiness) {
        OmnitureTracking.trackNewLaunchScreenLobNavigation(lineOfBusiness)
    }

    private fun carsLOBOnNewPOS(): Boolean {
        //the POS check will be removed once we are sure there are no issues on cars path for these POS
        val carsPresentOnPOS = PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.ARGENTINA && PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.AUSTRIA
            && PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.BRAZIL && PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.BELGIUM
            && PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.DENMARK && PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.MEXICO
            && PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.SPAIN && PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.NETHERLANDS
            && PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.NORWAY
        val showCarsLOB = carsPresentOnPOS || Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppCarsWebViewNewPOS)
        return showCarsLOB
    }
}
