package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.vm.packages.PackageSuggestionAdapterViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.Observable
import rx.Observer
import rx.Subscription
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageSuggestionAdapterViewModelTest {

    @Test
    fun packagesShouldShowOnlyAirportInNearbySuggestions() {
        val mockSuggestionV4Services = MockSuggestionV4Services()
        val viewModel = PackageSuggestionAdapterViewModel(RuntimeEnvironment.application, mockSuggestionV4Services, true, null)
        assertTrue(viewModel.shouldShowOnlyAirportNearbySuggestions())
    }

    class MockSuggestionV4Services : ISuggestionV4Services {
        override fun getLxSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, disablePOI: Boolean): Subscription {
            TODO("not implemented: getLxSuggestionsV4")
        }

        override fun getHotelSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, sameAsWeb: Boolean, guid: String?): Subscription {
            TODO("not implemented: getHotelSuggestionsV4")
        }

        override fun suggestNearbyGaia(lat: Double, lng: Double, sortType: String, lob: String, locale: String, siteId: Int, isMISForRealWorldEnabled: Boolean): Observable<MutableList<GaiaSuggestion>> {
            TODO("not implemented: suggestNearbyGaia")
        }

        override fun suggestPackagesV4(query: String, isDest: Boolean, isMISForRealWorldEnabled: Boolean, observer: Observer<List<SuggestionV4>>, guid: String?): Subscription {
            TODO("not implemented: suggestPackagesV4")
        }

        override fun suggestRailsV4(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>): Subscription {
            TODO("not implemented: suggestRailsV4")
        }

        override fun getAirports(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, guid: String): Subscription {
            TODO("not implemented: getAirports")
        }
    }
}
