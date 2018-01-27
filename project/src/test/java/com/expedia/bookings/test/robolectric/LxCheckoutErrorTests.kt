package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.lx.LXCheckoutResponse
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.lx.LXCheckoutPresenter
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.LXCheckoutSummaryWidget
import com.expedia.bookings.widget.LXErrorWidget
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
class LxCheckoutErrorTests {

    private val mockActivityObjects = MockActivityObjects()

    private lateinit var apiError: ApiError
    private lateinit var checkoutResponseForPriceChange: LXCheckoutResponse
    lateinit var errorWidget: LXErrorWidget
    private var activity: Activity by Delegates.notNull()
    lateinit var errorText: android.widget.TextView
    lateinit var errorImage: ImageView
    lateinit var errorButton: Button
    lateinit var errorToolbar: android.support.v7.widget.Toolbar
    lateinit var checkoutPresenter: LXCheckoutPresenter
    lateinit var checkoutToolbar: ViewGroup

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_LX)
        Ui.getApplication(activity).defaultLXComponents()

        checkoutPresenter = LayoutInflater.from(activity).inflate(R.layout.test_lx_checkout_presenter, null) as LXCheckoutPresenter
        errorWidget = checkoutPresenter.findViewById<View>(R.id.lx_checkout_error_widget) as LXErrorWidget
        errorText = errorWidget.findViewById<View>(R.id.error_text) as android.widget.TextView
        errorImage = errorWidget.findViewById<View>(R.id.error_image) as ImageView
        errorButton = errorWidget.findViewById<View>(R.id.error_action_button) as Button
        errorToolbar = errorWidget.findViewById<View>(R.id.error_toolbar) as android.support.v7.widget.Toolbar
        checkoutToolbar = checkoutPresenter.findViewById<View>(R.id.checkout_toolbar) as ViewGroup
        Events.register(checkoutPresenter)
    }

    @After fun after() {
        Events.unregister(checkoutPresenter)
    }

    @Test
    fun testPaymentFailed() {
        performLxCheckoutError("PaymentFailed")

        assertEquals(ApiError.Code.PAYMENT_FAILED, apiError.errorCode)
        assertEquals(View.VISIBLE, errorWidget.visibility)
        assertEquals(View.VISIBLE, errorImage.visibility)
        assertEquals(activity.getResources().getString(R.string.payment_failed_label), errorToolbar.title)
        assertEquals(activity.getResources().getString(R.string.edit_payment), errorButton.text)
        assertEquals(activity.resources.getString(R.string.reservation_payment_failed), errorText.text)

        errorButton.performClick()

        val editbox = checkoutPresenter.findViewById<View>(R.id.edit_creditcard_number)
        val sectionBillingInfo = checkoutPresenter.findViewById<View>(R.id.section_billing_info)
        assertEquals(View.VISIBLE, sectionBillingInfo.visibility)
        assertEquals(View.VISIBLE, editbox.visibility)
    }

    @Test
    fun testUnknownError() {
        performLxCheckoutError("UnknownError")

        assertEquals(ApiError.Code.UNKNOWN_ERROR, apiError.errorCode)
        assertEquals(View.VISIBLE, errorWidget.visibility)
        assertEquals(View.VISIBLE, errorImage.visibility)
        assertEquals(activity.getResources().getString(R.string.lx_error_text), errorToolbar.title)
        assertEquals(activity.getResources().getString(R.string.retry), errorButton.text)
        assertEquals(Phrase.from(activity, R.string.error_server_TEMPLATE).put("brand", BuildConfig.brand).format().toString(), errorText.text)
    }

    @Test
    fun testInvalidInput() {

        performLxCheckoutError("InvalidInput")

        assertEquals(ApiError.Code.INVALID_INPUT, apiError.errorCode)
        assertEquals(View.VISIBLE, errorWidget.visibility)
        assertEquals(View.VISIBLE, errorImage.visibility)
        assertEquals(activity.getResources().getString(R.string.lx_invalid_input_text), errorToolbar.title)
        assertEquals(activity.getResources().getString(R.string.edit_info), errorButton.text)
        assertEquals(activity.getResources().getString(R.string.reservation_invalid_name), errorText.text)

        errorButton.performClick()

        //Assert that Traveller details screen is displayed
        val checkoutToolbarTitle = checkoutToolbar.getChildAt(2) as android.widget.TextView
        val mainContactInfoCardView = checkoutPresenter.findViewById<View>(R.id.main_contact_info_card_view) as android.widget.FrameLayout
        assertEquals("Traveler details", checkoutToolbarTitle.text.toString())
        assertEquals(View.VISIBLE, mainContactInfoCardView.visibility)
    }

    @Test
    fun testTripAlreadyBooked() {
        performLxCheckoutError("AlreadyBooked")

        assertEquals(ApiError.Code.TRIP_ALREADY_BOOKED, apiError.errorCode)
        assertEquals(View.VISIBLE, errorWidget.visibility)
        assertEquals(View.VISIBLE, errorImage.visibility)
        assertEquals(activity.getResources().getString(R.string.lx_duplicate_trip_text), errorToolbar.title)
        assertEquals(activity.getResources().getString(R.string.my_trips), errorButton.text)
        assertEquals(activity.getResources().getString(R.string.reservation_already_exists), errorText.text)
    }

    @Test
    fun testSessionTimeout() {
        performLxCheckoutError("SessionTimeout")

        assertEquals(ApiError.Code.SESSION_TIMEOUT, apiError.errorCode)
        assertEquals(View.VISIBLE, errorWidget.visibility)
        assertEquals(View.VISIBLE, errorImage.visibility)
        assertEquals(activity.getResources().getString(R.string.session_timeout), errorToolbar.title)
        assertEquals(activity.getResources().getString(R.string.edit_search), errorButton.text)
        assertEquals(activity.getResources().getString(R.string.reservation_time_out), errorText.text)
    }

    @Test
    fun testPriceChangeErrorMessageOnErrorScreen() {
        performLxCheckoutWithPriceChange()

        assertEquals(ApiError.Code.PRICE_CHANGE, checkoutResponseForPriceChange.firstError.errorCode)
        assertEquals(View.VISIBLE, errorWidget.visibility)
        assertEquals(View.VISIBLE, errorImage.visibility)
        assertEquals(activity.getResources().getString(R.string.lx_price_change_text), errorToolbar.title)
        assertEquals(activity.getResources().getString(R.string.view_price_change), errorButton.text)
        assertEquals(activity.resources.getString(R.string.lx_error_price_changed), errorText.text)
    }

    @Test
    fun testPriceChangeErrorMessageOnCheckoutScreen() {
        performLxCheckoutWithPriceChange()

        val checkoutSummaryWidget = LayoutInflater.from(activity).inflate(R.layout.lx_checkout_summary_widget, null) as LXCheckoutSummaryWidget
        LXStateTestUtil.selectActivityState()
        LXStateTestUtil.offerSelected()
        val originalPrice = checkoutResponseForPriceChange.originalPrice
        val latestPrice = checkoutResponseForPriceChange.newTotalPrice
        checkoutSummaryWidget.bind(originalPrice, latestPrice, null)
        val priceChangeContainer = checkoutSummaryWidget.findViewById<View>(R.id.price_change_container)
        val priceChangeText = priceChangeContainer.findViewById<View>(R.id.price_change_text) as TextView
        val expectedPriceChangeString = activity.getResources().getString(R.string.price_changed_from_TEMPLATE, originalPrice.formattedMoney)

        assertEquals(View.VISIBLE, priceChangeContainer.visibility)
        assertEquals(View.VISIBLE, priceChangeText.visibility)
        assertEquals(expectedPriceChangeString, priceChangeText.text)
    }

    @Test
    fun testCVVScreen() {
        val billingInfo = getBillingInfo()
        Events.post(Events.ShowCVV(billingInfo))
        val promptText = checkoutPresenter.findViewById<View>(R.id.cvv_prompt_text_view) as com.mobiata.android.widget.AutoResizeTextView
        val cvvToolbarCheckout = checkoutPresenter.findViewById<View>(R.id.cvv_toolbar) as ViewGroup
        val toolBarTitle = cvvToolbarCheckout.getChildAt(1) as android.widget.TextView
        val bookButton = checkoutPresenter.findViewById<View>(R.id.book_button)
        val authorizedSignature = checkoutPresenter.findViewById<View>(R.id.authorized_signature_text) as android.widget.TextView
        val signatureTextView = checkoutPresenter.findViewById<View>(R.id.signature_text_view) as android.widget.TextView
        assertEquals("Finish Booking", toolBarTitle.text)
        assertEquals("Security code for card ending in 4448", promptText.text.toString())
        assertEquals(" T. User", signatureTextView.text.toString())
        assertEquals("Authorized Signature", authorizedSignature.text.toString())
        assertEquals(false, bookButton.isEnabled)
    }

    @Test
    fun testWhenMissingCheckoutParam() {
        mockActivityObjects.setCheckoutParamsWithErrorAsFirstName("HappyPath", false)
        val lxKickoffCheckoutCallEvent = Events.LXKickOffCheckoutCall(mockActivityObjects.getCheckoutParams())
        checkoutPresenter.onDoCheckoutCall(lxKickoffCheckoutCallEvent)
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val okButton = alertDialog.findViewById<View>(android.R.id.button3) as Button
        val errorMessage = alertDialog.findViewById<View>(android.R.id.message) as android.widget.TextView
        assertEquals(true, alertDialog.isShowing)
        assertEquals("Missing checkout params.", errorMessage.text)
        assertEquals("OK", okButton.text )
        //Tap on Ok button
        okButton.performClick()
        assertEquals(false, alertDialog.isShowing)
    }

    @Test
    fun testShowLXRules() {
        val checkoutSummaryWidget = LayoutInflater.from(activity).inflate(R.layout.lx_checkout_summary_widget, null) as LXCheckoutSummaryWidget
        val lxRulesToolbar = checkoutPresenter.findViewById<View>(R.id.lx_rules_toolbar) as ViewGroup
        val lxRulesToolbarTitle = lxRulesToolbar.getChildAt(1) as android.widget.TextView
        val freeCancellationPolicyText = checkoutPresenter.findViewById<View>(R.id.cancellation_policy_header_text_view) as android.widget.TextView
        checkoutSummaryWidget.showLxRules()

        assertEquals("Legal Information", lxRulesToolbarTitle.text.toString())
        assertEquals(View.VISIBLE, freeCancellationPolicyText.visibility)
    }

    private fun performLxCheckoutError(errorType: String) {
        apiError = mockActivityObjects.getCheckoutError(errorType)
        val lxKickoffCheckoutCallEvent = Events.LXKickOffCheckoutCall(mockActivityObjects.getCheckoutParams())
        checkoutPresenter.onDoCheckoutCall(lxKickoffCheckoutCallEvent)
        errorWidget.bind(apiError)
    }

    private fun performLxCheckoutWithPriceChange() {
        checkoutResponseForPriceChange = mockActivityObjects.getCheckoutResponseForPriceChange()
        val lxKickoffCheckoutCallEvent = Events.LXKickOffCheckoutCall(mockActivityObjects.getCheckoutParams())
        checkoutPresenter.onDoCheckoutCall(lxKickoffCheckoutCallEvent)
        errorWidget.bind(checkoutResponseForPriceChange.firstError)
    }

    private fun getBillingInfo(): BillingInfo {

        val billingInfo = BillingInfo()
        billingInfo.firstName = "Test"
        billingInfo.lastName = "User"
        billingInfo.nameOnCard = "Test User"
        billingInfo.email = "qa-ehcc@mobiata.com"
        billingInfo.setNumberAndDetectType("4444444444444448", activity)
        billingInfo.expirationDate = LocalDate.now().plusYears(1)
        billingInfo.securityCode = "111"
        billingInfo.telephone = "4155555555"
        billingInfo.telephoneCountryCode = "1"
        return billingInfo
    }
}
