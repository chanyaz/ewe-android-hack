package com.expedia.ui

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AddToCalendarUtils
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.Ui
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelDeepLinkHandler
import com.expedia.vm.HotelTravelerParams
import com.google.android.gms.maps.MapView
import rx.Observer

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
        resultsMapView.onCreate(savedInstanceState)
        detailsMapView.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            OmnitureTracking.trackHotelsABTest()
        }

        if (intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
            handleDeeplink(intent)
        } else {
            hotelPresenter.setDefaultTransition(Screen.SEARCH)
        }
    }

    private fun handleDeeplink(intent: Intent) {
        val hotelSearchParams = HotelsV2DataUtil.getHotelV2SearchParamsFromJSON(intent.getStringExtra(EXTRA_HOTEL_SEARCH_PARAMS))
        HotelDeepLinkHandler(this,
                deepLinkSearchObserver,
                suggestionLookupObserver,
                currentLocationSearchObserver,
                hotelPresenter,
                hotelPresenter.searchPresenter.searchViewModel.suggestionObserver)
                .handleNavigationViaDeepLink(hotelSearchParams)
    }

    override fun onBackPressed() {
        if (!hotelPresenter.back()) {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        resultsMapView.onPause()
        detailsMapView.onPause()
        super.onPause()

        if (isFinishing()) {
            clearCCNumber()
            clearStoredCard()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
            handleDeeplink(intent)
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
        resultsMapView.onSaveInstanceState(outState)
        detailsMapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    private val deepLinkSearchObserver = endlessObserver<HotelSearchParams?> { hotelSearchParams ->
        setupDeepLinkSearch(hotelSearchParams, true)
    }

    private val suggestionLookupObserver = endlessObserver<Pair<String, Observer<List<SuggestionV4>>>> { params ->
        startSuggestionLookup(params.first, params.second)
    }

    private fun startSuggestionLookup(displayName: String, callback: Observer<List<SuggestionV4>>) {
        val service = Ui.getApplication(this).hotelComponent().suggestionsService()
        service.getHotelSuggestionsV4(displayName, ServicesUtil.generateClientId(this), callback, PointOfSale.getSuggestLocaleIdentifier())
    }

    private val currentLocationSearchObserver = endlessObserver<HotelSearchParams?> { hotelSearchParams ->
        startCurrentLocationSearch(hotelSearchParams)
    }

    private fun startCurrentLocationSearch(hotelSearchParams: HotelSearchParams?) {
        hotelPresenter.setDefaultTransition(Screen.RESULTS)
        hotelPresenter.searchPresenter.searchViewModel.suggestionObserver.onNext(hotelSearchParams?.suggestion)
        hotelPresenter.searchObserver.onNext(hotelSearchParams)
        setupDeepLinkSearch(hotelSearchParams, false)
    }

    private fun setupDeepLinkSearch(hotelSearchParams: HotelSearchParams?, shouldExecuteSearch: Boolean) {
        hotelPresenter.searchPresenter.searchViewModel.enableDateObserver.onNext(Unit)
        hotelPresenter.searchPresenter.selectTravelers(HotelTravelerParams(hotelSearchParams?.adults ?: 1, hotelSearchParams?.children ?: emptyList()))
        val dates = Pair (hotelSearchParams?.checkIn, hotelSearchParams?.checkOut)
        hotelPresenter.searchPresenter.searchViewModel.datesObserver.onNext(dates)
        hotelPresenter.searchPresenter.selectDates(hotelSearchParams?.checkIn, hotelSearchParams?.checkOut)
        if (shouldExecuteSearch) {
            hotelPresenter.searchObserver.onNext(hotelSearchParams)
        }
    }

    // Showing different presenter based on deeplink
    enum class Screen {
        SEARCH,
        DETAILS,
        RESULTS
    }
}

