package com.expedia.vm.itin

import android.content.Context
import com.expedia.bookings.data.hotels.HotelApplyCouponParameters
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.WeatherParams
import com.expedia.bookings.data.itin.WUndergroundForecastDay
import com.expedia.bookings.data.itin.WUndergroundSearchResponse
import com.expedia.bookings.services.WeatherServices
import com.expedia.util.endlessObserver
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class WeatherViewModel(val context: Context, val weatherServices: WeatherServices) {

    val weatherDownloadsObservable = PublishSubject.create<Observable<WUndergroundSearchResponse>>()
    val weatherParamsObservable = BehaviorSubject.create<WeatherParams>()

    private val weatherObservable = Observable.concat(weatherDownloadsObservable)

    val currentWeatherObservable = BehaviorSubject.create<String>()
    val forecastDaysObservable = BehaviorSubject.create<List<WUndergroundForecastDay>>()

    init {
        weatherParamsObservable.subscribe { params ->
            val observable = weatherServices.getWeather(params)
            weatherDownloadsObservable.onNext(observable)
        }

        weatherObservable.subscribe(endlessObserver { weather ->
            currentWeatherObservable.onNext(weather.wUndergroundCurrentConditions.toString())
            forecastDaysObservable.onNext(weather.simpleForecast.forecastDays)
        })
    }

}