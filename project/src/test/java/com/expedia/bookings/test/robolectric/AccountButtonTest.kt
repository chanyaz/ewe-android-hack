package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightTrip
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.RewardsInfo
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.cars.CarCreateTripResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.lx.LXCreateTripResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.trips.TripBucketItemCar
import com.expedia.bookings.data.trips.TripBucketItemFlight
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.data.trips.TripBucketItemLX
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.data.trips.TripBucketItemTransport
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.widget.AccountButton
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import org.robolectric.shadows.ShadowView
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class))
class AccountButtonTest {
    private val context = RuntimeEnvironment.application
    var accountButton by Delegates.notNull<AccountButton>()
    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Control)
        accountButton = LayoutInflater.from(activity).inflate(R.layout.account_button_v2_test, null) as AccountButton
        Db.getTripBucket().clear()
    }

    @Test
    fun testNoRewardsForHotelsV2() {
        val createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        val rewards = accountButton.getRewardsForLOB(LineOfBusiness.HOTELS)
        assertNull(rewards)
    }

    @Test
    fun testRewardsForHotelsV2() {
        val createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        val rewardsInfo = RewardsInfo()
        rewardsInfo.totalAmountToEarn = Money("1234", "USD")
        createTripResponse.rewards = rewardsInfo
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        val rewards = accountButton.getRewardsForLOB(LineOfBusiness.HOTELS)
        assertNotNull(rewards)
    }

    @Test
    fun testNoRewardsForFlights() {
        val flightTrip = FlightTrip()
        val tripBucketItemFlight = TripBucketItemFlight(flightTrip, null)
        Db.getTripBucket().add(tripBucketItemFlight)
        val rewards = accountButton.getRewardsForLOB(LineOfBusiness.FLIGHTS)
        assertNull(rewards)
    }

    @Test
    fun testRewardsForFlights() {
        val rewardsInfo = RewardsInfo()
        rewardsInfo.totalAmountToEarn = Money("1234", "USD")
        val flightTrip = FlightTrip()
        flightTrip.rewards = rewardsInfo
        val tripBucketItemFlight = TripBucketItemFlight(flightTrip, null)
        Db.getTripBucket().add(tripBucketItemFlight)
        val rewards = accountButton.getRewardsForLOB(LineOfBusiness.FLIGHTS)
        assertNotNull(rewards)
    }

    @Test
    fun testSignInTextWithRewards() {
        val rewardsInfo = RewardsInfo()
        rewardsInfo.totalAmountToEarn = Money("12", "USD")
        val rewardsText = accountButton.getSignInWithRewardsAmountText(rewardsInfo).toString()
        val expectedText = Phrase.from(context, R.string.Sign_in_to_earn_TEMPLATE).put("reward", "$12" ).format().toString()
        assertEquals(expectedText, rewardsText)
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSignInTextWithoutRewards() {
        val rewardsText = accountButton.signInWithoutRewardsText.toString()
        val expectedText = Phrase.from(context, R.string.Sign_in_with_TEMPLATE).putOptional("brand", "Expedia").format().toString()
        assertEquals(expectedText, rewardsText)
    }

    @Test
    fun testNullRewardsOtherThanHotelV2AndFlights() {
        Db.getTripBucket().add(TripBucketItemCar(CarCreateTripResponse()))
        Db.getTripBucket().add(TripBucketItemLX(LXCreateTripResponse()))
        Db.getTripBucket().add(TripBucketItemTransport(LXCreateTripResponse()))
        val packageCreateTripResponse = PackageCreateTripResponse()
        packageCreateTripResponse.validFormsOfPayment = emptyList()
        Db.getTripBucket().add(TripBucketItemPackages(packageCreateTripResponse))
        assertNull(accountButton.getRewardsForLOB(LineOfBusiness.CARS))
        assertNull(accountButton.getRewardsForLOB(LineOfBusiness.LX))
        assertNull(accountButton.getRewardsForLOB(LineOfBusiness.PACKAGES))
        assertNull(accountButton.getRewardsForLOB(LineOfBusiness.TRANSPORT))
    }

    @Test
    fun testRewardsForPackages() {
        val createTripResponse = PackageCreateTripResponse()
        val rewardsInfo = RewardsInfo()
        rewardsInfo.totalAmountToEarn = Money("1234", "USD")
        createTripResponse.rewards = rewardsInfo
        Db.getTripBucket().add(TripBucketItemPackages(createTripResponse))
        val rewards = accountButton.getRewardsForLOB(LineOfBusiness.PACKAGES)
        assertNotNull(rewards)
    }

    @Test
    fun testSignInTextWithRewardsContentDescription() {
        val rewardsInfo = RewardsInfo()
        rewardsInfo.totalAmountToEarn = Money("12", "USD")
        val rewardContentDescriptionText = accountButton.getSignInWithRewardsContentDescriptionText(rewardsInfo).toString()
        val expectedText = Phrase.from(context, R.string.Sign_in_to_earn_cont_desc_TEMPLATE).put("reward", "$12" ).format().toString()
        assertEquals(expectedText,rewardContentDescriptionText)
    }

    @Test
    fun testRewardsForFlightV2() {
        val createTripResponse = FlightCreateTripResponse()
        val rewardsInfo = RewardsInfo()
        rewardsInfo.totalAmountToEarn = Money("1234", "USD")
        createTripResponse.rewards = rewardsInfo
        Db.getTripBucket().add(TripBucketItemFlightV2(createTripResponse))
        val rewardsFlightV2 = accountButton.getRewardsForLOB(LineOfBusiness.FLIGHTS_V2)
        assertNotNull(rewardsFlightV2)

        rewardsInfo.totalAmountToEarn = null
        val noAmountToEarn = accountButton.getRewardsForLOB(LineOfBusiness.FLIGHTS_V2)
        assertNull(noAmountToEarn)

        createTripResponse.rewards = null
        val noReward = accountButton.getRewardsForLOB(LineOfBusiness.FLIGHTS_V2)
        assertNull(noReward)

    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.MRJET, MultiBrand.EBOOKERS))
    fun testSignInBackgroundWithNoRewards() {
        val isTablet = false
        Db.getTripBucket().clear()

        accountButton.bind(false, false, null, LineOfBusiness.HOTELS)
        val loginContainer = accountButton.findViewById(R.id.account_login_container);
        val shadowDrawable = Shadows.shadowOf(loginContainer.background);

        assertEquals(R.drawable.material_cko_acct_btn_bg, shadowDrawable.createdFromResId)
    }
}
