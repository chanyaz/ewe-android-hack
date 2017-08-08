package com.expedia.bookings.rail.presenter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ConfirmationRowCardView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.rail.RailConfirmationViewModel

class RailConfirmationPresenter(context: Context, attrs: AttributeSet?) : Presenter(context, attrs) {
    val itinNumber: TextView by bindView(R.id.itin_number)
    val destination: TextView by bindView(R.id.destination)
    val viewItinButton: Button by bindView(R.id.view_itin_button)

    val outboundLegCard: ConfirmationRowCardView by bindView(R.id.outbound_leg_card)
    val inboundLegCard: ConfirmationRowCardView by bindView(R.id.inbound_leg_card)

    var viewModel: RailConfirmationViewModel by notNullAndObservable { vm ->
        vm.itinNumberObservable.subscribeText(itinNumber)
        vm.destinationObservable.subscribeText(destination)
        vm.inboundCardVisibility.subscribeVisibility(inboundLegCard)
        vm.outboundCardTitleObservable.subscribeText(outboundLegCard.title)
        vm.outboundCardSubTitleObservable.subscribeText(outboundLegCard.subTitle)
        vm.inboundCardTitleObservable.subscribeText(inboundLegCard.title)
        vm.inboundCardSubTitleObservable.subscribeText(inboundLegCard.subTitle)
    }

    init {
        View.inflate(context, R.layout.rail_confirmation_presenter, this)

        viewItinButton.setBackgroundColor(ContextCompat.getColor(context, R.color.rail_primary_color))
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
}