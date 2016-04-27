package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.RewardsInfo
import com.expedia.bookings.data.TripBucketItemCar
import com.expedia.bookings.data.TripBucketItemFlight
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.TripBucketItemLX
import com.expedia.bookings.data.TripBucketItemPackages
import com.expedia.bookings.data.cars.CarCreateTripResponse
import com.expedia.bookings.data.lx.LXCreateTripResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowResourcesTemp
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.widget.AccountButton
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.properties.Delegates
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesTemp::class))
class AccountButtonTest {
    var accountButton by Delegates.notNull<AccountButton>()
    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Before
    fun before()
    {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Hotels)
        accountButton = LayoutInflater.from(activity).inflate(R.layout.account_button_v2_test, null) as AccountButton
        Db.getTripBucket().clear()
    }

    @Test
    fun testGetRewardsReturnsNullWithNoRewards() {
        val createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        val rewards = accountButton.getRewardsForLOB(LineOfBusiness.HOTELSV2)
        assertNull(rewards)
    }

    @Test
    fun testGetRewardsReturnsRewards() {
        val createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        val rewardsInfo = RewardsInfo()
        rewardsInfo.totalAmountToEarn = Money("1234", "USD")
        createTripResponse.rewards = rewardsInfo
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        val rewards = accountButton.getRewardsForLOB(LineOfBusiness.HOTELSV2)
        assertNotNull(rewards)
    }

    @Test
    fun testGetRewardsReturnsNullOtherThanHotelV2() {
        Db.getTripBucket().add(TripBucketItemCar(CarCreateTripResponse()))
        Db.getTripBucket().add(TripBucketItemFlight())
        Db.getTripBucket().add(TripBucketItemLX(LXCreateTripResponse()))
        val packageCreateTripResponse = PackageCreateTripResponse()
        packageCreateTripResponse.validFormsOfPayment = emptyList()
        Db.getTripBucket().add(TripBucketItemPackages(packageCreateTripResponse))
        assertNull(accountButton.getRewardsForLOB(LineOfBusiness.CARS))
        assertNull(accountButton.getRewardsForLOB(LineOfBusiness.LX))
        assertNull(accountButton.getRewardsForLOB(LineOfBusiness.FLIGHTS))
        assertNull(accountButton.getRewardsForLOB(LineOfBusiness.PACKAGES))
    }
}