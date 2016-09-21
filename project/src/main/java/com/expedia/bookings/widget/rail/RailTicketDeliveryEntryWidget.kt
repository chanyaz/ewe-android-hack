package com.expedia.bookings.widget.rail

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.RailLocation
import com.expedia.bookings.section.SectionLocation
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ScrollView
import com.expedia.bookings.widget.SpinnerAdapterWithHint
import com.expedia.bookings.widget.TicketDeliverySelectionStatus
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailTicketDeliveryEntryViewModel
import com.expedia.vm.rail.RailTicketDeliveryOptionViewModel
import rx.subjects.PublishSubject

class RailTicketDeliveryEntryWidget(context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {

    val stationContainer: RailTicketDeliveryOptionWidget by bindView(R.id.station_container)
    val mailDeliveryContainer: RailTicketDeliveryOptionWidget by bindView(R.id.mail_delivery_container)
    val mailShippingAddressContainer: View by bindView(R.id.mail_shipping_address_container)
    val mailDeliveryAddress: SectionLocation by bindView(R.id.mail_delivery_address)
    val doneClicked = PublishSubject.create<Unit>()
    val closeSubject = PublishSubject.create<Unit>()

    var viewModel: RailTicketDeliveryEntryViewModel by notNullAndObservable { vm ->
        vm.deliveryByMailSupported.subscribe { supported ->
            if (supported) {
                toggleMailDelivery(1.0f, true)
            } else {
                toggleMailDelivery(0.25f, false)
            }
        }

        vm.ticketDeliveryObservable.subscribe { currentSelection ->
            if (currentSelection == TicketDeliveryMethod.DELIVER_BY_MAIL) {
                stationContainer.viewModel.statusChanged.onNext(TicketDeliverySelectionStatus.UNSELECTED)
                mailDeliveryContainer.viewModel.statusChanged.onNext(TicketDeliverySelectionStatus.SELECTED)
                mailShippingAddressContainer.visibility = View.VISIBLE
            } else {
                stationContainer.viewModel.statusChanged.onNext(TicketDeliverySelectionStatus.SELECTED)
                mailDeliveryContainer.viewModel.statusChanged.onNext(TicketDeliverySelectionStatus.UNSELECTED)
                mailShippingAddressContainer.visibility = View.GONE
            }
        }

        vm.ticketDeliveryByPostOptions.subscribe { options ->
            val location = RailLocation()
            // TODO We are currently only showing one option in country for delivery.
            // Api also returns just GB for all the delivery options for now. We need to change this once api supports more countries.
            // Also remove filtering from RailTicketDeliveryEntryViewModel.ticketDeliveryByPostOptions
            location.ticketDeliveryCountryCodes = listOf("GB")
            location.tickerDeliveryOptions = options.map { SpinnerAdapterWithHint.SpinnerItem(it.ticketDeliveryDescription, it) }
            mailDeliveryAddress.bind(location)
            mailDeliveryAddress.setLineOfBusiness(LineOfBusiness.RAILS)
        }
    }

    init {
        View.inflate(context, R.layout.ticket_delivery_entry_widget, this)
        stationContainer.viewModel = RailTicketDeliveryOptionViewModel(context)
        mailDeliveryContainer.viewModel = RailTicketDeliveryOptionViewModel(context)

        stationContainer.setOnClickListener {
            viewModel.ticketDeliveryObservable.onNext(TicketDeliveryMethod.PICKUP_AT_STATION)
        }

        mailDeliveryContainer.setOnClickListener {
            viewModel.ticketDeliveryObservable.onNext(TicketDeliveryMethod.DELIVER_BY_MAIL)
        }

        doneClicked.subscribe {
            if (isValid()) {
                updateCompletionStatus()
                closeSubject.onNext(Unit)
            }
        }
    }

    private fun toggleMailDelivery(alpha: Float, isEnabled: Boolean) {
        mailDeliveryContainer.alpha = alpha
        mailDeliveryContainer.isEnabled = isEnabled
    }

    fun isValid(): Boolean {
        var valid = true
        if (viewModel.ticketDeliveryObservable.value == TicketDeliveryMethod.DELIVER_BY_MAIL) {
            valid =  mailDeliveryAddress.performValidation()
        }
        return valid
    }

    fun updateCompletionStatus() {
        viewModel.ticketDeliveryMethodSelected.onNext(viewModel.ticketDeliveryObservable.value)
    }

    fun entryStatus(status: TicketDeliveryMethod) {
        viewModel.ticketDeliveryObservable.onNext(status)
    }

    fun isComplete(): Boolean {
        // TODO form validation for delivery by mail
        return true
    }
}
