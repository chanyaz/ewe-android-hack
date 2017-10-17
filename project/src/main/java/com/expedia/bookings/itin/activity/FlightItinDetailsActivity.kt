package com.expedia.bookings.itin.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinConfirmationViewModel
import com.expedia.bookings.itin.vm.FlightItinDetailsViewModel
import com.expedia.bookings.itin.vm.FlightItinDurationViewModel
import com.expedia.bookings.itin.vm.FlightItinSegmentSummaryViewModel
import com.expedia.bookings.itin.vm.FlightItinToolbarViewModel
import com.expedia.bookings.itin.widget.FlightItinDurationWidget
import com.expedia.bookings.itin.widget.FlightItinSegmentSummaryWidget
import com.expedia.bookings.itin.widget.ItinConfirmationWidget
import com.expedia.bookings.itin.widget.ItinToolbar
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
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
            legSummaryWidget.viewModel.updateWidget(params)
            flightSummaryContainer.addView(legSummaryWidget)
        }
        vm.updateConfirmationSubject.subscribe { params ->
            itinConfirmationWidget.viewModel.updateWidget(params)
        }
        vm.updateTotalDurationSubject.subscribe { params ->
            flightDurationWidget.viewModel.updateWidget(params)
        }
    }
    private val itinConfirmationWidget: ItinConfirmationWidget by lazy {
        findViewById(R.id.widget_itin_flight_confirmation_cardview) as ItinConfirmationWidget
    }
    private val itinToolbar: ItinToolbar by lazy {
        findViewById(R.id.widget_flight_itin_toolbar) as ItinToolbar
    }

    private val flightDurationWidget by lazy {
        findViewById(R.id.widget_itin_flight_duration_cardview) as FlightItinDurationWidget
    }

    var toolbarViewModel: FlightItinToolbarViewModel by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finishActivity()
        }
    }
    private val flightSummaryContainer: LinearLayout by lazy {
        findViewById(R.id.flight_itin_summary_container) as LinearLayout
    }
    private var confirmationViewModel: FlightItinConfirmationViewModel by Delegates.notNull()

    private var durationViewModel: FlightItinDurationViewModel by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultTripComponents()
        setContentView(R.layout.flight_itin_card_details)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        viewModel = FlightItinDetailsViewModel(this, intent.getStringExtra(FlightItinDetailsActivity.FLIGHT_ITIN_ID))
        toolbarViewModel = FlightItinToolbarViewModel(this)
        itinToolbar.viewModel = toolbarViewModel
        confirmationViewModel = FlightItinConfirmationViewModel(this)
        itinConfirmationWidget.viewModel = confirmationViewModel
        durationViewModel = FlightItinDurationViewModel()
        flightDurationWidget.viewModel = durationViewModel

        OmnitureTracking.trackItinFlight(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onBackPressed() {
        finishActivity()
    }

    fun finishActivity() {
        finish()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }
}
