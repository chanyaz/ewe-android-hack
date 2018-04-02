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
import com.expedia.bookings.presenter.shared.StoredCouponListAdapter
import com.expedia.bookings.presenter.shared.StoredCouponViewHolder
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
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.extensions.getParentTextInputLayout
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
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppHotelMaterialForms)
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
    fun testStoredCouponErrorClearedOnExpanded() {
        val errorMessageTestObserver = TestObserver<String>()
        couponWidget.viewmodel.storedCouponViewModel.errorMessageObservable.subscribe(errorMessageTestObserver)
        couponWidget.storedCouponWidget.viewModel.storedCouponsSubject.onNext(CouponTestUtil.createStoredCouponAdapterData())

        couponWidget.viewmodel.onCouponWidgetExpandSubject.onNext(true)

        errorMessageTestObserver.assertValueCount(1)
        assertEquals("", errorMessageTestObserver.values()[0])
    }

    @Test
    fun testStoredCouponErrorNotClearedOnExpandWithNoCoupons() {
        val errorMessageTestObserver = TestObserver<String>()
        couponWidget.viewmodel.storedCouponViewModel.errorMessageObservable.subscribe(errorMessageTestObserver)
        couponWidget.storedCouponWidget.viewModel.storedCouponsSubject.onNext(listOf())

        couponWidget.viewmodel.onCouponWidgetExpandSubject.onNext(true)

        errorMessageTestObserver.assertValueCount(0)
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
    fun testSavedCouponHeaderTextFocusability() {
        val savedText = couponWidget.findViewById<TextView>(R.id.header_text_view)

        assertTrue(savedText.isFocusable)
        assertTrue(savedText.isFocusableInTouchMode)
    }

    @Test
    fun testStoredCouponContentDescription() {
        val couponAdapter = couponWidget.storedCouponWidget.storedCouponRecyclerView.adapter as StoredCouponListAdapter
        couponAdapter.coupons.addAll(CouponTestUtil.createStoredCouponAdapterData())
        val viewHolder = couponAdapter.onCreateViewHolder(couponWidget, 0) as StoredCouponViewHolder
        couponAdapter.bindViewHolder(viewHolder, 0)

        assertEquals("A", viewHolder.couponNameTextView.text)
        assertEquals("A Button", viewHolder.couponNameTextView.contentDescription)
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

    @Test
    fun testAppliedCouponRequestsFocusOnClosed() {
        couponWidget.isExpanded = true
        couponWidget.viewmodel.hasDiscountObservable.onNext(true)
        couponWidget.viewmodel.applyCouponViewModel.applyCouponSuccessObservable.onNext(HotelCreateTripResponse())

        assertTrue(couponWidget.applied.hasFocus())
        assertFalse(couponWidget.unexpanded.hasFocus())
    }

    @Test
    fun testUnexpandedTextFocusedOnCouponClosed() {
        couponWidget.isExpanded = true
        couponWidget.isExpanded = false

        assertTrue(couponWidget.unexpanded.hasFocus())
        assertFalse(couponWidget.applied.hasFocus())
    }

    @Test
    fun testFunctionalityOfClickOnStoredCouponViewHolder() {
        val testSubscriber = TestObserver.create<HotelCreateTripResponse.SavedCoupon>()
        couponWidget.viewmodel.storedCouponViewModel.applyStoredCouponObservable.subscribe(testSubscriber)
        val couponAdapter = couponWidget.storedCouponWidget.storedCouponRecyclerView.adapter as StoredCouponListAdapter
        couponAdapter.coupons.addAll(CouponTestUtil.createStoredCouponAdapterData())
        couponAdapter.applyStoredCouponObservable.onNext(0)

        assertEquals("1", testSubscriber.values()[0].instanceId)
        assertEquals("A", testSubscriber.values()[0].name)
    }

    @Test
    fun testApplyCouponErrorClearedAfterStoredCouponClicked() {
        couponWidget.viewmodel.applyCouponViewModel.errorMessageObservable.onNext("Hello")
        couponWidget.showError(true)

        val couponAdapter = couponWidget.storedCouponWidget.storedCouponRecyclerView.adapter as StoredCouponListAdapter
        couponAdapter.coupons.addAll(CouponTestUtil.createStoredCouponAdapterData())
        couponAdapter.applyStoredCouponObservable.onNext(0)

        assertEquals(null, couponWidget.couponCode.getParentTextInputLayout()!!.error)
        assertFalse(couponWidget.couponCode.getParentTextInputLayout()!!.isErrorEnabled)
    }

    @Test
    fun testStoredCouponErrorClearedAfterManuallyApplyingCoupon() {
        val errorMessageTestObserver = TestObserver<String>()
        couponWidget.viewmodel.storedCouponViewModel.errorMessageObservable.subscribe(errorMessageTestObserver)

        couponWidget.viewmodel.storedCouponViewModel.storedCouponActionParam.onNext(CouponTestUtil.storedCouponParam(false, "hotel_coupon_errors_expired"))

        errorMessageTestObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)
        errorMessageTestObserver.assertValueCount(1)
        assertEquals("Sorry, but this coupon has expired.", errorMessageTestObserver.values()[0])

        couponWidget.viewmodel.applyCouponViewModel.applyActionCouponParam.onNext(CouponTestUtil.applyCouponParam(true))

        errorMessageTestObserver.assertValueCount(2)
        assertEquals("", errorMessageTestObserver.values()[1])
    }
}
