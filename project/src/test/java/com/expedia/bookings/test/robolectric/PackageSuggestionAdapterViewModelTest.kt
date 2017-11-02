package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.test.robolectric.RoboTestHelper.getContext
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.vm.packages.PackageSuggestionAdapterViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageSuggestionAdapterViewModelTest {

    @Test
    fun packagesShouldShowOnlyAirportInNearbySuggestions() {
        val mockSuggestionV4Services = MockSuggestionV4Services()
        val viewModel = PackageSuggestionAdapterViewModel(RuntimeEnvironment.application, mockSuggestionV4Services, true, null)
        assertTrue(viewModel.shouldShowOnlyAirportNearbySuggestions())
    }

    @Test
    fun isMISForRealWorldEnabledTrue() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesMISRealWorldGeo)

        val mockSuggestionV4Services = MockSuggestionV4Services()
        val viewModel = PackageSuggestionAdapterViewModel(RuntimeEnvironment.application, mockSuggestionV4Services, true, null)
        assertTrue(viewModel.isMISForRealWorldEnabled())
    }

    @Test
    fun isMISForRealWorldEnabledFalseForMID() {
        SettingUtils.save(getContext(), R.string.preference_packages_mid_api, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesMidApi, AbacusUtils.EBAndroidAppPackagesMISRealWorldGeo)

        val mockSuggestionV4Services = MockSuggestionV4Services()
        val viewModel = PackageSuggestionAdapterViewModel(RuntimeEnvironment.application, mockSuggestionV4Services, true, null)
        assertFalse(viewModel.isMISForRealWorldEnabled())
    }

    class MockSuggestionV4Services : ISuggestionV4Services {
        override fun getLxSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, disablePOI: Boolean): Disposable {
            TODO("not implemented: getLxSuggestionsV4")
        }

        override fun getHotelSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, sameAsWeb: Boolean, guid: String?): Disposable {
            TODO("not implemented: getHotelSuggestionsV4")
        }

        override fun suggestNearbyGaia(lat: Double, lng: Double, sortType: String, lob: String, locale: String, siteId: Int, isMISForRealWorldEnabled: Boolean): Observable<MutableList<GaiaSuggestion>> {
            TODO("not implemented: suggestNearbyGaia")
        }

        override fun suggestPackagesV4(query: String, isDest: Boolean, isMISForRealWorldEnabled: Boolean, observer: Observer<List<SuggestionV4>>, guid: String?): Disposable {
            TODO("not implemented: suggestPackagesV4")
        }

        override fun suggestRailsV4(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>): Disposable {
            TODO("not implemented: suggestRailsV4")
        }

        override fun getAirports(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, guid: String): Disposable {
            TODO("not implemented: getAirports")
        }
    }
}
