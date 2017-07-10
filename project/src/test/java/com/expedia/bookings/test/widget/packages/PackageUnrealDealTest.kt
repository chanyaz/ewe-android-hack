package com.expedia.bookings.test.widget.packages

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.PointsEarnInfo
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.widget.packages.PackageHotelCellViewHolder
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class PackageUnrealDealTest {
    private var hotelCellView: ViewGroup by Delegates.notNull()
    private var packageHotelHolder: PackageHotelCellViewHolder by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFreeUnrealDealUSPos() {
        hotelCellView = LayoutInflater.from(activity).inflate(R.layout.package_hotel_cell, null, false) as ViewGroup
        packageHotelHolder = PackageHotelCellViewHolder(hotelCellView, 200)

        val hotel = makeHotel()
        packageHotelHolder.bindHotelData(hotel)
        Assert.assertTrue(packageHotelHolder.unrealDealMessage.visibility.equals(View.VISIBLE))
        Assert.assertEquals("Get your hotel for free by booking together.", packageHotelHolder.unrealDealMessage.text)
    }

    @Test fun testNoFreeUnrealDealUKPos() {
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_no_free_unreal_deal.json")
        hotelCellView = LayoutInflater.from(activity).inflate(R.layout.package_hotel_cell, null, false) as ViewGroup
        packageHotelHolder = PackageHotelCellViewHolder(hotelCellView, 200)

        val hotel = makeHotel()
        packageHotelHolder.bindHotelData(hotel)
        Assert.assertTrue(packageHotelHolder.unrealDealMessage.visibility.equals(View.VISIBLE))
        Assert.assertEquals("Book this and save $110 (20%)", packageHotelHolder.unrealDealMessage.text)
    }

    private fun makeHotel(): Hotel {
        val hotel = Hotel()
        hotel.hotelId = "happy"
        hotel.localizedName = "happy"
        hotel.lowRateInfo = HotelRate()
        hotel.distanceUnit = "Miles"
        hotel.lowRateInfo.currencyCode = "USD"
        hotel.lowRateInfo.loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(PointsEarnInfo(320, 0, 320), null), false)
        hotel.packageOfferModel = PackageOfferModel()
        hotel.packageOfferModel.featuredDeal = true
        hotel.packageOfferModel.brandedDealData = PackageOfferModel.BrandedDealData()
        hotel.packageOfferModel.brandedDealData.dealVariation = PackageOfferModel.DealVariation.FreeHotel
        hotel.packageOfferModel.brandedDealData.savingsAmount = "$110"
        hotel.packageOfferModel.brandedDealData.savingPercentageOverPackagePrice = "20"

        return hotel
    }
}