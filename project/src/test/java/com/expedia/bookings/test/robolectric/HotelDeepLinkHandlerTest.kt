package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.ui.HotelActivity
import com.expedia.vm.HotelDeepLinkHandler
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.Observer
import rx.observers.TestObserver

@RunWith(RobolectricRunner::class)
class HotelDeepLinkHandlerTest {

    lateinit var testDeepLinkSearchObserver: TestObserver<HotelSearchParams?>
    lateinit var testSuggestionLookupObserver: TestObserver<Pair<String, Observer<List<SuggestionV4>>>>
    lateinit var testCurrentLocationSearchObserver: TestObserver<HotelSearchParams?>
    lateinit var testSearchSuggestionObserver: TestObserver<SuggestionV4>
    val hotelPresenter = Mockito.mock(HotelPresenter::class.java)

    @Before fun setup() {
        testDeepLinkSearchObserver = TestObserver<HotelSearchParams?>()
        testSuggestionLookupObserver = TestObserver<Pair<String, Observer<List<SuggestionV4>>>>()
        testCurrentLocationSearchObserver = TestObserver<HotelSearchParams?>()
        testSearchSuggestionObserver = TestObserver<SuggestionV4>()
    }

    @Test fun handleCurrentLocationDeepLink() {
        val handlerUnderTest = HotelDeepLinkHandler(RuntimeEnvironment.application, testDeepLinkSearchObserver, testSuggestionLookupObserver, testCurrentLocationSearchObserver, hotelPresenter, testSearchSuggestionObserver)

        // 1) create suggestionv4 with type MY_LOCATION, lat/lon populated to something other than 0/0
        val suggestion = SuggestionV4()
        suggestion.type = com.expedia.bookings.data.HotelSearchParams.SearchType.MY_LOCATION.name
        suggestion.coordinates = SuggestionV4.LatLng()
        suggestion.coordinates.lat = 42.0
        suggestion.coordinates.lng = -81.0

        // 2) create HotelSearchParams from suggestionv4 + dates
        val hotelSearchParams = createHotelSearchParamsForSuggestion(suggestion)

        // 3) call handleNavigationViaDeepLink with hotelSearchParams
        handlerUnderTest.handleNavigationViaDeepLink(hotelSearchParams)

        // 4) verify
        testCurrentLocationSearchObserver.assertReceivedOnNext(listOf(hotelSearchParams))
        Assert.assertEquals(0, testSuggestionLookupObserver.onNextEvents.size)
        Assert.assertEquals(0, testDeepLinkSearchObserver.onNextEvents.size)
        Assert.assertEquals(0, testSearchSuggestionObserver.onNextEvents.size)
    }

	@Test fun handleSpecificHotelDeepLink() {
        val handlerUnderTest = HotelDeepLinkHandler(RuntimeEnvironment.application, testDeepLinkSearchObserver, testSuggestionLookupObserver, testCurrentLocationSearchObserver, hotelPresenter, testSearchSuggestionObserver)

        // 1) create suggestionv4 with specific hotel
        val suggestion = SuggestionV4()
        suggestion.type = com.expedia.bookings.data.HotelSearchParams.SearchType.HOTEL.name
        suggestion.hotelId = "1234"
        suggestion.gaiaId = "1234"
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "Hotel 1234"
        suggestion.regionNames.shortName = "Hotel 1234"

        // 2) create HotelSearchParams from suggestionv4 + dates
        val hotelSearchParams = createHotelSearchParamsForSuggestion(suggestion)

        // 3) call handleNavigateionDeepLink
        handlerUnderTest.handleNavigationViaDeepLink(hotelSearchParams)

        // 4) verify
        Mockito.verify(hotelPresenter).setDefaultTransition(HotelActivity.Screen.DETAILS)
        Assert.assertEquals(0, testCurrentLocationSearchObserver.onNextEvents.size)
        Assert.assertEquals(0, testSuggestionLookupObserver.onNextEvents.size)
        testDeepLinkSearchObserver.assertReceivedOnNext(listOf(hotelSearchParams))
        testSearchSuggestionObserver.assertReceivedOnNext(listOf(hotelSearchParams.suggestion))
	}

	@Test fun handleLocationDeepLink() {
        val handlerUnderTest = HotelDeepLinkHandler(RuntimeEnvironment.application, testDeepLinkSearchObserver, testSuggestionLookupObserver, testCurrentLocationSearchObserver, hotelPresenter, testSearchSuggestionObserver)

        // 1) create suggestionv4 with regionName
        val suggestion = SuggestionV4()
        suggestion.type = com.expedia.bookings.data.HotelSearchParams.SearchType.CITY.name
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "Portland, ME"
        suggestion.regionNames.shortName = "Portland, ME"
        suggestion.coordinates = SuggestionV4.LatLng()
        suggestion.coordinates.lat = 0.0
        suggestion.coordinates.lng = 0.0

        // 2) create HotelSearchParams from suggestionv4 + dates
        val hotelSearchParams = createHotelSearchParamsForSuggestion(suggestion)

        // 3) call handleNavigationDeepLink
        handlerUnderTest.handleNavigationViaDeepLink(hotelSearchParams)

        // 4) verify
        Assert.assertEquals(0, testCurrentLocationSearchObserver.onNextEvents.size)
        Assert.assertEquals(1, testSuggestionLookupObserver.onNextEvents.size)
        Assert.assertEquals("Portland, ME", testSuggestionLookupObserver.onNextEvents[0].first)
        Assert.assertEquals(0, testDeepLinkSearchObserver.onNextEvents.size)
        testSearchSuggestionObserver.assertReceivedOnNext(listOf(hotelSearchParams.suggestion))
	}

	@Test fun handleLatLonDeepLink() {
        val handlerUnderTest = HotelDeepLinkHandler(RuntimeEnvironment.application, testDeepLinkSearchObserver, testSuggestionLookupObserver, testCurrentLocationSearchObserver, hotelPresenter, testSearchSuggestionObserver)

        // 1) create suggestionv4 with regionName
        val suggestion = SuggestionV4()
        suggestion.type = com.expedia.bookings.data.HotelSearchParams.SearchType.ADDRESS.name
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "(44.761827,-85.600372)"
        suggestion.regionNames.shortName = "(44.761827,-85.600372)"
        suggestion.coordinates = SuggestionV4.LatLng()
        suggestion.coordinates.lat = 44.761827
        suggestion.coordinates.lng = -85.600372

        // 2) create HotelSearchParams from suggestionv4 + dates
        val hotelSearchParams = createHotelSearchParamsForSuggestion(suggestion)

        // 3) call handleNavigateionDeepLink
        handlerUnderTest.handleNavigationViaDeepLink(hotelSearchParams)

        // 4) verify
        Mockito.verify(hotelPresenter).setDefaultTransition(HotelActivity.Screen.RESULTS)
        Assert.assertEquals(0, testCurrentLocationSearchObserver.onNextEvents.size)
        Assert.assertEquals(0, testSuggestionLookupObserver.onNextEvents.size)
        testDeepLinkSearchObserver.assertReceivedOnNext(listOf(hotelSearchParams))
        testSearchSuggestionObserver.assertReceivedOnNext(listOf(hotelSearchParams.suggestion))
	}

	@Test fun handleAirAttachDeepLink() {
        val handlerUnderTest = HotelDeepLinkHandler(RuntimeEnvironment.application, testDeepLinkSearchObserver, testSuggestionLookupObserver, testCurrentLocationSearchObserver, hotelPresenter, testSearchSuggestionObserver)

        // 1) create suggestionv4 with regionName and gaiaId
        val suggestion = SuggestionV4()
        suggestion.type = com.expedia.bookings.data.HotelSearchParams.SearchType.FREEFORM.name
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "La Paz"
        suggestion.regionNames.shortName = "La Paz"
        suggestion.gaiaId = "5678"

        // 2) create HotelSearchParams from suggestionv4 + dates
        val hotelSearchParams = createHotelSearchParamsForSuggestion(suggestion)

        // 3) call handleNavigateionDeepLink
        handlerUnderTest.handleNavigationViaDeepLink(hotelSearchParams)

        // 4) verify
        Mockito.verify(hotelPresenter).setDefaultTransition(HotelActivity.Screen.RESULTS)
        Assert.assertEquals(0, testCurrentLocationSearchObserver.onNextEvents.size)
        Assert.assertEquals(0, testSuggestionLookupObserver.onNextEvents.size)
        testDeepLinkSearchObserver.assertReceivedOnNext(listOf(hotelSearchParams))
        testSearchSuggestionObserver.assertReceivedOnNext(listOf(hotelSearchParams.suggestion))
	}

    private fun createHotelSearchParamsForSuggestion(suggestion: SuggestionV4): HotelSearchParams {
        val checkInDate = LocalDate()
        val checkOutDate = checkInDate.plusDays(1)
        return HotelSearchParams.Builder(10, 500)
                .destination(suggestion)
                .adults(1)
                .startDate(checkInDate)
                .endDate(checkOutDate).build() as HotelSearchParams
    }
}