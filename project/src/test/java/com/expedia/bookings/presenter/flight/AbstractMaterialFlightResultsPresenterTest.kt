package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.presenter.shared.FlightResultsListViewPresenter
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.FlightOffersViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
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
                interceptor, Schedulers.immediate(), Schedulers.immediate())
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
    fun obFeesOfferSelectedShowPaymentFees() {
        createSystemUnderTest(false)

        val flightLegYesObFees = FlightLeg()
        flightLegYesObFees.mayChargeObFees = true
        sut.flightOfferViewModel.outboundSelected.onNext(flightLegYesObFees)

        assertEquals(View.VISIBLE, sut.overviewPresenter.paymentFeesMayApplyTextView.visibility)
    }

    @Test
    fun noObFeesOfferDontShowPaymentFees() {
        createSystemUnderTest(false)

        val flightLegNoObFees = FlightLeg()
        flightLegNoObFees.mayChargeObFees = false
        sut.flightOfferViewModel.outboundSelected.onNext(flightLegNoObFees)

        assertEquals(View.GONE, sut.overviewPresenter.paymentFeesMayApplyTextView.visibility)
    }

    @Test
    fun obFeeDetailsUrlSet() {
        createSystemUnderTest(false)

        val testSubscriber = TestSubscriber<String>()
        val expectedUrl = "http://url"
        sut.paymentFeeInfoWebView.viewModel.webViewURLObservable.subscribe(testSubscriber)

        sut.flightOfferViewModel.obFeeDetailsUrlObservable.onNext(expectedUrl)

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

    private fun createSystemUnderTest(isOutboundPresenter: Boolean) {
        sut = TestFlightResultsPresenter(context, null, isOutboundPresenter)
        sut.flightOfferViewModel = FlightOffersViewModel(context, service)
        sut.setupComplete()
    }

    private inner class TestFlightResultsPresenter(context: Context, attrs: AttributeSet?, val outboundPresenter: Boolean) : AbstractMaterialFlightResultsPresenter(context, attrs) {
        override fun isOutboundResultsPresenter(): Boolean {
            return outboundPresenter
        }

        override fun trackFlightResultsLoad() {
            throw UnsupportedOperationException()
        }

        override fun trackFlightOverviewLoad() {
            throw UnsupportedOperationException()
        }

        override fun trackFlightSortFilterLoad() {
            throw UnsupportedOperationException()
        }

    }
}
