package com.expedia.bookings.test

import android.app.Activity
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
import com.expedia.bookings.presenter.shared.StoredCouponAppliedStatus
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.LoyaltyServices
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

    private var vm: HotelCouponViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = RuntimeEnvironment.application
        vm = HotelCouponViewModel(context, service.services!!, PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!))
    }

    @Test
    fun testCouponError() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        val couponWidget = LayoutInflater.from(activity).inflate(R.layout.test_material_forms_coupon_widget_stub, null) as MaterialFormsCouponWidget
        couponWidget.viewmodel = vm
        vm.errorMessageObservable.onNext("Hello")
        couponWidget.showError(true)
        assertTrue(couponWidget.couponCode.getParentTextInputLayout()!!.isErrorEnabled)
        assertEquals("Hello", couponWidget.couponCode.getParentTextInputLayout()!!.error)
    }

    @Test
    fun testShowHotelCheckoutView() {
        setupHotelCheckout()
        checkout.couponCardView.isExpanded = true

        assertTrue(checkout.couponCardView.showHotelCheckoutView("1"))

        checkout.couponCardView.isExpanded = false

        assertTrue(checkout.couponCardView.showHotelCheckoutView("1"))

        (checkout.couponCardView as MaterialFormsCouponWidget).storedCouponWidget.viewModel.storedCouponsSubject.onNext(createStoredCouponAdapterData())
        checkout.couponCardView.isExpanded = true

        assertFalse(checkout.couponCardView.showHotelCheckoutView("1"))

        (checkout.couponCardView as MaterialFormsCouponWidget).storedCouponWidget.viewModel.storedCouponsSubject.onNext(createStoredCouponAdapterData())
        checkout.couponCardView.isExpanded = false

        assertTrue(checkout.couponCardView.showHotelCheckoutView("1"))
    }

    @Test
    fun testDataOnStoredCouponSubject() {
        val applyStoredCouponTestSubject = TestSubscriber.create<List<StoredCouponAdapter>>()
        setupHotelCheckout(mockHotelServices.getHotelCouponCreateTripResponse())
        (checkout.couponCardView as MaterialFormsCouponWidget).storedCouponWidget.viewModel.storedCouponsSubject.subscribe(applyStoredCouponTestSubject)
        checkout.createTripViewmodel.tripResponseObservable.onNext(mockHotelServices.getHotelCouponCreateTripResponse())

        assertEquals(3, applyStoredCouponTestSubject.onNextEvents[0].count())

        checkout.createTripViewmodel.tripResponseObservable.onNext(mockHotelServices.getHappyCreateTripResponse())

        assertEquals(0, applyStoredCouponTestSubject.onNextEvents[1].count())
    }

    fun setupHotelCheckout(withTripResponse: HotelCreateTripResponse = mockHotelServices.getHappyCreateTripResponse() ) {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        Ui.getApplication(RuntimeEnvironment.application).defaultHotelComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Db.sharedInstance.resetTravelers()
        activity.setTheme(R.style.Theme_Hotels_Default)
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppHotelMaterialForms, R.string.preference_enable_hotel_material_forms)
        val checkoutView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_checkout_presenter, null) as HotelCheckoutPresenter
        checkout = checkoutView.hotelCheckoutWidget
        checkout.paymentInfoCardView.viewmodel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
        checkout.setSearchParams(HotelPresenterTestUtil.getDummyHotelSearchParams(activity))
        goToCheckout(withTripResponse)
    }

    private fun goToCheckout(withTripResponse: HotelCreateTripResponse = mockHotelServices.getHappyCreateTripResponse()) {
        checkout.createTripViewmodel = HotelCreateTripViewModel(mockHotelServices.services!!,
                PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!))
        checkout.showCheckout(HotelOffersResponse.HotelRoomResponse())
        checkout.createTripViewmodel.tripResponseObservable.onNext(withTripResponse)
    }

    fun createStoredCouponAdapterData(): List<StoredCouponAdapter> {
        val savedCoupon1 = createSavedCoupon("A", "1")
        val storedCouponAdapter1 = StoredCouponAdapter(savedCoupon1, StoredCouponAppliedStatus.DEFAULT)

        return listOf<StoredCouponAdapter>(storedCouponAdapter1)
    }

    fun createSavedCoupon(name: String, instanceId: String, redemptionStatus: HotelCreateTripResponse.RedemptionStatus = HotelCreateTripResponse.RedemptionStatus.VALID): HotelCreateTripResponse.SavedCoupon {
        val savedCoupon = HotelCreateTripResponse.SavedCoupon()
        savedCoupon.instanceId = instanceId
        savedCoupon.name = name
        savedCoupon.redemptionStatus = redemptionStatus
        return savedCoupon
    }
}
