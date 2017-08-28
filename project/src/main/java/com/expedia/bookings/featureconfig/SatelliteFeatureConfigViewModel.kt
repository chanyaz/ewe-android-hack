package com.expedia.bookings.featureconfig

import com.activeandroid.Cache
import com.expedia.bookings.services.SatelliteServices
import com.expedia.bookings.utils.Ui
import rx.Observer
import rx.subjects.PublishSubject
import javax.inject.Inject


class SatelliteFeatureConfigViewModel {
    val featureConfigResponseObservable = PublishSubject.create<List<String>>()

    lateinit var satelliteServices: SatelliteServices
        @Inject set

    init {
        Ui.getApplication(Cache.getContext()).appComponent().inject(this)
    }

    fun fetchRemoteConfig() {
        satelliteServices.fetchFeatureConfig(featureConfigResponseObserver)
    }

    val featureConfigResponseObserver = object : Observer<List<String>> {
        override fun onNext(featureConfigResponse: List<String>) {
            featureConfigResponseObservable.onNext(featureConfigResponse)
            //TODO handle errors
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable?) {
            //TODO do something
        }
    }
}