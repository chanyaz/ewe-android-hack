package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.HotelSearchParams.SearchType
import com.expedia.bookings.hotel.deeplink.HotelDeepLinkHandler
import com.expedia.bookings.hotel.deeplink.HotelLandingPage
import com.expedia.bookings.hotel.util.HotelSuggestionManager
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.testutils.builder.TestSuggestionV4Builder
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class HotelDeepLinkHandlerTest {
    val testGenericSearchSubscriber = TestSubscriber.create<HotelSearchParams>()
    val testHotelIdToDetailsSubscriber = TestSubscriber.create<HotelSearchParams>()
    val testHotelIdToResultsSubscriber = TestSubscriber.create<HotelSearchParams>()
    val testErrorSearchSubscriber = TestSubscriber.create<Unit>()

    lateinit var handlerUnderTest: HotelDeepLinkHandler
    private val testSuggestionManager = TestHotelSuggestionManager(Mockito.mock(SuggestionV4Services::class.java))

    private val expectedCurrentLocationText = RuntimeEnvironment.application.getString(R.string.current_location)

    @Before fun setup() {
        handlerUnderTest = HotelDeepLinkHandler(RuntimeEnvironment.application, testSuggestionManager)
        handlerUnderTest.hotelSearchDeepLinkSubject.subscribe(testGenericSearchSubscriber)
        handlerUnderTest.hotelIdToDetailsSubject.subscribe(testHotelIdToDetailsSubscriber)
        handlerUnderTest.hotelIdToResultsSubject.subscribe(testHotelIdToResultsSubscriber)
        handlerUnderTest.deepLinkInvalidSubject.subscribe(testErrorSearchSubscriber)
    }

    @Test fun handleCurrentLocationDeepLink() {
        val suggestion = TestSuggestionV4Builder().type(SearchType.MY_LOCATION.name)
                .coordinates(42.0, -81.0).build()

        val hotelSearchParams = createHotelSearchParamsForSuggestion(suggestion)
        handlerUnderTest.handleNavigationViaDeepLink(hotelSearchParams, null)


        assertNotNull(testGenericSearchSubscriber.onNextEvents[0])
        val returnedSuggestion = testGenericSearchSubscriber.onNextEvents[0].suggestion

        assertEquals(expectedCurrentLocationText, returnedSuggestion.regionNames.displayName)
        assertEquals(expectedCurrentLocationText, returnedSuggestion.regionNames.shortName)

        testHotelIdToDetailsSubscriber.assertNoValues()
        testErrorSearchSubscriber.assertNoValues()
    }

	@Test fun handleSpecificHotelDeepLink_NoLandingPage() {
        val suggestion = TestSuggestionV4Builder().type(SearchType.HOTEL.name)
                .hotelId("1234").gaiaId("1234")
                .regionDisplayName("Hotel 1234").regionShortName("Hotel 1234").build()
        val hotelSearchParams = createHotelSearchParamsForSuggestion(suggestion)

        handlerUnderTest.handleNavigationViaDeepLink(hotelSearchParams, null)

        testGenericSearchSubscriber.assertNoValues()
        testErrorSearchSubscriber.assertNoValues()
        testHotelIdToResultsSubscriber.assertNoValues()
        testHotelIdToDetailsSubscriber.assertReceivedOnNext(listOf(hotelSearchParams))
	}

    @Test fun handleSpecificHotelDeepLink_ResultsLandingPage() {
        val suggestion = TestSuggestionV4Builder().type(SearchType.HOTEL.name)
                .hotelId("1234").gaiaId("1234")
                .regionDisplayName("Hotel 1234").regionShortName("Hotel 1234").build()
        val hotelSearchParams = createHotelSearchParamsForSuggestion(suggestion)

        handlerUnderTest.handleNavigationViaDeepLink(hotelSearchParams, HotelLandingPage.RESULTS)

        testGenericSearchSubscriber.assertNoValues()
        testErrorSearchSubscriber.assertNoValues()
        testHotelIdToDetailsSubscriber.assertNoValues()

        testHotelIdToResultsSubscriber.assertReceivedOnNext(listOf(hotelSearchParams))
    }

	@Test fun handleLocationDeepLink() {
        val suggestion = TestSuggestionV4Builder().type(SearchType.CITY.name)
                .regionDisplayName("Portland, ME").regionShortName("Portland, ME")
                .coordinates(0.0, 0.0).build()

        val hotelSearchParams = createHotelSearchParamsForSuggestion(suggestion)

        handlerUnderTest.handleNavigationViaDeepLink(hotelSearchParams, null)
        testSuggestionManager.suggestionReturnSubject.onNext(suggestion)

        testGenericSearchSubscriber.assertValueCount(1)
        testHotelIdToDetailsSubscriber.assertNoValues()
	}

	@Test fun handleLatLonDeepLink() {
        val suggestion = TestSuggestionV4Builder().type(SearchType.ADDRESS.name)
                .regionDisplayName("(44.761827,-85.600372)")
                .regionShortName("(44.761827,-85.600372)")
                .coordinates(44.761827, -85.600372).build()

        val hotelSearchParams = createHotelSearchParamsForSuggestion(suggestion)

        handlerUnderTest.handleNavigationViaDeepLink(hotelSearchParams, null)

        testHotelIdToDetailsSubscriber.assertNoValues()
        testGenericSearchSubscriber.assertReceivedOnNext(listOf(hotelSearchParams))
	}

	@Test fun handleAirAttachDeepLink() {
        val suggestion = TestSuggestionV4Builder().type(SearchType.FREEFORM.name)
                .regionShortName("La Paz").regionDisplayName("La Paz")
                .gaiaId("5678").build()

        val hotelSearchParams = createHotelSearchParamsForSuggestion(suggestion)
        handlerUnderTest.handleNavigationViaDeepLink(hotelSearchParams, null)
        testSuggestionManager.suggestionReturnSubject.onNext(suggestion)

        testHotelIdToDetailsSubscriber.assertNoValues()
        testGenericSearchSubscriber.assertReceivedOnNext(listOf(hotelSearchParams))
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

    private class TestHotelSuggestionManager(service: SuggestionV4Services) : HotelSuggestionManager(service) {
        override fun fetchHotelSuggestions(context: Context, regionName: String) {
            //do nothing for test
        }
    }
}