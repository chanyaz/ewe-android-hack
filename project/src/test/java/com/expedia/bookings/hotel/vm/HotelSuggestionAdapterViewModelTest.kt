package com.expedia.bookings.hotel.vm

import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.travelgraph.SearchInfo
import com.expedia.bookings.data.travelgraph.TravelerInfo
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.shared.data.SuggestionDataItem
import com.expedia.bookings.shared.util.GaiaNearbyManager
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.testutils.builder.TestSuggestionV4Builder
import com.expedia.vm.HotelSuggestionAdapterViewModel
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelSuggestionAdapterViewModelTest {
    private val context = RuntimeEnvironment.application
    private val mockService = Mockito.mock(ISuggestionV4Services::class.java)
    private val mockLocationObservable = PublishSubject.create<Location>()
    private val viewModel = HotelSuggestionAdapterViewModel(context, mockService, mockLocationObservable)
    private val testSubscriber = TestObserver<List<SuggestionDataItem>>()

    private val mockGaiaManager = TestGaiaManager(mockService)

    @Before
    fun setup() {
        viewModel.gaiaManager = mockGaiaManager
    }

    @Test
    fun testQueryEmpty() {
        viewModel.suggestionItemsSubject.subscribe(testSubscriber)

        viewModel.queryObserver.onNext("")
        testSubscriber.assertValueAt(0, { output -> output.isEmpty() })
    }

    @Test
    fun testQueryEmptyWithGaia() {
        val expectedGaiaName = "Hello"
        viewModel.suggestionItemsSubject.subscribe(testSubscriber)

        mockLocationObservable.onNext(Mockito.mock(Location::class.java))
        mockGaiaManager.suggestionsSubject.onNext(listOf(getSuggestion(name = expectedGaiaName)))
        viewModel.queryObserver.onNext("")

        assertEquals(2, testSubscriber.valueCount())
        val outputSuggestions = testSubscriber.values()[1]
        val first = outputSuggestions[0] as SuggestionDataItem.CurrentLocation
        val second = outputSuggestions[1] as SuggestionDataItem.Label
        val third = outputSuggestions[2] as SuggestionDataItem.SuggestionDropDown

        assertEquals(context.getString(R.string.current_location),
                first.suggestion.regionNames.displayName)
        assertEquals(context.getString(R.string.nearby_locations), second.suggestionLabel)
        assertEquals(expectedGaiaName, third.suggestion.regionNames.displayName)
    }

    @Test
    fun testGaiaPlusRecentSearches() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelRecentSearch)
        val expectedGaiaName = "Hello"
        val recentSearchId = "12345"
        viewModel.suggestionItemsSubject.subscribe(testSubscriber)

        val recentSearch = SearchInfo(getSuggestion(recentSearchId), LocalDate.now(), LocalDate.now(), TravelerInfo())
        viewModel.setUserSearchHistory(listOf(recentSearch))
        mockLocationObservable.onNext(Mockito.mock(Location::class.java))
        mockGaiaManager.suggestionsSubject.onNext(listOf(getSuggestion(name = expectedGaiaName)))

        val outputSuggestions = testSubscriber.values()[1]
        val first = outputSuggestions[0] as SuggestionDataItem.CurrentLocation
        val second = outputSuggestions[1] as SuggestionDataItem.Label
        val third = outputSuggestions[2] as SuggestionDataItem.SearchInfoDropDown
        val four = outputSuggestions[3] as SuggestionDataItem.Label
        val five = outputSuggestions[4] as SuggestionDataItem.SuggestionDropDown

        assertEquals(context.getString(R.string.current_location), first.suggestion.regionNames.displayName)
        assertEquals(context.getString(R.string.suggestion_label_recent_search), second.suggestionLabel)
        assertEquals(recentSearchId, third.searchInfo.destination.gaiaId)
        assertEquals(context.getString(R.string.nearby_locations), four.suggestionLabel)
        assertEquals(expectedGaiaName, five.suggestion.regionNames.displayName)
    }

    @Test
    fun testRawQuery() {
        val dumbObserver = viewModel.generateSuggestionServiceCallback()
        viewModel.suggestionItemsSubject.subscribe(testSubscriber)

        viewModel.queryObserver.onNext("ORD")
        dumbObserver.onNext(emptyList())

        @Suppress("UNCHECKED_CAST")
        val output = testSubscriber.values()[0] as List<SuggestionDataItem.SuggestionDropDown>
        assertEquals("\"ORD\"", output[0].suggestion.regionNames.displayName)
        assertEquals(1, output.size)
    }

    @Test
    fun testSimpleSuggestion() {
        val expectedId = "987"
        val dumbObserver = viewModel.generateSuggestionServiceCallback()
        viewModel.suggestionItemsSubject.subscribe(testSubscriber)

        dumbObserver.onNext(listOf(getSuggestion(expectedId)))

        @Suppress("UNCHECKED_CAST")
        val output = testSubscriber.values()[0] as List<SuggestionDataItem.SuggestionDropDown>
        assertEquals(expectedId, output[0].suggestion.gaiaId)
        assertEquals(1, output.size)
    }

    @Test
    fun testRawBeforeESS() {
        val expectedId = "987"
        val dumbObserver = viewModel.generateSuggestionServiceCallback()
        viewModel.suggestionItemsSubject.subscribe(testSubscriber)

        viewModel.queryObserver.onNext("ORD")
        dumbObserver.onNext(listOf(getSuggestion(expectedId)))

        @Suppress("UNCHECKED_CAST")
        val output = testSubscriber.values()[0] as List<SuggestionDataItem.SuggestionDropDown>
        assertEquals("\"ORD\"", output[0].suggestion.regionNames.displayName)
        assertEquals(expectedId, output[1].suggestion.gaiaId)
    }

    @Test
    fun testNearByCurrentLocation() {
        val expectedGaiaName = "Hello"
        mockLocationObservable.onNext(Mockito.mock(Location::class.java))
        viewModel.suggestionItemsSubject.subscribe(testSubscriber)

        mockGaiaManager.suggestionsSubject.onNext(listOf(getSuggestion(name = expectedGaiaName)))

        val outputSuggestions = testSubscriber.values()[0]
        assertEquals(3, outputSuggestions.size, "FAILURE: Expected multiple results, current location should clone first gaia result not replace it")

        val first = outputSuggestions[0] as SuggestionDataItem.CurrentLocation
        val second = outputSuggestions[1] as SuggestionDataItem.Label
        val third = outputSuggestions[2] as SuggestionDataItem.SuggestionDropDown

        assertEquals(context.getString(R.string.current_location), first.suggestion.regionNames.displayName)
        assertEquals(SuggestionV4.IconType.CURRENT_LOCATION_ICON, first.suggestion.iconType)
        assertEquals(context.getString(R.string.nearby_locations), second.suggestionLabel)
        assertEquals(expectedGaiaName, third.suggestion.regionNames.displayName)
        assertEquals(SuggestionV4.IconType.CURRENT_LOCATION_ICON, third.suggestion.iconType)
    }

    @Test
    fun testGaiaThenEss() {
        val expectedESSId = "987"
        val dumbObserver = viewModel.generateSuggestionServiceCallback()
        viewModel.suggestionItemsSubject.subscribe(testSubscriber)

        mockLocationObservable.onNext(Mockito.mock(Location::class.java))
        mockGaiaManager.suggestionsSubject.onNext(listOf(getSuggestion()))
        dumbObserver.onNext(listOf(getSuggestion(expectedESSId)))

        assertEquals(2, testSubscriber.valueCount())
        @Suppress("UNCHECKED_CAST")
        val output = testSubscriber.values()[1] as List<SuggestionDataItem.SuggestionDropDown>
        assertEquals(1, output.size, "FAILURE: Expected the only results shown are ESS")
        assertEquals(expectedESSId, output[0].suggestion.gaiaId)
    }

    @Test
    fun testVerifyLabels() {
        assertEquals(context.getString(R.string.nearby_locations), viewModel.getCurrentLocationLabel())
        assertEquals(context.getString(R.string.suggestion_label_past_searches), viewModel.getPastSuggestionsLabel())
    }

    private fun getSuggestion(id: String = "123", name: String = "test"): SuggestionV4 {
        return TestSuggestionV4Builder().gaiaId(id).regionDisplayName(name).build()
    }

    private class TestGaiaManager(services: ISuggestionV4Services) : GaiaNearbyManager(services) {
        override fun nearBySuggestions(
            location: Location,
            nearbySortType: String,
            lobString: String,
            misForRealWorldEnabled: Boolean
        ) {
            // do nothing
        }
    }
}
