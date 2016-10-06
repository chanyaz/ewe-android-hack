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
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.bookings.widget.PackageCheckoutPresenter
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class))
class PackageCheckoutTest {

    val server = MockWebServer()
    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    lateinit var travelerValidator: TravelerValidator

    private var checkout: PackageCheckoutPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()

    @Before fun before() {
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Ui.getApplication(RuntimeEnvironment.application).defaultPackageComponents()
        travelerValidator = Ui.getApplication(RuntimeEnvironment.application).travelerComponent().travelerValidator()
        setUpPackageDb()
        travelerValidator.updateForNewSearch(Db.getPackageParams())
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.package_checkout_test)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java).withIntent(styledIntent).create().visible().get()
        setUpCheckout()
    }

    @Test
    fun testCheckoutSuccess() {
        createTrip()
        enterValidTraveler()
        enterValidPayment()

        assertEquals(TravelerCheckoutStatus.COMPLETE, checkout.travelerSummaryCard.getStatus())
        assertEquals(ContactDetailsCompletenessStatus.COMPLETE, checkout.paymentWidget.paymentStatusIcon.status)
        assertEquals(View.VISIBLE, checkout.totalPriceWidget.visibility)
        assertEquals(true, checkout.getCheckoutViewModel().builder.hasValidTravelerAndBillingInfo())
        assertEquals(0f, checkout.slideToPurchaseLayout.translationY)
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

        errorResponseSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS)
        errorResponseSubscriber.assertValueCount(1)
        errorResponseSubscriber.assertValue(ApiError(ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS))

        (checkout.paymentWidget as BillingDetailsPaymentWidget).addressLineOne.setText("1735 Steiner st")
        checkout.paymentWidget.validateAndBind()
        checkout.onSlideAllTheWay()

        checkoutResponseSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS)
        checkoutResponseSubscriber.assertValueCount(1)

        assertEquals("malcolmnguyen@gmail.com", checkoutResponseSubscriber.onNextEvents[0].second)
    }

    private fun createTrip() {
        checkout.travelerManager.updateDbTravelers(Db.getPackageParams(), activity)
        val tripResponseSubscriber = TestSubscriber<TripResponse>()
        checkout.getCreateTripViewModel().tripResponseObservable.subscribe(tripResponseSubscriber)

        val createTripParams = PackageCreateTripParams("create_trip", "", 1, false, emptyList())
        checkout.getCreateTripViewModel().tripParams.onNext(createTripParams)

        tripResponseSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS)
        tripResponseSubscriber.assertValueCount(1)

        checkout.updateTravelerPresenter()
    }

    private fun enterValidTraveler() {
        enterTraveler(Db.getTravelers().first())
        checkout.openTravelerPresenter()
        checkout.travelerPresenter.doneClicked.onNext(Unit)
        checkout.show(BaseCheckoutPresenter.CheckoutDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
    }

    private fun enterValidPayment() {
        val billingInfo = getBillingInfo()
        checkout.paymentWidget.sectionBillingInfo.bind(billingInfo)
        checkout.paymentWidget.sectionLocation.bind(billingInfo.location)
        checkout.showPaymentPresenter()
        checkout.paymentWidget.showPaymentForm()
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
        checkout = activity.findViewById(R.id.package_checkout_presenter) as PackageCheckoutPresenter
        checkout.getCreateTripViewModel().packageServices = packageServiceRule.services!!
        checkout.getCheckoutViewModel().packageServices = packageServiceRule.services!!
        checkout.show(BaseCheckoutPresenter.CheckoutDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
        checkout.slideToPurchaseLayout.visibility = View.VISIBLE
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
        val origin = SuggestionV4()
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport = SuggestionV4.Airport()
        airport.airportCode = "SFO"
        hierarchyInfo.airport = airport
        origin.hierarchyInfo = hierarchyInfo

        val destination = SuggestionV4()
        destination.hierarchyInfo = hierarchyInfo

        val packageParams = PackageSearchParams.Builder(12, 329).infantSeatingInLap(infantsInLap).startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(2)).origin(origin).destination(destination).adults(adults).children(children).build() as PackageSearchParams
        Db.setPackageParams(packageParams)
    }
}
