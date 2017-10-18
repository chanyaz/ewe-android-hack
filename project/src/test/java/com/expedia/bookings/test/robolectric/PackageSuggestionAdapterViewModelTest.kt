package com.expedia.bookings.test.robolectric

import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.vm.packages.PackageSuggestionAdapterViewModel
import okhttp3.OkHttpClient
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.schedulers.Schedulers
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageSuggestionAdapterViewModelTest {

    @Test
    fun packagesShouldShowOnlyAirportInNearbySuggestions() {
        val suggestionV4Service = SuggestionV4Services("http://localhost:", "http://localhost:", OkHttpClient(), MockInterceptor(), MockInterceptor(), MockInterceptor(), Schedulers.immediate(), Schedulers.immediate())
        val viewModel = PackageSuggestionAdapterViewModel(RuntimeEnvironment.application, suggestionV4Service, true, null)
        assertTrue(viewModel.shouldShowOnlyAirportNearbySuggestions())
    }
}