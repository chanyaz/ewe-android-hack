package com.expedia.bookings.test

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
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
import com.expedia.bookings.test.robolectric.CouponTestUtil
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.MaterialFormsCouponWidget
import com.expedia.bookings.widget.getParentTextInputLayout
import com.expedia.vm.HotelCreateTripViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class MaterialFormsHotelCouponTest {

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
    }

    @Test
    fun testCouponError() {
        checkout.couponCardView.viewmodel.errorMessageObservable.onNext("Hello")
        checkout.couponCardView.showError(true)
        assertTrue(checkout.couponCardView.couponCode.getParentTextInputLayout()!!.isErrorEnabled)
        assertEquals("Hello", checkout.couponCardView.couponCode.getParentTextInputLayout()!!.error)
    }

    @Test
    fun testShowHotelCheckoutView() {
        goToCheckout(mockHotelServices.getHappyCreateTripResponse())
        checkout.couponCardView.isExpanded = true

        assertTrue(checkout.couponCardView.showHotelCheckoutView("1"))

        checkout.couponCardView.isExpanded = false

        assertTrue(checkout.couponCardView.showHotelCheckoutView("1"))

        (checkout.couponCardView as MaterialFormsCouponWidget).storedCouponWidget.viewModel.storedCouponsSubject.onNext(CouponTestUtil.createStoredCouponAdapterData())
        checkout.couponCardView.isExpanded = true

        assertFalse(checkout.couponCardView.showHotelCheckoutView("1"))

        (checkout.couponCardView as MaterialFormsCouponWidget).storedCouponWidget.viewModel.storedCouponsSubject.onNext(CouponTestUtil.createStoredCouponAdapterData())
        checkout.couponCardView.isExpanded = false

        assertTrue(checkout.couponCardView.showHotelCheckoutView("1"))
    }

    @Test
    fun testDataOnStoredCouponSubject() {
        val applyStoredCouponTestSubject = TestSubscriber.create<List<StoredCouponAdapter>>()
        goToCheckout(mockHotelServices.getHotelCouponCreateTripResponse())
        (checkout.couponCardView as MaterialFormsCouponWidget).storedCouponWidget.viewModel.storedCouponsSubject.subscribe(applyStoredCouponTestSubject)
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
}
