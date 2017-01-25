package com.expedia.vm.itin

import com.expedia.bookings.data.itin.WUndergroundForecastDay
import com.expedia.bookings.data.itin.WUndergroundForecastTemp
import rx.subjects.BehaviorSubject

/**
 * Created by Supreeth on 1/24/17.
 */
class WeatherForecastViewModel {

    val forecastDayObserver = BehaviorSubject.create<WUndergroundForecastDay>()

    val minTempObservable = BehaviorSubject.create<WUndergroundForecastTemp>()
    val maxTempObservable = BehaviorSubject.create<WUndergroundForecastTemp>()

    init {
        forecastDayObserver.subscribe { forecastDay ->
            minTempObservable.onNext(forecastDay.minTemp)
            maxTempObservable.onNext(forecastDay.maxTemp)
        }
    }
}