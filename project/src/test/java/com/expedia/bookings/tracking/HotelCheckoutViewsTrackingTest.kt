package com.expedia.bookings.tracking

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.presenter.hotel.HotelCheckoutMainViewPresenter
import com.expedia.bookings.presenter.hotel.HotelCheckoutPresenter
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.PaymentWidget
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class HotelCheckoutViewsTrackingTest {
    private var checkout: HotelCheckoutMainViewPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        Ui.getApplication(RuntimeEnvironment.application).defaultHotelComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        activity.setTheme(R.style.Theme_Hotels_Default)
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelMaterialForms)
        val checkoutView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_checkout_presenter, null) as HotelCheckoutPresenter
        checkout = checkoutView.hotelCheckoutWidget
        checkout.paymentInfoCardView.viewmodel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
        checkout.setSearchParams(HotelPresenterTestUtil.getDummyHotelSearchParams(activity))
    }

    @Test
    fun testTrackMaterialEditTravelerDetails() {
        setupMaterialForms()
        checkout.travelerSummaryCardView.performClick()
        checkout.travelersPresenter.closeSubject.onNext(Unit)
        OmnitureTestUtils.assertStateTracked("App.Hotels.Checkout.Traveler.Edit.Info",
                OmnitureMatchers.withAbacusTestBucketed(24870),
                mockAnalyticsProvider)
    }

    @Test
    fun testTrackControlEditTravelerDetails() {
        checkout.mainContactInfoCardView.performClick()
        OmnitureTestUtils.assertStateTracked("App.Hotels.Checkout.Traveler.Edit.Info",
                OmnitureMatchers.withAbacusTestControl(24870),
                mockAnalyticsProvider)
    }

    @Test
    fun testTrackMaterialEditPaymentDetails() {
        setupMaterialForms()
        ExpediaBookingApp.setIsRobolectric(false)
        checkout.paymentInfoCardView.show(PaymentWidget.PaymentDefault())
        checkout.paymentInfoCardView.show(PaymentWidget.PaymentDetails())

        OmnitureTestUtils.assertStateTracked("App.Hotels.Checkout.Payment.Edit.Card",
                OmnitureMatchers.withAbacusTestBucketed(24870),
                mockAnalyticsProvider)
    }

    @Test
    fun testTrackControlEditPaymentDetails() {
        ExpediaBookingApp.setIsRobolectric(false)
        checkout.paymentInfoCardView.show(PaymentWidget.PaymentDefault())
        checkout.paymentInfoCardView.show(PaymentWidget.PaymentDetails())

        OmnitureTestUtils.assertStateTracked("App.Hotels.Checkout.Payment.Edit.Card",
                OmnitureMatchers.withAbacusTestControl(24870),
                mockAnalyticsProvider)
    }

    @Test
    fun testEditPaymentNotTrackedWhenGoingToFromDetailsToDefault() {
        ExpediaBookingApp.setIsRobolectric(false)
        checkout.paymentInfoCardView.show(PaymentWidget.PaymentDetails())
        checkout.paymentInfoCardView.show(PaymentWidget.PaymentDefault())

        OmnitureTestUtils.assertStateTrackedNumTimes("App.Hotels.Checkout.Payment.Edit.Card",
                OmnitureMatchers.withAbacusTestControl(24870),
                0, mockAnalyticsProvider)
    }

    @Test
    fun testEditPaymentNotTrackedWhenGoingToOptions() {
        ExpediaBookingApp.setIsRobolectric(false)
        checkout.paymentInfoCardView.show(PaymentWidget.PaymentDefault())
        checkout.paymentInfoCardView.show(PaymentWidget.PaymentOption())

        OmnitureTestUtils.assertStateTrackedNumTimes("App.Hotels.Checkout.Payment.Edit.Card",
                OmnitureMatchers.withAbacusTestControl(24870),
                0, mockAnalyticsProvider)
    }

    @Test
    fun testEditPaymentNotTrackedWhenGoingFromDetailsToOptions() {
        ExpediaBookingApp.setIsRobolectric(false)
        checkout.paymentInfoCardView.show(PaymentWidget.PaymentDetails())
        checkout.paymentInfoCardView.show(PaymentWidget.PaymentOption())

        OmnitureTestUtils.assertStateTrackedNumTimes("App.Hotels.Checkout.Payment.Edit.Card",
                OmnitureMatchers.withAbacusTestControl(24870),
                0, mockAnalyticsProvider)
    }

    @Test
    fun testEditPaymentTrackedWhenGoingFromOptionsToDetails() {
        ExpediaBookingApp.setIsRobolectric(false)
        checkout.paymentInfoCardView.show(PaymentWidget.PaymentOption())
        checkout.paymentInfoCardView.show(PaymentWidget.PaymentDetails())

        OmnitureTestUtils.assertStateTracked("App.Hotels.Checkout.Payment.Edit.Card",
                OmnitureMatchers.withAbacusTestControl(24870),
                mockAnalyticsProvider)
    }

    @Test
    fun testTrackControlEnterCouponWidget() {
        checkout.couponCardView.applied.visibility = View.GONE
        checkout.couponCardView.performClick()

        OmnitureTestUtils.assertLinkTracked("Universal Checkout", "App.CKO.Coupon",
                OmnitureMatchers.withAbacusTestControl(24870), mockAnalyticsProvider)
    }

    @Test
    fun testTrackMaterialEnterCouponWidget() {
        setupMaterialForms()
        checkout.couponCardView.applied.visibility = View.GONE
        checkout.couponCardView.performClick()

        OmnitureTestUtils.assertLinkTracked("Universal Checkout", "App.CKO.Coupon",
                OmnitureMatchers.withAbacusTestBucketed(24870), mockAnalyticsProvider)
    }

    private fun setupMaterialForms() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelMaterialForms)
        val checkoutView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_checkout_presenter, null) as HotelCheckoutPresenter
        checkout = checkoutView.hotelCheckoutWidget
        checkout.paymentInfoCardView.viewmodel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
        checkout.setSearchParams(HotelPresenterTestUtil.getDummyHotelSearchParams(activity))
    }
}
