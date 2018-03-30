package com.expedia.bookings.itin.flight.traveler

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.flight.manageBooking.Sweeett
import com.expedia.bookings.itin.scopes.SweetScope
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

class FlightItinTravelerInfoActivity : AppCompatActivity() {

    companion object {
        private const val FLIGHT_ITIN_ID = "FLIGHT_ITIN_ID"

        @JvmStatic
        fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, FlightItinTravelerInfoActivity::class.java)
            i.putExtra(FLIGHT_ITIN_ID, id)
            return i
        }
    }

    private val travelerToolbar: ItinToolbar by bindView(R.id.widget_traveler_itin_toolbar)
    private val travelerTabWidget: TabLayout by bindView(R.id.widget_traveler_itin_tab_layout)
    private val travelerInfoWidget: FlightItinTravelerInfoWidget by bindView(R.id.traveler_info_card_view)
    private val travelerPreferencesWidget: FlightItinTravelerPreferenceWidget by bindView(R.id.preferences_card_view)

    private val lifecycleObserver: Sweeett<SweetScope> by lazy {
        val stringProvider: StringSource = Ui.getApplication(this).appComponent().stringProvider()
        val scope = SweetScope(travelerToolbar, stringProvider, travelerTabWidget, travelerInfoWidget,travelerPreferencesWidget, intent.getStringExtra(FLIGHT_ITIN_ID))
        val sweet = Sweeett(scope)
        sweet.finishSubject.subscribe {
            finishActivity()
        }
        sweet
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_itin_traveler_info)
        this.lifecycle.addObserver(lifecycleObserver)
    }

    override fun onBackPressed() = finishActivity()

    fun finishActivity() {
        finish()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }
}
