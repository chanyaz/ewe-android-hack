package com.expedia.bookings.packages.vm

import android.location.Location
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.data.GaiaSuggestionRequest
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageSuggestionAdapterViewModelTest {
    @Test
    fun testLocationLabelsForPackages() {
        val viewModel = PackageSuggestionAdapterViewModel(RuntimeEnvironment.application, MockSuggestionV4Services(), true, null)
        assertEquals("Airports near you", viewModel.getCurrentLocationLabel())
        assertEquals("Recent searches", viewModel.getPastSuggestionsLabel())
    }

    @Test
    fun testNearbySearchParams() {
        val testLocationObservable = PublishSubject.create<Location>()
        val mockSuggestionV4Services = MockSuggestionV4Services(suggestNearbyGaiaCallback = { request ->
            assertEquals("packages", request.lob)
            assertEquals("popularity", request.sortType)
            assertTrue(request.misForRealWorldEnabled)
        })
        PackageSuggestionAdapterViewModel(RuntimeEnvironment.application, mockSuggestionV4Services, true, testLocationObservable)
        testLocationObservable.onNext(Location(""))
    }

    @Test
    fun testGetSuggestionService() {
        val sampleQuery = "Sample Query"
        val mockSuggestionV4Services = MockSuggestionV4Services(suggestPackagesV4Callback = { query, isDest, _, guid ->
            assertEquals(sampleQuery, query)
            assertTrue(isDest)
            assertEquals(Db.sharedInstance.abacusGuid, guid)
        })
        val viewModel = PackageSuggestionAdapterViewModel(RuntimeEnvironment.application, mockSuggestionV4Services, true, null)
        viewModel.getSuggestionService(sampleQuery)
    }

    class MockSuggestionV4Services(val suggestNearbyGaiaCallback: ((request: GaiaSuggestionRequest) -> Unit)? = null,
                                   val suggestPackagesV4Callback: ((query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, guid: String?) -> Unit)? = null) : ISuggestionV4Services {

        override fun getLxSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, disablePOI: Boolean, isEssRegionTypeCallEnabled: Boolean): Disposable {
            TODO("not implemented: getLxSuggestionsV4")
        }

        override fun getHotelSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>): Disposable {
            TODO("not implemented: getHotelSuggestionsV4")
        }

        override fun suggestNearbyGaia(request: GaiaSuggestionRequest): Observable<MutableList<GaiaSuggestion>> {
            suggestNearbyGaiaCallback?.invoke(request)
            return Observable.just(mutableListOf(getGaiaSuggestion()))
        }

        override fun suggestPackagesV4(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, guid: String?): Disposable {
            suggestPackagesV4Callback?.invoke(query, isDest, observer, guid)
            return Mockito.mock(Disposable::class.java)
        }

        override fun suggestRailsV4(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>): Disposable {
            TODO("not implemented: suggestRailsV4")
        }

        override fun getAirports(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, guid: String): Disposable {
            TODO("not implemented: getAirports")
        }

        override fun essDomainResolution(): Observable<ResponseBody> {
            TODO("not implemented: getAirports")
        }

        private fun getGaiaSuggestion(): GaiaSuggestion {
            val suggestion = GaiaSuggestion()
            suggestion.gaiaID = "180000"
            suggestion.type = "multi_city_vicinity"
            val position = GaiaSuggestion.Position("Point", arrayOf(77.22496, 28.635308))
            val localizedNames = arrayOf(GaiaSuggestion.LocalizedName(1043, "Delhi (and vicinity)",
                    "Delhi (and vicinity), India", "Delhi (and vicinity), India", "DEL"))
            val country = GaiaSuggestion.Country("India", "IND")
            suggestion.name = "Delhi (and vicinity), India"
            suggestion.country = country
            suggestion.position = position
            suggestion.localizedNames = localizedNames

            return suggestion
        }
    }
}
