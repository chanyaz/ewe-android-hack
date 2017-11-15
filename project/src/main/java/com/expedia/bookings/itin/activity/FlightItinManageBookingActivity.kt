package com.expedia.bookings.itin.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinToolbarViewModel
import com.expedia.bookings.itin.vm.FlightItinManageBookingViewModel
import com.expedia.bookings.itin.widget.ItinToolbar
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable

class FlightItinManageBookingActivity : AppCompatActivity() {

    companion object {
        private const val ITIN_ID = "ITIN_ID"

        @JvmStatic
        fun createIntent(context: Context, id: String): Intent {
            val intent = Intent(context, FlightItinManageBookingActivity::class.java)
            intent.putExtra(FlightItinManageBookingActivity.ITIN_ID, id)
            return intent
        }
    }

    private val itinToolbar by bindView<ItinToolbar>(R.id.manage_booking_flight_itin_toolbar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultTripComponents()
        setContentView(R.layout.manage_booking_flight_itin)
        viewModel = FlightItinManageBookingViewModel(this, intent.getStringExtra(FlightItinManageBookingActivity.ITIN_ID))
        toolbarViewModel = FlightItinToolbarViewModel()
        itinToolbar.viewModel = toolbarViewModel
    }

    override fun onResume() {
        super.onResume()
        viewModel.setUp()
    }

    var viewModel: FlightItinManageBookingViewModel by notNullAndObservable { vm ->
        vm.itinCardDataNotValidSubject.subscribe {
            finishActivity()
        }
        vm.updateToolbarSubject.subscribe { params ->
            itinToolbar.viewModel.updateWidget(params)
        }
    }

    var toolbarViewModel: FlightItinToolbarViewModel by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finishActivity()
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