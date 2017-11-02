package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.RailLocation
import com.expedia.bookings.data.TicketDeliveryOption
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.SpinnerAdapterWithHint
import com.expedia.bookings.widget.TicketDeliverySelectionStatus
import com.expedia.bookings.widget.shared.EntryFormToolbar
import com.expedia.util.notNullAndObservable
import com.expedia.vm.EntryFormToolbarViewModel
import com.expedia.vm.rail.RailTicketDeliveryEntryViewModel
import com.expedia.vm.rail.RailTicketDeliveryOptionViewModel
import io.reactivex.subjects.PublishSubject

class RailTicketDeliveryEntryWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val stationContainer: RailTicketDeliveryOptionWidget by bindView(R.id.station_container)
    val mailDeliveryContainer: RailTicketDeliveryOptionWidget by bindView(R.id.mail_delivery_container)
    val mailShippingAddressContainer: View by bindView(R.id.mail_shipping_address_container)
    val deliveryAddressEntry: RailDeliveryAddressEntry by bindView(R.id.rail_delivery_address_entry)
    val closeSubject = PublishSubject.create<Unit>()

    val toolbarViewModel = EntryFormToolbarViewModel()

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
            toolbarViewModel.formFilledIn.onNext(areEntryFormsFilled())
        }

        vm.ticketDeliveryByPostOptions.subscribe { options ->
            val location = RailLocation()
            // TODO We are currently only showing one option in country for delivery.
            // Api also returns just GB for all the delivery options for now. We need to change this once api supports more countries.
            // Also remove filtering from RailTicketDeliveryEntryViewModel.ticketDeliveryByPostOptions
            location.ticketDeliveryCountryCodes = listOf("GB")
            location.tickerDeliveryOptions = options.map { SpinnerAdapterWithHint.SpinnerItem(it.ticketDeliveryDescription, it) }
            deliveryAddressEntry.mailDeliverySectionLocation.bind(location)
        }

        vm.ticketDeliveryMethodSelected.subscribe { selected ->
            val ticketDeliveryOption: TicketDeliveryOption
            if (selected == TicketDeliveryMethod.PICKUP_AT_STATION) {
                ticketDeliveryOption = TicketDeliveryOption(RailCreateTripResponse.RailTicketDeliveryOptionToken.PICK_UP_AT_TICKETING_OFFICE_NONE)
            } else {
                val railLocation = deliveryAddressEntry.getLocation()
                val ticketDeliveryOptionToken = railLocation.ticketDeliveryOptionSelected!!.ticketDeliveryOptionToken
                ticketDeliveryOption = TicketDeliveryOption(ticketDeliveryOptionToken, railLocation)
            }
            vm.ticketDeliveryOption = ticketDeliveryOption
            vm.ticketDeliveryOptionSubject.onNext(ticketDeliveryOption)
        }
    }

    private val toolbar: EntryFormToolbar by bindView(R.id.rail_ticket_delivery_toolbar)

    init {
        View.inflate(context, R.layout.ticket_delivery_entry_widget, this)
        toolbar.viewModel = toolbarViewModel
        stationContainer.viewModel = RailTicketDeliveryOptionViewModel(context)
        mailDeliveryContainer.viewModel = RailTicketDeliveryOptionViewModel(context)

        stationContainer.setOnClickListener {
            viewModel.ticketDeliveryObservable.onNext(TicketDeliveryMethod.PICKUP_AT_STATION)
        }

        mailDeliveryContainer.setOnClickListener {
            viewModel.ticketDeliveryObservable.onNext(TicketDeliveryMethod.DELIVER_BY_MAIL)
        }

        toolbarViewModel.doneClicked.subscribe {
            if (isValid()) {
                updateCompletionStatus()
                closeSubject.onNext(Unit)
            }
        }

        toolbarViewModel.nextClicked.subscribe {
            deliveryAddressEntry.focusNext()
        }

        deliveryAddressEntry.formsFilledInSubject.subscribe(toolbarViewModel.formFilledIn)
    }

    fun updateCompletionStatus() {
        viewModel.ticketDeliveryMethodSelected.onNext(viewModel.ticketDeliveryObservable.value)
    }

    fun entryStatus(status: TicketDeliveryMethod) {
        viewModel.ticketDeliveryObservable.onNext(status)
    }

    fun getTicketDeliveryOption(): TicketDeliveryOption {
        if (viewModel.ticketDeliveryOption == null) {
            return TicketDeliveryOption(RailCreateTripResponse.RailTicketDeliveryOptionToken.PICK_UP_AT_TICKETING_OFFICE_NONE)
        } else {
            return viewModel.ticketDeliveryOption!!
        }
    }

    fun resetFocusToToolbarNavigationIcon() {
        AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar)
    }

    private fun toggleMailDelivery(alpha: Float, isEnabled: Boolean) {
        mailDeliveryContainer.alpha = alpha
        mailDeliveryContainer.isEnabled = isEnabled
    }

    private fun isValid(): Boolean {
        var valid = true
        if (viewModel.ticketDeliveryObservable.value == TicketDeliveryMethod.DELIVER_BY_MAIL) {
            valid =  deliveryAddressEntry.isValid()
        }
        return valid
    }

    private fun areEntryFormsFilled() : Boolean {
        if (viewModel.ticketDeliveryObservable.value == TicketDeliveryMethod.DELIVER_BY_MAIL) {
            return deliveryAddressEntry.areFormsFilledIn()
        }
        return true
    }
}
