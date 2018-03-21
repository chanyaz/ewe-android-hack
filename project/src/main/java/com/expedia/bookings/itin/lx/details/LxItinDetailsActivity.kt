package com.expedia.bookings.itin.lx.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.lx.ItinLxRepo
import com.expedia.bookings.itin.lx.LxItinToolbarViewModel
import com.expedia.bookings.itin.scopes.LxItinToolbarScope
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.Intentable
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable

class LxItinDetailsActivity : AppCompatActivity() {

    companion object : Intentable {
        private const val LX_ITIN_ID = "LX_ITIN_ID"

        @JvmStatic
        override fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, LxItinDetailsActivity::class.java)
            i.putExtra(LX_ITIN_ID, id)
            return i
        }
    }

    val toolbar: ItinToolbar by bindView(R.id.widget_lx_itin_toolbar)
    val itineraryManager: ItineraryManager = ItineraryManager.getInstance()

    lateinit var jsonUtil: IJsonToItinUtil
    lateinit var stringProvider: StringSource
    lateinit var lxRepo: ItinLxRepo

    var toolbarViewModel: LxItinToolbarViewModel<LxItinToolbarScope> by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finishActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lx_itin_card_details)
        Ui.getApplication(this).defaultTripComponents()

        stringProvider = Ui.getApplication(this).appComponent().stringProvider()
        jsonUtil = Ui.getApplication(this).tripComponent().jsonUtilProvider()
        lxRepo = ItinLxRepo(intent.getStringExtra(LX_ITIN_ID), jsonUtil, itineraryManager.syncFinishObservable)

        val toolbarScope = LxItinToolbarScope(stringProvider, lxRepo, this)
        toolbarViewModel = LxItinToolbarViewModel(toolbarScope)
        toolbar.viewModel = toolbarViewModel
        lxRepo.invalidDataSubject.subscribe {
            finishActivity()
        }
    }

    fun finishActivity() {
        lxRepo.dispose()
        finish()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }
}
