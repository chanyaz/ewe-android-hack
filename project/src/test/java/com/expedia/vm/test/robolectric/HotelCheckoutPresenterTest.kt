package com.expedia.vm.test.robolectric

import android.graphics.drawable.ColorDrawable
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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
import com.expedia.vm.traveler.HotelTravelersViewModel
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
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
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
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.material_checkout_background_color))
        checkout.paymentInfoCardView.viewmodel.menuVisibility.onNext(false)
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.checkout_overview_background_color))
    }

    @Test
    fun testCheckoutScrollViewColor() {
        checkout.paymentInfoCardView.viewmodel.menuVisibility.onNext(true)
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.checkout_overview_background_color))
        checkout.paymentInfoCardView.viewmodel.menuVisibility.onNext(false)
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.checkout_overview_background_color))
    }

    @Test
    fun testMaterialTravelerBackgroundColor() {
        setupHotelMaterialForms()
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.checkout_overview_background_color))

        checkout.travelerSummaryCardView.performClick()
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.material_checkout_background_color))
    }

    @Test
    fun testTravelerBackgroundColor() {
        setupHotelMaterialForms()
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.checkout_overview_background_color))

        checkout.mainContactInfoCardView.performClick()
        assertEquals((checkout.scrollView.background as ColorDrawable).color, ContextCompat.getColor(activity, R.color.checkout_overview_background_color))
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
        val doneMenuVisibilitySubscriber = TestSubscriber<Unit>()
        val toolbarTitleTestSubscriber = TestSubscriber<String>()
        checkout.toolbar.viewModel.visibleMenuWithTitleDone.subscribe(doneMenuVisibilitySubscriber)
        checkout.toolbar.viewModel.toolbarTitle.subscribe(toolbarTitleTestSubscriber)

        assertCheckoutOverviewToolbarTitleAndMenu()
        checkout.travelerSummaryCardView.performClick()

        doneMenuVisibilitySubscriber.assertValueCount(1)
        assertEquals("Done", checkout.menuDone.title.toString())
        assertTrue(checkout.menuDone.isVisible)
        toolbarTitleTestSubscriber.assertValue("Enter Traveler Details")
        assertEquals("Enter Traveler Details", checkout.toolbar.title)
    }

    @Test
    fun testCheckoutOverviewToolbarReturningFromTravelerMaterialTestOn() {
        setupHotelMaterialForms()
        val testToolbarNavIconContDescSubjectSubscriber = TestSubscriber<String>()
        checkout.travelersPresenter.toolbarNavIconContDescSubject.subscribe(testToolbarNavIconContDescSubjectSubscriber)
        checkout.travelerSummaryCardView.performClick()
        checkout.travelersPresenter.closeSubject.onNext(Unit)

        testToolbarNavIconContDescSubjectSubscriber.assertValue("Back")
        assertCheckoutOverviewToolbarTitleAndMenu()
    }

    @Test
    fun testCheckoutOverviewToolbarReturningFromTravelerMaterialTestOff() {
        checkout.mainContactInfoCardView.performClick()
        checkout.mainContactInfoCardView.isExpanded = false

        assertHotelOverviewVisibility(expectedVisibility = VISIBLE)
        assertCheckoutOverviewToolbarTitleAndMenu()
    }

    @Test
    fun testMaterialTravelerToolbarOnDoneClick() {
        setupHotelMaterialForms()
        val testDoneSubscriber = TestSubscriber<Unit>()
        checkout.travelersPresenter.doneClicked.subscribe(testDoneSubscriber)
        checkout.travelerSummaryCardView.performClick()
        checkout.toolbar.menuItem.actionView.performClick()

        testDoneSubscriber.assertNoValues()
    }

    @Test
    fun testTravelerWidgetToolbarWithHotelMaterialABTestOff() {
        val doneMenuVisibilitySubscriber = TestSubscriber<Unit>()
        checkout.toolbar.viewModel.visibleMenuWithTitleDone.subscribe(doneMenuVisibilitySubscriber)


        checkout.show(CheckoutBasePresenter.Ready())
        val travelerContactDetailsWidget = (LayoutInflater.from(activity).inflate(R.layout.test_traveler_contact_details_widget, null) as TravelerContactDetailsWidget)
        checkout.toolbar.viewModel.expanded.onNext(travelerContactDetailsWidget)
        checkout.mainContactInfoCardView.setExpanded(true)

        doneMenuVisibilitySubscriber.assertNoValues()
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

    @Test
    fun testMaterialEntryWidgetTravelerButtonLob() {
        setupHotelMaterialForms()

        assertEquals(LineOfBusiness.HOTELS, checkout.travelersPresenter.travelerEntryWidget.travelerButton.lineOfBusiness)
    }

    @Test
    fun testTravelersPresenterViewModel() {
        setupHotelMaterialForms()

        assertEquals(HotelTravelersViewModel::class.java, checkout.travelersPresenter.viewModel::class.java)
    }

    @Test
    fun testMaterialTravelerWidgetVisibility() {
        setupHotelMaterialForms()
        assertEquals(VISIBLE, checkout.travelerSummaryCardView.visibility)
        assertEquals(GONE, checkout.travelersPresenter.travelerEntryWidget.visibility)
        assertEquals(GONE, checkout.mainContactInfoCardView.visibility)

        checkout.travelerSummaryCardView.performClick()

        assertEquals(VISIBLE, checkout.travelersPresenter.travelerEntryWidget.visibility)
        assertEquals(GONE, checkout.travelerSummaryCardView.visibility)
        assertEquals(GONE, checkout.mainContactInfoCardView.visibility)
    }

    @Test
    fun testTravelerWidgetVisibility() {
        assertEquals(View.VISIBLE, checkout.mainContactInfoCardView.visibility)
        assertEquals(View.GONE, checkout.travelerSummaryCardView.visibility)

        checkout.mainContactInfoCardView.performClick()

        assertEquals(VISIBLE, checkout.mainContactInfoCardView.sectionTravelerInfo.visibility)
        assertEquals(GONE, checkout.travelerSummaryCardView.visibility)
    }
    
    @Test
    fun testMaterialHotelOverviewUiVisibility() {
        setupHotelMaterialForms()
        assertHotelOverviewVisibility(expectedVisibility = VISIBLE)
        checkout.travelerSummaryCardView.performClick()

        assertHotelOverviewVisibility(expectedVisibility = GONE)
    }

    @Test
    fun testHotelOverviewUiVisibility() {
        assertHotelOverviewVisibility(expectedVisibility = VISIBLE)
        checkout.mainContactInfoCardView.performClick()

        assertHotelOverviewVisibility(expectedVisibility = GONE)
    }

    @Test
    fun testCloseSubjectReturnsTravelerToOverview() {
        setupHotelMaterialForms()
        checkout.travelerSummaryCardView.performClick()
        checkout.travelersPresenter.closeSubject.onNext(Unit)

        assertHotelOverviewVisibility(expectedVisibility = VISIBLE)
        assertEquals(GONE, checkout.travelersPresenter.visibility)
        assertEquals(VISIBLE, checkout.travelerSummaryCardView.visibility)
    }

    private fun assertHotelOverviewVisibility(expectedVisibility: Int) {
        assertEquals(expectedVisibility, checkout.summaryContainer.visibility)
        assertEquals(expectedVisibility, checkout.paymentInfoCardView.visibility)
        assertEquals(expectedVisibility, checkout.loginWidget.visibility)
        assertEquals(expectedVisibility, checkout.couponContainer.visibility)
        assertEquals(expectedVisibility, checkout.legalInformationText.visibility)
        assertEquals(expectedVisibility, checkout.disclaimerText.visibility)
        assertEquals(expectedVisibility, checkout.depositPolicyText.visibility)
        assertEquals(expectedVisibility, checkout.space.visibility)
    }

    private fun assertCheckoutOverviewToolbarTitleAndMenu() {
        assertEquals("Checkout", checkout.toolbar.title)
        assertEquals("Next", checkout.menuDone.title)
        assertTrue(checkout.menuDone.isVisible)
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
