package com.expedia.bookings.itin.cars.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.itin.cars.ItinCarRepo
import com.expedia.bookings.itin.cars.ItinCarRepoInterface
import com.expedia.bookings.itin.common.ItinImageWidget
import com.expedia.bookings.itin.common.ItinMapWidget
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.common.ItinManageBookingWidget
import com.expedia.bookings.itin.common.ItinTimingsWidget
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.scopes.CarsMasterScope
import com.expedia.bookings.itin.tripstore.data.ItinCar
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
import com.expedia.util.notNullAndObservable
import javax.inject.Inject

class CarsItinDetailsActivity : AppCompatActivity() {

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

    val toolbar: ItinToolbar by bindView(R.id.widget_itin_toolbar)
    val manageBookingWidget: ItinManageBookingWidget by bindView(R.id.widget_manage_booking)
    val pickupMapWidget: ItinMapWidget<ItinCar> by bindView(R.id.pickup_map_widget)
    val dropOffMapWidget: ItinMapWidget<ItinCar> by bindView(R.id.dropOff_map_widget)
    val imageWidget: ItinImageWidget<ItinCar> by bindView(R.id.itin_image_widget)
    val timingsWidget: ItinTimingsWidget<ItinCar> by bindView(R.id.itin_timings_widget)

    var toolbarViewModel: NewItinToolbarViewModel by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finish()
        }
    }

    var dropOffMapViewModel: CarItinDropOffMapWidgetViewModel by notNullAndObservable { vm ->
        vm.showVisibilitySubject.subscribe {
            dropOffMapWidget.visibility = View.VISIBLE
        }
    }

    lateinit var repo: ItinCarRepoInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.car_itin_detail_activity)
        Ui.getApplication(this).tripComponent().inject(this)

        val activityLauncher = ActivityLauncher(this)
        val webViewLauncher: IWebViewLauncher = WebViewLauncher(this)
        val itinId = intent.getStringExtra(CAR_ITIN_ID)
        val tripsTracking = TripsTracking
        val phoneHandler = PhoneHandler(this)
        repo = ItinCarRepo(itinId, jsonUtil, ItineraryManager.getInstance().syncFinishObservable)

        val scope = CarsMasterScope(stringProvider, webViewLauncher, this, activityLauncher, repo, toaster, phoneHandler, tripsTracking)

        imageWidget.viewModel = CarItinImageViewModel(scope)

        toolbarViewModel = CarItinToolbarViewModel(scope)
        toolbar.viewModel = toolbarViewModel

        timingsWidget.viewModel = CarItinTimingsWidgetViewModel(scope)

        val mapScope = CarItinMapWidgetViewModelScope(stringProvider, tripsTracking, this, repo, toaster, phoneHandler, activityLauncher)

        pickupMapWidget.viewModel = CarItinPickupMapWidgetViewModel(mapScope)
        dropOffMapViewModel = CarItinDropOffMapWidgetViewModel(mapScope)

        dropOffMapWidget.viewModel = dropOffMapViewModel

        manageBookingWidget.viewModel = CarItinManageBookingWidgetViewModel(scope)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
        repo.dispose()
    }
}
