package com.expedia.vm.test.traveler

import android.content.Context
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.data.ValidPayment
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.traveler.TravelerViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TravelerViewModelTest {
    val context = RuntimeEnvironment.application

    @Test
    fun testPassportCountryRequiredWhenInternational() {
        setupInternationalTrip()
        val travelerVM = TestTravelerViewModel(context, 0)
        assertTrue(travelerVM.shouldShowPassportDropdown())
    }

    @Test
    fun testPassportCountryRequired() {
        setupPassportRequiredResponse()
        val travelerVM = TestTravelerViewModel(context, 0)
        assertTrue(travelerVM.shouldShowPassportDropdown())
    }

    @Test
    fun testPassportCountryNotRequiredWhenDomestic() {
        setupDomesticTrip()
        val travelerVM = TestTravelerViewModel(context, 0)
        assertFalse(travelerVM.shouldShowPassportDropdown())
    }

    private fun setupDomesticTrip() {
        val offer = FlightTripDetails.FlightOffer()
        offer.isInternational = false
        setupTripResponse(offer)
    }

    private fun setupPassportRequiredResponse() {
        val offer = FlightTripDetails.FlightOffer()
        offer.isPassportNeeded = true
        setupTripResponse(offer)
    }

    private fun setupInternationalTrip() {
        val offer = FlightTripDetails.FlightOffer()
        offer.isInternational = true
        setupTripResponse(offer)
    }

    private fun setupTripResponse(offer: FlightTripDetails.FlightOffer) {
        val tripDetails = FlightTripDetails()
        tripDetails.offer = offer

        val packageDetails = PackageCreateTripResponse.PackageDetails()
        packageDetails.flight = PackageCreateTripResponse.FlightProduct()
        packageDetails.flight.details = tripDetails

        val response = PackageCreateTripResponse()
        response.packageDetails = packageDetails

        val validFormsOfPayment = ArrayList<ValidFormOfPayment>()
        response.validFormsOfPayment = validFormsOfPayment

        val trip = TripBucketItemPackages(response)
        Db.getTripBucket().clear(LineOfBusiness.PACKAGES)
        Db.getTripBucket().add(trip)
    }

    class TestTravelerViewModel(context: Context, index: Int) : TravelerViewModel(context, index) {
        override fun updateTraveler(traveler: Traveler) {
            // do nothing
        }

        override fun getTraveler(): Traveler {
            return Traveler()
        }
    }
}
