package com.expedia.bookings.presenter.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.hotel.BaseHotelResultsPresenter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelResultsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng

public class PackageHotelResultsPresenter(context: Context, attrs: AttributeSet) : BaseHotelResultsPresenter(context, attrs) {
    var viewmodel: HotelResultsViewModel by notNullAndObservable { vm ->
        vm.hotelResultsObservable.subscribe {
            vm.hotelResultsObservable.subscribe(listResultsObserver)
        }
        vm.hotelResultsObservable.subscribe(mapViewModel.hotelResultsSubject)

        vm.titleSubject.subscribe {
            toolbar.title = context.getString(R.string.package_hotel_results_toolbar_title_TEMPLATE, it)
        }

        vm.subtitleSubject.subscribe {
            toolbar.subtitle = it
        }

        vm.paramsSubject.subscribe { params ->
            if (params.suggestion.coordinates != null) {
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(params.suggestion.coordinates.lat, params.suggestion.coordinates.lng), 14.0f))
            }
            filterView.sortByObserver.onNext(params.suggestion.isCurrentLocationSearch && !params.suggestion.isGoogleSuggestionSearch)
            filterView.viewmodel.clearObservable.onNext(Unit)
        }
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_package_hotel_results, this)
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.packages_primary_color))
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(adapterListener)
    }

    override fun doAreaSearch() {
    }

    override fun hideSearchThisArea() {
    }

    override fun showSearchThisArea() {
    }

}
