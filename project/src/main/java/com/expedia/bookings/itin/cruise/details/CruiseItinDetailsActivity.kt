package com.expedia.bookings.itin.cruise.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.itin.common.ItinRepo
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.cruise.toolbar.CruiseItinToolbarViewModel
import com.expedia.bookings.itin.scopes.CruiseScope
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.Intentable
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import javax.inject.Inject

class CruiseItinDetailsActivity : AppCompatActivity() {

    companion object : Intentable {
        private const val CRUISE_ITIN_ID = "CRUISE_ITIN_ID"

        @JvmStatic
        override fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, CruiseItinDetailsActivity::class.java)
            i.putExtra(CRUISE_ITIN_ID, id)
            return i
        }
    }

    val toolbar: ItinToolbar by bindView(R.id.widget_itin_toolbar)

    lateinit var jsonUtil: IJsonToItinUtil
        @Inject set

    lateinit var stringProvider: StringSource
        @Inject set

    lateinit var repo: ItinRepoInterface

    var toolbarViewModel: NewItinToolbarViewModel by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finish()
        }
    }
    var tripsTracking: ITripsTracking = TripsTracking

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cruise_itin_detail_activity)
        Ui.getApplication(this).tripComponent().inject(this)

        val itinId = intent.getStringExtra(CRUISE_ITIN_ID)
        repo = ItinRepo(itinId, jsonUtil, ItineraryManager.getInstance().syncFinishObservable)
        val scope = CruiseScope(repo, stringProvider, this, tripsTracking)

        toolbarViewModel = CruiseItinToolbarViewModel(scope)
        toolbar.viewModel = toolbarViewModel
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }
}
