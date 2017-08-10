package com.expedia.util

import com.activeandroid.Cache.getContext
import com.expedia.bookings.services.SatelliteServices
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.Ui
import com.mobiata.android.Log
import com.mobiata.android.Params.LOGGING_TAG
import rx.Observer
import rx.subjects.PublishSubject
import javax.inject.Inject

class SatelliteViewModel {

    lateinit var sattelliteServices: SatelliteServices
        @Inject set

    val satelliteResponseSubject = PublishSubject.create<List<String>>()

    private val featureConfigObserver = object : Observer<List<String>> {
        override fun onError(e: Throwable?) {
        }

        override fun onNext(satelliteSearchResponse: List<String>) {
            if (satelliteSearchResponse != null) {
                satelliteResponseSubject.onNext(satelliteSearchResponse)
                SatelliteConfigManager().storeSatelliteResponse(getContext(), satelliteSearchResponse.toString())
            } else {
                Log.d(LOGGING_TAG, "HMAC 403 or no tests in list")
            }
        }

        override fun onCompleted() {
            // do nothing
        }
    }

    init {
        Ui.getApplication(getContext()).launchComponent().inject(this)
    }

    fun fetchFeatureConfig() {
        val callSatellite: Boolean = SatelliteConfigManager().shouldCallSatellite(getContext())
        if (callSatellite) {
            sattelliteServices.fetchFeatureConfig(featureConfigObserver, ServicesUtil.generateClientId(getContext()))
        }
    }

}
