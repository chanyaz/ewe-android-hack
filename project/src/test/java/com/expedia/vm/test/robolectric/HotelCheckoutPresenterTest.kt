package com.expedia.vm.test.robolectric

import android.graphics.drawable.ColorDrawable
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.presenter.hotel.HotelCheckoutMainViewPresenter
import com.expedia.bookings.presenter.hotel.HotelCheckoutPresenter
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.CouponWidget
import com.expedia.bookings.widget.MaterialFormsCouponWidget
import com.expedia.vm.HotelCreateTripViewModel
import junit.framework.Assert.assertTrue
import com.expedia.bookings.widget.CheckoutBasePresenter
import com.expedia.bookings.widget.TravelerContactDetailsWidget
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelCheckoutPresenterTest {
    private var checkout: HotelCheckoutMainViewPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()
    val loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    val mockHotelServices: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        Ui.getApplication(RuntimeEnvironment.application).defaultHotelComponents()
        activity.setTheme(R.style.Theme_Hotels_Default)
        AbacusTestUtils.unbucketTestAndDisableFeature(activity, AbacusUtils.EBAndroidAppHotelMaterialForms, R.string.preference_enable_hotel_material_forms)
        val checkoutView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_checkout_presenter, null) as HotelCheckoutPresenter
        checkout = checkoutView.hotelCheckoutWidget
        checkout.paymentInfoCardView.viewmodel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
        checkout.setSearchParams(HotelPresenterTestUtil.getDummyHotelSearchParams(activity))
        goToCheckout()
    }

    @Test
    fun testMaterialCheckoutScrollViewColor() {
        setupHotelMaterialForms()
        checkout.paymentInfoCardView.viewmodel.menuVisibility.onNext(true)
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.white))
        checkout.paymentInfoCardView.viewmodel.menuVisibility.onNext(false)
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.gray100))
    }

    @Test
    fun testCheckoutScrollViewColor() {
        checkout.paymentInfoCardView.viewmodel.menuVisibility.onNext(true)
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.gray100))
        checkout.paymentInfoCardView.viewmodel.menuVisibility.onNext(false)
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.gray100))
    }

    @Test
    fun testMaterialCouponWidgetInflatedOnMaterialFormsBucketedAndFeatureToggleON() {
        setupHotelMaterialForms()
        assertTrue(checkout.couponCardView is MaterialFormsCouponWidget)
    }

    @Test
    fun testOldCouponWidgetInflatedOnMaterialFormsBucketedAndFeatureToggleOFF() {
        assertTrue(checkout.couponCardView is CouponWidget)
    }

    @Test
    fun testMaterialCouponWidgetApplyFiresDone() {
        val doneClickedSubscriber = TestSubscriber<Unit>()

        setupHotelMaterialForms()
        checkout.toolbar.viewModel.doneClicked.subscribe { doneClickedSubscriber.onNext(Unit) }

        doneClickedSubscriber.assertValueCount(0)

        checkout.toolbar.onMenuItemClicked(activity, "Apply")

        doneClickedSubscriber.assertValueCount(1)

        checkout.toolbar.onMenuItemClicked(activity, "Apply Button")

        doneClickedSubscriber.assertValueCount(2)
    }

    @Test
    fun testTravelerWidgetToolbarWithHotelMaterialABTestOn() {
        setupHotelMaterialForms()
        checkout.show(CheckoutBasePresenter.Ready())
        val travelerContactDetailsWidget = (LayoutInflater.from(activity).inflate(R.layout.test_traveler_contact_details_widget, null) as TravelerContactDetailsWidget)
        checkout.toolbar.viewModel.expanded.onNext(travelerContactDetailsWidget)
        checkout.mainContactInfoCardView.setExpanded(true)

        assertEquals("Done", checkout.menuDone.title.toString())
    }

    @Test
    fun testTravelerWidgetToolbarWithHotelMaterialABTestOff() {
        val doneMenuVisibilitySubscriber = TestSubscriber<Unit>()
        checkout.toolbar.viewModel.visibleMenuWithTitleDone.subscribe(doneMenuVisibilitySubscriber)


        checkout.show(CheckoutBasePresenter.Ready())
        val travelerContactDetailsWidget = (LayoutInflater.from(activity).inflate(R.layout.test_traveler_contact_details_widget, null) as TravelerContactDetailsWidget)
        checkout.toolbar.viewModel.expanded.onNext(travelerContactDetailsWidget)
        checkout.mainContactInfoCardView.setExpanded(true)

        assertEquals("Next", checkout.menuDone.title.toString())
    }

    @Test
    fun testCouponWidgetToolbarWhenExpandedWithHotelMaterialABTestTurnedOn() {
        setupHotelMaterialForms()
        checkout.show(CheckoutBasePresenter.Ready())
        val couponWidget = LayoutInflater.from(activity).inflate(R.layout.test_material_forms_coupon_widget_stub, null) as MaterialFormsCouponWidget
        checkout.toolbar.viewModel.expanded.onNext(couponWidget)

        assertEquals("Apply", checkout.menuDone.title.toString())
    }

    @Test
    fun testCouponWidgetToolbarWhenExpandedWithHotelMaterialABTestTurnedOff() {
        val checkoutView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_checkout_presenter, null) as HotelCheckoutPresenter
        checkout = checkoutView.hotelCheckoutWidget

        checkout.show(CheckoutBasePresenter.Ready())
        val couponWidget = LayoutInflater.from(activity).inflate(R.layout.coupon_widget_stub, null) as CouponWidget
        checkout.toolbar.viewModel.expanded.onNext(couponWidget)

        assertEquals("Submit", checkout.menuDone.title.toString())
    }
    
    fun testMaterialTravelerWidget() {
        setupHotelMaterialForms()

        assertEquals(View.VISIBLE, checkout.travelerSummaryCardView.visibility)
        assertEquals(View.GONE, checkout.mainContactInfoCardView.visibility)
    }

    @Test
    fun testTravelerWidget() {
        assertEquals(View.VISIBLE, checkout.mainContactInfoCardView.visibility)
        assertEquals(View.GONE, checkout.travelerSummaryCardView.visibility)
    }

    private fun setupHotelMaterialForms() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        Ui.getApplication(RuntimeEnvironment.application).defaultHotelComponents()
        activity.setTheme(R.style.Theme_Hotels_Default)
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppHotelMaterialForms, R.string.preference_enable_hotel_material_forms)
        val checkoutView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_checkout_presenter, null) as HotelCheckoutPresenter
        checkout = checkoutView.hotelCheckoutWidget
        checkout.setSearchParams(HotelPresenterTestUtil.getDummyHotelSearchParams(activity))
        checkout.paymentInfoCardView.viewmodel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
        goToCheckout()
    }

    private fun goToCheckout() {
        checkout.createTripViewmodel = HotelCreateTripViewModel(mockHotelServices.services!!,
                PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!))
        checkout.showCheckout(HotelOffersResponse.HotelRoomResponse())
        checkout.createTripViewmodel.tripResponseObservable.onNext(mockHotelServices.getHappyCreateTripResponse())
    }
}
