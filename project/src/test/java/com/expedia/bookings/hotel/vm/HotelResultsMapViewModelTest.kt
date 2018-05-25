package com.expedia.bookings.hotel.vm

import android.location.Location
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.Neighborhood
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.mobiata.android.maps.MapUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelResultsMapViewModelTest {

    val context = RuntimeEnvironment.application

    @Test
    fun testGetMapBoundsFilteredResponse() {
        val viewModel = createViewModel()
        val searchResponse = createSearchResponse(isFilteredResponse = true)

        val mapBounds = viewModel.getMapBounds(searchResponse)
        assertDefaultBoxAllHotel(mapBounds)
    }

    @Test
    fun testGetMapBoundsSearchNeighborhood() {
        val viewModel = createViewModel()
        var searchResponse = createSearchResponse(searchRegionId = "region1")
        val mapBounds = viewModel.getMapBounds(searchResponse)

        assertEquals(0.04, mapBounds.northeast.latitude)
        assertEquals(0.04, mapBounds.northeast.longitude)
        assertEquals(0.02, mapBounds.southwest.latitude)
        assertEquals(0.02, mapBounds.southwest.longitude)
    }

    @Test
    fun testGetMapBoundsNotInsideSearchAreaWithMostInterestingNeighborhood() {
        val viewModel = createViewModel()
        var searchResponse = createSearchResponse(searchRegionId = "regionX")
        val mapBounds = viewModel.getMapBounds(searchResponse)

        assertEquals(0.01, mapBounds.northeast.latitude)
        assertEquals(0.01, mapBounds.northeast.longitude)
        assertEquals(0.0, mapBounds.southwest.latitude)
        assertEquals(0.0, mapBounds.southwest.longitude)
    }

    @Test
    fun testGetMapBoundsNotInsideSearchAreaNullMostInterestingNeighborhood() {
        val viewModel = createViewModel()
        var searchResponse = createSearchResponse()
        searchResponse.allNeighborhoodsInSearchRegion.clear()
        val mapBounds = viewModel.getMapBounds(searchResponse)

        assertDefaultBoxAllHotel(mapBounds)
    }

    @Test
    fun testGetMapBoundsNotInsideSearchAreaWithMostInterestingNeighborhoodNullHotel() {
        val viewModel = createViewModel()
        var searchResponse = createSearchResponse(searchRegionId = "regionX")
        searchResponse.allNeighborhoodsInSearchRegion[0].hotels = null
        val mapBounds = viewModel.getMapBounds(searchResponse)

        assertDefaultBoxAllHotel(mapBounds)
    }

    @Test
    fun testGetMapBoundsNotInsideSearchAreaWithMostInterestingNeighborhoodEmptyHotel() {
        val viewModel = createViewModel()
        var searchResponse = createSearchResponse(searchRegionId = "regionX")
        searchResponse.allNeighborhoodsInSearchRegion[0].hotels = ArrayList()
        val mapBounds = viewModel.getMapBounds(searchResponse)

        assertDefaultBoxAllHotel(mapBounds)
    }

    @Test
    fun testGetMapBoundsWithClosestHotelWithNeighborhood() {
        val viewModel = createViewModel(lat = 0.4, lng = 0.4)
        var searchResponse = createSearchResponse()
        searchResponse.allNeighborhoodsInSearchRegion.clear()
        val mapBounds = viewModel.getMapBounds(searchResponse)

        assertEquals(0.04, mapBounds.northeast.latitude)
        assertEquals(0.04, mapBounds.northeast.longitude)
        assertEquals(0.00, mapBounds.southwest.latitude)
        assertEquals(0.00, mapBounds.southwest.longitude)
    }

    @Test
    fun testGetMapBoundsWithClosestHotelWithNeighborhoodNotInNeighborhoodsMap() {
        val viewModel = createViewModel(lat = 0.4, lng = 0.4)
        var searchResponse = createSearchResponse(searchRegionId = "regionY")
        searchResponse.allNeighborhoodsInSearchRegion.clear()
        searchResponse.neighborhoodsMap.clear()
        val mapBounds = viewModel.getMapBounds(searchResponse)

        assertDefaultBoxAllHotel(mapBounds)
    }

    @Test
    fun testGetMapBoundsAllElseCase() {
        val viewModel = createViewModel(lat = 0.0, lng = 0.0)
        var searchResponse = createSearchResponse(searchRegionId = "regionY")
        searchResponse.allNeighborhoodsInSearchRegion.clear()
        searchResponse.neighborhoodsMap.clear()
        searchResponse.hotelList[0].locationId = null
        val mapBounds = viewModel.getMapBounds(searchResponse)

        assertDefaultBoxAllHotel(mapBounds)
    }

    @Test
    fun testGetMapBoundsOneHotel() {
        val viewModel = createViewModel(lat = 2.0, lng = 2.0)
        var searchResponse = createSearchResponse(searchRegionId = "region2")
        val hotel = createHotel(2.0, 2.0)
        searchResponse.allNeighborhoodsInSearchRegion[2].hotels.add(hotel)
        val mapBounds = viewModel.getMapBounds(searchResponse)

        val center = LatLng(2.0, 2.0)
        val east = SphericalUtil.computeOffset(center, viewModel.minHotelBoundSize / 2, viewModel.EAST_DIRECTION)
        val west = SphericalUtil.computeOffset(center, viewModel.minHotelBoundSize / 2, viewModel.WEST_DIRECTION)

        val north = SphericalUtil.computeOffset(center, viewModel.minHotelBoundSize / 2, viewModel.NORTH_DIRECTION)
        val south = SphericalUtil.computeOffset(center, viewModel.minHotelBoundSize / 2, viewModel.SOUTH_DIRECTION)

        assertEquals(north.latitude, mapBounds.northeast.latitude)
        assertEquals(east.longitude, mapBounds.northeast.longitude)
        assertEquals(south.latitude, mapBounds.southwest.latitude)
        assertEquals(west.longitude, mapBounds.southwest.longitude)
        val horizontalDistanceInMeter = MapUtils.milesToKilometers(MapUtils.getDistance(mapBounds.center.latitude, mapBounds.northeast.longitude, mapBounds.center.latitude, mapBounds.southwest.longitude)) * 1000
        assertTrue(horizontalDistanceInMeter >= viewModel.minHotelBoundSize)
        val verticalDistanceInMeter = MapUtils.milesToKilometers(MapUtils.getDistance(mapBounds.northeast.latitude, mapBounds.center.longitude, mapBounds.southwest.latitude, mapBounds.center.longitude)) * 1000
        assertTrue(verticalDistanceInMeter >= viewModel.minHotelBoundSize)
    }

    @Test
    fun testGetMapBoundsGroupedUpHotel() {
        val viewModel = createViewModel()
        var searchResponse = createSearchResponse(hotelSpacing = 0.0001)
        var mapBounds = viewModel.getMapBounds(searchResponse)

        assertEquals(0.0013989805032393373, mapBounds.northeast.latitude)
        assertEquals(0.001398980503239851, mapBounds.northeast.longitude)
        assertEquals(-0.0012989805032393373, mapBounds.southwest.latitude)
        assertEquals(-0.0012989805032398508, mapBounds.southwest.longitude)
        var horizontalDistanceInMeter = MapUtils.milesToKilometers(MapUtils.getDistance(mapBounds.center.latitude, mapBounds.northeast.longitude, mapBounds.center.latitude, mapBounds.southwest.longitude)) * 1000
        assertTrue(horizontalDistanceInMeter >= viewModel.minHotelBoundSize)
        var verticalDistanceInMeter = MapUtils.milesToKilometers(MapUtils.getDistance(mapBounds.northeast.latitude, mapBounds.center.longitude, mapBounds.southwest.latitude, mapBounds.center.longitude)) * 1000
        assertTrue(verticalDistanceInMeter >= viewModel.minHotelBoundSize)

        searchResponse = createSearchResponse(searchRegionId = "region1", hotelSpacing = 0.0001)
        mapBounds = viewModel.getMapBounds(searchResponse)

        assertEquals(0.0016489805032393373, mapBounds.northeast.latitude)
        assertEquals(0.0016489805032578288, mapBounds.northeast.longitude)
        assertEquals(-0.0010489805032393373, mapBounds.southwest.latitude)
        assertEquals(-0.0010489805032578286, mapBounds.southwest.longitude)
        horizontalDistanceInMeter = MapUtils.milesToKilometers(MapUtils.getDistance(mapBounds.center.latitude, mapBounds.northeast.longitude, mapBounds.center.latitude, mapBounds.southwest.longitude)) * 1000
        assertTrue(horizontalDistanceInMeter >= viewModel.minHotelBoundSize)
        verticalDistanceInMeter = MapUtils.milesToKilometers(MapUtils.getDistance(mapBounds.northeast.latitude, mapBounds.center.longitude, mapBounds.southwest.latitude, mapBounds.center.longitude)) * 1000
        assertTrue(verticalDistanceInMeter >= viewModel.minHotelBoundSize)
    }

    private fun assertDefaultBoxAllHotel(mapBounds: LatLngBounds) {
        assertEquals(0.04, mapBounds.northeast.latitude)
        assertEquals(0.04, mapBounds.northeast.longitude)
        assertEquals(0.0, mapBounds.southwest.latitude)
        assertEquals(0.0, mapBounds.southwest.longitude)
    }

    private fun createViewModel(lat: Double = -0.1, lng: Double = -0.1): HotelResultsMapViewModel {
        val currentLocation = Location("current")
        currentLocation.latitude = lat
        currentLocation.longitude = lng
        return HotelResultsMapViewModel(context, currentLocation)
    }

    private fun createSearchResponse(searchRegionId: String = "region0",
                                     numHotel: Int = 5, hotelSpacing: Double = 0.01,
                                     isFilteredResponse: Boolean = false,
                                     numNeighborhood: Int = 3): HotelSearchResponse {
        val hotelSearchResponse = HotelSearchResponse()
        hotelSearchResponse.searchRegionId = searchRegionId

        val hotelList = ArrayList<Hotel>()
        for (i in 0 until numHotel) {
            hotelList.add(createHotel(hotelSpacing * i, hotelSpacing * i))
        }

        hotelSearchResponse.hotelList = hotelList
        hotelSearchResponse.isFilteredResponse = isFilteredResponse
        val allNeighborhoodsInSearchRegion = ArrayList<Neighborhood>()
        val neighborhoodsMap = HashMap<String, Neighborhood>()
        for (i in 0 until numNeighborhood) {
            val neighborhood = createNeighborhood("region$i", ArrayList(), numNeighborhood - i)
            if (i == 0) {
                neighborhood.hotels = hotelList.subList(0, hotelList.count() / 2)
            } else if (i == 1) {
                neighborhood.hotels = hotelList.subList(hotelList.count() / 2, hotelList.count())
            }
            allNeighborhoodsInSearchRegion.add(neighborhood)
            neighborhoodsMap["region$i"] = neighborhood
        }

        hotelSearchResponse.allNeighborhoodsInSearchRegion = allNeighborhoodsInSearchRegion
        hotelSearchResponse.neighborhoodsMap = neighborhoodsMap

        return hotelSearchResponse
    }

    private fun createHotel(lat: Double, lng: Double): Hotel {
        val hotel = Hotel()
        hotel.latitude = lat
        hotel.longitude = lng
        return hotel
    }

    private fun createNeighborhood(id: String, hotels: List<Hotel>, score: Int): Neighborhood {
        val neighborhood = Neighborhood()
        neighborhood.name = "region"
        neighborhood.id = id
        neighborhood.hotels = hotels
        neighborhood.score = score
        return neighborhood
    }
}
