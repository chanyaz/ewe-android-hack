package com.expedia.bookings.itin.lx.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.scopes.LxLifeCycleObserverScope
import com.expedia.bookings.itin.utils.ActivityLauncher
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.Intentable
import com.expedia.bookings.itin.utils.WebViewLauncher
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

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
    val manageBookingWidget: LxItinManageBookingWidget by bindView(R.id.widget_manage_booking)
    val mapWidget: LxItinMapWidget by bindView(R.id.map_widget)

    val lifecycleObserver: LxItinDetailsActivityLifecycleObserver<LxLifeCycleObserverScope> by lazy {
        val stringProvider = Ui.getApplication(this).appComponent().stringProvider()
        val jsonUtil = Ui.getApplication(this).tripComponent().jsonUtilProvider()
        val webViewLauncher: IWebViewLauncher = WebViewLauncher(this)
        val activityLauncher = ActivityLauncher(this)
        val itinId = intent.getStringExtra(LX_ITIN_ID)
        val tripsTracking = TripsTracking
        val scope = LxLifeCycleObserverScope(stringProvider, webViewLauncher, activityLauncher, jsonUtil, itinId, manageBookingWidget, toolbar, tripsTracking, mapWidget)
        LxItinDetailsActivityLifecycleObserver(scope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lx_itin_card_details)
        Ui.getApplication(this).defaultTripComponents()
        this.lifecycle.addObserver(lifecycleObserver)
        lifecycleObserver.finishSubject.subscribe {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }
}
