package com.expedia.vm.packages

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.SearchParamsHistoryUtil
import com.expedia.bookings.utils.Ui
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class PackageSearchViewModelTest {

    val context = RuntimeEnvironment.application
    lateinit var sut: PackageSearchViewModel

    @Test
    fun testRetainPackageSearchParams() {
        givenDefaultTravelerComponent()
        createSystemUnderTest()
        sut.performSearchObserver.onNext(getDummyPackageSearchParams())
        val testSubscriber = TestSubscriber.create<Unit>()
        SearchParamsHistoryUtil.loadPreviousFlightSearchParams(RuntimeEnvironment.application, { loadedParams ->
            testSubscriber.onNext(Unit)
            assertNotNull(loadedParams)
        })
        testSubscriber.awaitValueCount(1, 2, TimeUnit.SECONDS)
    }

    private fun getDummyPackageSearchParams(): PackageSearchParams {
        val origin = SuggestionV4()
        val destination = SuggestionV4()
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(2)

        val paramsBuilder = PackageSearchParams.Builder(26, 369)
                .origin(origin)
                .destination(destination)
                .startDate(startDate)
                .adults(1)
                .children(listOf(1,2,3))
                .endDate(endDate) as PackageSearchParams.Builder

        return paramsBuilder.build()
    }

    @Test
    fun testPackageSearchDayWithDate() {
        givenDefaultTravelerComponent()
        createSystemUnderTest()
        val currentLocale = Locale.getDefault()

        Locale.setDefault(Locale.US)
        val startDate = LocalDate(2017, 7, 17)
        val endDate = LocalDate(2017, 7, 25)
        var expectedStartDate = "Mon, Jul 17"
        var expectedEndDate = "Tue, Jul 25"
        var expectedNumberOfNights = JodaUtils.daysBetween(startDate, endDate)

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
        var expectedStartDate = "월, 7월 17"
        var expectedEndDate = "화, 7월 25"

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
        var expectedStartDate = "月, 7 17"
        var expectedEndDate = "火, 7 25"
        var expectedNumberOfNights = JodaUtils.daysBetween(startDate, endDate)

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
}