package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.util.Optional
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageToolbarViewModelTest {

    lateinit var context: Context
    lateinit var currentDate: LocalDate
    lateinit var sut: PackageToolbarViewModel

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = PackageToolbarViewModel(context)

        sut.refreshToolBar.onNext(true)
        sut.country.onNext(Optional("India"))
        sut.airport.onNext(Optional("BLR"))

        currentDate = LocalDate()
        sut.date.onNext(currentDate)
    }

    @Test
    fun testHappyWithMultipleTravelers() {
        val titleSubscriber = TestObserver<String>()
        sut.titleSubject.subscribe(titleSubscriber)

        val subtitleSubscriber = TestObserver<CharSequence>()
        sut.subtitleSubject.subscribe(subtitleSubscriber)

        val menuVisibilitySubscriber = TestObserver<Boolean>()
        sut.menuVisibilitySubject.subscribe(menuVisibilitySubscriber)

        val suggestion = getSuggestionV4(true, true, true)
        var numberOfTravelers = 3

        sut.regionNames.onNext(Optional(suggestion.regionNames))
        sut.travelers.onNext(numberOfTravelers)
        sut.isOutboundSearch.onNext(true)

        assertTrue(titleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS))
        assertEquals(1, titleSubscriber.valueCount())

        var expectedResult = "Outbound to Bengaluru, India (BLR)"
        assertEquals(expectedResult, titleSubscriber.values()[0])

        assertTrue(subtitleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS))
        assertEquals(1, subtitleSubscriber.valueCount())

        expectedResult = LocaleBasedDateFormatUtils.dateTimeToEEEMMMddyyyy(currentDate) + ", " + numberOfTravelers + " travelers"
        assertEquals(expectedResult, subtitleSubscriber.values()[0])

        assertTrue(menuVisibilitySubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS))
        assertEquals(true, menuVisibilitySubscriber.values()[0])
    }

    @Test
    fun testHappyWithSingleTraveler() {
        val titleSubscriber = TestObserver<String>()
        sut.titleSubject.subscribe(titleSubscriber)

        val subtitleSubscriber = TestObserver<CharSequence>()
        sut.subtitleSubject.subscribe(subtitleSubscriber)

        val suggestion = getSuggestionV4(true, true, true)
        val numberOfTravelers = 1

        sut.regionNames.onNext(Optional(suggestion.regionNames))
        sut.travelers.onNext(1)
        sut.isOutboundSearch.onNext(true)

        titleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(1, titleSubscriber.valueCount())

        var expectedResult = "Outbound to Bengaluru, India (BLR)"
        assertEquals(expectedResult, titleSubscriber.values()[0])

        subtitleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(1, subtitleSubscriber.valueCount())

        expectedResult = LocaleBasedDateFormatUtils.dateTimeToEEEMMMddyyyy(currentDate) + ", " + numberOfTravelers + " traveler"
        assertEquals(expectedResult, subtitleSubscriber.values()[0])
    }

    @Test
    fun testHappyWithSingleTravelerInbound() {
        val titleSubscriber = TestObserver<String>()
        sut.titleSubject.subscribe(titleSubscriber)

        val subtitleSubscriber = TestObserver<CharSequence>()
        sut.subtitleSubject.subscribe(subtitleSubscriber)

        val suggestion = getSuggestionV4(true, true, true)
        val numberOfTravelers = 1

        sut.regionNames.onNext(Optional(suggestion.regionNames))
        sut.travelers.onNext(1)
        sut.isOutboundSearch.onNext(false)

        titleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(1, titleSubscriber.valueCount())

        var expectedResult = "Inbound to Bengaluru, India (BLR)"
        assertEquals(expectedResult, titleSubscriber.values()[0])

        subtitleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(1, subtitleSubscriber.valueCount())

        expectedResult = LocaleBasedDateFormatUtils.dateTimeToEEEMMMddyyyy(currentDate) + ", " + numberOfTravelers + " traveler"
        assertEquals(expectedResult, subtitleSubscriber.values()[0])
    }

    @Test
    fun testWithRegionNamesWithNoShortName() {
        val titleSubscriber = TestObserver<String>()
        sut.titleSubject.subscribe(titleSubscriber)

        val subtitleSubscriber = TestObserver<CharSequence>()
        sut.subtitleSubject.subscribe(subtitleSubscriber)

        val suggestion = getSuggestionV4(true, true, false)

        sut.regionNames.onNext(Optional(suggestion.regionNames))
        sut.travelers.onNext(3)
        sut.isOutboundSearch.onNext(true)

        assertTrue(titleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS))
        assertEquals(1, titleSubscriber.valueCount())

        assertTrue(subtitleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS))
        assertEquals(1, subtitleSubscriber.valueCount())
    }

    @Test
    fun testWithRegionNamesWithOnlyLastSearchName() {
        val titleSubscriber = TestObserver<String>()
        sut.titleSubject.subscribe(titleSubscriber)

        val subtitleSubscriber = TestObserver<CharSequence>()
        sut.subtitleSubject.subscribe(subtitleSubscriber)

        val suggestion = getSuggestionV4(true, true, false, false, true)

        sut.regionNames.onNext(Optional(suggestion.regionNames))
        sut.travelers.onNext(3)
        sut.isOutboundSearch.onNext(true)

        assertTrue(titleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS))
        assertEquals(1, titleSubscriber.valueCount())

        assertTrue(subtitleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS))
        assertEquals(1, subtitleSubscriber.valueCount())
    }

    @Test
    fun testWithRegionNamesWithOnlyFullName() {
        val titleSubscriber = TestObserver<String>()
        sut.titleSubject.subscribe(titleSubscriber)

        val subtitleSubscriber = TestObserver<CharSequence>()
        sut.subtitleSubject.subscribe(subtitleSubscriber)

        val suggestion = getSuggestionV4(true, false, false, true)

        sut.regionNames.onNext(Optional(suggestion.regionNames))
        sut.travelers.onNext(3)
        sut.isOutboundSearch.onNext(true)

        assertTrue(titleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS))
        assertEquals(1, titleSubscriber.valueCount())

        assertTrue(subtitleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS))
        assertEquals(1, subtitleSubscriber.valueCount())
    }

    @Test
    fun testWithNullRegionNames() {
        val titleSubscriber = TestObserver<String>()
        sut.titleSubject.subscribe(titleSubscriber)

        val subtitleSubscriber = TestObserver<CharSequence>()
        sut.subtitleSubject.subscribe(subtitleSubscriber)

        val suggestion = getSuggestionV4()

        sut.regionNames.onNext(Optional(suggestion.regionNames))
        sut.travelers.onNext(3)
        sut.isOutboundSearch.onNext(true)

        assertTrue(titleSubscriber.awaitValueCount(0, 1, TimeUnit.SECONDS))
        assertEquals(0, titleSubscriber.valueCount())

        assertTrue(subtitleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS))
        assertEquals(1, subtitleSubscriber.valueCount())
    }

    @Test
    fun testWithRegionNamesButNullAirport() {
        val titleSubscriber = TestObserver<String>()
        sut.titleSubject.subscribe(titleSubscriber)

        val subtitleSubscriber = TestObserver<CharSequence>()
        sut.subtitleSubject.subscribe(subtitleSubscriber)

        val suggestion = getSuggestionV4(true, true)

        sut.airport.onNext(Optional(null))
        sut.regionNames.onNext(Optional(suggestion.regionNames))
        sut.travelers.onNext(3)
        sut.isOutboundSearch.onNext(true)

        assertTrue(titleSubscriber.awaitValueCount(0, 1, TimeUnit.SECONDS))
        assertEquals(0, titleSubscriber.valueCount())

        assertTrue(subtitleSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS))
        assertEquals(1, subtitleSubscriber.valueCount())
    }

    private fun getSuggestionV4(withRegionNames: Boolean = false, withDisplayName: Boolean = false, withShortName: Boolean = false, withFullName: Boolean = false, withLastSearchName: Boolean = false): SuggestionV4 {
        val suggestion = SuggestionV4()
        if (withRegionNames) {
            suggestion.regionNames = SuggestionV4.RegionNames()
            suggestion.regionNames.displayName = if (withDisplayName) "Bengaluru, India (BLR - Kempegowda Intl.)<I><B> near </B></I>Bangalore Palace, Bengaluru, India" else ""
            suggestion.regionNames.lastSearchName = if (withLastSearchName) "Bengaluru, India (BLR - Kempegowda Intl.)" else null
            suggestion.regionNames.shortName = if (withShortName) "Bengaluru, India (BLR - Kempegowda Intl.)" else null
            suggestion.regionNames.fullName = if (withFullName) "Bengaluru, India" else null
        }
        return suggestion
    }
}
