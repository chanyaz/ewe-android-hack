package com.expedia.ui

import android.os.Bundle
import android.os.PersistableBundle
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.convertPackageToSearchParams
import com.expedia.bookings.presenter.packages.PackageHotelPresenter
import com.expedia.bookings.utils.*
import com.google.android.gms.maps.MapView

class PackageHotelActivity : AbstractAppCompatActivity() {
    val hotelsPresenter: PackageHotelPresenter by lazy {
        findViewById(R.id.package_hotel_presenter) as PackageHotelPresenter
    }

    val resultsMapView: MapView by lazy {
        hotelsPresenter.findViewById<MapView>(R.id.map_view)
    }

    val detailsMapView: MapView by lazy {
        hotelsPresenter.findViewById<MapView>(R.id.details_map_view)
    }
    var restorePackageActivityForNullParams = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Db.getPackageParams() == null) {
            setResult(Constants.PACKAGE_PARAMS_NULL_RESTORE)
            restorePackageActivityForNullParams = true
            finish()
            return
        }
        setContentView(R.layout.package_hotel_activity)
        Ui.showTransparentStatusBar(this)
        resultsMapView.onCreate(savedInstanceState)
        detailsMapView.onCreate(savedInstanceState)
        AlertDialogUtils.showBookmarkDialog(this)

        if (intent.hasExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM)) {
            // back to hotel room, should also be able to back to hotel results
            Db.setPackageResponse(PackageResponseUtils.loadPackageResponse(this, PackageResponseUtils.RECENT_PACKAGE_HOTELS_FILE, isMidAPIEnabled(this)))
            val hotelOffers = PackageResponseUtils.loadHotelOfferResponse(this, PackageResponseUtils.RECENT_PACKAGE_HOTEL_OFFER_FILE)
            hotelsPresenter.selectedPackageHotel = Db.getPackageSelectedHotel()
            hotelsPresenter.detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_hotel_stay),  resources.getInteger(R.integer.max_calendar_selectable_date_range)))
            hotelsPresenter.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(hotelOffers)
            hotelsPresenter.detailPresenter.hotelMapView.viewmodel.offersObserver.onNext(hotelOffers)
            hotelsPresenter.defaultTransitionObserver.onNext(Screen.DETAILS)
            hotelsPresenter.resultsPresenter.viewModel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_hotel_stay),  resources.getInteger(R.integer.max_calendar_selectable_date_range)))
            hotelsPresenter.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(Db.getPackageResponse()))

        } else if (intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
            // change hotel room
            hotelsPresenter.defaultTransitionObserver.onNext(Screen.DETAILS_ONLY)
            hotelsPresenter.hotelSelectedObserver.onNext(Db.getPackageSelectedHotel())

        } else {
            hotelsPresenter.defaultTransitionObserver.onNext(Screen.RESULTS)
        }
    }

    override fun onPause() {
        resultsMapView.onPause()
        detailsMapView.onPause()
        super.onPause()
    }

    override fun onResume() {
        resultsMapView.onResume()
        detailsMapView.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        if (!restorePackageActivityForNullParams) {
            resultsMapView.onDestroy()
            detailsMapView.onDestroy()
        }
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

    override fun onBackPressed() {
        if (!hotelsPresenter.back()) {
            super.onBackPressed()
        }
    }

    enum class Screen {
        DETAILS,
        RESULTS,
        DETAILS_ONLY
    }
}