package com.expedia.bookings.itin.lx.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinImageWidget
import com.expedia.bookings.itin.common.ItinMapWidget
import com.expedia.bookings.itin.common.ItinTimingsWidget
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.common.ItinManageBookingWidget
import com.expedia.bookings.itin.scopes.LxLifeCycleObserverScope
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.utils.ActivityLauncher
import com.expedia.bookings.itin.utils.IToaster
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.Intentable
import com.expedia.bookings.itin.utils.PhoneHandler
import com.expedia.bookings.itin.utils.WebViewLauncher
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import javax.inject.Inject

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

    lateinit var toaster: IToaster
        @Inject set

    val toolbar: ItinToolbar by bindView(R.id.widget_lx_itin_toolbar)
    val manageBookingWidget: ItinManageBookingWidget by bindView(R.id.widget_manage_booking)
    val mapWidget: ItinMapWidget by bindView(R.id.map_widget)
    val redeemVoucherWidget: LxItinRedeemVoucherWidget by bindView(R.id.widget_lx_itin_redeem_voucher)
    val imageWidget: ItinImageWidget by bindView(R.id.itin_image_widget)
    val timingsWidget: ItinTimingsWidget<ItinLx> by bindView(R.id.itin_timings_widget)

    val lifecycleObserver: LxItinDetailsActivityLifecycleObserver<LxLifeCycleObserverScope<ItinLx>> by lazy {
        val stringProvider = Ui.getApplication(this).appComponent().stringProvider()
        val jsonUtil = Ui.getApplication(this).tripComponent().jsonUtilProvider()
        val webViewLauncher: IWebViewLauncher = WebViewLauncher(this)
        val activityLauncher = ActivityLauncher(this)
        val itinId = intent.getStringExtra(LX_ITIN_ID)
        val tripsTracking = TripsTracking
        val phoneHandler = PhoneHandler(this)
        val scope = LxLifeCycleObserverScope(stringProvider, webViewLauncher, activityLauncher, jsonUtil, itinId, manageBookingWidget, toolbar, tripsTracking, mapWidget, redeemVoucherWidget, toaster, phoneHandler, imageWidget, timingsWidget)
        LxItinDetailsActivityLifecycleObserver(scope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lx_itin_card_details)
        Ui.getApplication(this).defaultTripComponents()
        Ui.getApplication(this).tripComponent().inject(this)
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
