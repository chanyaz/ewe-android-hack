package com.expedia.ui

import android.os.Bundle
import android.os.PersistableBundle
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.convertPackageToSearchParams
import com.expedia.bookings.presenter.packages.PackageHotelPresenter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isMidAPIEnabled
import com.google.android.gms.maps.MapView

class PackageHotelActivity : AbstractAppCompatActivity() {
    val hotelsPresenter by bindView<PackageHotelPresenter>(R.id.package_hotel_presenter)

    val resultsMapView: MapView by lazy {
        hotelsPresenter.findViewById<MapView>(R.id.map_view)
    }

    val detailsMapView: MapView by lazy {
        hotelsPresenter.findViewById<MapView>(R.id.details_map_view)
    }
    var restorePackageActivityForNullParams = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Db.sharedInstance.packageParams == null) {
            setResult(Constants.PACKAGE_PARAMS_NULL_RESTORE)
            restorePackageActivityForNullParams = true
            finish()
            return
        }
        setContentView(R.layout.package_hotel_activity)
        Ui.showTransparentStatusBar(this)
        resultsMapView.onCreate(savedInstanceState)
        detailsMapView.onCreate(savedInstanceState)

        if (intent.hasExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM)) {
            // back to hotel room, should also be able to back to hotel results
            Db.setPackageResponse(PackageResponseUtils.loadPackageResponse(this, PackageResponseUtils.RECENT_PACKAGE_HOTELS_FILE, isMidAPIEnabled(this)))
            val hotelOffers = PackageResponseUtils.loadHotelOfferResponse(this, PackageResponseUtils.RECENT_PACKAGE_HOTEL_OFFER_FILE)
            if (hotelOffers != null) {
                hotelsPresenter.selectedPackageHotel = Db.getPackageSelectedHotel()
                hotelsPresenter.detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.sharedInstance.packageParams, resources.getInteger(R.integer.calendar_max_days_hotel_stay), resources.getInteger(R.integer.max_calendar_selectable_date_range)))
                hotelsPresenter.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(hotelOffers)
                hotelsPresenter.detailPresenter.hotelMapView.viewmodel.offersObserver.onNext(hotelOffers)
                hotelsPresenter.defaultTransitionObserver.onNext(Screen.DETAILS)
                hotelsPresenter.resultsPresenter.viewModel.paramsSubject.onNext(convertPackageToSearchParams(Db.sharedInstance.packageParams, resources.getInteger(R.integer.calendar_max_days_hotel_stay), resources.getInteger(R.integer.max_calendar_selectable_date_range)))
                hotelsPresenter.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(Db.getPackageResponse()))
            }
        } else if (intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
            // change hotel room
            if (isMidAPIEnabled(this)) {
                Db.setPackageResponse(PackageResponseUtils.loadPackageResponse(this, PackageResponseUtils.RECENT_PACKAGE_HOTELS_FILE, isMidAPIEnabled(this)))
            }
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
            if (Db.getCachedPackageResponse() != null) {
                Db.setPackageResponse(Db.getCachedPackageResponse())
                Db.setCachedPackageResponse(null)
            }
            super.onBackPressed()
        }
    }

    enum class Screen {
        DETAILS,
        RESULTS,
        DETAILS_ONLY
    }
}
