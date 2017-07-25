package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.packages.PackageCheckoutOverviewViewModel
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
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
        Db.setPackageParams(PackageSearchParams(SuggestionV4(), SuggestionV4(), LocalDate.now(), LocalDate.now(), 1, emptyList<Int>(), false))
        hotel.largeThumbnailUrl = "/testurl"
        hotel.hotelCity = "New York"
        hotel.hotelStateProvince = "NY"
        hotel.hotelCountry = "USA"
        hotel.checkInDate = "1989-09-06"
        hotel.checkOutDate = "2021-09-06"

        trip.packageDetails = packageDetails
        packageDetails.hotel = hotel

        val cityTestSubscriber = TestSubscriber<String>()
        val datesTestSubscriber = TestSubscriber<String>()
        val travelerTestSubscriber = TestSubscriber<String>()

        val urlTestSubscriber = TestSubscriber<List<String>>(1)

        viewmodel.cityTitle.subscribe(cityTestSubscriber)
        viewmodel.datesTitle.subscribe(datesTestSubscriber)
        viewmodel.datesTitleContDesc.subscribe(datesTestSubscriber)
        viewmodel.travelersTitle.subscribe(travelerTestSubscriber)
        viewmodel.url.subscribe(urlTestSubscriber)

        viewmodel.tripResponseSubject.onNext(trip)

        assertEquals("New York, NY", cityTestSubscriber.onNextEvents[0])
        assertEquals("Wed Sep 06, 1989 - Mon Sep 06, 2021", datesTestSubscriber.onNextEvents[0])
        assertEquals("Wed Sep 06, 1989 to Mon Sep 06, 2021", datesTestSubscriber.onNextEvents[1])
        assertEquals("1 traveler", travelerTestSubscriber.onNextEvents[0])
        assertEquals("https://media.expedia.com/tes.jpg", urlTestSubscriber.onNextEvents[0][0])
        urlTestSubscriber.assertValueCount(1)

        trip.packageDetails.hotel.hotelCity = "Tokyo"
        trip.packageDetails.hotel.hotelCountry = "JPN"
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val country = SuggestionV4.Country()
        country.name = "Japan"
        hierarchyInfo.country = country
        Db.getPackageParams().destination?.hierarchyInfo = hierarchyInfo
        viewmodel.tripResponseSubject.onNext(trip)

        cityTestSubscriber.assertValueCount(2)
        assertEquals("Tokyo, Japan", cityTestSubscriber.onNextEvents[1])

        assertEquals("$1,000", trip.bundleTotal.formattedMoney)
        packageDetails.pricing.bundleTotal = null

        assertEquals("$950", trip.bundleTotal.formattedMoney)

    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }
}
