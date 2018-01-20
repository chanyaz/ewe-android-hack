package com.expedia.bookings.test

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelApplyCouponCodeParameters
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.presenter.hotel.HotelCheckoutMainViewPresenter
import com.expedia.bookings.presenter.hotel.HotelCheckoutPresenter
import com.expedia.bookings.presenter.shared.StoredCouponAdapter
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.LoyaltyServices
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
import rx.observers.TestSubscriber
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
    fun testCouponError() {
        couponWidget.viewmodel.errorMessageObservable.onNext("Hello")
        couponWidget.showError(true)
        assertTrue(couponWidget.couponCode.getParentTextInputLayout()!!.isErrorEnabled)
        assertEquals("Hello", couponWidget.couponCode.getParentTextInputLayout()!!.error)
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
        val applyStoredCouponTestSubject = TestSubscriber.create<List<StoredCouponAdapter>>()
        goToCheckout(mockHotelServices.getHotelCouponCreateTripResponse())
        couponWidget.storedCouponWidget.viewModel.storedCouponsSubject.subscribe(applyStoredCouponTestSubject)
        checkout.createTripViewmodel.tripResponseObservable.onNext(mockHotelServices.getHotelCouponCreateTripResponse())

        assertEquals(3, applyStoredCouponTestSubject.onNextEvents[0].count())

        checkout.createTripViewmodel.tripResponseObservable.onNext(mockHotelServices.getHappyCreateTripResponse())

        assertEquals(0, applyStoredCouponTestSubject.onNextEvents[1].count())
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
        val pointsDetails = UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(1000f, PointsType.BURN, Money("100", "USD")))
        val couponParams = HotelApplyCouponCodeParameters.Builder()
                .tripId("bc9fec5d-7539-41e9-ab60-3e593f0912fe")
                .isFromNotSignedInToSignedIn(false)
                .couponCode("not_valid")
                .userPreferencePointsDetails(listOf(pointsDetails))
                .build()
        couponWidget.viewmodel.couponParamsObservable.onNext(couponParams)

        assertTrue(couponWidget.applied.visibility == View.GONE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCouponAppliedOnCKOIsVisible() {
        val pointsDetails = UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(1000f, PointsType.BURN, Money("100", "USD")))
        val couponParams = HotelApplyCouponCodeParameters.Builder()
                .tripId("bc9fec5d-7539-41e9-ab60-3e593f0912fe")
                .isFromNotSignedInToSignedIn(false)
                .couponCode("happypath_createtrip_saved_coupons_select")
                .userPreferencePointsDetails(listOf(pointsDetails))
                .build()
        couponWidget.viewmodel.couponParamsObservable.onNext(couponParams)

        assertTrue(couponWidget.applied.visibility == View.VISIBLE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCouponAppliedDetailsOnCheckoutScreen() {
        val pointsDetails = UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(1000f, PointsType.BURN, Money("100", "USD")))
        val couponParams = HotelApplyCouponCodeParameters.Builder()
                .tripId("bc9fec5d-7539-41e9-ab60-3e593f0912fe")
                .isFromNotSignedInToSignedIn(false)
                .couponCode("happypath_createtrip_saved_coupons_select")
                .userPreferencePointsDetails(listOf(pointsDetails))
                .build()
        couponWidget.viewmodel.couponParamsObservable.onNext(couponParams)

        assertEquals("Coupon applied!", couponWidget.appliedCouponMessage.text)
        assertEquals("Escape Friends and Family Coupon - Dec 2017 (-$11.03)", couponWidget.appliedCouponSubtitle.text)
    }
}
