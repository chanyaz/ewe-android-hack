package com.expedia.bookings.presenter.flight

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightItinDetailsResponse
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.extensions.subscribeContentDescription
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.shared.KrazyglueWidget
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.FlightV2Utils
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
import com.expedia.util.Optional
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
        vm.crossSellWidgetVisibility.subscribe { show ->
            if (show) {
                FlightsV2Tracking.trackAirAttachShown()
            }
        }
        vm.formattedTravelersStringSubject.subscribeText(flightSummary.numberOfTravelers)
        vm.tripTotalPriceSubject.subscribeText(flightSummary.tripPrice)

        vm.showTripProtectionMessage.subscribeVisibility(tripProtectionDivider)
        vm.showTripProtectionMessage.subscribeVisibility(tripProtectionLabel)
        vm.rewardPointsObservable.subscribeTextAndVisibility(flightSummary.pointsEarned)

        if (isKrazyglueOnFlightsConfirmationEnabled(context)) {
            vm.krazyglueHotelsObservable.subscribe(krazyglueWidget.viewModel.hotelsObservable)
            vm.destinationObservable.subscribe(krazyglueWidget.viewModel.cityObservable)
            vm.krazyGlueHotelSearchParamsObservable.subscribe(krazyglueWidget.viewModel.hotelSearchParamsObservable)
            vm.krazyGlueRegionIdObservable.subscribe(krazyglueWidget.viewModel.regionIdObservable)
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
        setCardViewModelsFromCheckoutResponse(response)
        viewModel.confirmationObservable.onNext(Pair(response, email))
        viewModel.flightCheckoutResponseObservable.onNext(response)
        hotelCrossSell.viewModel.confirmationObservable.onNext(response)
        toolbar.viewModel.bindTripId(response.newTrip?.tripId ?: "")
    }

    fun showConfirmationInfoFromWebCheckoutView(response: FlightItinDetailsResponse) {
        setCardViewModelsFromItinResponse(response)
        viewModel.itinDetailsResponseObservable.onNext(response)
        hotelCrossSell.viewModel.itinDetailsResponseObservable.onNext(response)
        toolbar.viewModel.bindTripId(response.responseData.tripId ?: "")
        val expediaRewards = response.responseData.rewardList.firstOrNull()?.totalPoints?.toString()
        viewModel.setRewardsPoints.onNext(Optional(expediaRewards))
    }

    private fun setCardViewModelsFromCheckoutResponse(response: FlightCheckoutResponse) {
        val outbound = response.getFirstFlightLeg()
        val inbound = response.getLastFlightLeg()

        var flightTitle = FlightV2Utils.getDepartureToArrivalTitleFromCheckoutResponseLeg(context, outbound)
        var flightSubtitle = FlightV2Utils.getDepartureToArrivalSubtitleFromCheckoutResponseLeg(context, outbound)
        var flightUrl = FlightV2Utils.getAirlineUrlFromCheckoutResponseLeg(outbound) ?: ""
        var flightDepartureDateTitle = FlightV2Utils.getDepartureOnDateStringFromCheckoutResponseLeg(context, outbound)
        outboundFlightCard.viewModel = FlightConfirmationCardViewModel(flightTitle, flightSubtitle, flightUrl, flightDepartureDateTitle)
        if (inbound != outbound && viewModel.inboundCardVisibility.value ?: false) {
            flightTitle = FlightV2Utils.getDepartureToArrivalTitleFromCheckoutResponseLeg(context, inbound)
            flightSubtitle = FlightV2Utils.getDepartureToArrivalSubtitleFromCheckoutResponseLeg(context, inbound)
            flightUrl = FlightV2Utils.getAirlineUrlFromCheckoutResponseLeg(inbound) ?: ""
            flightDepartureDateTitle = FlightV2Utils.getDepartureOnDateStringFromCheckoutResponseLeg(context, inbound)
            inboundFlightCard.viewModel = FlightConfirmationCardViewModel(flightTitle, flightSubtitle, flightUrl, flightDepartureDateTitle)
        }
    }

    private fun setCardViewModelsFromItinResponse(response: FlightItinDetailsResponse) {
        val outbound = response.responseData.flights.firstOrNull()?.legs?.firstOrNull()
        val inbound = response.responseData.flights.lastOrNull()?.legs?.getOrNull(1)

        var flightTitle = FlightV2Utils.getDepartureToArrivalTitleFromItinResponseLeg(context, outbound)
        var flightSubTitle = FlightV2Utils.getDepartureToArrivalSubtitleFromItinResponseLeg(context, outbound)
        var flightUrl = outbound?.airlineLogoURL ?: ""
        var flightDepartureDateTitle = FlightV2Utils.getDepartureOnDateStringFromItinResponseLeg(context, outbound)
        outboundFlightCard.viewModel = FlightConfirmationCardViewModel(flightTitle, flightSubTitle, flightUrl, flightDepartureDateTitle)
        val isRoundTrip = response.responseData.flights.firstOrNull()?.legs?.size ?: 0 > 1
        viewModel.inboundCardVisibility.onNext(isRoundTrip)
        if (inbound != outbound && isRoundTrip) {
            flightTitle = FlightV2Utils.getDepartureToArrivalTitleFromItinResponseLeg(context, inbound)
            flightSubTitle = FlightV2Utils.getDepartureToArrivalSubtitleFromItinResponseLeg(context, inbound)
            flightUrl = inbound?.airlineLogoURL ?: ""
            flightDepartureDateTitle = FlightV2Utils.getDepartureOnDateStringFromItinResponseLeg(context, inbound)
            inboundFlightCard.viewModel = FlightConfirmationCardViewModel(flightTitle, flightSubTitle, flightUrl, flightDepartureDateTitle)
        }
    }
}
