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
import kotlin.test.assertEquals

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
    fun testFreeUnrealDealUSPosFreeHotel() {
        hotelCellView = LayoutInflater.from(activity).inflate(R.layout.package_hotel_cell, null, false) as ViewGroup
        packageHotelHolder = PackageHotelCellViewHolder(hotelCellView)

        val hotel = makeHotel()
        hotel.packageOfferModel.brandedDealData.dealVariation = PackageOfferModel.DealVariation.FreeHotel
        packageHotelHolder.bindHotelData(hotel)
        Assert.assertTrue(packageHotelHolder.unrealDealMessage.visibility.equals(View.VISIBLE))
        Assert.assertEquals("Get your hotel for free by booking together.", packageHotelHolder.unrealDealMessage.text)
        assertEquals("Unreal Deal Get your hotel for free by booking together. happy with 4.0 of 5 rating. 3.5 of 5 guest rating. Price $22. Includes hotel and flights. Old price $27 Button",
                packageHotelHolder.cardView.contentDescription.toString())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFreeUnrealDealUSPosFreeFlight() {
        hotelCellView = LayoutInflater.from(activity).inflate(R.layout.package_hotel_cell, null, false) as ViewGroup
        packageHotelHolder = PackageHotelCellViewHolder(hotelCellView)

        val hotel = makeHotel()
        hotel.packageOfferModel.brandedDealData.dealVariation = PackageOfferModel.DealVariation.FreeFlight
        packageHotelHolder.bindHotelData(hotel)
        Assert.assertTrue(packageHotelHolder.unrealDealMessage.visibility.equals(View.VISIBLE))
        Assert.assertEquals("Book this and save 100% on your flight.", packageHotelHolder.unrealDealMessage.text)
        assertEquals("Unreal Deal Book this and save 100% on your flight. happy with 4.0 of 5 rating. 3.5 of 5 guest rating. Price $22. Includes hotel and flights. Old price $27 Button",
                packageHotelHolder.cardView.contentDescription.toString())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFreeUnrealDealUSPosFreeOneNightHotel() {
        hotelCellView = LayoutInflater.from(activity).inflate(R.layout.package_hotel_cell, null, false) as ViewGroup
        packageHotelHolder = PackageHotelCellViewHolder(hotelCellView)

        val hotel = makeHotel()
        hotel.packageOfferModel.brandedDealData.dealVariation = PackageOfferModel.DealVariation.FreeOneNightHotel
        hotel.packageOfferModel.brandedDealData.freeNights = "1"
        packageHotelHolder.bindHotelData(hotel)
        Assert.assertTrue(packageHotelHolder.unrealDealMessage.visibility.equals(View.VISIBLE))
        Assert.assertEquals("1 free night when you book with a flight.", packageHotelHolder.unrealDealMessage.text)
        assertEquals("Unreal Deal 1 free night when you book with a flight. happy with 4.0 of 5 rating. 3.5 of 5 guest rating. Price $22. Includes hotel and flights. Old price $27 Button",
                packageHotelHolder.cardView.contentDescription.toString())
    }

    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    @Test fun testNoFreeUnrealDealUKPos() {
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_no_free_unreal_deal.json")
        hotelCellView = LayoutInflater.from(activity).inflate(R.layout.package_hotel_cell, null, false) as ViewGroup
        packageHotelHolder = PackageHotelCellViewHolder(hotelCellView)

        val hotel = makeHotel()
        packageHotelHolder.bindHotelData(hotel)
        Assert.assertTrue(packageHotelHolder.unrealDealMessage.visibility.equals(View.VISIBLE))
        Assert.assertEquals("Book this and save $110 (20%)", packageHotelHolder.unrealDealMessage.text)
        assertEquals("Unreal Deal Book this and save $110 (20%) happy with 4.0 of 5 rating. 3.5 of 5 guest rating. Price $22. Includes hotel and flights. Old price $27 Button",
                packageHotelHolder.cardView.contentDescription.toString())
    }

    private fun makeHotel(): Hotel {
        val hotel = Hotel()
        hotel.hotelId = "happy"
        hotel.localizedName = "happy"
        hotel.lowRateInfo = HotelRate()
        hotel.distanceUnit = "Miles"
        hotel.hotelGuestRating = 3.5f
        hotel.hotelStarRating = 4f
        hotel.lowRateInfo.priceToShowUsers = 22F
        hotel.lowRateInfo.currencyCode = "USD"
        hotel.lowRateInfo.strikethroughPriceToShowUsers = 27f
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
