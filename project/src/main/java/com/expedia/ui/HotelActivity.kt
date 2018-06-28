package com.expedia.ui

import android.content.ComponentCallbacks2
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.dagger.HotelComponentInjector
import com.expedia.bookings.data.BaseHotelFilterOptions
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.deeplink.HotelLandingPage
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.bookings.utils.AddToCalendarUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.PermissionsUtils.requestLocationPermission
import com.google.android.gms.maps.MapView

class HotelActivity : AbstractAppCompatActivity(), ComponentCallbacks2 {
    val hotelPresenter by bindView<HotelPresenter>(R.id.hotel_presenter)

    val resultsMapView: MapView by lazy {
        hotelPresenter.findViewById<MapView>(R.id.map_view)
    }

    val detailsMapView: MapView by lazy {
        hotelPresenter.findViewById<MapView>(R.id.details_map_view)
    }

    val hotelComponentInjector = HotelComponentInjector()

    var infositeDeeplinkDontBackToSearch = false
        private set
    private var keepHotelModuleOnDestroy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hotelComponentInjector.inject(this)
        setContentView(R.layout.activity_hotel)
        Ui.showTransparentStatusBar(this)
        val mapState = savedInstanceState?.getBundle(Constants.HOTELS_MAP_STATE)
        resultsMapView.onCreate(mapState)
        detailsMapView.onCreate(mapState)

        if (intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
            val locationPermission = ContextCompat.checkSelfPermission(this.baseContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
            if (locationPermission == PackageManager.PERMISSION_DENIED) {
                requestLocationPermission(this)
            } else {
                handleDeepLink(intent)
            }
        } else {
            hotelPresenter.setDefaultTransition(Screen.SEARCH)
        }
    }

    private fun handleDeepLink(intent: Intent) {
        infositeDeeplinkDontBackToSearch = intent.getBooleanExtra(Codes.INFOSITE_DEEPLINK_DONT_BACK_TO_SEARCH, false)
        keepHotelModuleOnDestroy = intent.getBooleanExtra(Codes.KEEP_HOTEL_MODULE_ON_DESTROY, false)

        val searchParams = HotelsV2DataUtil.getHotelV2SearchParamsFromJSON(intent.getStringExtra(HotelExtras.EXTRA_HOTEL_SEARCH_PARAMS))
        if (intent.hasExtra(Codes.DEALS) && searchParams != null) {
            searchParams.sortType = BaseHotelFilterOptions.SortType.MOBILE_DEALS.sortName
            searchParams.shopWithPoints = false
        }
        val landingPage = intent.getStringExtra(HotelExtras.LANDING_PAGE)
        hotelPresenter.handleDeepLink(searchParams, HotelLandingPage.fromId(landingPage))
    }

    override fun onBackPressed() {
        if (!hotelPresenter.back()) {
            Db.sharedInstance.setTemporarilySavedCard(null)
            super.onBackPressed()
        }
    }

    override fun onPause() {
        resultsMapView.onPause()
        detailsMapView.onPause()
        super.onPause()

        if (isFinishing) {
            clearCCNumber()
            clearStoredCard()
        } else {
            Ui.hideKeyboard(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
            handleDeepLink(intent)
        }
    }

    override fun onResume() {
        resultsMapView.onResume()
        detailsMapView.onResume()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            AddToCalendarUtils.requestCodeAddCheckInToCalendarActivity -> {
                // show add to calendar for checkOut date
                hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.showAddToCalendarIntent(false, this)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        hotelPresenter.cleanup()
    }

    override fun onDestroy() {
        hotelPresenter.onDestroyed()
        resultsMapView.onDestroy()
        detailsMapView.onDestroy()
        if (!keepHotelModuleOnDestroy) {
            hotelComponentInjector.clear(this)
        }
        super.onDestroy()
    }

    override fun onLowMemory() {
        resultsMapView.onLowMemory()
        detailsMapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                PicassoHelper.clearCache()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        val mapState = Bundle()
        resultsMapView.onSaveInstanceState(mapState)
        detailsMapView.onSaveInstanceState(mapState)
        outState!!.putBundle(Constants.HOTELS_MAP_STATE, mapState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Ui.getApplication(this).hotelComponent() == null) {
            hotelComponentInjector.inject(this)
        }
        handleDeepLink(intent)
    }

    // Showing different presenter based on deeplink
    enum class Screen {
        SEARCH,
        DETAILS,
        RESULTS
    }
}
