package com.expedia.vm

import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.data.SuggestionDataItem
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.builder.TestGaiaSuggestionBuilder
import com.expedia.testutils.builder.TestSuggestionV4Builder
import com.mobiata.android.util.SettingUtils
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyDouble
import org.mockito.Matchers.anyString
import org.mockito.Matchers.anyInt
import org.mockito.Matchers.anyBoolean
import org.mockito.Mockito
import org.mockito.Mockito.`when` as whenever
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelSuggestionAdapterViewModelTest {
    private val context = RuntimeEnvironment.application
    private val mockService = Mockito.mock(ISuggestionV4Services::class.java)
    private val mockGaiaObservable = PublishSubject.create<MutableList<GaiaSuggestion>>()
    private val mockLocationObservable = PublishSubject.create<Location>()
    private val viewModel = HotelSuggestionAdapterViewModel(context, mockService, mockLocationObservable)
    private val testSubscriber = TestObserver<List<SuggestionDataItem>>()

    @Before
    fun setUp() {
        whenever(mockService.suggestNearbyGaia(anyDouble(), anyDouble(), anyString(), anyString(),
                anyString(), anyInt(), anyBoolean())).thenReturn(mockGaiaObservable)
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
        mockGaiaObservable.onNext(mutableListOf(getGaiaSuggestion(expectedGaiaName)))
        viewModel.queryObserver.onNext("")

        assertEquals(2, testSubscriber.valueCount())
        val outputSuggestions = testSubscriber.values()[1]
        val first = outputSuggestions[0] as SuggestionDataItem.CurrentLocation
        val second = outputSuggestions[1] as SuggestionDataItem.Label
        val third = outputSuggestions[2] as SuggestionDataItem.V4

        assertEquals(context.getString(R.string.current_location),
                first.suggestion.regionNames.displayName)
        assertEquals(context.getString(R.string.nearby_locations), second.suggestionlabel)
        assertEquals(expectedGaiaName, third.suggestion.regionNames.displayName)
    }

    @Test
    fun testGaiaPlusRecentSearches() {
        SettingUtils.save(context, R.string.preference_user_search_history, true)
        val expectedGaiaName = "Hello"
        val recentSearchId = "12345"
        viewModel.suggestionItemsSubject.subscribe(testSubscriber)

        viewModel.setUserSearchHistory(listOf(getSuggestion(recentSearchId)))
        mockLocationObservable.onNext(Mockito.mock(Location::class.java))
        mockGaiaObservable.onNext(mutableListOf(getGaiaSuggestion(expectedGaiaName)))

        val outputSuggestions = testSubscriber.values()[0]
        val first = outputSuggestions[0] as SuggestionDataItem.CurrentLocation
        val second = outputSuggestions[1] as SuggestionDataItem.Label
        val third = outputSuggestions[2] as SuggestionDataItem.V4
        val four = outputSuggestions[3] as SuggestionDataItem.Label
        val five = outputSuggestions[4] as SuggestionDataItem.V4

        assertEquals(context.getString(R.string.current_location), first.suggestion.regionNames.displayName)
        assertEquals(context.getString(R.string.nearby_locations), second.suggestionlabel)
        assertEquals(expectedGaiaName, third.suggestion.regionNames.displayName)
        assertEquals("Travel Graph Searches", four.suggestionlabel)
        assertEquals(recentSearchId, five.suggestion.gaiaId)
    }

    @Test
    fun testRawQuery() {
        val dumbObserver = viewModel.generateSuggestionServiceCallback()
        viewModel.suggestionItemsSubject.subscribe(testSubscriber)

        viewModel.queryObserver.onNext("ORD")
        dumbObserver.onNext(emptyList())

        val output = testSubscriber.values()[0] as List<SuggestionDataItem.V4>
        assertEquals("\"ORD\"", output[0].suggestion.regionNames.displayName)
        assertEquals(1, output.size)
    }

    @Test
    fun testSimpleSuggestion() {
        val expectedId = "987"
        val dumbObserver = viewModel.generateSuggestionServiceCallback()
        viewModel.suggestionItemsSubject.subscribe(testSubscriber)

        dumbObserver.onNext(listOf(getSuggestion(expectedId)))

        val output = testSubscriber.values()[0] as List<SuggestionDataItem.V4>
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

        val output = testSubscriber.values()[0] as List<SuggestionDataItem.V4>
        assertEquals("\"ORD\"", output[0].suggestion.regionNames.displayName)
        assertEquals(expectedId, output[1].suggestion.gaiaId)
    }

    @Test
    fun testNearByCurrentLocation() {
        val expectedGaiaName = "Hello"
        mockLocationObservable.onNext(Mockito.mock(Location::class.java))
        viewModel.suggestionItemsSubject.subscribe(testSubscriber)

        mockGaiaObservable.onNext(mutableListOf(getGaiaSuggestion(expectedGaiaName)))

        val outputSuggestions = testSubscriber.values()[0]
        assertEquals(3, outputSuggestions.size, "FAILURE: Expected multiple results, current location should clone first gaia result not replace it")

        val first = outputSuggestions[0] as SuggestionDataItem.CurrentLocation
        val second = outputSuggestions[1] as SuggestionDataItem.Label
        val third = outputSuggestions[2] as SuggestionDataItem.V4

        assertEquals(context.getString(R.string.current_location), first.suggestion.regionNames.displayName)
        assertEquals(SuggestionV4.IconType.CURRENT_LOCATION_ICON, first.suggestion.iconType)
        assertEquals(context.getString(R.string.nearby_locations), second.suggestionlabel)
        assertEquals(expectedGaiaName, third.suggestion.regionNames.displayName)
        assertEquals(SuggestionV4.IconType.CURRENT_LOCATION_ICON, third.suggestion.iconType)
    }

    @Test
    fun testGaiaThenEss() {
        val expectedESSId = "987"
        val expectedGaiaName = "Hello"
        val dumbObserver = viewModel.generateSuggestionServiceCallback()
        viewModel.suggestionItemsSubject.subscribe(testSubscriber)

        mockLocationObservable.onNext(Mockito.mock(Location::class.java))
        mockGaiaObservable.onNext(mutableListOf(getGaiaSuggestion(expectedGaiaName)))
        dumbObserver.onNext(listOf(getSuggestion(expectedESSId)))

        assertEquals(2, testSubscriber.valueCount())
        val output = testSubscriber.values()[1] as List<SuggestionDataItem.V4>
        assertEquals(1, output.size, "FAILURE: Expected the only results shown are ESS")
        assertEquals(expectedESSId, output[0].suggestion.gaiaId)
    }

    @Test
    fun testVerifyLabels() {
        assertEquals(context.getString(R.string.nearby_locations), viewModel.getCurrentLocationLabel())
        assertEquals(context.getString(R.string.suggestion_label_recent_search), viewModel.getPastSuggestionsLabel())
    }

    private fun getSuggestion(id: String = "123") : SuggestionV4 {
       return TestSuggestionV4Builder().gaiaId(id).build()
    }

    private fun getGaiaSuggestion(name: String) : GaiaSuggestion {
        return TestGaiaSuggestionBuilder().gaiaId("123").type("type")
                .localizedName(1, name, name, name)
                .country("America", "usa")
                .position(1.0,1.0).build()
    }
}
