package com.expedia.vm.hotel

import android.content.Context
import android.location.Location
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.hotel.map.HotelMapMarker
import com.expedia.util.endlessObserver
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

open class HotelResultsMapViewModel(val context: Context, val currentLocation: Location) {
    val mapInitializedObservable = PublishSubject.create<Unit>()
    val createMarkersObservable = PublishSubject.create<Unit>()
    val clusterChangeSubject = PublishSubject.create<Unit>()

    var hotels: List<Hotel> = emptyList()

    //inputs
    val hotelResultsSubject = BehaviorSubject.create<HotelSearchResponse>()
    val mapResultsSubject = PublishSubject.create<HotelSearchResponse>()
    val carouselSwipedObservable = PublishSubject.create<HotelMapMarker>()
    val mapBoundsSubject = BehaviorSubject.create<LatLng>()

    //outputs
    val newBoundsObservable = PublishSubject.create<LatLngBounds>()
    val sortedHotelsObservable = PublishSubject.create<List<Hotel>>()
    val soldOutHotel = PublishSubject.create<Hotel>()

    var selectedMapMarker: HotelMapMarker? = null

    val hotelSoldOutWithIdObserver = endlessObserver<String> { soldOutHotelId ->
        val hotel = hotels.firstOrNull { it.hotelId == soldOutHotelId }
        if (hotel != null) {
            hotel.isSoldOut = true
            soldOutHotel.onNext(hotel)
        }
    }

    init {
        createMarkersObservable.subscribe {
            val response = hotelResultsSubject.value
            if (response != null) newBoundsObservable.onNext(getMapBounds(response))
        }

        mapBoundsSubject.subscribe {
            // Map bounds has changed(Search this area or map was animated to a region),
            // sort nearest hotels from center of screen
            val currentRegion = it
            val location = Location("currentRegion")
            location.latitude = currentRegion.latitude
            location.longitude = currentRegion.longitude
            val sortedHotels = sortByLocation(location, hotels)
            sortedHotelsObservable.onNext(sortedHotels)
        }

        hotelResultsSubject.subscribe { response ->
            hotels = response.hotelList
            if (response.hotelList != null && response.hotelList.size > 0) {
                newBoundsObservable.onNext(getMapBounds(response))
            }
        }

        mapResultsSubject.subscribe { response ->
            hotels = response.hotelList
            sortedHotelsObservable.onNext(hotels)
        }
    }

    private fun getMapBounds(response: HotelSearchResponse): LatLngBounds {
        val searchRegionId = response.searchRegionId
        val currentLocationLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)

        val sortedHotels = sortByLocation(currentLocation, response.hotelList)

        val allHotelsBox: LatLngBounds = boxHotels(response.hotelList)

        //If filtered, show all hotels
        if (response.isFilteredResponse) {
            return allHotelsBox
        }

        val isInsideSearchArea = allHotelsBox.contains(currentLocationLatLng)
        val closestHotelWithNeighborhood: Hotel? = sortedHotels.firstOrNull { it.locationId != null }
        val searchNeighborhood: HotelSearchResponse.Neighborhood? = response.allNeighborhoodsInSearchRegion.filter { it.id == searchRegionId }.firstOrNull()

        //If neighborhood search, zoom to that neighborhood
        if (searchNeighborhood != null && searchNeighborhood.hotels.isNotEmpty()) {
            return boxHotels(searchNeighborhood.hotels)
            //if current location is not within the search results, zoom to most "Dense" neighborhood
        } else if (!isInsideSearchArea) {
            val mostInterestingNeighborhood: HotelSearchResponse.Neighborhood? = response.allNeighborhoodsInSearchRegion.sortedByDescending { it.score }.firstOrNull()

            if (mostInterestingNeighborhood != null && mostInterestingNeighborhood.hotels != null && mostInterestingNeighborhood.hotels.isNotEmpty()) {
                return boxHotels(mostInterestingNeighborhood.hotels)
            }

            return allHotelsBox
            //If current location is near a neighborhood, zoom to closest neighborhood
        } else if (closestHotelWithNeighborhood != null) {
            val closestNieghborhood = response.neighborhoodsMap[closestHotelWithNeighborhood.locationId]
            if (closestNieghborhood == null) {
                return allHotelsBox
            } else {
                return boxHotels(closestNieghborhood.hotels)
            }
            //Default, zoom out and show all hotels
        } else {
            return allHotelsBox
        }
    }

    private fun sortByLocation(location: Location, hotels: List<Hotel>): List<Hotel> {
        val hotelLocation = Location("other")
        val sortedHotels = hotels.sortedBy { h ->
            hotelLocation.latitude = h.latitude
            hotelLocation.longitude = h.longitude
            location.distanceTo(hotelLocation)
        }
        return sortedHotels
    }

    private fun boxHotels(hotels: List<Hotel>): LatLngBounds {
        val box = LatLngBounds.Builder()
        for (hotel in hotels) {
            box.include(LatLng(hotel.latitude, hotel.longitude))
        }

        return box.build()
    }
}
