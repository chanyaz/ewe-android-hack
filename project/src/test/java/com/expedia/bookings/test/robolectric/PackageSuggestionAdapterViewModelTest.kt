package com.expedia.bookings.test.robolectric

import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.vm.packages.PackageSuggestionAdapterViewModel
import okhttp3.OkHttpClient
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import io.reactivex.schedulers.Schedulers
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageSuggestionAdapterViewModelTest {

    @Test
    fun packagesShouldShowOnlyAirportInNearbySuggestions() {
<<<<<<< HEAD
        val suggestionV4Service = SuggestionV4Services("http://localhost:", "http://localhost:", OkHttpClient(), MockInterceptor(), MockInterceptor(), MockInterceptor(), Schedulers.immediate(), Schedulers.immediate())
=======
        val suggestionV4Service = SuggestionV4Services("http://localhost:", "http://localhost:", OkHttpClient(), MockInterceptor(), MockInterceptor(), Schedulers.trampoline(), Schedulers.trampoline())
>>>>>>> 7df61dae81... WIP
        val viewModel = PackageSuggestionAdapterViewModel(RuntimeEnvironment.application, suggestionV4Service, true, null)
        assertTrue(viewModel.shouldShowOnlyAirportNearbySuggestions())
    }
}