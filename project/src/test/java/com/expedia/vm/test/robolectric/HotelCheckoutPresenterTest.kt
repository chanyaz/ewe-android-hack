package com.expedia.vm.test.robolectric

import android.graphics.drawable.ColorDrawable
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelCheckoutV2Params
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.user.User
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.presenter.hotel.HotelCheckoutMainViewPresenter
import com.expedia.bookings.presenter.hotel.HotelCheckoutPresenter
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.tracking.FacebookEvents.Companion.userStateManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.CouponWidget
import com.expedia.bookings.widget.MaterialFormsCouponWidget
import com.expedia.vm.HotelCreateTripViewModel
import junit.framework.Assert.assertTrue
import com.expedia.bookings.widget.CheckoutBasePresenter
import com.expedia.bookings.widget.TravelerContactDetailsWidget
import com.expedia.vm.test.traveler.MockTravelerProvider
import com.expedia.vm.traveler.HotelTravelersViewModel
import org.junit.After
import com.expedia.vm.HotelCheckoutViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelCheckoutPresenterTest {
    private var checkout: HotelCheckoutMainViewPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()
    val loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    val mockHotelServices: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get
    val mockTravelerProvider = MockTravelerProvider()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        Ui.getApplication(RuntimeEnvironment.application).defaultHotelComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Db.resetTravelers()
        activity.setTheme(R.style.Theme_Hotels_Default)
        AbacusTestUtils.unbucketTestAndDisableFeature(activity, AbacusUtils.EBAndroidAppHotelMaterialForms, R.string.preference_enable_hotel_material_forms)
        val checkoutView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_checkout_presenter, null) as HotelCheckoutPresenter
        checkout = checkoutView.hotelCheckoutWidget
        checkout.paymentInfoCardView.viewmodel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
        checkout.setSearchParams(HotelPresenterTestUtil.getDummyHotelSearchParams(activity))
        goToCheckout()
    }

    @After
    fun tearDown() {
        Db.resetTravelers()
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
    fun testMaterialCouponWidgetApplyFiresMenuButtonPressed() {
        val testEnableSubmitButtonObservable = TestSubscriber<Boolean>()
        val testOnCouponSubmitClicked = TestSubscriber<Unit>()

        setupHotelMaterialForms()
        checkout.couponCardView.viewmodel.enableSubmitButtonObservable.subscribe(testEnableSubmitButtonObservable)
        checkout.couponCardView.onCouponSubmitClicked.subscribe(testOnCouponSubmitClicked)
        testEnableSubmitButtonObservable.assertValueCount(0)
        testOnCouponSubmitClicked.assertNoValues()

        checkout.couponCardView.performClick()
        checkout.toolbar.viewModel.onMenuItemClicked("Apply")
        testEnableSubmitButtonObservable.assertValue(false)
        testOnCouponSubmitClicked.assertValueCount(1)

        checkout.toolbar.viewModel.onMenuItemClicked("Apply Button")
        testEnableSubmitButtonObservable.assertValues(false, false)
        testOnCouponSubmitClicked.assertValueCount(2)
    }

    @Test
    fun testToolbarReceivesOnDoneClickedMethodDefaultTravelerWidget() {
        val testDoneClickedSubscriber = TestSubscriber<() -> Unit>()
        checkout.toolbar.viewModel.doneClickedMethod.subscribe(testDoneClickedSubscriber)
        checkout.mainContactInfoCardView.performClick()

        testDoneClickedSubscriber.assertValueCount(1)
    }

    @Test
    fun testToolbarReceivesOnDoneClickedMethodMaterialTraveler() {
        setupHotelMaterialForms()
        val testDoneClickedSubscriber = TestSubscriber<() -> Unit>()
        checkout.toolbar.viewModel.doneClickedMethod.subscribe(testDoneClickedSubscriber)
        checkout.travelerSummaryCardView.performClick()

        testDoneClickedSubscriber.assertValueCount(1)
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
        toolbarTitleTestSubscriber.assertValues("Edit Traveler 1 (Adult)", "Enter Traveler Details")
        assertEquals("Enter Traveler Details", checkout.toolbar.title)
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

    @Test
    fun testLoggedInTravelerSummaryCard() {
        setupHotelMaterialForms()

        givenLoggedInUserAndTravelerInDb()
        goToCheckout()
        checkout.travelerSummaryCard.viewModel.travelerStatusObserver.onNext(TravelerCheckoutStatus.COMPLETE)

        assertTravelerSummaryCard(expectedTitle = "Oscar The Grouch",
                expectedSubtitle = "773202LUNA",
                expectedStatus = TravelerCheckoutStatus.COMPLETE)
    }

    @Test
    fun testLoggedInUserEmailUsedForOtherTravelers() {
        setupHotelMaterialForms()
        checkout.travelerSummaryCardView.performClick()

        givenLoggedInUserAndTravelerInDb()
        val incompleteTraveler = Traveler()
        checkout.travelersPresenter.travelerEntryWidget.viewModel.updateTraveler(incompleteTraveler)

        assertEquals(incompleteTraveler, Db.getTravelers()[0])
        assertEquals("test@gmail.com", Db.getTravelers()[0].email)
    }

    @Test
    fun testTravelerCardUpdatesFromEntryFormWithIncompleteTraveler() {
        setupHotelMaterialForms()
        checkout.travelerSummaryCardView.performClick()

        givenLoggedInUserAndTravelerInDb()
        val incompleteTraveler = Traveler()
        incompleteTraveler.firstName = "test"
        checkout.travelersPresenter.travelerEntryWidget.viewModel.updateTraveler(incompleteTraveler)
        checkout.travelersPresenter.closeSubject.onNext(Unit)

        assertTravelerSummaryCard(expectedTitle = "test",
                expectedSubtitle = "",
                expectedStatus = TravelerCheckoutStatus.DIRTY)
    }

    @Test
    fun testTravelerCheckoutParamsWhenHotelMaterialFormIsTurnedOn() {
        val testCheckoutParams = TestSubscriber<HotelCheckoutV2Params>()
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppHotelMaterialForms, R.string.preference_enable_hotel_material_forms)
        val checkoutView = setupHotelCheckoutPresenter()
        checkoutView.hotelCheckoutViewModel.checkoutParams.subscribe(testCheckoutParams)
        var paymentSplits = PaymentSplits(PointsAndCurrency(771.40f, PointsType.BURN, Money("0", "USD")),
                PointsAndCurrency(0f, PointsType.EARN, Money("0", "USD")))

        checkoutView.onBookV2(null, paymentSplits)

        assertNotNull(testCheckoutParams.onNextEvents[0])
        testTravelerDetails(testCheckoutParams.onNextEvents[0].traveler)
    }

    @Test
    fun testTravelerCheckoutParamsWhenHotelMaterialFormIsTurnedOff() {
        val testCheckoutParams = TestSubscriber<HotelCheckoutV2Params>()
        val checkoutView = setupHotelCheckoutPresenter()
        checkoutView.hotelCheckoutViewModel.checkoutParams.subscribe(testCheckoutParams)
        var paymentSplits = PaymentSplits(PointsAndCurrency(771.40f, PointsType.BURN, Money("0", "USD")),
                PointsAndCurrency(0f, PointsType.EARN, Money("0", "USD")))

        checkoutView.onBookV2(null, paymentSplits)

        assertNotNull(testCheckoutParams.onNextEvents[0])
        testTravelerDetails(testCheckoutParams.onNextEvents[0].traveler)
    }

    @Test
    fun testHotelMaterialCouponExpandWithCouponSavedABTestOn() {
        setupHotelMaterialForms()
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppSavedCoupons)
        checkout.couponCardView.setExpanded(true)

        assertEquals(View.VISIBLE, (checkout.couponCardView as MaterialFormsCouponWidget).storedCouponWidget.visibility)
    }

    @Test
    fun testHotelMaterialCouponExpandWithCouponSavedABTestOff() {
        setupHotelMaterialForms()
        checkout.couponCardView.setExpanded(true)

        assertEquals(View.GONE, (checkout.couponCardView as MaterialFormsCouponWidget).storedCouponWidget.visibility)
    }

    private fun givenLoggedInUserAndTravelerInDb() {
        val testUser = User()
        val traveler = mockTravelerProvider.getCompleteMockTraveler()
        testUser.primaryTraveler = traveler
        userStateManager?.userSource?.user = testUser
        UserLoginTestUtil.setupUserAndMockLogin(testUser)
        Db.setTravelers(listOf(traveler))
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

    private fun setupHotelCheckoutPresenter(): HotelCheckoutPresenter {
        val checkoutView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_checkout_presenter, null) as HotelCheckoutPresenter
        checkoutView.hotelCheckoutViewModel = HotelCheckoutViewModel(mockHotelServices.services!!, PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!))
        Db.setTravelers(listOf(makeTraveler()))
        checkoutView.hotelCheckoutWidget.mainContactInfoCardView.sectionTravelerInfo.bind(makeTraveler())
        val v4 = SuggestionV4()
        v4.regionNames = SuggestionV4.RegionNames()
        v4.regionNames.fullName = "Las Vegas, NV"
        checkoutView.hotelSearchParams = HotelSearchParams(v4, LocalDate.now(), LocalDate.now().plusDays(3), 2, listOf(0), false, null, null)
        return checkoutView
    }

    private fun goToCheckout() {
        checkout.createTripViewmodel = HotelCreateTripViewModel(mockHotelServices.services!!,
                PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!))
        checkout.showCheckout(HotelOffersResponse.HotelRoomResponse())
        checkout.createTripViewmodel.tripResponseObservable.onNext(mockHotelServices.getHappyCreateTripResponse())
    }

    private fun assertTravelerSummaryCard(expectedTitle: String, expectedSubtitle: String, expectedStatus: TravelerCheckoutStatus) {
        assertEquals(expectedTitle, checkout.travelerSummaryCard.viewModel.getTitle())
        assertEquals(expectedSubtitle, checkout.travelerSummaryCard.viewModel.getSubtitle())
        assertEquals(expectedStatus, checkout.travelerSummaryCard.getStatus())
    }

    private fun makeTraveler(): Traveler {
        val traveler = Traveler()
        traveler.firstName = "JexperCC"
        traveler.lastName = "MobiataTestaverde"
        traveler.birthDate = LocalDate()
        traveler.email = "qa-ehcc@mobiata.com"
        traveler.phoneNumber = "4155555555"
        traveler.phoneCountryCode = "US"
        return traveler
    }

    private fun testTravelerDetails(traveler: com.expedia.bookings.data.payment.Traveler) {
        assertEquals("qa-ehcc@mobiata.com", traveler.email)
        assertEquals("JexperCC", traveler.firstName)
        assertEquals("MobiataTestaverde", traveler.lastName)
        assertEquals("4155555555", traveler.phone)
        assertEquals("US", traveler.phoneCountryCode)
    }
}
