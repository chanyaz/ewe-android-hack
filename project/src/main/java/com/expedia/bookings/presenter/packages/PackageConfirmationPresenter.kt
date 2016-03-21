package com.expedia.bookings.presenter.packages

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.packages.ConfirmationRowCardView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.PackageConfirmationViewModel

class PackageConfirmationPresenter(context: Context, attributeSet: AttributeSet) : Presenter(context, attributeSet) {
    val itinNumber: TextView by bindView(R.id.itin_number)
    val destination: TextView by bindView(R.id.destination)
    val expediaPoints: TextView by bindView(R.id.expedia_points)
    val destinationCard: ConfirmationRowCardView by bindView(R.id.destination_card)
    val outboundFlightCard: ConfirmationRowCardView by bindView(R.id.outbound_flight_card)
    val inboundFlightCard: ConfirmationRowCardView by bindView(R.id.inbound_flight_card)
    val viewItinButton: Button by bindView(R.id.view_itin_button)
    val confirmationContainer: LinearLayout by bindView(R.id.confirmation_container)

    var viewModel: PackageConfirmationViewModel by notNullAndObservable { vm ->
        vm.itinNumberMessageObservable.subscribeText(itinNumber)
        vm.destinationObservable.subscribeText(destination)
        vm.expediaPointsObservable.subscribeText(expediaPoints)
        vm.destinationTitleObservable.subscribeText(destinationCard.title)
        vm.destinationSubTitleObservable.subscribeText(destinationCard.subTitle)
        vm.outboundFlightCardTitleObservable.subscribeText(outboundFlightCard.title)
        vm.outboundFlightCardSubTitleObservable.subscribeText(outboundFlightCard.subTitle)
        vm.inboundFlightCardTitleObservable.subscribeText(inboundFlightCard.title)
        vm.inboundFlightCardSubTitleObservable.subscribeText(inboundFlightCard.subTitle)
        vm.itinNumberMessageObservable.subscribeText(itinNumber)
    }

    init {
        View.inflate(context, R.layout.package_confirmation_presenter, this)
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
}