package com.expedia.bookings.launch.vm

import android.content.Context
import com.expedia.bookings.data.weather.WeatherForecastParams
import com.expedia.bookings.data.weather.WeatherLocationParams
import com.expedia.bookings.data.weather.WeatherLocationResponse
import com.expedia.bookings.services.WeatherServices
import com.mobiata.android.Log
import rx.Observer
import rx.subjects.PublishSubject

class WeatherViewModel(val context: Context, val weatherServices: WeatherServices) {
    val locationSearchObservable = PublishSubject.create<String>()
    val searchWeatherForcasts = PublishSubject.create<String>()

    init {
        locationSearchObservable.subscribe { query ->
            val params = buildLocationSearchParams(query)
            weatherServices.locationSearch(params, getLocationResponseObservable())
        }
        searchWeatherForcasts.subscribe { locationCode ->
            val params = buildForecastSearchParams(locationCode)
        }
    }

    private fun buildLocationSearchParams(query: String): WeatherLocationParams {
        return WeatherLocationParams("MIc9ks5PfyJGAAiM153steNT6oqhAAsw" ,query)
    }

    private fun buildForecastSearchParams(locationCode: String): WeatherForecastParams {
        return WeatherForecastParams(locationCode)
    }

    fun getLocationResponseObservable(): Observer<List<WeatherLocationResponse>> {
        return object : Observer<List<WeatherLocationResponse>> {
            override fun onCompleted() {
                return
            }
            override fun onNext(response: List<WeatherLocationResponse>?) {
                Log.d("IT WORKEDDDD")
                searchWeatherForcasts.onNext(response?.get(0)?.key)

            }
            override fun onError(e: Throwable?) {
                Log.e("Whatsuuupppppp"+e?.message)
                return
            }
        }
    }
}