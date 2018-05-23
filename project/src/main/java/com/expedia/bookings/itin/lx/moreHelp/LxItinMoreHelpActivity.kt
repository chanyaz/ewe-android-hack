package com.expedia.bookings.itin.lx.moreHelp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.flight.common.ItinOmnitureUtils
import com.expedia.bookings.itin.lx.ItinLxRepo
import com.expedia.bookings.itin.scopes.LxItinMoreHelpViewModelScope
import com.expedia.bookings.itin.scopes.LxItinToolbarScope
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.Intentable
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable

class LxItinMoreHelpActivity : AppCompatActivity() {

    companion object : Intentable {
        private const val LX_ITIN_ID = "LX_ITIN_ID"

        @JvmStatic
        override fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, LxItinMoreHelpActivity::class.java)
            i.putExtra(LX_ITIN_ID, id)
            return i
        }
    }

    val toolbar: ItinToolbar by bindView(R.id.widget_lx_itin_toolbar)
    val lxItinMoreHelpWidget: LxItinMoreHelpWidget by bindView(R.id.widget_lx_itin_more_help)

    lateinit var jsonUtil: IJsonToItinUtil
    lateinit var lxRepo: ItinLxRepo
    lateinit var stringProvider: StringSource
    lateinit var moreHelpViewModel: LxItinMoreHelpViewModel<LxItinMoreHelpViewModelScope>
    var toolbarViewModel: LxItinMoreHelpToolbarViewModel<LxItinToolbarScope> by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finish()
        }
    }

    val itineraryManager: ItineraryManager = ItineraryManager.getInstance()
    val tripsTracking: ITripsTracking = TripsTracking

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lx_more_help)
        Ui.getApplication(this).defaultTripComponents()

        stringProvider = Ui.getApplication(this).appComponent().stringProvider()
        jsonUtil = Ui.getApplication(this).tripComponent().jsonUtilProvider()
        lxRepo = ItinLxRepo(intent.getStringExtra(LX_ITIN_ID), jsonUtil, itineraryManager.syncFinishObservable)

        val moreHelpScope = LxItinMoreHelpViewModelScope(stringProvider, lxRepo, this, tripsTracking)
        moreHelpViewModel = LxItinMoreHelpViewModel(moreHelpScope)
        lxItinMoreHelpWidget.viewModel = moreHelpViewModel

        val toolbarScope = LxItinToolbarScope(stringProvider, lxRepo, this)
        toolbarViewModel = LxItinMoreHelpToolbarViewModel(toolbarScope)
        toolbar.viewModel = toolbarViewModel

        lxRepo.liveDataItin.value?.let { trip ->
            trip.firstLx()?.let {
                val omnitureValues = ItinOmnitureUtils.createOmnitureTrackingValuesNew(trip, ItinOmnitureUtils.LOB.LX)
                TripsTracking.trackItinLxMoreHelpPageLoad(omnitureValues)
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }
}
