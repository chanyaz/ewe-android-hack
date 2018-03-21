package com.expedia.bookings.itin.flight.manageBooking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinBaseActivity
import com.expedia.bookings.itin.flight.common.FlightItinToolbarViewModel
import com.expedia.bookings.itin.common.ItinModifyReservationWidget
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.common.ItinToolbarViewModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable

class FlightItinManageBookingActivity : ItinBaseActivity() {

    companion object {
        private const val ITIN_ID = "ITIN_ID"

        @JvmStatic
        fun createIntent(context: Context, id: String): Intent {
            val intent = Intent(context, FlightItinManageBookingActivity::class.java)
            intent.putExtra(ITIN_ID, id)
            return intent
        }
    }

    private val itinToolbar by bindView<ItinToolbar>(R.id.manage_booking_flight_itin_toolbar)
    private val customerSupportDetails by bindView<FlightItinCustomerSupportDetails>(R.id.flight_itin_customer_support_widget)
    private var trackingFired = false
    private val legsDetailWidget by bindView<FlightItinLegsDetailWidget>(R.id.manage_booking_itin_flight_leg_detail)
    private val airlineSupportDetailsWidget by bindView<FlightItinAirlineSupportDetailsWidget>(R.id.flight_itin_airline_support_widget)
    private val modifyReservationWidget by bindView<ItinModifyReservationWidget>(R.id.flight_itin_modify_reservation_widget)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultTripComponents()
        setContentView(R.layout.manage_booking_flight_itin)
        viewModel = FlightItinManageBookingViewModel(this, intent.getStringExtra(ITIN_ID))
        toolbarViewModel = FlightItinToolbarViewModel()
        legsDetailWidget.viewModel = FlightItinLegsDetailWidgetViewModel()
        itinToolbar.viewModel = toolbarViewModel
    }

    override fun onResume() {
        super.onResume()
        viewModel.setUp()
        if (!trackingFired) {
            OmnitureTracking.trackItinFlightManageBookingActivity(viewModel.createOmnitureTrackingValues())
            trackingFired = true
        }
    }

    var viewModel: FlightItinManageBookingViewModel by notNullAndObservable { vm ->
        vm.itinCardDataNotValidSubject.subscribe {
            finish()
        }
        vm.updateToolbarSubject.subscribe { params ->
            val toolbarViewModel = itinToolbar.viewModel as ItinToolbarViewModel
            toolbarViewModel.updateWidget(params)
        }
        vm.customerSupportDetailsSubject.subscribe { params ->
            customerSupportDetails.viewModel = FlightItinCustomerSupportDetailsViewModel()
            customerSupportDetails.viewModel.updateWidget(params)
        }
        vm.flightLegDetailWidgetLegDataSubject.subscribe { params ->
            legsDetailWidget.viewModel.updateWidgetRecyclerViewSubject.onNext(params)
        }
        vm.flightLegDetailRulesAndRegulationSubject.subscribe { param ->
            legsDetailWidget.viewModel.rulesAndRestrictionDialogTextSubject.onNext(param)
        }
        vm.flightSplitTicketVisibilitySubject.subscribe { param ->
            legsDetailWidget.viewModel.shouldShowSplitTicketTextSubject.onNext(param)
        }
        vm.flightItinAirlineSupportDetailsSubject.subscribe { param ->
            airlineSupportDetailsWidget.viewModel = FlightItinAirlineSupportDetailsViewModel()
            airlineSupportDetailsWidget.viewModel.airlineSupportDetailsWidgetSubject.onNext(param)
        }
        vm.itinCardDataFlightObservable.subscribe {
            modifyReservationWidget.viewModel = FlightItinModifyReservationViewModel(this)
            modifyReservationWidget.viewModel.itinCardSubject.onNext(it)
        }
    }

    var toolbarViewModel: FlightItinToolbarViewModel by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finishActivity()
        }
    }

    fun finishActivity() {
        finish()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }

    override fun onSyncFinish() {
        viewModel.setUp()
    }
}
