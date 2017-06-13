package com.expedia.ui

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.bookings.utils.AddToCalendarUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.Ui
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelDeepLinkHandler
import com.expedia.vm.HotelSearchViewModel
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
        val mapState = savedInstanceState?.getBundle(Constants.HOTELS_MAP_STATE)
        resultsMapView.onCreate(mapState)
        detailsMapView.onCreate(mapState)

        if (intent.hasExtra(Codes.MEMBER_ONLY_DEALS)) {
            hotelPresenter.searchPresenter.memberDealsSearch = true
            hotelPresenter.searchPresenter.shopWithPointsWidget.visibility = View.INVISIBLE
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
                getSearchViewModel().destinationLocationObserver)
                .handleNavigationViaDeepLink(hotelSearchParams)
    }

    private fun getSearchViewModel(): HotelSearchViewModel {
        return hotelPresenter.searchPresenter.getSearchViewModel() as HotelSearchViewModel
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
        val mapState = Bundle();
        resultsMapView.onSaveInstanceState(mapState)
        detailsMapView.onSaveInstanceState(mapState)
        outState!!.putBundle(Constants.HOTELS_MAP_STATE, mapState);
    }

    private val deepLinkSearchObserver = endlessObserver<HotelSearchParams?> { hotelSearchParams ->
        setupDeepLinkSearch(hotelSearchParams, true)
    }

    private val suggestionLookupObserver = endlessObserver<Pair<String, Observer<List<SuggestionV4>>>> { params ->
        startSuggestionLookup(params.first, params.second)
    }

    private fun startSuggestionLookup(displayName: String, callback: Observer<List<SuggestionV4>>) {
        val service = Ui.getApplication(this).hotelComponent().suggestionsService()
        val sameAsWeb = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelAutoSuggestSameAsWeb)
        val guid: String? = if (sameAsWeb) Db.getAbacusGuid() else null
        service.getHotelSuggestionsV4(displayName, ServicesUtil.generateClient(this), callback, PointOfSale.getSuggestLocaleIdentifier(), sameAsWeb, guid)
    }

    private val currentLocationSearchObserver = endlessObserver<HotelSearchParams?> { hotelSearchParams ->
        startCurrentLocationSearch(hotelSearchParams)
    }

    private fun startCurrentLocationSearch(hotelSearchParams: HotelSearchParams?) {
        hotelPresenter.setDefaultTransition(Screen.RESULTS)
        getSearchViewModel().destinationLocationObserver.onNext(hotelSearchParams?.suggestion)
        hotelPresenter.searchObserver.onNext(hotelSearchParams)
        setupDeepLinkSearch(hotelSearchParams, false)
    }

    private fun setupDeepLinkSearch(hotelSearchParams: HotelSearchParams?, shouldExecuteSearch: Boolean) {
        hotelPresenter.searchPresenter.selectTravelers(TravelerParams(hotelSearchParams?.adults ?: 1, hotelSearchParams?.children ?: emptyList(), emptyList(), emptyList()))
        getSearchViewModel().datesUpdated(hotelSearchParams?.checkIn, hotelSearchParams?.checkOut)
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

