package com.expedia.bookings.itin.cars.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.itin.common.ItinCustomerSupportViewModel
import com.expedia.bookings.itin.common.ItinCustomerSupportWidget
import com.expedia.bookings.itin.common.ItinRepo
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.itin.scopes.CarItinMoreHelpMasterScope
import com.expedia.bookings.itin.scopes.ItinCustomerSupportWidgetViewModelScope
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.IToaster
import com.expedia.bookings.itin.utils.Intentable
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.itin.utils.WebViewLauncher
import com.expedia.bookings.tracking.ITripsTracking
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import javax.inject.Inject

class CarItinMoreHelpActivity : AppCompatActivity() {
    companion object : Intentable {
        private const val CAR_ITIN_ID = "CAR_ITIN_ID"

        @JvmStatic
        override fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, CarItinMoreHelpActivity::class.java)
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

    val toolbar: ItinToolbar by bindView(R.id.widget_car_more_help_toolbar)
    val carItinMoreHelpWidget: CarItinMoreHelpWidget by bindView(R.id.widget_car_itin_more_help)

    lateinit var repo: ItinRepoInterface
    val itinCustomerSupportWidget: ItinCustomerSupportWidget by bindView(R.id.widget_car_itin_customer_support)

    var toolbarViewModel: CarItinMoreHelpToolbarViewModel<CarItinMoreHelpMasterScope> by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finish()
        }
    }

    var moreHelpViewModel: CarItinMoreHelpViewModel<CarItinMoreHelpMasterScope> by notNullAndObservable { vm ->
        vm.invalidSubject.subscribe {
            finish()
        }
    }

    val itineraryManager: ItineraryManager = ItineraryManager.getInstance()
    var tripsTracking: ITripsTracking = TripsTracking

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.car_more_help)
        Ui.getApplication(this).defaultTripComponents()

        stringProvider = Ui.getApplication(this).appComponent().stringProvider()
        val tripsTracking = TripsTracking
        val jsonUtil = Ui.getApplication(this).appComponent().jsonUtilProvider()
        repo = ItinRepo(intent.getStringExtra(CAR_ITIN_ID), jsonUtil, itineraryManager.syncFinishObservable)
        val scope = CarItinMoreHelpMasterScope(stringProvider, this, tripsTracking, repo)

        toolbarViewModel = CarItinMoreHelpToolbarViewModel(scope)
        toolbar.viewModel = toolbarViewModel

        moreHelpViewModel = CarItinMoreHelpViewModel(scope)
        carItinMoreHelpWidget.viewModel = moreHelpViewModel

        val customerSupportWidgetScope = ItinCustomerSupportWidgetViewModelScope(stringProvider, repo, this, tripsTracking, WebViewLauncher(this), TripProducts.ACTIVITY.name)
        val customerSupportViewModel = ItinCustomerSupportViewModel(customerSupportWidgetScope)
        itinCustomerSupportWidget.viewModel = customerSupportViewModel
    }

    override fun finish() {
        super.finish()
        moreHelpViewModel.finishSubject.onNext(Unit)
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }
}
