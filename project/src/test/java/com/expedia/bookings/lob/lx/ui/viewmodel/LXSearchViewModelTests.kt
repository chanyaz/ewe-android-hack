package com.expedia.bookings.lob.lx.ui.viewmodel

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.lob.lx.ui.viewmodel.LXSearchViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LXSearchViewModelTests {
    var vm: LXSearchViewModel by Delegates.notNull()
    var activity : Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_LX)
        Ui.getApplication(activity).defaultLXComponents()
        vm = LXSearchViewModel(activity)

    }

    @Test
    fun selectDatesAndSearch() {
        val testSubscriber = TestSubscriber<LxSearchParams>()
        val expected = arrayListOf<LxSearchParams>()
        val suggestion = getDummySuggestion()

        vm.searchParamsObservable.subscribe(testSubscriber)

        // Selecting a location suggestion for search, as it is a necessary parameter for search
        vm.destinationLocationObserver.onNext(suggestion)

        // Selecting only start date should search with end date as the next day
        vm.datesUpdated(LocalDate.now(), null)
        vm.searchObserver.onNext(Unit)
        expected.add(LxSearchParams.Builder()
                .destination(suggestion)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14)).build() as LxSearchParams)

        // Select both start date and end date and search
        vm.datesUpdated(LocalDate.now(), LocalDate.now().plusDays(3))
        vm.searchObserver.onNext(Unit)
        expected.add(LxSearchParams.Builder()
                .destination(suggestion)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3)).build() as LxSearchParams)

        assertEquals(testSubscriber.onNextEvents[0].activityStartDate, expected[0].activityStartDate)
        assertEquals(testSubscriber.onNextEvents[1].activityEndDate, expected[1].activityEndDate)
    }

    @Test
    fun selectErrorObservablesForLXSearch() {
        val destinationErrorTestSubscriber = TestSubscriber<Unit>()
        val dateErrorTestSubscriber = TestSubscriber<Unit>()
        val searchParamsTestSubscriber = TestSubscriber<LxSearchParams>()


        val suggestion = getDummySuggestion()

        vm.errorNoDestinationObservable.subscribe(destinationErrorTestSubscriber)
        vm.errorNoDatesObservable.subscribe(dateErrorTestSubscriber)
        vm.searchParamsObservable.subscribe(searchParamsTestSubscriber)

        //Neither Destination nor date is selected
        vm.searchObserver.onNext(Unit)

        //Date is not selected yet
        vm.destinationLocationObserver.onNext(suggestion)
        vm.searchObserver.onNext(Unit)

        destinationErrorTestSubscriber.assertValueCount(1)
        dateErrorTestSubscriber.assertValueCount(1)
        searchParamsTestSubscriber.assertValueCount(0)
    }


    @Test
    fun testComputeDateInstructionText(){
        //When user has not selected the start date
        assertEquals("Select date",vm.getDateInstructionText(null, null))

        //When user has selected the start date
        val startDate = LocalDate.now();
        assertEquals(LocaleBasedDateFormatUtils.localDateToMMMd(startDate),vm.getDateInstructionText(startDate, null))
    }

    @Test
    fun testStartDate() {
        assertEquals(LocalDate.now(), vm.getFirstAvailableDate(), "Start Date is Today")
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        return suggestion
    }
}
