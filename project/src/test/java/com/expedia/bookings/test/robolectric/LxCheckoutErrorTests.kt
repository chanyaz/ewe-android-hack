package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.lx.LXCheckoutResponse
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.lx.LXCheckoutPresenter
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.LXCheckoutSummaryWidget
import com.expedia.bookings.widget.LXErrorWidget
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class, ShadowUserManager::class, ShadowAccountManagerEB::class))

class LxCheckoutErrorTests {
    var mockActivityServiceTestRule: MockActivityServiceTestRule = MockActivityServiceTestRule()
        @Rule get

    lateinit private var apiError: ApiError
    lateinit private var checkoutResponseForPriceChange: LXCheckoutResponse
    lateinit var errorWidget: LXErrorWidget
    private var activity: Activity by Delegates.notNull()
    lateinit var errorText: android.widget.TextView
    lateinit var errorImage: ImageView
    lateinit var errorButton: Button
    lateinit var errorToolbar: android.support.v7.widget.Toolbar
    lateinit var checkoutPresenter: LXCheckoutPresenter

    @Before fun before() {

        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_LX)
        Ui.getApplication(activity).defaultLXComponents()

        checkoutPresenter = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.test_lx_checkout_presenter, null) as LXCheckoutPresenter
        errorWidget = checkoutPresenter.findViewById(R.id.lx_checkout_error_widget) as LXErrorWidget
        errorText = errorWidget.findViewById(R.id.error_text) as android.widget.TextView
        errorImage = errorWidget.findViewById(R.id.error_image) as ImageView
        errorButton = errorWidget.findViewById(R.id.error_action_button) as Button
        errorToolbar = errorWidget.findViewById(R.id.error_toolbar) as android.support.v7.widget.Toolbar
    }

    @Test
     fun testPaymentFailed(){
        performLxCheckoutError("PaymentFailed")

        assertEquals(ApiError.Code.PAYMENT_FAILED, apiError.errorCode)
        assertEquals(View.VISIBLE, errorWidget.visibility)
        assertEquals(View.VISIBLE, errorImage.visibility)
        assertEquals(activity.getResources().getString(R.string.payment_failed_label), errorToolbar.title)
        assertEquals(activity.getResources().getString(R.string.edit_payment), errorButton.text)
        assertEquals(activity.getResources().getString(R.string.reservation_payment_failed), errorText.text)
    }

    @Test
    fun testUnknownError(){
        performLxCheckoutError("UnknownError")

        assertEquals(ApiError.Code.UNKNOWN_ERROR, apiError.errorCode)
        assertEquals(View.VISIBLE, errorWidget.visibility)
        assertEquals(View.VISIBLE, errorImage.visibility)
        assertEquals(activity.getResources().getString(R.string.lx_error_text), errorToolbar.title)
        assertEquals(activity.getResources().getString(R.string.retry), errorButton.text)
        assertEquals(Phrase.from(activity, R.string.error_server_TEMPLATE).put("brand", BuildConfig.brand).format().toString(), errorText.text)
    }

    @Test
    fun testInvalidInput(){
        performLxCheckoutError("InvalidInput")

        assertEquals(ApiError.Code.INVALID_INPUT, apiError.errorCode)
        assertEquals(View.VISIBLE, errorWidget.visibility)
        assertEquals(View.VISIBLE, errorImage.visibility)
        assertEquals(activity.getResources().getString(R.string.lx_invalid_input_text), errorToolbar.title)
        assertEquals(activity.getResources().getString(R.string.edit_info), errorButton.text)
        assertEquals(activity.getResources().getString(R.string.reservation_invalid_name), errorText.text)
    }

    @Test
    fun testTripAlreadyBooked(){
        performLxCheckoutError("AlreadyBooked")

        assertEquals(ApiError.Code.TRIP_ALREADY_BOOKED, apiError.errorCode)
        assertEquals(View.VISIBLE, errorWidget.visibility)
        assertEquals(View.VISIBLE, errorImage.visibility)
        assertEquals(activity.getResources().getString(R.string.lx_duplicate_trip_text), errorToolbar.title)
        assertEquals(activity.getResources().getString(R.string.my_trips), errorButton.text)
        assertEquals(activity.getResources().getString(R.string.reservation_already_exists), errorText.text)
    }

    @Test
    fun testSessionTimeout(){
        performLxCheckoutError("SessionTimeout")

        assertEquals(ApiError.Code.SESSION_TIMEOUT, apiError.errorCode)
        assertEquals(View.VISIBLE, errorWidget.visibility)
        assertEquals(View.VISIBLE, errorImage.visibility)
        assertEquals(activity.getResources().getString(R.string.session_timeout), errorToolbar.title)
        assertEquals(activity.getResources().getString(R.string.edit_search), errorButton.text)
        assertEquals(activity.getResources().getString(R.string.reservation_time_out), errorText.text)
    }

    @Test
    fun testPriceChangeErrorMessageOnErrorScreen(){
        performLxCheckout("PriceChange")

        assertEquals(ApiError.Code.PRICE_CHANGE, checkoutResponseForPriceChange.firstError.errorCode)
        assertEquals(View.VISIBLE, errorWidget.visibility)
        assertEquals(View.VISIBLE, errorImage.visibility)
        assertEquals(activity.getResources().getString(R.string.lx_price_change_text), errorToolbar.title)
        assertEquals(activity.getResources().getString(R.string.view_price_change), errorButton.text)
        assertEquals(activity.getResources().getString(R.string.lx_error_price_changed), errorText.text)
    }

    @Test
    fun testPriceChangeErrorMessageOnCheckoutScreen(){
        performLxCheckout("PriceChange")

        val checkoutSummaryWidget = LayoutInflater.from(activity).inflate(R.layout.lx_checkout_summary_widget, null) as LXCheckoutSummaryWidget
        LXStateTestUtil.selectActivityState()
        LXStateTestUtil.offerSelected()
        val originalPrice = checkoutResponseForPriceChange.originalPrice
        val latestPrice = checkoutResponseForPriceChange.newTotalPrice
        checkoutSummaryWidget.bind(originalPrice, latestPrice, null)
        val priceChangeContainer = checkoutSummaryWidget.findViewById(R.id.price_change_container)
        val priceChangeText = priceChangeContainer.findViewById(R.id.price_change_text) as TextView
        val expectedPriceChangeString = activity.getResources().getString(R.string.price_changed_from_TEMPLATE, originalPrice.formattedMoney)

        assertEquals(View.VISIBLE, priceChangeContainer.visibility)
        assertEquals(View.VISIBLE, priceChangeText.visibility)
        assertEquals(expectedPriceChangeString, priceChangeText.text)

    }

    private fun performLxCheckoutError(errorType: String) {
        apiError = mockActivityServiceTestRule.getCheckoutError(errorType)
        val lxKickoffCheckoutCallEvent = Events.LXKickOffCheckoutCall(mockActivityServiceTestRule.getCheckoutParams())
        checkoutPresenter.onDoCheckoutCall(lxKickoffCheckoutCallEvent)
        errorWidget.bind(apiError)
    }

    private fun performLxCheckout(errorType: String){
        checkoutResponseForPriceChange = mockActivityServiceTestRule.getCheckoutResponseForPriceChange(errorType)
        val lxKickoffCheckoutCallEvent = Events.LXKickOffCheckoutCall(mockActivityServiceTestRule.getCheckoutParams())
        checkoutPresenter.onDoCheckoutCall(lxKickoffCheckoutCallEvent)
        errorWidget.bind(checkoutResponseForPriceChange.firstError)
    }
}
