package com.expedia.util

import com.activeandroid.Cache.getContext
import com.expedia.bookings.data.SatelliteSearchResponse
import com.expedia.bookings.services.SatelliteServices
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.Ui
import rx.Observer
import rx.subjects.PublishSubject
import javax.inject.Inject

class SatelliteUtil {

    lateinit var satSearch: SatelliteServices
        @Inject set

    init {
        Ui.getApplication(getContext()).launchComponent().inject(this)
    }

    fun callSatellite() {

        val satelliteObserver = object : Observer<SatelliteSearchResponse> {
            override fun onError(e: Throwable?) {
            }
            val satelliteResponseSubject = PublishSubject.create<SatelliteSearchResponse>()

            override fun onNext(satelliteSearchResponse: SatelliteSearchResponse) {
                if (satelliteSearchResponse != null) {
                    satelliteResponseSubject.onNext(satelliteSearchResponse)
                } else {
                    //nothing, swallow errors
                }
            }

            override fun onCompleted() {
                // do nothing
            }
        }
        satSearch.satelliteSearch(satelliteObserver, ServicesUtil.generateClientId(getContext()))
    }

}
