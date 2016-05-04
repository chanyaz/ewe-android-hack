package com.expedia.vm.test.traveler

import android.app.Activity
import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.FlightCheckoutOverviewViewModel
import com.expedia.vm.packages.PackageCheckoutOverviewViewModel
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class CheckoutOverviewViewModelTest {

    private fun getContext(): Context {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        return activity
    }

    @Test
    fun packageViewModelTest() {
        val viewmodel = PackageCheckoutOverviewViewModel(getContext())
        val trip = PackageCreateTripResponse()
        val packageDetails = PackageCreateTripResponse.PackageDetails()
        val hotel = HotelCreateTripResponse.HotelProductResponse()
        Db.setPackageParams(PackageSearchParams(SuggestionV4(), SuggestionV4(), LocalDate.now(), LocalDate.now(), 1, emptyList<Int>(), false))
        hotel.largeThumbnailUrl = "/testurl"
        hotel.hotelCity = "New York"
        hotel.hotelStateProvince = "NY"
        hotel.checkInDate = "1989-09-06"
        hotel.checkOutDate = "2021-09-06"

        trip.packageDetails = packageDetails
        packageDetails.hotel = hotel

        val titleTestSubscriber = TestSubscriber<String>(3)
        val urlTestSubscriber = TestSubscriber<List<String>>(1)

        viewmodel.cityTitle.subscribe(titleTestSubscriber)
        viewmodel.datesTitle.subscribe(titleTestSubscriber)
        viewmodel.travelersTitle.subscribe(titleTestSubscriber)
        viewmodel.url.subscribe(urlTestSubscriber)

        viewmodel.tripResponseSubject.onNext(trip)

        assertEquals("New York, NY", titleTestSubscriber.onNextEvents[0])
        assertEquals("Wed Sep 06, 1989 - Mon Sep 06, 2021", titleTestSubscriber.onNextEvents[1])
        assertEquals("1 Traveler", titleTestSubscriber.onNextEvents[2])
        assertEquals("https://media.expedia.com/tes.jpg", urlTestSubscriber.onNextEvents[0][0])
        titleTestSubscriber.assertValueCount(3)
        urlTestSubscriber.assertValueCount(1)
    }

    @Test
    fun flightViewModelTest() {
        val viewmodel = FlightCheckoutOverviewViewModel(getContext())

        val origin = getFakeSuggestion("SFO")
        val destination = getFakeSuggestion("SEA")
        val params = FlightSearchParams.Builder(100, 500)
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now().withYear(1989).withMonthOfYear(9).withDayOfMonth(6))
                .endDate(LocalDate.now().withYear(2021).withMonthOfYear(9).withDayOfMonth(6))
                .adults(1).build() as FlightSearchParams


        val titleTestSubscriber = TestSubscriber<String>(3)
        val urlTestSubscriber = TestSubscriber<List<String>>(1)

        viewmodel.cityTitle.subscribe(titleTestSubscriber)
        viewmodel.datesTitle.subscribe(titleTestSubscriber)
        viewmodel.travelersTitle.subscribe(titleTestSubscriber)
        viewmodel.url.subscribe(urlTestSubscriber)

        viewmodel.params.onNext(params)

        assertEquals("San Francisco, CA", titleTestSubscriber.onNextEvents[0])
        assertEquals("Wed Sep 06, 1989 - Mon Sep 06, 2021", titleTestSubscriber.onNextEvents[1])
        assertEquals("1 Traveler", titleTestSubscriber.onNextEvents[2])
        titleTestSubscriber.assertValueCount(3)
        urlTestSubscriber.assertValueCount(1)
    }

    private fun getFakeSuggestion(airportCode: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport = SuggestionV4.Airport()
        airport.airportCode = airportCode
        hierarchyInfo.airport = airport
        val country = SuggestionV4.Country()
        country.name = ""
        hierarchyInfo.country = country
        suggestion.hierarchyInfo = hierarchyInfo

        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = "San Francisco, CA"
        regionName.displayName = "San Francisco, CA"
        suggestion.regionNames = regionName
        return suggestion
    }

}
