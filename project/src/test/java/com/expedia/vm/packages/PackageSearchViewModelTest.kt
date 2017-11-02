package com.expedia.vm.packages

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.SearchParamsHistoryUtil
import com.expedia.bookings.utils.Ui
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class PackageSearchViewModelTest {

    val context = RuntimeEnvironment.application
    lateinit var sut: PackageSearchViewModel

    @Test
    fun testRetainPackageSearchParams() {
        givenDefaultTravelerComponent()
        createSystemUnderTest()
        sut.performSearchObserver.onNext(getDummyPackageSearchParams(0, 2))
        val testSubscriber = TestObserver.create<Unit>()
        SearchParamsHistoryUtil.loadPreviousFlightSearchParams(RuntimeEnvironment.application, { loadedParams ->
            testSubscriber.onNext(Unit)
            assertNotNull(loadedParams)
        })
        testSubscriber.awaitValueCount(1, 2, TimeUnit.SECONDS)
    }

    @Test
    fun testSearchDatesRetained() {
        givenDefaultTravelerComponent()
        createSystemUnderTest()
        sut.performSearchObserver.onNext(getDummyPackageSearchParams(0, 2))
        sut.previousSearchParamsObservable.onNext(getDummyPackageSearchParams(0, 2))
        val startDate = LocalDate.now()
        val endDate = LocalDate.now().plusDays(2)
        val expectedStartDate = DateFormatUtils.formatLocalDateToEEEMMMdBasedOnLocale(startDate)
        val expectedEndDate = DateFormatUtils.formatLocalDateToEEEMMMdBasedOnLocale(endDate)
        val expectedNumberOfNights = JodaUtils.daysBetween(startDate, endDate)
        assertEquals("$expectedStartDate  -  $expectedEndDate ($expectedNumberOfNights nights)", sut.dateTextObservable.value)
    }

    @Test
    fun testSearchDatesInPastNotRetained() {
        givenDefaultTravelerComponent()
        createSystemUnderTest()
        sut.performSearchObserver.onNext(getDummyPackageSearchParams(-1, -2))
        sut.previousSearchParamsObservable.onNext(getDummyPackageSearchParams(-1, -2))
        val startDate = LocalDate.now().minusDays(1)
        val endDate = LocalDate.now().minusDays(2)
        val expectedStartDate = DateFormatUtils.formatLocalDateToEEEMMMdBasedOnLocale(startDate)
        val expectedEndDate = DateFormatUtils.formatLocalDateToEEEMMMdBasedOnLocale(endDate)
        val expectedNumberOfNights = JodaUtils.daysBetween(startDate, endDate)
        assertNotEquals("$expectedStartDate  -  $expectedEndDate ($expectedNumberOfNights nights)", sut.dateTextObservable.value)
        assertNull(sut.dateTextObservable.value)
    }

    @Test
    fun testPackageSearchDayWithDate() {
        givenDefaultTravelerComponent()
        createSystemUnderTest()
        val currentLocale = Locale.getDefault()

        Locale.setDefault(Locale.US)
        val startDate = LocalDate(2017, 7, 17)
        val endDate = LocalDate(2017, 7, 25)
        val expectedStartDate = "Mon, Jul 17"
        val expectedEndDate = "Tue, Jul 25"
        val expectedNumberOfNights = JodaUtils.daysBetween(startDate, endDate)

        sut.datesUpdated(startDate, endDate)
        assertEquals("$expectedStartDate  -  $expectedEndDate ($expectedNumberOfNights nights)", sut.dateTextObservable.value)

        val newStartDate = startDate.plusDays(20)
        val expectedNewStartDate = "Sun, Aug 6"

        sut.datesUpdated(newStartDate, null)
        assertEquals("$expectedNewStartDate – Select return date", sut.dateTextObservable.value)

        sut.datesUpdated(null, null)
        assertEquals("Select dates", sut.dateTextObservable.value)

        // Reset it back
        Locale.setDefault(currentLocale)
    }

    @Test
    @Config(qualifiers = "ko")
    fun testPackageSearchDayWithDateKR() {
        val currentLocale = Locale.getDefault()
        givenDefaultTravelerComponent()
        createSystemUnderTest()
        Locale.setDefault(Locale.KOREAN)
        val startDate = LocalDate(2017, 7, 17)
        val endDate = LocalDate(2017, 7, 25)
        val expectedStartDate = "월, 7월 17"
        val expectedEndDate = "화, 7월 25"

        sut.datesUpdated(startDate, endDate)
        assertEquals("$expectedStartDate  -  $expectedEndDate (8박)", sut.dateTextObservable.value)
        // Reset it back
        Locale.setDefault(currentLocale)
    }

    @Test
    @Config(qualifiers = "ja")
    fun testPackageSearchDayWithDateJP() {
        val currentLocale = Locale.getDefault()
        givenDefaultTravelerComponent()
        createSystemUnderTest()
        Locale.setDefault(Locale.JAPAN)
        val startDate = LocalDate(2017, 7, 17)
        val endDate = LocalDate(2017, 7, 25)
        val expectedStartDate = "月, 7 17"
        val expectedEndDate = "火, 7 25"
        val expectedNumberOfNights = JodaUtils.daysBetween(startDate, endDate)

        sut.datesUpdated(startDate, endDate)
        assertEquals("$expectedStartDate ～ $expectedEndDate ($expectedNumberOfNights 泊)", sut.dateTextObservable.value)
        // Reset it back
        Locale.setDefault(currentLocale)
    }

    private fun givenDefaultTravelerComponent() {
        Ui.getApplication(context).defaultTravelerComponent()
    }

    private fun createSystemUnderTest() {
        sut = PackageSearchViewModel(context)
    }


    private fun getDummyPackageSearchParams(startDateOffset: Int, endDateOffset: Int): PackageSearchParams {
        val origin = getDummySuggestion()
        val destination = getDummySuggestion()
        val startDate = LocalDate.now().plusDays(startDateOffset)
        val endDate = startDate.plusDays(endDateOffset)

        val paramsBuilder = PackageSearchParams.Builder(26, 369)
                .origin(origin)
                .destination(destination)
                .startDate(startDate)
                .adults(1)
                .children(listOf(1,2,3))
                .endDate(endDate) as PackageSearchParams.Builder

        return paramsBuilder.build()
    }
    
    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "London"
        suggestion.regionNames.fullName = "London"
        suggestion.regionNames.shortName = "LHR"
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = "happy"
        suggestion.hierarchyInfo!!.airport!!.multicity = "happy"
        return suggestion
    }
}