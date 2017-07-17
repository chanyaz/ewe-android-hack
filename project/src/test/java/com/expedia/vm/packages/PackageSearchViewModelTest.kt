package com.expedia.vm.packages

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.SearchParamsHistoryUtil
import com.expedia.bookings.utils.Ui
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class PackageSearchViewModelTest {

    val context = RuntimeEnvironment.application
    lateinit var sut: PackageSearchViewModel

    @Test
    fun test() {
        SettingUtils.save(context, R.string.preference_packages_retain_search_params, true)
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

    private fun givenDefaultTravelerComponent() {
        Ui.getApplication(context).defaultTravelerComponent()
    }

    private fun createSystemUnderTest() {
        sut = PackageSearchViewModel(context)
    }
}