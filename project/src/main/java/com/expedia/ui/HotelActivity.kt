package com.expedia.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.content.ContextCompat
import android.transition.ChangeBounds
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.bookings.utils.AddToCalendarUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.Ui
import com.expedia.util.requestLocationPermission
import com.google.android.gms.maps.MapView

class HotelActivity : AbstractAppCompatActivity() {

    companion object {
        const val EXTRA_HOTEL_SEARCH_PARAMS = "hotelSearchParams"
    }

    val hotelPresenter: HotelPresenter by lazy {
        findViewById(R.id.hotel_presenter) as HotelPresenter
    }

    val resultsMapView: MapView by lazy {
        hotelPresenter.findViewById(R.id.map_view) as MapView
    }

    val detailsMapView: MapView by lazy {
        hotelPresenter.findViewById(R.id.details_map_view) as MapView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultHotelComponents()
        setContentView(R.layout.activity_hotel)
        Ui.showTransparentStatusBar(this)
        val mapState = savedInstanceState?.getBundle(Constants.HOTELS_MAP_STATE)
        resultsMapView.onCreate(mapState)
        detailsMapView.onCreate(mapState)

        if (intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
            val locationPermission = ContextCompat.checkSelfPermission(this.baseContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
            if (locationPermission == PackageManager.PERMISSION_DENIED) {
                requestLocationPermission(this)
            }
            else {
                handleDeepLink(intent)
            }
        } else {
            hotelPresenter.setDefaultTransition(Screen.SEARCH)
        }

        setUpAnimations()
    }

    private fun handleDeepLink(intent: Intent) {
        val searchParams = HotelsV2DataUtil.getHotelV2SearchParamsFromJSON(intent.getStringExtra(EXTRA_HOTEL_SEARCH_PARAMS))
        if (intent.hasExtra(Codes.MEMBER_ONLY_DEALS) && searchParams != null) {
            searchParams.sortType = HotelSearchParams.SortType.MOBILE_DEALS.sortName
            searchParams.shopWithPoints = false
        }
        hotelPresenter.handleDeepLink(searchParams)
    }

    override fun onBackPressed() {
        if (!hotelPresenter.back()) {
            Db.setTemporarilySavedCard(null)
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

    override fun onDestroy() {
        hotelPresenter.searchPresenter.shopWithPointsWidget.subscription.unsubscribe()
        hotelPresenter.searchPresenter.shopWithPointsWidget.shopWithPointsViewModel.subscription.unsubscribe()
        resultsMapView.onDestroy()
        detailsMapView.onDestroy()
        Ui.getApplication(this).setHotelComponent(null)
        super.onDestroy()
    }

    override fun onLowMemory() {
        resultsMapView.onLowMemory()
        detailsMapView.onLowMemory()
        super.onLowMemory()
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
        handleDeepLink(intent)
    }

    // Showing different presenter based on deeplink
    enum class Screen {
        SEARCH,
        DETAILS,
        RESULTS
    }

    private fun setUpAnimations() {
        val res = this.resources
        val sharedEnterTransition = ChangeBounds()
        sharedEnterTransition.duration = res.getInteger(R.integer.pro_wizard_shared_enter_duration).toLong()
        window.sharedElementEnterTransition = sharedEnterTransition

        val sharedReturnTransition = ChangeBounds()
        sharedReturnTransition.duration = res.getInteger(R.integer.pro_wizard_shared_return_duration).toLong()
        window.sharedElementReturnTransition = sharedReturnTransition

    }
}

