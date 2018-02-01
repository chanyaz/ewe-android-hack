package com.expedia.vm.hotel

import android.content.Context
import android.location.Location
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.hotel.map.HotelMapMarker
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.mobiata.android.maps.MapUtils

open class HotelResultsMapViewModel(val context: Context, val currentLocation: Location) {
    var selectedMapMarker: HotelMapMarker? = null

    val minHotelBoundSize = 300.0
    val NORTH_DIRECTION = 0.0
    val EAST_DIRECTION = 90.0
    val SOUTH_DIRECTION = 180.0
    val WEST_DIRECTION = 270.0

    fun getMapBounds(res: BaseApiResponse): LatLngBounds {
        val response = res as HotelSearchResponse
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
            val closestNeighborhood = response.neighborhoodsMap[closestHotelWithNeighborhood.locationId]
            if (closestNeighborhood == null) {
                return allHotelsBox
            } else {
                return boxHotels(closestNeighborhood.hotels)
            }
            //Default, zoom out and show all hotels
        } else {
            return allHotelsBox
        }
    }

    fun sortByLocation(location: Location, hotels: List<Hotel>): List<Hotel> {
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

        var bounds = box.build()

        val northEast = bounds.northeast
        val southWest = bounds.southwest
        val center = bounds.center

        if (milesToMeters(MapUtils.getDistance(center.latitude, northEast.longitude, center.latitude, southWest.longitude)) < minHotelBoundSize) {
            val east = SphericalUtil.computeOffset(center, minHotelBoundSize / 2, EAST_DIRECTION)
            val west = SphericalUtil.computeOffset(center, minHotelBoundSize / 2, WEST_DIRECTION)
            box.include(east)
            box.include(west)
        }

        if (milesToMeters(MapUtils.getDistance(northEast.latitude, center.longitude, southWest.latitude, center.longitude)) < minHotelBoundSize) {
            val north = SphericalUtil.computeOffset(center, minHotelBoundSize / 2, NORTH_DIRECTION)
            val south = SphericalUtil.computeOffset(center, minHotelBoundSize / 2, SOUTH_DIRECTION)
            box.include(north)
            box.include(south)
        }

        return box.build()
    }

    private fun milesToMeters(miles: Double): Double {
        return MapUtils.milesToKilometers(miles) * 1000
    }
}
