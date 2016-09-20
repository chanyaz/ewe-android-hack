package com.expedia.bookings.widget

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.cars.CarCategory
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder
import com.expedia.bookings.data.cars.CarInfo
import com.expedia.bookings.data.cars.CarType
import com.expedia.bookings.data.cars.CategorizedCarOffers
import com.expedia.bookings.data.cars.RateTerm
import com.expedia.bookings.data.cars.SearchCarFare
import com.expedia.bookings.data.cars.SearchCarOffer
import com.expedia.bookings.otto.Events
import com.expedia.bookings.test.MockCarServiceTestRule
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class CarConfirmationWidgetTests {

    var mockCarServiceTestRule: MockCarServiceTestRule = MockCarServiceTestRule()
        @Rule get

    @Test
    fun testCarConfirmationWidgetButtonsAccessibility() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val carConfirmationWidget = LayoutInflater.from(activity).inflate(R.layout.widget_car_confirmation, null) as CarConfirmationWidget
        PointOfSaleTestConfiguration.configurePointOfSale(activity, "MockSharedData/pos_locale_test_config.json")
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val carCreateTripResponse = mockCarServiceTestRule.getHappyCreateTripResponse()

        val carCheckoutParamsBuilder = CarCheckoutParamsBuilder()
                .grandTotal(Money(0, "USD"))
                .firstName("happy_0")
                .lastName("lastName")
                .tripId("tripId")
                .phoneCountryCode("phoneCountryCode")
                .phoneNumber("phoneNumber")
                .emailAddress("emailAddress")
                .guid("guid")

        val carCheckoutTripResponse = mockCarServiceTestRule.getCheckoutTripResponse(carCheckoutParamsBuilder.build())

        val carsKickoffCheckoutCallEvent = Events.CarsKickOffCheckoutCall(carCheckoutParamsBuilder)
        val carsShowConfirmationEvent = Events.CarsShowConfirmation(carCheckoutTripResponse)
        val carShowDetailsEvent = Events.CarsShowDetails(mockCategorizedCarOffer())
        val carsCheckoutCreateTripSuccess = Events.CarsCheckoutCreateTripSuccess(carCreateTripResponse)

        carConfirmationWidget.onDoCheckoutCall(carsKickoffCheckoutCallEvent)
        carConfirmationWidget.onCarsShowDetails(carShowDetailsEvent)
        carConfirmationWidget.onCheckoutCreateTripSuccess(carsCheckoutCreateTripSuccess)
        carConfirmationWidget.onShowConfirmation(carsShowConfirmationEvent)

        assertEquals("Flights to San Francisco button", carConfirmationWidget.addFlightTextView.contentDescription)
        assertEquals("Hotels in San Francisco button", carConfirmationWidget.addHotelTextView.contentDescription)
        assertEquals("Directions to Fox button", carConfirmationWidget.directionsTextView.contentDescription)
    }

    fun mockCategorizedCarOffer(): CategorizedCarOffers {
        val categorizedCarOffer = CategorizedCarOffers("Compact", CarCategory.COMPACT)

        val searchCarFare = SearchCarFare()
        searchCarFare.total = Money(10, "USD")
        searchCarFare.rate = Money(10, "USD")
        searchCarFare.rateTerm = RateTerm.DAILY

        val carInfo = CarInfo()
        carInfo.carCategoryDisplayLabel = "Compact"
        carInfo.type = CarType.FOUR_DOOR_CAR

        val searchOffer = SearchCarOffer()
        searchOffer.fare = searchCarFare
        searchOffer.vehicleInfo = carInfo
        categorizedCarOffer.add(searchOffer)

        return categorizedCarOffer
    }
}