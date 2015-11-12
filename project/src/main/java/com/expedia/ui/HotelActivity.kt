package com.expedia.ui

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AddToCalendarUtils
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.PaymentWidget
import com.expedia.vm.HotelTravelerParams
import com.google.android.gms.maps.MapView
import com.mobiata.android.Log
import rx.Observer

public class HotelActivity : AbstractAppCompatActivity() {

    val hotelPresenter: HotelPresenter by lazy {
        findViewById(R.id.hotel_presenter) as HotelPresenter
    }

    val resultsMapView: MapView by lazy {
        hotelPresenter.findViewById(R.id.map_view) as MapView
    }

    val detailsMapView: MapView by lazy {
        hotelPresenter.findViewById(R.id.detailed_map_view) as MapView
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

        if (getIntent().hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
            handleNavigationViaDeepLink()
        } else {
            hotelPresenter.defaultTransitionObserver.onNext(Screen.SEARCH)
        }
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

    override fun onResume() {
        resultsMapView.onResume()
        detailsMapView.onResume()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PaymentWidget.REQUEST_CODE_GOOGLE_WALLET_ACTIVITY -> when (resultCode) {
                Activity.RESULT_OK -> {
                    if (Db.getBillingInfo() != null) {
                        hotelPresenter.checkoutPresenter.hotelCheckoutWidget.paymentInfoCardView.sectionBillingInfo.bind(Db.getBillingInfo())
                        hotelPresenter.checkoutPresenter.hotelCheckoutWidget.paymentInfoCardView.setExpanded(false)
                        hotelPresenter.checkoutPresenter.hotelCheckoutWidget.mainContactInfoCardView.bindGoogleWalletTraveler(Db.getGoogleWalletTraveler())
                        hotelPresenter.checkoutPresenter.hotelCheckoutWidget.mainContactInfoCardView.setExpanded(false)
                    }
                    return
                }
            }

            AddToCalendarUtils.requestCodeAddCheckInToCalendarActivity -> { // show add to calendar for checkOut date
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

    fun handleNavigationViaDeepLink() {
        val hotelSearchParams = HotelsV2DataUtil.getHotelV2SearchParamsFromJSON(getIntent().getStringExtra("hotelSearchParams"))
        if (hotelSearchParams != null) {
            val isCurrentLocationSearch = "MY_LOCATION".equals(hotelSearchParams?.suggestion?.type)
            if (isCurrentLocationSearch) {
                hotelSearchParams?.suggestion?.regionNames?.displayName = resources.getString(R.string.current_location)
                hotelSearchParams?.suggestion?.regionNames?.shortName = resources.getString(R.string.current_location)
                if (hotelSearchParams?.suggestion?.coordinates?.lat == 0.0 || hotelSearchParams?.suggestion?.coordinates?.lng == 0.0)
                    CurrentLocationObservable.create(this).subscribe(generateLocationServiceCallback(hotelSearchParams))
                else {
                    startCurrentLocationSearch(hotelSearchParams)
                }
            } else {
                hotelPresenter.searchPresenter.searchViewModel.suggestionObserver.onNext(hotelSearchParams?.suggestion)
                if (hotelSearchParams.suggestion.hotelId == null) {
                    val displayName = hotelSearchParams?.suggestion?.regionNames?.displayName ?: ""
                    if (displayName.length() > 0 ) {
                        val service = Ui.getApplication(this).hotelComponent().suggestionsService()
                        service.getHotelSuggestionsV4(displayName, ServicesUtil.generateClientId(this), generateSuggestionServiceCallback(hotelSearchParams))
                        return
                    }
                }
                setUpDeepLinkSearch(hotelSearchParams, isCurrentLocationSearch)
                hotelPresenter.defaultTransitionObserver.onNext(Screen.DETAILS)
            }

        }
    }

    private fun setUpDeepLinkSearch(hotelSearchParams: com.expedia.bookings.data.hotels.HotelSearchParams?, isCurrentLocationSearch: Boolean) {
        hotelPresenter.searchPresenter.searchViewModel.enableDateObserver.onNext(Unit)
        hotelPresenter.searchPresenter.traveler.viewmodel.travelerParamsObservable.onNext(HotelTravelerParams(hotelSearchParams?.adults ?: 1, hotelSearchParams?.children ?: emptyList()))
        val dates = Pair (hotelSearchParams?.checkIn, hotelSearchParams?.checkOut)
        hotelPresenter.searchPresenter.searchViewModel.datesObserver.onNext(dates)
        hotelPresenter.searchPresenter.calendar.setSelectedDates(hotelSearchParams?.checkIn, hotelSearchParams?.checkOut)
        if (!isCurrentLocationSearch) {
            hotelPresenter.searchObserver.onNext(hotelSearchParams)
        }
    }

    private fun generateSuggestionServiceCallback(hotelSearchParams: com.expedia.bookings.data.hotels.HotelSearchParams): Observer<List<SuggestionV4>> {
        return object : Observer<List<SuggestionV4>> {
            override fun onNext(essSuggestions: List<SuggestionV4>) {
                hotelPresenter.defaultTransitionObserver.onNext(Screen.RESULTS)
                hotelSearchParams.suggestion.gaiaId = essSuggestions.first().gaiaId
                setUpDeepLinkSearch(hotelSearchParams, false)
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                hotelPresenter.defaultTransitionObserver.onNext(Screen.SEARCH)
                Log.e("Hotel Suggestions Error", e)
            }
        }
    }

    private fun startCurrentLocationSearch(hotelSearchParams: com.expedia.bookings.data.hotels.HotelSearchParams?) {
        hotelPresenter.defaultTransitionObserver.onNext(Screen.RESULTS)
        hotelPresenter.searchPresenter.searchViewModel.suggestionObserver.onNext(hotelSearchParams?.suggestion)
        hotelPresenter.searchObserver.onNext(hotelSearchParams)
        setUpDeepLinkSearch(hotelSearchParams, true)
    }

    private fun generateLocationServiceCallback(hotelSearchParams: com.expedia.bookings.data.hotels.HotelSearchParams?): Observer<Location> {
        return object : Observer<Location> {
            override fun onNext(location: Location) {
                val coordinate = SuggestionV4.LatLng()
                coordinate.lat = location.latitude
                coordinate.lng = location.longitude
                hotelSearchParams?.suggestion?.coordinates = coordinate
                startCurrentLocationSearch(hotelSearchParams)
            }

            override fun onCompleted() {
                // ignore
            }

            override fun onError(e: Throwable?) {
                hotelPresenter.defaultTransitionObserver.onNext(Screen.SEARCH)
            }
        }
    }

    // Showing different presenter based on deeplink
    public enum class Screen{
        SEARCH,
        DETAILS,
        RESULTS
    }


}

