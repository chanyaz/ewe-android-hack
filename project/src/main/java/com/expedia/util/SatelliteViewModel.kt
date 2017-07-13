package com.expedia.util

import com.activeandroid.Cache.getContext
import com.expedia.bookings.services.SatelliteServices
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.Ui
import rx.Observer
import rx.subjects.PublishSubject
import javax.inject.Inject

class SatelliteViewModel {

    lateinit var satSearch: SatelliteServices
        @Inject set

    val satelliteResponseSubject = PublishSubject.create<List<String>>()

    private val featureConfigObserver = object : Observer<List<String>> {
        override fun onError(e: Throwable?) {
        }

        override fun onNext(satelliteSearchResponse: List<String>) {
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

    init {
        Ui.getApplication(getContext()).defaultLaunchComponents()
        Ui.getApplication(getContext()).launchComponent().inject(this)
    }

    fun fetchFeatureConfig() {
        satSearch.subscribeSatellite(featureConfigObserver, ServicesUtil.generateClientId(getContext()))
    }

}
