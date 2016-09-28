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
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelCrossSellView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.ConfirmationRowCardView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.flights.FlightConfirmationViewModel

class FlightConfirmationPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val confirmationContainer: LinearLayout by bindView(R.id.confirmation_container)
    val itinNumber: TextView by bindView(R.id.itin_number)
    val destination: TextView by bindView(R.id.destination)
    val expediaPoints: TextView by bindView(R.id.expedia_points)
    val viewItinButton: Button by bindView(R.id.view_itin_button)

    val outboundFlightCard: ConfirmationRowCardView by bindView(R.id.outbound_flight_card)
    val inboundFlightCard: ConfirmationRowCardView by bindView(R.id.inbound_flight_card)

    val hotelCrossSell: HotelCrossSellView by bindView(R.id.hotel_cross_sell_widget)

    var viewModel: FlightConfirmationViewModel by notNullAndObservable { vm ->
        vm.itinNumberMessageObservable.subscribeText(itinNumber)
        vm.destinationObservable.subscribeText(destination)
        vm.rewardPointsObservable.subscribeTextAndVisibility(expediaPoints)
        vm.itinNumberMessageObservable.subscribeText(itinNumber)
        vm.inboundCardVisibility.subscribeVisibility(inboundFlightCard)
        vm.crossSellWidgetVisibility.subscribeVisibility(hotelCrossSell)
    }

    init {
        View.inflate(context, R.layout.flight_confirmation_presenter, this)
        confirmationContainer.setPadding(0, Ui.toolbarSizeWithStatusBar(context), 0, 0)
        viewItinButton.setOnClickListener {
            (context as AppCompatActivity).finish()
            NavUtils.goToItin(context)
        }
    }

    override fun back(): Boolean {
        (context as AppCompatActivity).finish()
        NavUtils.goToItin(context)
        return true
    }

    fun showConfirmationInfo(response: FlightCheckoutResponse, email: String){
        viewModel.confirmationObservable.onNext(Pair(response, email))
        hotelCrossSell.viewModel.confirmationObservable.onNext(response)
    }
}
