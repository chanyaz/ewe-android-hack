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
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
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
    lateinit private var service: FlightServices
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
                interceptor, Schedulers.trampoline(), Schedulers.trampoline())
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

        sut.resultsPresenter.flightSelectedSubject.onNext(setupFlightLeg())
        assertEquals(com.expedia.bookings.presenter.shared.FlightOverviewPresenter::class.java.name, sut.currentState)
    }

    @Test
    fun doNotShowOverviewWhenInSimplifyTest() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppSimplifyFlightShopping)
        createSystemUnderTest(isOutboundPresenter = true)

        sut.resultsPresenter.resultsViewModel.flightResultsObservable.onNext(emptyList())
        assertEquals(FlightResultsListViewPresenter::class.java.name, sut.currentState)

        sut.resultsPresenter.flightSelectedSubject.onNext(setupFlightLeg())
        assertEquals(FlightResultsListViewPresenter::class.java.name, sut.currentState)
    }

    @Test
    fun obFeesOfferSelectedShowPaymentFees() {
        createSystemUnderTest(false)

        val flightLegYesObFees = FlightLeg()
        flightLegYesObFees.mayChargeObFees = true
        sut.flightOfferViewModel.outboundSelected.onNext(flightLegYesObFees)

        assertEquals(View.VISIBLE, sut.overviewPresenter.paymentFeesMayApplyTextView.visibility)
    }

    @Test
    fun testLegalPaymentMessageOnOutboundFlight() {
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_with_airline_payment_fees.json")
        createSystemUnderTest(isOutboundPresenter = true)
        sut.resultsPresenter.lineOfBusinessSubject.onNext(sut.getLineOfBusiness())
        assertEquals(context.getString(R.string.airline_additional_fee_notice),
                sut.resultsPresenter.getAirlinePaymentFeesTextView().text)
        assertEquals(View.VISIBLE, sut.resultsPresenter.getAirlinePaymentFeesTextView().visibility)
    }

    @Test
    fun testLegalPaymentMessageOnInboundFlight() {
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_with_airline_payment_fees.json")
        createSystemUnderTest(isOutboundPresenter = false)
        sut.resultsPresenter.lineOfBusinessSubject.onNext(sut.getLineOfBusiness())
        assertEquals(context.getString(R.string.airline_additional_fee_notice),
                sut.resultsPresenter.getAirlinePaymentFeesTextView().text)
        assertEquals(View.VISIBLE, sut.resultsPresenter.getAirlinePaymentFeesTextView().visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun noObFeesOfferDontShowPaymentFees() {
        createSystemUnderTest(false)

        val flightLegNoObFees = FlightLeg()
        flightLegNoObFees.mayChargeObFees = false
        sut.flightOfferViewModel.outboundSelected.onNext(flightLegNoObFees)

        assertEquals(View.GONE, sut.overviewPresenter.paymentFeesMayApplyTextView.visibility)
    }

    @Test
    fun obFeeDetailsUrlNotSetWhenOBFeeNotShown() {
        createSystemUnderTest(false)

        val testSubscriber = TestObserver<String>()
        val expectedUrl = "http://url"
        sut.paymentFeeInfoWebView.viewModel.webViewURLObservable.subscribe(testSubscriber)

        sut.flightOfferViewModel.obFeeDetailsUrlObservable.onNext(expectedUrl)

        testSubscriber.assertValueCount(0)
    }

    @Test
    fun obFeeDetailsUrlSet() {
        createSystemUnderTest(false)

        val testSubscriber = TestObserver<String>()
        val expectedUrl = "http://url"
        sut.paymentFeeInfoWebView.viewModel.webViewURLObservable.subscribe(testSubscriber)

        sut.flightOfferViewModel.obFeeDetailsUrlObservable.onNext(expectedUrl)

        sut.overviewPresenter.showPaymentFeesObservable.onNext(true)
        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(expectedUrl)
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

    @Test
    fun showPaymentFees() {
        createSystemUnderTest(true)
        setupFlightLeg()
        val testSubscriber = TestObserver<Boolean>()
        sut.overviewPresenter.showPaymentFeesObservable.subscribe(testSubscriber)

        sut.overviewPresenter.paymentFeesMayApplyTextView.performClick()

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(true)
    }

    @Test
    fun showTextPaymentFees() {
        createSystemUnderTest(true)
        setupFlightLeg(false)
        val testSubscriber = TestObserver<Boolean>()
        sut.overviewPresenter.showPaymentFeesObservable.subscribe(testSubscriber)
        sut.flightOfferViewModel.obFeeDetailsUrlObservable.onNext("")
        sut.overviewPresenter.paymentFeesMayApplyTextView.performClick()

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(false)
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

    private fun setupFlightLeg(hasObFees: Boolean = true): FlightLeg {
        val flightLeg = FlightLeg()
        flightLeg.flightSegments = arrayListOf<FlightLeg.FlightSegment>()
        flightLeg.legId = "leg-id"
        flightLeg.baggageFeesUrl = "test"
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.urgencyMessage = PackageOfferModel.UrgencyMessage()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$646.00"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "$646.00"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("646.00", "USD")
        flightLeg.mayChargeObFees = hasObFees
        return flightLeg
    }

    private inner class TestFlightResultsPresenter(context: Context, attrs: AttributeSet?, val outboundPresenter: Boolean) : AbstractMaterialFlightResultsPresenter(context, attrs) {
        override fun isOutboundResultsPresenter(): Boolean {
            return outboundPresenter
        }

        override fun trackFlightResultsLoad() {
            throw UnsupportedOperationException()
        }

        override fun trackFlightOverviewLoad() {
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
