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
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.ConfirmationRowCardView
import com.expedia.bookings.widget.ConfirmationSummaryCardView
import com.expedia.bookings.widget.HotelCrossSellView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.ConfirmationToolbar
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.util.updateVisibility
import com.expedia.vm.ConfirmationToolbarViewModel
import com.expedia.vm.flights.FlightConfirmationCardViewModel
import com.expedia.vm.flights.FlightConfirmationViewModel

class FlightConfirmationPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val confirmationContainer: LinearLayout by bindView(R.id.confirmation_container)
    val itinNumber: TextView by bindView(R.id.itin_number)
    val tripBookedMessage: TextView by bindView(R.id.trip_booked_message)
    val destination: TextView by bindView(R.id.destination)
    val expediaPoints: TextView by bindView(R.id.expedia_points)
    val viewItinButton: Button by bindView(R.id.view_itin_button)
    val isNewConfirmationScreenEnabled = FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_additional_content_flight_confirmation)

    val outboundFlightCard: ConfirmationRowCardView by bindView(R.id.outbound_flight_card)
    val inboundFlightCard: ConfirmationRowCardView by bindView(R.id.inbound_flight_card)
    val hotelCrossSell: HotelCrossSellView by bindView(R.id.hotel_cross_sell_widget)

    //    TODO flight summary & toolbar cannot be null on new confirmation screen, but must be null on old confirmation screen
    var flightSummary: ConfirmationSummaryCardView ?= null
    var toolbar: ConfirmationToolbar ?= null

    var viewModel: FlightConfirmationViewModel by notNullAndObservable { vm ->
        vm.itinNumberMessageObservable.subscribeText(itinNumber)
        vm.destinationObservable.subscribeText(destination)
        vm.itinNumberMessageObservable.subscribeText(itinNumber)
        vm.inboundCardVisibility.subscribeVisibility(inboundFlightCard)
        vm.crossSellWidgetVisibility.subscribeVisibility(hotelCrossSell)
        vm.isNewConfirmationScreenEnabled.onNext(isNewConfirmationScreenEnabled)
        if (isNewConfirmationScreenEnabled) {
            vm.formattedTravelersStringSubject.subscribeText(flightSummary?.numberOfTravelers)
            vm.tripTotalPriceSubject.subscribeText(flightSummary?.tripPrice)
            vm.traveledDistanceObservable.subscribeText(flightSummary?.totalDistance)
            val tripProtectionLabel = findViewById(R.id.trip_protection) as TextView
            val tripProtectionDivider = findViewById(R.id.trip_protection_divider)
            vm.showTripProtectionMessage.subscribe { isVisible ->
                tripProtectionDivider.updateVisibility(isVisible)
                tripProtectionLabel.updateVisibility(isVisible)
            }
        }
        vm.rewardPointsObservable.subscribeTextAndVisibility(flightSummary?.pointsEarned ?: expediaPoints)
    }

    init {
        View.inflate(context, if (isNewConfirmationScreenEnabled) R.layout.new_flight_confirmation_presenter
                                else R.layout.flight_confirmation_presenter, this)
        viewItinButton.setOnClickListener {
            (context as AppCompatActivity).finish()
            NavUtils.goToItin(context)
        }
        if (isNewConfirmationScreenEnabled) {
            flightSummary = findViewById(R.id.trip_summary_card) as ConfirmationSummaryCardView
            tripBookedMessage.setText(R.string.trip_is_booked)
            toolbar = findViewById(R.id.checkout_toolbar) as ConfirmationToolbar
            toolbar?.viewModel = ConfirmationToolbarViewModel(context)
        }
        confirmationContainer.setPadding(0, if (isNewConfirmationScreenEnabled)
            Ui.getStatusBarHeight(context) else Ui.toolbarSizeWithStatusBar(context), 0, 0)
    }

    override fun back(): Boolean {
        (context as AppCompatActivity).finish()
        NavUtils.goToItin(context)
        return true
    }

    fun showConfirmationInfo(response: FlightCheckoutResponse, email: String){
        setCardViewModels(response)
        viewModel.confirmationObservable.onNext(Pair(response, email))
        hotelCrossSell.viewModel.confirmationObservable.onNext(response)
        if (isNewConfirmationScreenEnabled) {
            toolbar?.viewModel?.bindCheckoutResponseData(response)
        }
    }

    fun setCardViewModels(response: FlightCheckoutResponse) {
        val outbound = response.getFirstFlightLeg()
        val inbound = response.getLastFlightLeg()
        val destinationCity = outbound.segments?.last()?.arrivalAirportAddress?.city ?: ""
        val numberOfGuests = response.passengerDetails.size

        outboundFlightCard.viewModel = FlightConfirmationCardViewModel(context, outbound, numberOfGuests, isNewConfirmationScreenEnabled)
        viewModel.destinationObservable.onNext(destinationCity)
        viewModel.numberOfTravelersSubject.onNext(numberOfGuests)
        if (inbound != outbound && viewModel.inboundCardVisibility.value ?: false) {
            inboundFlightCard.viewModel = FlightConfirmationCardViewModel(context, inbound, numberOfGuests, isNewConfirmationScreenEnabled)
        }
    }
}
