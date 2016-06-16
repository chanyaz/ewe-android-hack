package com.expedia.vm.test.robolectric

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
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageCheckoutOverviewViewModelTest {

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

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }
}
