package com.expedia.bookings.itin.scopes

import android.support.design.widget.TabLayout
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.flight.traveler.FlightItinTravelerInfoWidget
import com.expedia.bookings.itin.flight.traveler.FlightItinTravelerPreferenceWidget
import com.expedia.bookings.itin.utils.StringSource

data class SweetScope(override val toolbar: ItinToolbar,
                      override val strings: StringSource,
                      override val tabLayout: TabLayout,
                      override val travelerInfo: FlightItinTravelerInfoWidget,
                      override val travelerPreference: FlightItinTravelerPreferenceWidget,
                      override val itinId: String
) : HasToolbar, HasStringProvider, HasTravelerPreference, HasTravelerInfo, HasTabLayout, HasItinId