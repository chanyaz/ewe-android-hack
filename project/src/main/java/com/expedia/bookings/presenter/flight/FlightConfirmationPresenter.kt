package com.expedia.bookings.presenter.flight

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.shared.KrazyglueWidget
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isKrazyglueOnFlightsConfirmationEnabled
import com.expedia.bookings.widget.ConfirmationRowCardView
import com.expedia.bookings.widget.ConfirmationSummaryCardView
import com.expedia.bookings.widget.HotelCrossSellView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.FlightConfirmationToolbar
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeContentDescription
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.ConfirmationToolbarViewModel
import com.expedia.vm.flights.FlightConfirmationCardViewModel
import com.expedia.vm.flights.FlightConfirmationViewModel

class FlightConfirmationPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

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

    var viewModel: FlightConfirmationViewModel by notNullAndObservable { vm ->
        vm.itinNumContentDescriptionObservable.subscribeContentDescription(itinNumber)
        vm.destinationObservable.subscribeText(destination)
        vm.itinNumberMessageObservable.subscribeText(itinNumber)
        vm.inboundCardVisibility.subscribeVisibility(inboundFlightCard)
        vm.crossSellWidgetVisibility.subscribeVisibility(hotelCrossSell)
        vm.formattedTravelersStringSubject.subscribeText(flightSummary.numberOfTravelers)
        vm.tripTotalPriceSubject.subscribeText(flightSummary.tripPrice)

        vm.showTripProtectionMessage.subscribeVisibility(tripProtectionDivider)
        vm.showTripProtectionMessage.subscribeVisibility(tripProtectionLabel)
        vm.rewardPointsObservable.subscribeTextAndVisibility(flightSummary.pointsEarned)

        if (isKrazyglueOnFlightsConfirmationEnabled(context)) {
            vm.krazyglueHotelsObservable.subscribe(krazyglueWidget.viewModel.hotelsObservable)
            vm.krazyglueDestinationObservable.subscribe(krazyglueWidget.viewModel.cityObservable)
        }
    }

    init {
        View.inflate(context, R.layout.flight_confirmation_presenter, this)
        viewItinButton.setOnClickListener {
            (context as AppCompatActivity).finish()
            NavUtils.goToItin(context)
        }
        toolbar.viewModel = ConfirmationToolbarViewModel(context)

        confirmationContainer.setPadding(0, Ui.getStatusBarHeight(context), 0, 0)
        tripBookedMessage.setText(R.string.trip_is_booked)
    }

    override fun back(): Boolean {
        (context as AppCompatActivity).finish()
        NavUtils.goToItin(context)
        return true
    }

    fun showConfirmationInfo(response: FlightCheckoutResponse, email: String) {
        setCardViewModels(response)
        viewModel.confirmationObservable.onNext(Pair(response, email))
        hotelCrossSell.viewModel.confirmationObservable.onNext(response)
        toolbar.viewModel.bindCheckoutResponseData(response)
    }

    fun setCardViewModels(response: FlightCheckoutResponse) {
        val outbound = response.getFirstFlightLeg()
        val inbound = response.getLastFlightLeg()
        val destinationCity = outbound.segments?.last()?.arrivalAirportAddress?.city ?: ""
        val numberOfGuests = response.passengerDetails.size

        outboundFlightCard.viewModel = FlightConfirmationCardViewModel(context, outbound, numberOfGuests)
        viewModel.destinationObservable.onNext(destinationCity)
        viewModel.numberOfTravelersSubject.onNext(numberOfGuests)
        if (inbound != outbound && viewModel.inboundCardVisibility.value ?: false) {
            inboundFlightCard.viewModel = FlightConfirmationCardViewModel(context, inbound, numberOfGuests)
        }
    }
}
