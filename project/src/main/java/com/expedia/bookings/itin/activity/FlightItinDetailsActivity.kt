package com.expedia.bookings.itin.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.itin.utils.ShareTripHelper
import com.expedia.bookings.itin.vm.FlightItinBagaggeInfoViewModel
import com.expedia.bookings.itin.vm.FlightItinBookingInfoViewModel
import com.expedia.bookings.itin.vm.FlightItinConfirmationViewModel
import com.expedia.bookings.itin.vm.FlightItinDetailsViewModel
import com.expedia.bookings.itin.vm.FlightItinLayoverViewModel
import com.expedia.bookings.itin.vm.FlightItinMapWidgetViewModel
import com.expedia.bookings.itin.vm.FlightItinSegmentSummaryViewModel
import com.expedia.bookings.itin.vm.FlightItinToolbarViewModel
import com.expedia.bookings.itin.vm.FlightItinTotalDurationViewModel
import com.expedia.bookings.itin.widget.FlightItinBookingDetailsWidget
import com.expedia.bookings.itin.widget.FlightItinMapWidget
import com.expedia.bookings.itin.widget.FlightItinSegmentSummaryWidget
import com.expedia.bookings.itin.widget.ItinConfirmationWidget
import com.expedia.bookings.itin.widget.ItinTimeDurationWidget
import com.expedia.bookings.itin.widget.ItinToolbar
import com.expedia.bookings.itin.widget.ItinWebviewInfoWidget
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import kotlin.properties.Delegates

class FlightItinDetailsActivity : AppCompatActivity() {

    companion object {
        private const val FLIGHT_ITIN_ID = "FLIGHT_ITIN_ID"

        @JvmStatic
        fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, FlightItinDetailsActivity::class.java)
            i.putExtra(FlightItinDetailsActivity.FLIGHT_ITIN_ID, id)
            return i
        }
    }

    var viewModel: FlightItinDetailsViewModel by notNullAndObservable { vm ->
        vm.itinCardDataNotValidSubject.subscribe {
            finishActivity()
        }
        vm.updateToolbarSubject.subscribe { params ->
            itinToolbar.viewModel.updateWidget(params)
        }
        vm.clearLegSummaryContainerSubject.subscribe {
            flightSummaryContainer.removeAllViews()
        }
        vm.createSegmentSummaryWidgetsSubject.subscribe { params ->
            val legSummaryWidget = FlightItinSegmentSummaryWidget(this, null)
            legSummaryWidget.viewModel = FlightItinSegmentSummaryViewModel(this)
            legSummaryWidget.viewModel.updateSegmentInformation(params)
            flightSummaryContainer.addView(legSummaryWidget)
        }
        vm.createLayoverWidgetSubject.subscribe { layoverDuration ->
            val layoverWidget = ItinTimeDurationWidget(this, null)
            layoverWidget.viewModel = FlightItinLayoverViewModel(this)
            layoverWidget.viewModel.updateWidget(layoverDuration)
            flightSummaryContainer.addView(layoverWidget)
        }
        vm.createTotalDurationWidgetSubject.subscribe { totalDuration ->
            flightTotalDurationWidget.viewModel = FlightItinTotalDurationViewModel(this)
            flightTotalDurationWidget.viewModel.updateWidget(totalDuration)
        }

        vm.createBaggageInfoWebviewWidgetSubject.subscribe { webviewURL ->
            flightItinBaggageInfoWidget.viewModel = FlightItinBagaggeInfoViewModel(this)
            flightItinBaggageInfoWidget.viewModel.updateWidgetWithBaggageInfoUrl(webviewURL)

        }
        vm.updateConfirmationSubject.subscribe { params ->
            itinConfirmationWidget.viewModel.updateWidget(params)
        }
        vm.createBookingInfoWidgetSubject.subscribe { params ->
            flightBookingDetailsWidget.viewModel = FlightItinBookingInfoViewModel(this, intent.getStringExtra(FlightItinDetailsActivity.FLIGHT_ITIN_ID))
            flightBookingDetailsWidget.viewModel.updateBookingInfoWidget(params)
        }
        vm.itinCardDataFlightObservable.subscribe(flightItinMapWidgetViewModel.itinCardDataObservable)
    }

    private val itinConfirmationWidget by bindView<ItinConfirmationWidget>(R.id.widget_itin_flight_confirmation_cardview)
    private val itinToolbar by bindView<ItinToolbar>(R.id.widget_flight_itin_toolbar)
    private val flightBookingDetailsWidget: FlightItinBookingDetailsWidget by bindView(R.id.widget_flight_itin_booking_details)
    private val flightSummaryContainer by bindView<LinearLayout>(R.id.flight_itin_summary_container)
    private val flightTotalDurationWidget: ItinTimeDurationWidget by bindView(R.id.widget_itin_flight_total_duration_cardview)
    private val flightItinBaggageInfoWidget: ItinWebviewInfoWidget by bindView(R.id.widget_itin_webview_info_cardview)
    private val flightItinMapWidget by bindView<FlightItinMapWidget>(R.id.widget_itin_flight_map)

    private var confirmationViewModel: FlightItinConfirmationViewModel by Delegates.notNull()
    private val flightItinMapWidgetViewModel by lazy {
        FlightItinMapWidgetViewModel()
    }
    var toolbarViewModel: FlightItinToolbarViewModel by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finishActivity()
        }
        vm.shareIconClickedSubject.subscribe {
            val shareHelper = ShareTripHelper(this, viewModel.itinCardDataFlight)
            shareHelper.fetchShortShareUrlShowShareDialog()
            OmnitureTracking.trackItinShareStart(TripComponent.Type.FLIGHT)
        }
    }

    private var trackingFired = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultTripComponents()
        setContentView(R.layout.flight_itin_card_details)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        viewModel = FlightItinDetailsViewModel(this, intent.getStringExtra(FlightItinDetailsActivity.FLIGHT_ITIN_ID))
        toolbarViewModel = FlightItinToolbarViewModel()
        itinToolbar.viewModel = toolbarViewModel
        confirmationViewModel = FlightItinConfirmationViewModel(this)
        itinConfirmationWidget.viewModel = confirmationViewModel
        flightItinMapWidget.viewModel = flightItinMapWidgetViewModel
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        if(!trackingFired) {
            OmnitureTracking.trackItinFlight(this, viewModel.createOmnitureTrackingValues())
            trackingFired = true
        }
    }

    override fun onBackPressed() {
        finishActivity()
    }

    fun finishActivity() {
        finish()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }
}
