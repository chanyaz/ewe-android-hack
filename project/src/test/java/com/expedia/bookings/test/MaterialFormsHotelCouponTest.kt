package com.expedia.bookings.test

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.presenter.hotel.HotelCheckoutMainViewPresenter
import com.expedia.bookings.presenter.hotel.HotelCheckoutPresenter
import com.expedia.bookings.presenter.shared.StoredCouponAdapter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.CouponTestUtil
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.MaterialFormsCouponWidget
import com.expedia.bookings.widget.getParentTextInputLayout
import com.expedia.vm.HotelCouponViewModel
import com.expedia.vm.HotelCreateTripViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class MaterialFormsHotelCouponTest {

    lateinit var couponWidget: MaterialFormsCouponWidget

    var service = ServicesRule(HotelServices::class.java)
        @Rule get

    private var activity: FragmentActivity by Delegates.notNull()

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    val mockHotelServices: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    private var checkout: HotelCheckoutMainViewPresenter by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        Ui.getApplication(RuntimeEnvironment.application).defaultHotelComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Db.sharedInstance.resetTravelers()
        activity.setTheme(R.style.Theme_Hotels_Default)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelMaterialForms)
        val checkoutView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_checkout_presenter, null) as HotelCheckoutPresenter
        checkout = checkoutView.hotelCheckoutWidget
        checkout.paymentInfoCardView.viewmodel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
        checkout.setSearchParams(HotelPresenterTestUtil.getDummyHotelSearchParams(activity))
        checkout.couponCardView.viewmodel = HotelCouponViewModel(activity, service.services!!, PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!))
        couponWidget = checkout.couponCardView as MaterialFormsCouponWidget
        couponWidget.isExpanded = false
    }

    @Test
    fun testApplyCouponError() {
        couponWidget.viewmodel.applyCouponViewModel.errorMessageObservable.onNext("Hello")
        couponWidget.showError(true)
        assertTrue(couponWidget.couponCode.getParentTextInputLayout()!!.isErrorEnabled)
        assertEquals("Hello", couponWidget.couponCode.getParentTextInputLayout()!!.error)
    }

    @Test
    fun testStoredCouponError() {
        val errorMessageTestObserver = TestObserver<String>()
        couponWidget.viewmodel.storedCouponViewModel.errorMessageObservable.subscribe(errorMessageTestObserver)

        couponWidget.viewmodel.storedCouponViewModel.storedCouponActionParam.onNext(CouponTestUtil.storedCouponParam(false, "hotel_coupon_errors_expired"))

        errorMessageTestObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)
        errorMessageTestObserver.assertValueCount(1)
        assertEquals("Sorry, but this coupon has expired.", errorMessageTestObserver.values()[0])
    }

    @Test
    fun testShowHotelCheckoutView() {
        goToCheckout(mockHotelServices.getHappyCreateTripResponse())
        couponWidget.isExpanded = true

        assertTrue(couponWidget.showHotelCheckoutView("1"))

        couponWidget.isExpanded = false

        assertTrue(checkout.couponCardView.showHotelCheckoutView("1"))

        couponWidget.storedCouponWidget.viewModel.storedCouponsSubject.onNext(CouponTestUtil.createStoredCouponAdapterData())
        couponWidget.isExpanded = true

        assertFalse(couponWidget.showHotelCheckoutView("1"))

        couponWidget.storedCouponWidget.viewModel.storedCouponsSubject.onNext(CouponTestUtil.createStoredCouponAdapterData())
        couponWidget.isExpanded = false

        assertTrue(couponWidget.showHotelCheckoutView("1"))
    }

    @Test
    fun testDataOnStoredCouponSubject() {
        val applyStoredCouponTestSubject = TestObserver.create<List<StoredCouponAdapter>>()
        goToCheckout(mockHotelServices.getHotelCouponCreateTripResponse())
        couponWidget.storedCouponWidget.viewModel.storedCouponsSubject.subscribe(applyStoredCouponTestSubject)
        checkout.createTripViewmodel.tripResponseObservable.onNext(mockHotelServices.getHotelCouponCreateTripResponse())

        assertEquals(3, applyStoredCouponTestSubject.values()[0].size)
    }

    private fun goToCheckout(withTripResponse: HotelCreateTripResponse = mockHotelServices.getHappyCreateTripResponse()) {
        checkout.createTripViewmodel = HotelCreateTripViewModel(mockHotelServices.services!!,
                PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!))
        checkout.showCheckout(HotelOffersResponse.HotelRoomResponse())
        checkout.createTripViewmodel.tripResponseObservable.onNext(withTripResponse)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCouponAppliedOnCKOIsNotVisible() {
        couponWidget.viewmodel.applyCouponViewModel.applyActionCouponParam.onNext(CouponTestUtil.applyCouponParam(false))

        assertTrue(couponWidget.applied.visibility == View.GONE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testStoredCouponAppliedOnCKOIsNotVisible() {
        couponWidget.viewmodel.storedCouponViewModel.storedCouponActionParam.onNext(CouponTestUtil.storedCouponParam(false))

        assertTrue(couponWidget.applied.visibility == View.GONE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCouponAppliedOnCKOIsVisible() {
        couponWidget.viewmodel.applyCouponViewModel.applyActionCouponParam.onNext(CouponTestUtil.applyCouponParam())

        assertTrue(couponWidget.applied.visibility == View.VISIBLE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testStoredCouponAppliedOnCKOIsVisible() {
        goToCheckout(mockHotelServices.getHappyCreateTripResponse())
        couponWidget.viewmodel.storedCouponViewModel.storedCouponActionParam.onNext(CouponTestUtil.storedCouponParam())

        assertTrue(couponWidget.applied.visibility == View.VISIBLE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCouponAppliedDetailsOnCheckoutScreen() {
        couponWidget.viewmodel.applyCouponViewModel.applyActionCouponParam.onNext(CouponTestUtil.applyCouponParam())

        assertEquals("Coupon applied!", couponWidget.appliedCouponMessage.text)
        assertEquals("Escape Friends and Family Coupon - Dec 2017 (-$11.03)", couponWidget.appliedCouponSubtitle.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testStoredCouponAppliedDetailsOnCheckoutScreen() {
        couponWidget.viewmodel.storedCouponViewModel.storedCouponActionParam.onNext(CouponTestUtil.storedCouponParam())

        assertEquals("Coupon applied!", couponWidget.appliedCouponMessage.text)
        assertEquals("Escape Friends and Family Coupon - Dec 2017 (-$11.03)", couponWidget.appliedCouponSubtitle.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCouponAppliedDetailsNoIdOnCheckoutScreen() {
        couponWidget.viewmodel.applyCouponViewModel.applyActionCouponParam.onNext(CouponTestUtil.applyCouponParam(success = false, couponCode = "hotel_coupon_success", tripId = "526abf74-430e-4449-b793-e072e0beecbf"))

        assertEquals("Coupon applied!", couponWidget.appliedCouponMessage.text)
        assertEquals("CL9XR34LHP6V3H2B (-$23.80)", couponWidget.appliedCouponSubtitle.text)
    }
}
