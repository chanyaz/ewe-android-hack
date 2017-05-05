package com.expedia.bookings.widget.packages

import android.support.v4.app.FragmentActivity
import android.view.View
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
import com.expedia.bookings.enums.TravelerCheckoutStatus
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
import com.expedia.bookings.widget.PackageCheckoutPresenter
import com.expedia.vm.packages.BundleOverviewViewModel
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
class PackageCheckoutTest {

    val server = MockWebServer()
    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    lateinit var travelerValidator: TravelerValidator

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
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java).withIntent(styledIntent).create().visible().get()
        overview = activity.findViewById(R.id.package_overview_presenter) as PackageOverviewPresenter

        setUpCheckout()
    }

    @Test
    fun testCheckoutSuccess() {
        createTrip()
        enterValidTraveler()
        enterValidPayment()

        assertEquals(TravelerCheckoutStatus.COMPLETE, checkout.travelerSummaryCard.getStatus())
        assertEquals(ContactDetailsCompletenessStatus.COMPLETE, checkout.paymentWidget.paymentStatusIcon.status)
        assertEquals(View.VISIBLE, overview.totalPriceWidget.visibility)
        assertEquals(true, checkout.getCheckoutViewModel().builder.hasValidTravelerAndBillingInfo())
        assertEquals(0f, checkout.slideToPurchaseLayout.translationY)
        assertEquals(View.GONE, checkout.slideTotalText.visibility)
        assertEquals(View.VISIBLE, checkout.slideToPurchaseSpace.visibility)
    }

        @Test
    fun testCheckoutSuccessWithResortFee() {
        createTripWithResortFee()
        enterValidTraveler()
        enterValidPayment()

        assertEquals(TravelerCheckoutStatus.COMPLETE, checkout.travelerSummaryCard.getStatus())
        assertEquals(ContactDetailsCompletenessStatus.COMPLETE, checkout.paymentWidget.paymentStatusIcon.status)
        assertEquals(View.VISIBLE, overview.totalPriceWidget.visibility)
        assertEquals(true, checkout.getCheckoutViewModel().builder.hasValidTravelerAndBillingInfo())
        assertEquals(0f, checkout.slideToPurchaseLayout.translationY)
        assertEquals(View.VISIBLE, checkout.slideTotalText.visibility)
        assertEquals("Your card will be charged $173.68", checkout.slideTotalText.text)
        assertEquals(View.GONE, checkout.slideToPurchaseSpace.visibility)
    }

    @Test
    fun testSlideTotalTextVisibilityDependsOnResortFees() {
        createTripWithResortFee()
        enterValidTraveler()
        enterValidPayment()

        assertEquals(View.VISIBLE, checkout.slideTotalText.visibility)
        assertEquals("Your card will be charged $173.68", checkout.slideTotalText.text)
        assertEquals(View.GONE, checkout.slideToPurchaseSpace.visibility)

        val createTripParams = PackageCreateTripParams("create_trip", "", 1, false, emptyList())
        checkout.getCreateTripViewModel().tripParams.onNext(createTripParams)
        checkout.getCheckoutViewModel().animateInSlideToPurchaseObservable.onNext(true)

        assertEquals(View.GONE, checkout.slideTotalText.visibility)
        assertEquals("", checkout.slideTotalText.text)
        assertEquals(View.VISIBLE, checkout.slideToPurchaseSpace.visibility)
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
        checkout.onSlideAllTheWay()

        errorResponseSubscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
        errorResponseSubscriber.assertValueCount(1)
        errorResponseSubscriber.assertValue(ApiError(ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS))

        (checkout.paymentWidget as BillingDetailsPaymentWidget).addressLineOne.setText("1735 Steiner st")
        checkout.paymentWidget.validateAndBind()
        checkout.onSlideAllTheWay()

        checkoutResponseSubscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
        checkoutResponseSubscriber.assertValueCount(1)

        assertEquals("malcolmnguyen@gmail.com", checkoutResponseSubscriber.onNextEvents[0].second)
    }

    @Test
    fun testLoggedInUserPaymentStatusMultipleCards() {
        val testUserLoggedIn = TestSubscriber<Boolean>()
        checkout.paymentWidget.viewmodel.userLogin.subscribe(testUserLoggedIn)
        createTrip()

        val testUser = User()
        val testInvalidCard = setUpCreditCards("1234567890123456", "testInvalid", PaymentType.UNKNOWN, "1")
        val testCard1 = setUpCreditCards("4111111111111111", "testVisa", PaymentType.CARD_VISA, "2")
        val testCard2 = setUpCreditCards("6111111111111111", "testDiscover", PaymentType.CARD_DISCOVER, "3")

        testUser.addStoredCreditCard(testInvalidCard)
        testUser.addStoredCreditCard(testCard1)
        testUser.addStoredCreditCard(testCard2)
        testUser.primaryTraveler = enterTraveler(Traveler())
        Db.setUser(testUser)
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
        val testFirstInvalidCard = setUpCreditCards("1234567890123456", "testInvalid", PaymentType.UNKNOWN, "1")
        val testSecondInvalidCard = setUpCreditCards("6543210987654321", "testInvalidOther", PaymentType.CARD_CHINA_UNION_PAY, "2")
        val testThirdInvalidCard = setUpCreditCards("0000000000000000", "testInvalidLast", PaymentType.CARD_CARTE_BLEUE, "3")
        testUser.addStoredCreditCard(testFirstInvalidCard)
        testUser.addStoredCreditCard(testSecondInvalidCard)
        testUser.addStoredCreditCard(testThirdInvalidCard)
        testUser.primaryTraveler = enterTraveler(Traveler())
        Db.setUser(testUser)
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
        assertEquals(View.VISIBLE, overview.totalPriceWidget.visibility)
        assertEquals(true, checkout.getCheckoutViewModel().builder.hasValidTravelerAndBillingInfo())
        assertEquals(0f, checkout.slideToPurchaseLayout.translationY)

        val testUser = User()
        testUser.primaryTraveler = enterTraveler(Traveler())
        Db.setUser(testUser)
        UserLoginTestUtil.setupUserAndMockLogin(testUser)
        checkout.onLoginSuccess()

        testUserLoggedIn.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(true, testUserLoggedIn.onNextEvents[0])
        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, checkout.paymentWidget.paymentStatusIcon.status)
    }

    private fun createTrip() {
        checkout.travelerManager.updateDbTravelers(Db.getPackageParams())
        val tripResponseSubscriber = TestSubscriber<TripResponse>()
        checkout.getCreateTripViewModel().createTripResponseObservable.subscribe(tripResponseSubscriber)

        val createTripParams = PackageCreateTripParams("create_trip", "", 1, false, emptyList())
        checkout.getCreateTripViewModel().tripParams.onNext(createTripParams)

        tripResponseSubscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
        tripResponseSubscriber.assertValueCount(1)

        checkout.updateTravelerPresenter()
    }

    private fun enterValidTraveler() {
        enterTraveler(Db.getTravelers().first())
        checkout.openTravelerPresenter()
        checkout.travelersPresenter.doneClicked.onNext(Unit)
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
        return traveler
    }

    private fun getBillingInfo(): BillingInfo {
        val info = BillingInfo()
        info.email = "qa-ehcc@mobiata.com"
        info.firstName = "JexperCC"
        info.lastName = "MobiataTestaverde"
        info.nameOnCard = info.firstName + " " + info.lastName
        info.setNumberAndDetectType("4111111111111111")
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
        overview.bundleWidget.viewModel = BundleOverviewViewModel(activity.applicationContext, packageServiceRule.services!!)
        overview.bundleWidget.viewModel.hotelParamsObservable.onNext(getPackageSearchParams(1, emptyList(), false))
        checkout.slideToPurchaseLayout.visibility = View.VISIBLE
        overview.totalPriceWidget.visibility = View.VISIBLE
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
        checkout.getCreateTripViewModel().createTripResponseObservable.subscribe(tripResponseSubscriber)

        val createTripParams = PackageCreateTripParams("create_trip_with_resort_fee", "", 1, false, emptyList())
        checkout.getCreateTripViewModel().tripParams.onNext(createTripParams)

        tripResponseSubscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
        tripResponseSubscriber.assertValueCount(1)

        checkout.updateTravelerPresenter()
    }
}
