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
//                System.out.println(satelliteSearchResponse)
                val FEATURE_CONFIG = "featureConfig"
                val prefs = getContext().getSharedPreferences(FEATURE_CONFIG, 0).edit()
                prefs.clear()
                prefs.apply()
                prefs.putString("test-id",satelliteSearchResponse.toString())
                prefs.putLong("timestamp",System.currentTimeMillis())
                prefs.apply()
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
        satSearch.fetchFeatureConfig(featureConfigObserver, ServicesUtil.generateClientId(getContext()))
    }

}
