package com.expedia.bookings.activity

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

import com.expedia.bookings.R
import com.expedia.bookings.dagger2.ViewModelFactory
import com.expedia.bookings.presenter.shared.KrazyglueWidget
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.viewmodel.FlightConfirmationViewModel
import com.expedia.bookings.widget.*
import javax.inject.Inject

class FlightConfirmationActivity : AppCompatActivity() {

    @Inject lateinit var viewModelFactory: ViewModelFactory

    val confirmationContainer: LinearLayout by bindView(R.id.confirmation_container)
    val itinNumber: TextView by bindView(R.id.itin_number)
    val tripBookedMessage: TextView by bindView(R.id.trip_booked_message)
    val destination: TextView by bindView(R.id.destination)
    val viewItinButton: Button by bindView(R.id.view_itin_button)
    val outboundFlightCard: ConfirmationRowCardView by bindView(R.id.outbound_flight_card)
    val inboundFlightCard: ConfirmationRowCardView by bindView(R.id.inbound_flight_card)
    val hotelCrossSell: HotelCrossSellView by bindView(R.id.hotel_cross_sell_widget)
    val flightSummary: ConfirmationSummaryCardView by bindView(R.id.trip_summary_card)
    val toolbar: FlightConfirmationToolbar by bindView(R.id.confirmation_toolbar)
    val tripProtectionLabel: TextView by bindView(R.id.trip_protection)
    val tripProtectionDivider: View by bindView(R.id.trip_protection_divider)
    val krazyglueWidget: KrazyglueWidget by bindView(R.id.krazyglue_widget)


    private val viewModel: FlightConfirmationViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(FlightConfirmationViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_confirmation)
    }
}
