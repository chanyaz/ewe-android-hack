package com.expedia.bookings.itin.scopes

import android.support.design.widget.TabLayout
import com.expedia.bookings.itin.common.ItinModifyReservationWidget
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.flight.manageBooking.FlightItinAirlineSupportDetailsWidget
import com.expedia.bookings.itin.flight.manageBooking.FlightItinCustomerSupportDetails
import com.expedia.bookings.itin.flight.manageBooking.FlightItinLegsDetailWidget
import com.expedia.bookings.itin.flight.traveler.FlightItinTravelerInfoWidget
import com.expedia.bookings.itin.flight.traveler.FlightItinTravelerPreferenceWidget

interface HasToolbar {
    val toolbar: ItinToolbar
}

interface HasTabLayout {
    val tabLayout: TabLayout
}

interface HasTravelerInfo {
    val travelerInfo: FlightItinTravelerInfoWidget
}

interface HasTravelerPreference {
    val travelerPreference: FlightItinTravelerPreferenceWidget
}

interface HasItinId {
    val itinId: String
}