package com.expedia.bookings.presenter.flight

import android.support.v4.app.FragmentActivity
import android.text.Spanned
import android.text.SpannedString
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.activity.FlightAndPackagesRulesActivity
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.presenter.packages.FlightTravelersPresenter
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.traveler.TravelerSummaryCard
import com.expedia.testutils.Assert.assertViewIsNotVisible
import com.expedia.testutils.Assert.assertViewIsVisible
import com.expedia.vm.test.traveler.MockTravelerProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import com.expedia.bookings.services.TestObserver
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner :: class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))

class FlightCheckoutPresenterTest {

    private var activity: FragmentActivity by Delegates.notNull()
    lateinit var checkoutPresenter: FlightCheckoutPresenter
    val mockTravelerProvider = MockTravelerProvider()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultFlightComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        checkoutPresenter = LayoutInflater.from(activity).inflate(R.layout.test_flight_checkout_presenter, null) as FlightCheckoutPresenter
    }

    @Test
    fun flightCheckoutPresenterDefaultState() {
        assertNotNull(checkoutPresenter)
        val accountButton = checkoutPresenter.loginWidget
        assertViewIsVisible(accountButton)
        val travlerDefaultState = checkoutPresenter.travelerSummaryCardView
        assertViewIsVisible(travlerDefaultState)
        val scrollView = checkoutPresenter.scrollView
        assertViewIsVisible(scrollView)
        val legalInformationText = checkoutPresenter.legalInformationText
        assertViewIsVisible(legalInformationText)
    }

    @Test
    fun shouldShowFlightAndPackagesRulesActivity() {
        checkoutPresenter.legalInformationText.performClick()
        val shadowActivity = shadowOf(activity)
        val intent = shadowActivity.getNextStartedActivity()
        assertEquals(FlightAndPackagesRulesActivity::class.java!!.getName(), intent.component.className)
    }

    @Test
    fun shouldShowTravlerEntryWidget() {
        mockTravelerProvider.updateDBWithMockTravelers(1, Traveler())
        checkoutPresenter.travelerSummaryCardView.findViewById<TravelerSummaryCard>(R.id.traveler_default_state).performClick()
        assertEquals(FlightTravelersPresenter::class.java.name, checkoutPresenter.currentState)
    }

    @Test
    fun showCardFeeWarnings() {
        checkoutPresenter.flightCheckoutViewModel.showCardFeeWarningText.onNext(true)
        checkoutPresenter.flightCheckoutViewModel.cardFeeWarningTextSubject.onNext(SpannedString("ABCDEFG"))
        assertViewIsVisible(checkoutPresenter.cardFeeWarningTextView)
        assertEquals("ABCDEFG", checkoutPresenter.cardFeeWarningTextView.text.toString())

        var testSubscriber = TestObserver<Spanned>()
        checkoutPresenter.flightCheckoutViewModel.cardFeeWarningTextSubject.subscribe(testSubscriber)
        checkoutPresenter.flightCheckoutViewModel.selectedCardFeeObservable.onNext(getMoney(100))
        testSubscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
        assertEquals("A payment method fee of $100 is included in the trip total.", checkoutPresenter.cardFeeWarningTextView.text.toString())
        assertEquals("Payment method fee: $100", checkoutPresenter.cardProcessingFeeTextView.text.toString())
        checkoutPresenter.paymentWidget.viewmodel.showingPaymentForm.onNext(true)
        assertFalse(checkoutPresenter.flightCheckoutViewModel.showCardFeeWarningText.value)
        assertViewIsNotVisible(checkoutPresenter.cardFeeWarningTextView)

        testSubscriber = TestObserver<Spanned>()
        checkoutPresenter.flightCheckoutViewModel.cardFeeWarningTextSubject.subscribe(testSubscriber)
        checkoutPresenter.flightCheckoutViewModel.selectedCardFeeObservable.onNext(getMoney(0))
        testSubscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
        assertEquals("", checkoutPresenter.cardFeeWarningTextView.text.toString())
        checkoutPresenter.paymentWidget.viewmodel.cardBIN.onNext("654321")
        checkoutPresenter.paymentWidget.viewmodel.showingPaymentForm.onNext(false)
        assertFalse(checkoutPresenter.flightCheckoutViewModel.showCardFeeWarningText.value)
        assertViewIsNotVisible(checkoutPresenter.cardFeeWarningTextView)
    }

    @Test
    fun hideCardFeeWarningWhenNoFeeAndShowingPaymentForm() {
        givenCardFees(hasCardFee = false, showingPaymentForm = true, warningText = "")

        assertViewIsNotVisible(checkoutPresenter.cardFeeWarningTextView)
    }

    @Test
    fun hideCardFeeWarningWhenNoFeeAndNotShowingPaymentFormAndHasCard() {
        givenCardFees(hasCardFee = false, showingPaymentForm = false, warningText = "")

        assertViewIsNotVisible(checkoutPresenter.cardFeeWarningTextView)
    }

    @Test
    fun hideCardFeeWarningWhenFeeAndNotOnPaymentWidget() {
        givenCardFees(hasCardFee = true, showingPaymentForm = false, warningText = "card fees may apply")

        assertViewIsNotVisible(checkoutPresenter.cardFeeWarningTextView)
    }

    @Test
    fun showCardFeeWarningWhenFeeAndOnPaymentWidget() {
        givenCardFees(hasCardFee = true, showingPaymentForm = false, warningText = "card fees may apply")

        assertViewIsNotVisible(checkoutPresenter.cardFeeWarningTextView)
    }

    @Test
    fun showCardFeeWarningWithoutPaymentMethodNotShowingPaymentForm() {
        givenCardFees(hasCardFee = false, showingPaymentForm = false, warningText = "card fees may apply", hasCard = false)

        assertViewIsVisible(checkoutPresenter.cardFeeWarningTextView)
    }

    @Test
    fun hideCardFeeWarningWithPaymentMethodNotShowingPaymentForm() {
        givenCardFees(hasCardFee = false, showingPaymentForm = false, warningText = "card fees may apply", hasCard = true)

        assertViewIsNotVisible(checkoutPresenter.cardFeeWarningTextView)
    }

    private fun givenCardFees(hasCardFee: Boolean, showingPaymentForm: Boolean, warningText: String, hasCard: Boolean = true) {
        checkoutPresenter.flightCheckoutViewModel.paymentTypeSelectedHasCardFee.onNext(hasCardFee)
        checkoutPresenter.paymentWidget.viewmodel.showingPaymentForm.onNext(showingPaymentForm)
        checkoutPresenter.flightCheckoutViewModel.cardFeeWarningTextSubject.onNext(SpannedString(warningText))
        if (hasCard) {
            checkoutPresenter.paymentWidget.viewmodel.cardBIN.onNext("654321")
        }
    }

    private fun getMoney(moneyValue: Int): Money {
        val money = Money(moneyValue, "USD")
        money.formattedPrice = "$" + moneyValue
        return money
    }
}
