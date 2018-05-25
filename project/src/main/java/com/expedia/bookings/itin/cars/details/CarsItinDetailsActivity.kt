package com.expedia.bookings.itin.cars.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.itin.cars.ItinCarRepo
import com.expedia.bookings.itin.common.ItinImageWidget
import com.expedia.bookings.itin.common.ItinMapWidget
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.common.ItinManageBookingWidget
import com.expedia.bookings.itin.scopes.CarsMasterScope
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.ActivityLauncher
import com.expedia.bookings.itin.utils.IToaster
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.Intentable
import com.expedia.bookings.itin.utils.PhoneHandler
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.itin.utils.WebViewLauncher
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import javax.inject.Inject

class CarsItinDetailsActivity: AppCompatActivity() {

    companion object : Intentable {
        private const val CAR_ITIN_ID = "CAR_ITIN_ID"

        @JvmStatic
        override fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, CarsItinDetailsActivity::class.java)
            i.putExtra(CAR_ITIN_ID, id)
            return i
        }
    }

    lateinit var toaster: IToaster
        @Inject set

    lateinit var jsonUtil: IJsonToItinUtil
        @Inject set

    lateinit var stringProvider: StringSource
        @Inject set

    val toolbar: ItinToolbar by bindView(R.id.widget_lx_itin_toolbar)
    val manageBookingWidget: ItinManageBookingWidget by bindView(R.id.widget_manage_booking)
    val pickupMapWidget: ItinMapWidget by bindView(R.id.pickup_map_widget)
    val dropOffMapWidget: ItinMapWidget by bindView(R.id.dropOff_map_widget)
    val imageWidget: ItinImageWidget by bindView(R.id.itin_image_widget)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.car_itin_detail_activity)
        Ui.getApplication(this).tripComponent().inject(this)
        val activityLauncher = ActivityLauncher(this)
        val webViewLauncher: IWebViewLauncher = WebViewLauncher(this)
        val itinId = intent.getStringExtra(CAR_ITIN_ID)
        val tripsTracking = TripsTracking
        val phoneHandler = PhoneHandler(this)
        val repo = ItinCarRepo(itinId, jsonUtil, ItineraryManager.getInstance().syncFinishObservable)
        val scope = CarsMasterScope(stringProvider, webViewLauncher, this, activityLauncher, repo, toaster, phoneHandler, tripsTracking)
        imageWidget.viewModel = CarItinImageViewModel(scope)
    }
}