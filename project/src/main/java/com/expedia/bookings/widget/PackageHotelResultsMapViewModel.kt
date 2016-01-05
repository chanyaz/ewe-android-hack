package com.expedia.bookings.widget

import android.content.Context
import android.location.Location
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.vm.BaseResultsMapViewModel
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.ui.IconGenerator
import rx.subjects.PublishSubject

public class PackageHotelResultsMapViewModel(context: Context, currentLocation: Location, factory: IconGenerator) : BaseResultsMapViewModel(context, currentLocation, factory) {
    val hotelResultsSubject = PublishSubject.create<PackageSearchResponse.HotelPackage>()
    val mapResultsSubject = PublishSubject.create<PackageSearchResponse.HotelPackage>()

    init {
        hotelResultsSubject.subscribe { response ->
            hotels = response.hotels
            if (response.hotels != null && response.hotels.size > 0) {
                newBoundsObservable.onNext(getMapBounds(response.hotels))
            }
        }

        mapResultsSubject.subscribe { response ->
            hotels = response.hotels
            markersObservable.onNext(hotels)
        }
    }

    fun getMapBounds(hotels: List<Hotel>): LatLngBounds {
        val allHotelsBox: LatLngBounds = boxHotels(hotels)
        return allHotelsBox
    }
}