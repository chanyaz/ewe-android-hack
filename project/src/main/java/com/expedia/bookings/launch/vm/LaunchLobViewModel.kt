package com.expedia.bookings.launch.vm

import android.content.Context
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.util.PackageUtil
import com.expedia.util.endlessObserver
import com.mobiata.android.util.SettingUtils
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

class LaunchLobViewModel(val context: Context, val hasInternetConnectionChangeSubject: BehaviorSubject<Boolean>?, val posChangeSubject: BehaviorSubject<Unit>?) {

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

        val packagesPOSABTestEnabled = !PackageUtil.isPackageLOBUnderABTest
        val forceEnablePackagesLOB = SettingUtils.get(context, R.string.preference_force_enable_packages_lob, false)

        if ((pos.supports(LineOfBusiness.PACKAGES) && packagesPOSABTestEnabled) || forceEnablePackagesLOB) {
            lobs.add(LobInfo.PACKAGES)
        }

        if (pos.supports(LineOfBusiness.CARS)) {
            lobs.add(LobInfo.CARS)
        }

        if (pos.supports(LineOfBusiness.LX)) {
            lobs.add(LobInfo.ACTIVITIES)
        }

        val isPosSupportingRails = pos.supports(LineOfBusiness.RAILS)
                || (pos.supportsRailsWebView() && (!pos.isRailsWebViewBehindABTest || AbacusFeatureConfigManager.isBucketedForTest(context, pos.railsWebViewABTestID)))
        if (isPosSupportingRails) {
            lobs.add(LobInfo.RAILS)
        }

        return lobs
    }

    private fun trackLobNavigation(lineOfBusiness: LineOfBusiness) {
        OmnitureTracking.trackNewLaunchScreenLobNavigation(lineOfBusiness)
    }
}
