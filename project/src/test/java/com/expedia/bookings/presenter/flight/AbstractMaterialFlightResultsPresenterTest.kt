package com.expedia.bookings.presenter.flight

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.presenter.shared.FlightResultsListViewPresenter
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.FlightOffersViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class AbstractMaterialFlightResultsPresenterTest {

    private val context = RuntimeEnvironment.application
    private lateinit var sut: AbstractMaterialFlightResultsPresenter
    private lateinit var service: FlightServices
    var server: MockWebServer = MockWebServer()
        @Rule get

    @Before
    fun setup() {
        val logger = HttpLoggingInterceptor()
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        service = FlightServices("http://localhost:" + server.port,
                okhttp3.OkHttpClient.Builder().addInterceptor(logger).build(),
                listOf(interceptor), Schedulers.trampoline(), Schedulers.trampoline(), false)
        Ui.getApplication(context).defaultTravelerComponent()
    }

    @Test
    fun searchMenuVisibility() {
        createSystemUnderTest(isOutboundPresenter = true)

        sut.toolbarViewModel.menuVisibilitySubject.onNext(true)
        assertTrue(sut.menuSearch.isVisible)

        sut.toolbarViewModel.menuVisibilitySubject.onNext(false)
        assertFalse(sut.menuSearch.isVisible)
    }

    @Test
    fun showResultsOnNewResults() {
        createSystemUnderTest(isOutboundPresenter = true)
        sut.resultsPresenter.resultsViewModel.flightResultsObservable.onNext(emptyList())

        assertEquals(FlightResultsListViewPresenter::class.java.name, sut.currentState)
    }

    @Test
    fun showOverviewWhenNotInSimplifyTest() {
        createSystemUnderTest(isOutboundPresenter = true)
        sut.resultsPresenter.flightSelectedSubject.onNext(createFlightLeg("leg0"))

        assertEquals(com.expedia.bookings.presenter.shared.FlightOverviewPresenter::class.java.name, sut.currentState)
    }

    @Test
    fun doNotShowOverviewWhenInSimplifyTest() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppSimplifyFlightShopping)
        createSystemUnderTest(isOutboundPresenter = true)

        sut.resultsPresenter.resultsViewModel.flightResultsObservable.onNext(emptyList())
        assertEquals(FlightResultsListViewPresenter::class.java.name, sut.currentState)

        sut.resultsPresenter.flightSelectedSubject.onNext(createFlightLeg("leg0"))
        assertEquals(FlightResultsListViewPresenter::class.java.name, sut.currentState)
    }

    @Test
    fun testPaymentFeesVisibilityForOutboundFlight() {
        createSystemUnderTest(false)
        sut.flightOfferViewModel.outboundSelected.onNext(createFlightLeg("leg0"))

        assertEquals(View.GONE, sut.overviewPresenter.paymentFeesMayApplyTextView.visibility)
    }

    @Test
    fun testPaymentFeesVisibilityForInboundFlight() {
        createSystemUnderTest(false)
        sut.flightOfferViewModel.outboundSelected.onNext(createFlightLeg("leg0"))
        sut.flightOfferViewModel.inboundSelected.onNext(createFlightLeg("leg0"))

        assertEquals(View.GONE, sut.overviewPresenter.paymentFeesMayApplyTextView.visibility)
    }

    @Test
    fun toolbarViewModelIsOutboundPresenterTrue() {
        createSystemUnderTest(isOutboundPresenter = true)

        assertTrue(sut.toolbarViewModel.isOutboundSearch.value)
    }

    @Test
    fun toolbarViewModelIsOutboundPresenterFalse() {
        createSystemUnderTest(isOutboundPresenter = false)

        assertFalse(sut.toolbarViewModel.isOutboundSearch.value)
    }

    private fun createSystemUnderTest(isOutboundPresenter: Boolean) {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        sut = TestFlightResultsPresenter(activity, null, isOutboundPresenter)
        sut.flightOfferViewModel = FlightOffersViewModel(activity, service)
        sut.flightOfferViewModel.isRoundTripSearchSubject.onNext(false)
        sut.flightOfferViewModel.searchParamsObservable.onNext(getSearchParams(sut.flightOfferViewModel.isRoundTripSearchSubject.value).build())
        sut.setupComplete()
    }

    private fun getSearchParams(roundTrip: Boolean): FlightSearchParams.Builder {
        val origin = getDummySuggestion()
        val destination = getDummySuggestion()
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(2)
        val paramsBuilder = FlightSearchParams.Builder(26, 500)
                .origin(origin)
                .destination(destination)
                .startDate(startDate)
                .adults(1) as FlightSearchParams.Builder
        paramsBuilder.flightCabinClass("coach")

        if (roundTrip) {
            paramsBuilder.endDate(endDate)
        }
        return paramsBuilder
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = ""
        return suggestion
    }

    private fun createFlightLeg(legId: String): FlightLeg {
        val flightLeg = FlightLeg()
        flightLeg.flightSegments = arrayListOf<FlightLeg.FlightSegment>()
        flightLeg.legId = legId
        flightLeg.baggageFeesUrl = "test"
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.urgencyMessage = PackageOfferModel.UrgencyMessage()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$646.00"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "$646.00"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("646.00", "USD")
        return flightLeg
    }

    private inner class TestFlightResultsPresenter(context: Context, attrs: AttributeSet?, val outboundPresenter: Boolean) : AbstractMaterialFlightResultsPresenter(context, attrs) {
        override fun isOutboundResultsPresenter(): Boolean {
            return outboundPresenter
        }

        override fun trackFlightResultsLoad() {
            throw UnsupportedOperationException()
        }

        override fun trackFlightOverviewLoad(flight: FlightLeg) {
            // Do nothing
        }

        override fun trackFlightSortFilterLoad() {
            throw UnsupportedOperationException()
        }

        override fun trackFlightScrollDepth(scrollDepth: Int) {
            //Do Nothing
        }
    }
}
