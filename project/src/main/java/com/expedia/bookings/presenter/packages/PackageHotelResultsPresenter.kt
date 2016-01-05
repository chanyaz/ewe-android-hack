package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.presenter.hotel.BaseResultsPresenter
import com.expedia.bookings.widget.PackageHotelResultsMapViewModel
import com.expedia.util.notNullAndObservable
import com.expedia.vm.PackageHotelResultsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng

public class PackageHotelResultsPresenter(context: Context, attrs: AttributeSet) : BaseResultsPresenter(context, attrs) {

    var viewmodel: PackageHotelResultsViewModel by notNullAndObservable { vm ->
        vm.hotelResultsObservable.subscribe {
            adapter.resultsSubject.onNext(Pair(it.hotels, HotelRate.UserPriceType.UNKNOWN))
        }
        vm.hotelResultsObservable.subscribe((mapViewModel as PackageHotelResultsMapViewModel).hotelResultsSubject)

        vm.titleSubject.subscribe {
            toolbar.title = it
        }

        vm.subtitleSubject.subscribe {
            toolbar.subtitle = it
        }

        vm.paramsSubject.subscribe { params ->
            if (params.destination.coordinates != null) {
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(params.destination.coordinates.lat, params.destination.coordinates.lng), 14.0f))
            }
        }
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_hotel_results_package, this)
        mapViewModel = PackageHotelResultsMapViewModel(context, lastBestLocationSafe(), iconFactory)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mapView = findViewById(R.id.map_view) as MapView
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(adapterListener)
        mapView.getMapAsync(this)
        show(ResultsList());
    }

}
