package com.expedia.bookings.launch.vm

import android.content.Context
import com.expedia.bookings.data.weather.WeatherForecastParams
import com.expedia.bookings.data.weather.WeatherForecastResponse
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
            weatherServices.getFiveDayForecast(params, getForecastResponseObservable())
        }
    }

    private fun buildLocationSearchParams(query: String): WeatherLocationParams {
        return WeatherLocationParams("MIc9ks5PfyJGAAiM153steNT6oqhAAsw", query)
    }

    private fun buildForecastSearchParams(locationCode: String): WeatherForecastParams {
        return WeatherForecastParams("MIc9ks5PfyJGAAiM153steNT6oqhAAsw", locationCode)
    }

    fun getLocationResponseObservable(): Observer<List<WeatherLocationResponse>> {
        return object : Observer<List<WeatherLocationResponse>> {
            override fun onCompleted() {
                return
            }
            override fun onNext(response: List<WeatherLocationResponse>?) {
                Log.d("Location Key Retrieved!")
                searchWeatherForcasts.onNext(response?.get(0)?.key)

            }
            override fun onError(e: Throwable?) {
                Log.e("FAILEDDDDDD"+e?.message)
                return
            }
        }
    }

    fun getForecastResponseObservable(): Observer<WeatherForecastResponse> {
        return object : Observer<WeatherForecastResponse> {
            override fun onCompleted() {
                return
            }

            override fun onNext(t: WeatherForecastResponse?) {
                Log.d("Forecast Received!")
            }

            override fun onError(e: Throwable?) {
                Log.e("FAILEDDDDDD"+e?.message)
                return
            }
        }
    }
}