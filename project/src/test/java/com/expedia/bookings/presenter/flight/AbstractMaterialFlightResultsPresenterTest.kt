package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.presenter.shared.FlightResultsListViewPresenter
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.FlightSearchViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class AbstractMaterialFlightResultsPresenterTest {

    val context = RuntimeEnvironment.application

    private lateinit var sut: AbstractMaterialFlightResultsPresenter

    @Before
    fun setup() {
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
        givenFlightSearchViewModel()

        sut.resultsPresenter.resultsViewModel.flightResultsObservable.onNext(emptyList())

        assertEquals(FlightResultsListViewPresenter::class.java.name, sut.currentState)
    }

    @Test
    fun obFeesOfferSelectedShowPaymentFees() {
        createSystemUnderTest(false)
        givenFlightSearchViewModel()

        val flightLegYesObFees = FlightLeg()
        flightLegYesObFees.mayChargeObFees = true
        sut.flightSearchViewModel.outboundSelected.onNext(flightLegYesObFees)

        assertEquals(View.VISIBLE, sut.overviewPresenter.paymentFeesMayApplyTextView.visibility)
    }

    @Test
    fun noObFeesOfferDontShowPaymentFees() {
        createSystemUnderTest(false)
        givenFlightSearchViewModel()

        val flightLegNoObFees = FlightLeg()
        flightLegNoObFees.mayChargeObFees = false
        sut.flightSearchViewModel.outboundSelected.onNext(flightLegNoObFees)

        assertEquals(View.GONE, sut.overviewPresenter.paymentFeesMayApplyTextView.visibility)
    }

    @Test
    fun obFeeDetailsUrlSet() {
        createSystemUnderTest(false)
        givenFlightSearchViewModel()

        val testSubscriber = TestSubscriber<String>()
        val expectedUrl = "http://url"
        sut.paymentFeeInfoWebView.viewModel.webViewURLObservable.subscribe(testSubscriber)

        sut.flightSearchViewModel.obFeeDetailsUrlObservable.onNext(expectedUrl)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(expectedUrl)
    }

    @Test
    fun toolbarViewModelIsOutboundPresenterTrue() {
        createSystemUnderTest(isOutboundPresenter = true)
        givenFlightSearchViewModel()

        assertTrue(sut.toolbarViewModel.isOutboundSearch.value)
    }

    @Test
    fun toolbarViewModelIsOutboundPresenterFalse() {
        createSystemUnderTest(isOutboundPresenter = false)
        givenFlightSearchViewModel()

        assertFalse(sut.toolbarViewModel.isOutboundSearch.value)
    }

    private fun createSystemUnderTest(isOutboundPresenter: Boolean) {
        sut = TestFlightResultsPresenter(context, null, isOutboundPresenter)
    }

    private fun givenFlightSearchViewModel() {
        val flightServices = Mockito.mock(FlightServices::class.java)
        sut.flightSearchViewModel = FlightSearchViewModel(context, flightServices)
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
