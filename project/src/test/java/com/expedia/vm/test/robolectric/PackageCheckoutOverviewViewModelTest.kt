package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.services.subscribeTestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.StrUtils
import com.expedia.vm.packages.OverviewHeaderData
import com.expedia.vm.packages.PackageCheckoutOverviewViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageCheckoutOverviewViewModelTest {

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun packageViewModelTest() {
        val viewmodel = PackageCheckoutOverviewViewModel(getContext())
        val trip = PackageCreateTripResponse()
        val packageDetails = PackageCreateTripResponse.PackageDetails()
        packageDetails.pricing = PackageCreateTripResponse.Pricing()
        packageDetails.pricing.bundleTotal = Money(1000, "USD")
        packageDetails.pricing.packageTotal = Money(950, "USD")
        val hotel = HotelCreateTripResponse.HotelProductResponse()
        Db.setPackageParams(PackageTestUtil.getPackageSearchParams(destinationCityName = "<B>New</B> <B>York</B>, NY, United States <ap>(JFK-John F. Kennedy Intl.)</ap>",
                childCount = emptyList()))
        hotel.largeThumbnailUrl = "/testurl"
        hotel.hotelCity = "New York"
        hotel.hotelStateProvince = "NY"
        hotel.hotelCountry = "USA"
        hotel.checkInDate = "1989-09-06"
        hotel.checkOutDate = "2021-09-06"

        trip.packageDetails = packageDetails
        packageDetails.hotel = hotel

        val cityTestSubscriber = TestObserver<String>()
        val datesTestSubscriber = TestObserver<String>()
        val travelerTestSubscriber = TestObserver<String>()

        val urlTestSubscriber = TestObserver<List<String>>()

        viewmodel.cityTitle.subscribe(cityTestSubscriber)
        viewmodel.datesTitle.subscribeTestObserver(datesTestSubscriber)
        viewmodel.datesTitleContDesc.subscribeTestObserver(datesTestSubscriber)
        viewmodel.travelersTitle.subscribe(travelerTestSubscriber)
        viewmodel.url.subscribe(urlTestSubscriber)

        viewmodel.tripResponseSubject.onNext(createOverviewHeaderData(trip))

        assertEquals("New York, NY", cityTestSubscriber.values()[0])
        assertEquals("Wed Sep 06, 1989 - Mon Sep 06, 2021", datesTestSubscriber.values()[0])
        assertEquals("Wed Sep 06, 1989 to Mon Sep 06, 2021", datesTestSubscriber.values()[1])
        assertEquals("1 traveler", travelerTestSubscriber.values()[0])
        assertEquals("https://media.expedia.com/tes.jpg", urlTestSubscriber.values()[0][0])
        urlTestSubscriber.assertValueCount(1)

        trip.packageDetails.hotel.hotelCity = "Tokyo"
        trip.packageDetails.hotel.hotelCountry = "JPN"
        val regionNames = SuggestionV4.RegionNames()
        regionNames.displayName = "<B>New</B> <B>York</B> University, <B>New</B> <B>York</B>, NY"

        Db.sharedInstance.packageParams.destination?.regionNames = regionNames
        viewmodel.tripResponseSubject.onNext(createOverviewHeaderData(trip))

        cityTestSubscriber.assertValueCount(2)
        assertEquals("New York University, New York", cityTestSubscriber.values()[1])

        assertEquals("$1,000", trip.bundleTotal.formattedMoney)
        packageDetails.pricing.bundleTotal = null

        assertEquals("$950", trip.bundleTotal.formattedMoney)

    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    private fun createOverviewHeaderData(trip: PackageCreateTripResponse): OverviewHeaderData {
        val hotel = trip.packageDetails.hotel
        val cityName = StrUtils.formatCity(Db.sharedInstance.packageParams.destination)

        return OverviewHeaderData(cityName, hotel.checkOutDate, hotel.checkInDate, hotel.largeThumbnailUrl)
    }
}
