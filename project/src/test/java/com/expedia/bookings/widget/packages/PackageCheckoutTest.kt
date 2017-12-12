package com.expedia.bookings.widget.packages

import android.support.v4.app.FragmentActivity
import android.view.View
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.packages.PackageOverviewPresenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.bookings.widget.PackageCheckoutPresenter
import com.expedia.vm.packages.BundleOverviewViewModel
import com.expedia.vm.test.traveler.MockTravelerProvider
import com.expedia.vm.traveler.TravelerSelectItemViewModel
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
class PackageCheckoutTest {

    val server = MockWebServer()
    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    lateinit var travelerValidator: TravelerValidator
    lateinit var userStateManager: UserStateManager

    private var checkout: PackageCheckoutPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()
    private var overview : PackageOverviewPresenter by Delegates.notNull()

    @Before fun before() {
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Ui.getApplication(RuntimeEnvironment.application).defaultPackageComponents()
        travelerValidator = Ui.getApplication(RuntimeEnvironment.application).travelerComponent().travelerValidator()
        setUpPackageDb()
        travelerValidator.updateForNewSearch(Db.getPackageParams())
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.package_overview_test)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
        overview = activity.findViewById<View>(R.id.package_overview_presenter) as PackageOverviewPresenter

        userStateManager = Ui.getApplication(RuntimeEnvironment.application).appComponent().userStateManager()

        setUpCheckout()
    }

    @Test
    fun testCheckoutSuccess() {
        createTrip()
        enterValidTraveler()
        enterValidPayment()

        assertEquals(TravelerCheckoutStatus.COMPLETE, checkout.travelerSummaryCard.getStatus())
        assertEquals(ContactDetailsCompletenessStatus.COMPLETE, checkout.paymentWidget.paymentStatusIcon.status)
        assertEquals(View.VISIBLE, overview.bottomCheckoutContainer.totalPriceWidget.visibility)
        assertEquals(true, checkout.getCheckoutViewModel().builder.hasValidTravelerAndBillingInfo())
        assertEquals(0f, overview.bottomCheckoutContainer.slideToPurchaseLayout.translationY)
        assertEquals(View.GONE, overview.bottomCheckoutContainer.slideTotalText.visibility)
        assertEquals(View.VISIBLE, overview.bottomCheckoutContainer.slideToPurchaseSpace.visibility)
    }

        @Test
    fun testCheckoutSuccessWithResortFee() {
        createTripWithResortFee()
        enterValidTraveler()
        enterValidPayment()
        checkout.getCheckoutViewModel().bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.CHECKOUT)

        assertEquals(TravelerCheckoutStatus.COMPLETE, checkout.travelerSummaryCard.getStatus())
        assertEquals(ContactDetailsCompletenessStatus.COMPLETE, checkout.paymentWidget.paymentStatusIcon.status)
        assertEquals(View.VISIBLE, overview.bottomCheckoutContainer.totalPriceWidget.visibility)
        assertEquals(true, checkout.getCheckoutViewModel().builder.hasValidTravelerAndBillingInfo())
        assertEquals(0f, overview.bottomCheckoutContainer.slideToPurchaseLayout.translationY)
        assertEquals(View.VISIBLE, overview.bottomCheckoutContainer.slideTotalText.visibility)
        assertEquals("Your card will be charged $173.68", overview.bottomCheckoutContainer.slideTotalText.text)
        assertEquals(View.GONE, overview.bottomCheckoutContainer.slideToPurchaseSpace.visibility)
    }

    @Test
    fun testSlideTotalTextVisibilityDependsOnResortFeesAndOverviewState() {
        createTripWithResortFee()
        enterValidTraveler()
        enterValidPayment()

        assertEquals(View.GONE, overview.bottomCheckoutContainer.slideTotalText.visibility)
        assertEquals("Your card will be charged $173.68", overview.bottomCheckoutContainer.slideTotalText.text)
        assertEquals(View.VISIBLE, overview.bottomCheckoutContainer.slideToPurchaseSpace.visibility)

        checkout.getCheckoutViewModel().bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.CHECKOUT)

        assertEquals(View.VISIBLE, overview.bottomCheckoutContainer.slideTotalText.visibility)
        assertEquals("Your card will be charged $173.68", overview.bottomCheckoutContainer.slideTotalText.text)
        assertEquals(View.GONE, overview.bottomCheckoutContainer.slideToPurchaseSpace.visibility)

        val createTripParams = PackageCreateTripParams("create_trip", "", 1, false, emptyList())
        checkout.getCreateTripViewModel().tripParams.onNext(createTripParams)
        checkout.getCheckoutViewModel().bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.CHECKOUT)

        assertEquals(View.GONE, overview.bottomCheckoutContainer.slideTotalText.visibility)
        assertEquals("", overview.bottomCheckoutContainer.slideTotalText.text)
        assertEquals(View.VISIBLE, overview.bottomCheckoutContainer.slideToPurchaseSpace.visibility)
    }

    @Test
    fun testCheckoutError() {
        val errorResponseSubscriber = TestSubscriber<ApiError>()
        checkout.getCheckoutViewModel().checkoutErrorObservable.subscribe(errorResponseSubscriber)

        val checkoutResponseSubscriber = TestSubscriber<Pair<BaseApiResponse, String>>()
        checkout.getCheckoutViewModel().bookingSuccessResponse.subscribe(checkoutResponseSubscriber)

        createTrip()
        enterValidTraveler()
        enterValidPayment()
        (checkout.paymentWidget as BillingDetailsPaymentWidget).addressLineOne.setText("errorcheckoutcard")
        checkout.paymentWidget.validateAndBind()
        overview.bottomCheckoutContainer.onSlideAllTheWay()

        errorResponseSubscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
        errorResponseSubscriber.assertValueCount(1)
        errorResponseSubscriber.assertValue(ApiError(ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS))

        (checkout.paymentWidget as BillingDetailsPaymentWidget).addressLineOne.setText("1735 Steiner st")
        (checkout.paymentWidget as BillingDetailsPaymentWidget).creditCardCvv.setText("123")
        checkout.paymentWidget.validateAndBind()
        overview.bottomCheckoutContainer.onSlideAllTheWay()

        checkoutResponseSubscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
        checkoutResponseSubscriber.assertValueCount(1)

        assertEquals("malcolmnguyen@gmail.com", checkoutResponseSubscriber.onNextEvents[0].second)
    }

    @Test
    fun testCheckoutErrorClearsCvv() {
        val clearCvvSubscriber = TestSubscriber<Unit>()
        checkout.getCheckoutViewModel().clearCvvObservable.subscribe(clearCvvSubscriber)
        val billingInfo = getBillingInfo()
        Db.setBillingInfo(billingInfo)
        Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(billingInfo)
        (checkout.paymentWidget as BillingDetailsPaymentWidget).creditCardCvv.setText(billingInfo.securityCode)

        assertEquals("111", Db.getBillingInfo().securityCode)
        assertEquals("111", (Db.getWorkingBillingInfoManager().workingBillingInfo.securityCode))
        assertEquals("111", (checkout.paymentWidget as BillingDetailsPaymentWidget).creditCardCvv.text.toString())

        checkout.getCheckoutViewModel().checkoutErrorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))

        clearCvvSubscriber.assertValueCount(1)
        assertNull(Db.getBillingInfo().securityCode)
        assertNull(Db.getWorkingBillingInfoManager().workingBillingInfo.securityCode)
        assertEquals("", (checkout.paymentWidget as BillingDetailsPaymentWidget).creditCardCvv.text.toString())
    }

    @Test
    fun testLoggedInUserPaymentStatusMultipleCards() {
        val testUserLoggedIn = TestSubscriber<Boolean>()
        checkout.paymentWidget.viewmodel.userLogin.subscribe(testUserLoggedIn)
        createTrip()

        val testUser = User()
        val testInvalidCard = setUpCreditCards("1234567890123456", "testInvalid", PaymentType.CARD_UNKNOWN, "1")
        val testCard1 = setUpCreditCards("4111111111111111", "testVisa", PaymentType.CARD_VISA, "2")
        val testCard2 = setUpCreditCards("6111111111111111", "testDiscover", PaymentType.CARD_DISCOVER, "3")

        testUser.addStoredCreditCard(testInvalidCard)
        testUser.addStoredCreditCard(testCard1)
        testUser.addStoredCreditCard(testCard2)
        testUser.primaryTraveler = enterTraveler(Traveler())
        userStateManager.userSource.user = testUser
        UserLoginTestUtil.setupUserAndMockLogin(testUser)

        checkout.onLoginSuccess()

        assertNotEquals(checkout.paymentWidget.sectionBillingInfo.billingInfo.storedCard, testInvalidCard)
        assertEquals(checkout.paymentWidget.sectionBillingInfo.billingInfo.storedCard, testCard1)
        assertEquals(ContactDetailsCompletenessStatus.COMPLETE, checkout.paymentWidget.paymentStatusIcon.status)
    }

    @Test
    fun testLoggedInUserPaymentStatusNoValidCards() {
        val testUserLoggedIn = TestSubscriber<Boolean>()
        checkout.paymentWidget.viewmodel.userLogin.subscribe(testUserLoggedIn)
        createTrip()

        val testUser = User()
        val testFirstInvalidCard = setUpCreditCards("1234567890123456", "testInvalid", PaymentType.CARD_UNKNOWN, "1")
        val testSecondInvalidCard = setUpCreditCards("6543210987654321", "testInvalidOther", PaymentType.CARD_CHINA_UNION_PAY, "2")
        val testThirdInvalidCard = setUpCreditCards("0000000000000000", "testInvalidLast", PaymentType.CARD_CARTE_BLEUE, "3")
        testUser.addStoredCreditCard(testFirstInvalidCard)
        testUser.addStoredCreditCard(testSecondInvalidCard)
        testUser.addStoredCreditCard(testThirdInvalidCard)
        testUser.primaryTraveler = enterTraveler(Traveler())
        userStateManager.userSource.user = testUser
        UserLoginTestUtil.setupUserAndMockLogin(testUser)

        checkout.onLoginSuccess()

        testUserLoggedIn.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(true, testUserLoggedIn.onNextEvents[0])
        assertEquals(checkout.paymentWidget.sectionBillingInfo.billingInfo.storedCard, null)
        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, checkout.paymentWidget.paymentStatusIcon.status)
    }

    @Test
    fun testGuestPaymentInfoClearedAfterUserLogsIn() {
        val testUserLoggedIn = TestSubscriber<Boolean>()
        checkout.paymentWidget.viewmodel.userLogin.subscribe(testUserLoggedIn)

        createTrip()
        enterValidTraveler()
        enterValidPayment()

        assertEquals(TravelerCheckoutStatus.COMPLETE, checkout.travelerSummaryCard.getStatus())
        assertEquals(ContactDetailsCompletenessStatus.COMPLETE, checkout.paymentWidget.paymentStatusIcon.status)
        assertEquals(View.VISIBLE, overview.bottomCheckoutContainer.totalPriceWidget.visibility)
        assertEquals(true, checkout.getCheckoutViewModel().builder.hasValidTravelerAndBillingInfo())
        assertEquals(0f, overview.bottomCheckoutContainer.slideToPurchaseLayout.translationY)

        val testUser = User()
        testUser.primaryTraveler = enterTraveler(Traveler())
        userStateManager.userSource.user = testUser
        UserLoginTestUtil.setupUserAndMockLogin(testUser)
        checkout.onLoginSuccess()

        testUserLoggedIn.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(true, testUserLoggedIn.onNextEvents[0])
        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, checkout.paymentWidget.paymentStatusIcon.status)
    }

    @Test
    fun testTravelerChangeShouldShowUpdateTravelerDialogForMainTravelerOnDoneClick() {
        givenCompletedTravelerEntryWidget()
        assertEquals("malcolm", Db.getTravelers()[0].firstName)
        checkout.travelersPresenter.travelerEntryWidget.viewModel.nameViewModel.firstNameViewModel.textSubject.onNext("Billy")

        checkout.travelersPresenter.onDoneClicked()
        assertUpdateTravelerDialog()
    }

    @Test
    fun testTravelerChangeShouldShowUpdateTravelerDialogForMainTravelerOnBack() {
        givenCompletedTravelerEntryWidget()
        assertEquals("nguyen", Db.getTravelers()[0].lastName)
        checkout.travelersPresenter.travelerEntryWidget.viewModel.nameViewModel.lastNameViewModel.textSubject.onNext("Billy")
        checkout.travelersPresenter.back()

        assertUpdateTravelerDialog()
    }

    @Test
    fun testTravelerChangeShouldShowUpdateTravelerDialogForDifferentPhoneFormat() {
        givenCompletedTravelerEntryWidget()
        assertEquals("9163355329", Db.getTravelers()[0].primaryPhoneNumber.number.replace("-", ""))
        checkout.travelersPresenter.travelerEntryWidget.viewModel.phoneViewModel.phoneViewModel.textSubject.onNext("987-654-321")
        checkout.travelersPresenter.onDoneClicked()

        assertUpdateTravelerDialog()
    }

    @Test
    fun testNoTravelerChangeShouldNotShowTravelerDialogOnDoneClick() {
        givenCompletedTravelerEntryWidget()
        checkout.travelersPresenter.onDoneClicked()
        val testDialog = ShadowAlertDialog.getLatestAlertDialog()

        assertNull(testDialog)
    }

    @Test
    fun testNoTravelerChangeShouldNotShowTravelerDialogOnBack() {
        givenCompletedTravelerEntryWidget()
        checkout.travelersPresenter.back()
        val testDialog = ShadowAlertDialog.getLatestAlertDialog()

        assertNull(testDialog)
    }

    @Test
    fun testSaveTravelerDialogShowsForNewTraveler() {
        givenCompletedTravelerEntryWidget()
        val newTraveler = enterTraveler(Traveler())
        newTraveler.tuid = 0

        checkout.travelersPresenter.travelerEntryWidget.viewModel.updateTraveler(newTraveler)
        assertEquals("malcolm", Db.getTravelers()[0].firstName)

        checkout.travelersPresenter.travelerEntryWidget.viewModel.nameViewModel.firstNameViewModel.textSubject.onNext("Billy")
        checkout.travelersPresenter.onDoneClicked()

        assertSaveTravelerDialog()
    }

    @Test
    fun testSaveTravelerDialogShowsForChangedKnownTravelerNumber() {
        givenCompletedTravelerEntryWidget()
        (checkout.travelersPresenter.travelerEntryWidget as FlightTravelerEntryWidget)
                .advancedOptionsWidget.travelerNumber.viewModel.textSubject.onNext("123456")

        checkout.travelersPresenter.onDoneClicked()
        assertUpdateTravelerDialog()
    }

    @Test
    fun testPaymentFeeHasNoLink() {
        createTrip()
        checkout.cardFeeWarningTextView.performClick()
        assertEquals(View.GONE, overview.paymentFeeInfoWebView.visibility)
    }

    @Test
    fun testRefreshedUserAccountDoesNotCrashBeforeOpeningPaymentWidget() {
        createTrip()
        checkout.onUserAccountRefreshed()

        assertEquals(View.GONE, checkout.paymentWidget.billingInfoContainer.visibility)

        checkout.showPaymentPresenter()
        checkout.paymentWidget.showPaymentForm(fromPaymentError = false)
        assertEquals(View.VISIBLE, checkout.paymentWidget.cardInfoContainer.visibility)
    }

    private fun createTrip() {
        checkout.travelerManager.updateDbTravelers(Db.getPackageParams())
        val tripResponseSubscriber = TestSubscriber<TripResponse>()
        checkout.getCreateTripViewModel().createTripResponseObservable.map { it.value }.subscribe(tripResponseSubscriber)

        val createTripParams = PackageCreateTripParams("create_trip", "", 1, false, emptyList())
        checkout.getCreateTripViewModel().tripParams.onNext(createTripParams)

        tripResponseSubscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
        tripResponseSubscriber.assertValueCount(1)

        checkout.updateTravelerPresenter()
    }

    private fun enterValidTraveler() {
        enterTraveler(Db.getTravelers().first())
        checkout.openTravelerPresenter()
        checkout.travelersPresenter.onDoneClicked()
        checkout.show(BaseCheckoutPresenter.CheckoutDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
    }

    private fun enterValidPayment() {
        val billingInfo = getBillingInfo()
        checkout.paymentWidget.sectionBillingInfo.bind(billingInfo)
        checkout.paymentWidget.sectionLocation.bind(billingInfo.location)
        checkout.showPaymentPresenter()
        checkout.paymentWidget.showPaymentForm(false)
        checkout.paymentWidget.validateAndBind()
        checkout.show(BaseCheckoutPresenter.CheckoutDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
    }

    private fun enterTraveler(traveler: Traveler): Traveler {
        traveler.firstName = "malcolm"
        traveler.lastName = "nguyen"
        traveler.gender = Traveler.Gender.MALE
        traveler.phoneNumber = "9163355329"
        traveler.email = "malcolmnguyen@gmail.com"
        traveler.birthDate = LocalDate.now().minusYears(18)
        traveler.seatPreference = Traveler.SeatPreference.WINDOW
        traveler.redressNumber = "123456"
        traveler.age = 18
        traveler.passengerCategory = PassengerCategory.ADULT
        traveler.primaryPassportCountry =  "USA"
        traveler.tuid = 12345
        return traveler
    }

    private fun getBillingInfo(): BillingInfo {
        val info = BillingInfo()
        info.email = "qa-ehcc@mobiata.com"
        info.firstName = "JexperCC"
        info.lastName = "MobiataTestaverde"
        info.nameOnCard = info.firstName + " " + info.lastName
        info.setNumberAndDetectType("4111111111111111", activity)
        info.securityCode = "111"
        info.telephone = "4155555555"
        info.telephoneCountryCode = "1"
        info.expirationDate = LocalDate.now()

        val location = Location()
        location.streetAddress = arrayListOf("123 street")
        location.city = "city"
        location.stateCode = "CA"
        location.countryCode = "US"
        location.postalCode = "12334"
        info.location = location

        return info
    }

    private fun setUpCheckout() {
        checkout = overview.getCheckoutPresenter()
        checkout.getCreateTripViewModel().packageServices = packageServiceRule.services!!
        checkout.getCheckoutViewModel().packageServices = packageServiceRule.services!!
        checkout.show(BaseCheckoutPresenter.CheckoutDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
        overview.getCheckoutPresenter().getCheckoutViewModel().obFeeDetailsUrlSubject.onNext("")
        overview.getCheckoutPresenter().getCheckoutViewModel().selectedFlightChargesFees.onNext("Airline Fee")
        overview.bundleWidget.viewModel = BundleOverviewViewModel(activity.applicationContext, packageServiceRule.services!!)
        overview.bundleWidget.viewModel.hotelParamsObservable.onNext(getPackageSearchParams(1, emptyList(), false))
        overview.bottomCheckoutContainer.slideToPurchaseLayout.visibility = View.VISIBLE
        overview.bottomCheckoutContainer.totalPriceWidget.visibility = View.VISIBLE
    }

    private fun setUpPackageDb() {
        val hotel = Hotel()
        hotel.packageOfferModel = PackageOfferModel()
        Db.setPackageSelectedHotel(hotel, HotelOffersResponse.HotelRoomResponse())

        val outboundFlight = FlightLeg()
        Db.setPackageSelectedOutboundFlight(outboundFlight)

        setPackageSearchParams(1, emptyList(), false)
    }

    private fun setPackageSearchParams(adults: Int, children: List<Int>, infantsInLap: Boolean) {
        Db.setPackageParams(getPackageSearchParams(adults, children, infantsInLap))
    }

    private fun getPackageSearchParams(adults: Int, children: List<Int>, infantsInLap: Boolean): PackageSearchParams {
        val origin = SuggestionV4()
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport = SuggestionV4.Airport()
        airport.airportCode = "SFO"
        hierarchyInfo.airport = airport
        val regionNames = SuggestionV4.RegionNames()
        regionNames.displayName = "San Francisco"
        regionNames.shortName = "SFO"
        regionNames.fullName = "SFO - San Francisco"

        origin.hierarchyInfo = hierarchyInfo
        val destination = SuggestionV4()
        destination.hierarchyInfo = hierarchyInfo
        destination.regionNames = regionNames
        origin.regionNames = regionNames

        return PackageSearchParams.Builder(12, 329).infantSeatingInLap(infantsInLap).startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(2)).origin(origin).destination(destination).adults(adults).children(children).build() as PackageSearchParams
    }

    private fun setUpCreditCards(cardNumber: String, description: String, type: PaymentType, id: String) : StoredCreditCard{
        val fakeCreditCard = StoredCreditCard()
        val billingInfo = getBillingInfo()
        fakeCreditCard.cardNumber = cardNumber
        fakeCreditCard.nameOnCard = billingInfo.nameOnCard
        fakeCreditCard.description = description
        fakeCreditCard.type = type
        fakeCreditCard.id = id
        fakeCreditCard.setIsSelectable(true)
        return fakeCreditCard
    }

    private fun createTripWithResortFee() {
        checkout.travelerManager.updateDbTravelers(Db.getPackageParams())
        val tripResponseSubscriber = TestSubscriber<TripResponse>()
        checkout.getCreateTripViewModel().createTripResponseObservable.map { it.value }.subscribe(tripResponseSubscriber)

        val createTripParams = PackageCreateTripParams("create_trip_with_resort_fee", "", 1, false, emptyList())
        checkout.getCreateTripViewModel().tripParams.onNext(createTripParams)

        tripResponseSubscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
        tripResponseSubscriber.assertValueCount(1)

        checkout.updateTravelerPresenter()
    }

    private fun givenCompletedTravelerEntryWidget(numOfTravelers: Int = 1) {
        val testUser = User()
        testUser.primaryTraveler = enterTraveler(Traveler())
        userStateManager.userSource.user = testUser
        UserLoginTestUtil.setupUserAndMockLogin(testUser)
        val mockTravelerProvider = MockTravelerProvider()
        mockTravelerProvider.updateDBWithMockTravelers(numOfTravelers, testUser.primaryTraveler)
        checkout.travelerSummaryCardView.findViewById<View>(R.id.traveler_default_state).performClick()
        checkout.travelersPresenter.travelerPickerWidget.viewModel.selectedTravelerSubject
                .onNext(TravelerSelectItemViewModel(activity, if (numOfTravelers > 1) 1 else 0, 18, PassengerCategory.ADULT))
        checkout.travelersPresenter.show(checkout.travelersPresenter.travelerEntryWidget)
        checkout.travelersPresenter.travelerEntryWidget.viewModel.updateTraveler(enterTraveler(Traveler()))
    }

    private fun assertUpdateTravelerDialog() {
        val testDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertNotNull(testDialog)
        assertEquals("Update Saved Traveler", testDialog.title)
        assertEquals("Update traveler information under your ${BuildConfig.brand} account to speed up future purchases?", testDialog.message)
    }

    private fun assertSaveTravelerDialog() {
        val testDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertNotNull(testDialog)
        assertEquals("Save Traveler", testDialog.title)
        assertEquals("Save traveler information under your ${BuildConfig.brand} account to speed up future purchases?", testDialog.message)
    }
}
