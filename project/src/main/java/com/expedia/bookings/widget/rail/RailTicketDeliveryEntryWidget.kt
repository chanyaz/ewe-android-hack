package com.expedia.bookings.widget.rail

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ScrollView
import com.expedia.bookings.widget.TicketDeliverySelectionStatus
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailTicketDeliveryEntryViewModel
import com.expedia.vm.rail.RailTicketDeliveryOptionViewModel
import rx.subjects.PublishSubject

class RailTicketDeliveryEntryWidget(context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {

    val stationContainer: RailTicketDeliveryOptionWidget by bindView(R.id.station_container)
    val mailDeliveryContainer: RailTicketDeliveryOptionWidget by bindView(R.id.mail_delivery_container)
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
            } else {
                stationContainer.viewModel.statusChanged.onNext(TicketDeliverySelectionStatus.SELECTED)
                mailDeliveryContainer.viewModel.statusChanged.onNext(TicketDeliverySelectionStatus.UNSELECTED)
            }
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
        if (viewModel.ticketDeliveryMethodSelected.value == TicketDeliveryMethod.DELIVER_BY_MAIL) {
            // TODO validate fields
            return true
        } else {
            return true
        }
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

    fun updateOnCreateTripResponse(ticketDeliveryOptionList: List<RailCreateTripResponse.RailTicketDeliveryOption>?) {
        var supported = false
        if (ticketDeliveryOptionList != null) {
            for (option in ticketDeliveryOptionList) {
                // Ticket delivery country code list is empty if only pick up at station supported
                if (CollectionUtils.isNotEmpty(option.ticketDeliveryCountryCodeList)) {
                    supported = true
                    break
                }
            }
        }
        viewModel.deliveryByMailSupported.onNext(supported)
    }
}
