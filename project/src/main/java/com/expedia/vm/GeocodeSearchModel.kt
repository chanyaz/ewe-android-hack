package com.expedia.vm

import android.content.Context
import android.location.Address
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.mobiata.android.BackgroundDownloader
import com.mobiata.android.LocationServices
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class GeocodeSearchModel(val context: Context) {

    private val BD_KEY = "geo_search"

    // input
    val searchObserver = BehaviorSubject.create<HotelSearchParams>()

    // outputs
    val geoResults = PublishSubject.create<List<Address>>()
    val errorObservable = PublishSubject.create<ApiError>()

    init {
        searchObserver.subscribe { hotelSearchParams ->
            val query = hotelSearchParams.suggestion.regionNames.shortName
            val bd = BackgroundDownloader.getInstance()
            bd.cancelDownload(BD_KEY)
            bd.startDownload(BD_KEY, mGeocodeDownload(query), geoCallback())
        }
    }

    private fun mGeocodeDownload(query: String): BackgroundDownloader.Download<List<Address>?> {
        return object : BackgroundDownloader.Download<List<Address>?> {
            override fun doDownload(): List<Address>? {
                return LocationServices.geocodeGoogle(context, query)
            }
        }
    }

    private fun geoCallback(): BackgroundDownloader.OnDownloadComplete<List<Address>?> {
        return object : BackgroundDownloader.OnDownloadComplete<List<Address>?> {
            override fun onDownload(results: List<Address>?) {
                if (results != null && results.count() > 0) {
                    geoResults.onNext(results)
                } else {
                    errorObservable.onNext(ApiError(ApiError.Code.HOTEL_SEARCH_NO_RESULTS))
                }
            }
        }
    }
}
