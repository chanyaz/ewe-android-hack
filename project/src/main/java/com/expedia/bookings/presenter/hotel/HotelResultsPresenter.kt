package com.expedia.bookings.presenter.hotel

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Rect
import android.location.Address
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FilterButtonWithCountWidget
import com.expedia.bookings.widget.MapLoadingOverlayWidget
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelResultsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.mobiata.android.BackgroundDownloader
import com.mobiata.android.LocationServices

public class HotelResultsPresenter(context: Context, attrs: AttributeSet) : BaseHotelResultsPresenter(context, attrs) {
    override val filterBtnWithCountWidget: FilterButtonWithCountWidget by bindView(R.id.sort_filter_button_container)
    override val searchThisArea: Button by bindView(R.id.search_this_area)
    override val loadingOverlay: MapLoadingOverlayWidget by bindView(R.id.map_loading_overlay)

    var viewmodel: HotelResultsViewModel by notNullAndObservable { vm ->
        vm.hotelResultsObservable.subscribe{
            filterBtnWithCountWidget.visibility = View.VISIBLE
            filterBtnWithCountWidget.translationY = 0f
            fab.isEnabled = true
        }
        vm.hotelResultsObservable.subscribe(listResultsObserver)
        vm.hotelResultsObservable.subscribe(mapViewModel.hotelResultsSubject)
        vm.mapResultsObservable.subscribe(listResultsObserver)
        vm.mapResultsObservable.subscribe(mapViewModel.mapResultsSubject)
        vm.mapResultsObservable.subscribe {
            val latLng = googleMap?.projection?.visibleRegion?.latLngBounds?.center
            mapViewModel.mapBoundsSubject.onNext(latLng)
            fab.isEnabled = true
        }

        vm.titleSubject.subscribe {
            toolbar.title = it
        }

        vm.subtitleSubject.subscribe {
            toolbar.subtitle = it
        }

        vm.paramsSubject.subscribe { params ->
            setMapToInitialState()
            showLoading()
            show(ResultsList())
            filterView.sortByObserver.onNext(params.suggestion.isCurrentLocationSearch && !params.suggestion.isGoogleSuggestionSearch)
            filterView.viewmodel.clearObservable.onNext(Unit)
        }

        vm.locationParamsSubject.subscribe { params ->
            loadingOverlay.animate(true)
            loadingOverlay.visibility = View.VISIBLE
            filterView.sortByObserver.onNext(params.isCurrentLocationSearch && !params.isGoogleSuggestionSearch)
            filterView.viewmodel.clearObservable.onNext(Unit)
        }
    }

    fun showLoading() {
        adapter.showLoading()
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(adapterListener)
        filterBtnWithCountWidget.visibility = View.GONE
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        ViewCompat.setElevation(loadingOverlay, context.resources.getDimension(R.dimen.launch_tile_margin_side))
        //Fetch, color, and slightly resize the searchThisArea location pin drawable
        val icon = ContextCompat.getDrawable(context, R.drawable.ic_material_location_pin).mutate()
        icon.setColorFilter(ContextCompat.getColor(context, R.color.hotels_primary_color), PorterDuff.Mode.SRC_IN)
        icon.bounds = Rect(icon.bounds.left, icon.bounds.top, (icon.bounds.right * 1.1).toInt(), (icon.bounds.bottom * 1.1).toInt())
        searchThisArea.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)

        //We don't want to show the searchThisArea button unless the map has just moved.
        searchThisArea.visibility = View.GONE
        searchThisArea.setOnClickListener({ view ->
            fab.isEnabled = false
            hideSearchThisArea()
            doAreaSearch()
            HotelV2Tracking().trackHotelsV2SearchAreaClick()
        })
    }

    override fun inflate() {
        View.inflate(getContext(), R.layout.widget_hotel_results, this)
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.hotels_primary_color))
    }

    override fun doAreaSearch() {
        val center = googleMap?.cameraPosition?.target
        val location = SuggestionV4()
        location.isSearchThisArea = true
        val region = SuggestionV4.RegionNames()
        region.displayName = context.getString(R.string.visible_map_area)
        region.shortName = context.getString(R.string.visible_map_area)
        location.regionNames = region
        val coordinate = SuggestionV4.LatLng()
        coordinate.lat = center?.latitude!!
        coordinate.lng = center?.longitude!!
        location.coordinates = coordinate
        viewmodel.locationParamsSubject.onNext(location)
    }

    override fun hideSearchThisArea() {
        if (searchThisArea?.getVisibility() == View.VISIBLE) {
            val anim: Animator = ObjectAnimator.ofFloat(searchThisArea, "alpha", 1f, 0f)
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationCancel(animation: Animator?) {
                    //Do nothing
                }

                override fun onAnimationEnd(animator: Animator?) {
                    searchThisArea?.setVisibility(View.GONE)
                }

                override fun onAnimationStart(animator: Animator?) {
                    //Do nothing
                }

                override fun onAnimationRepeat(animator: Animator?) {
                    //Do nothing
                }

            })
            anim.setDuration(DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION)
            anim.start()
        }
    }

    override fun showSearchThisArea() {
        if (currentState?.equals(ResultsMap::class.java.name) ?: false && searchThisArea?.visibility == View.GONE) {
            searchThisArea?.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(searchThisArea, "alpha", 0f, 1f).setDuration(DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION).start()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        super.onMapReady(googleMap)
        setMapToInitialState()
    }

    fun setMapToInitialState() {
        if (isMapReady) {
            if (viewmodel.paramsSubject.value?.suggestion?.coordinates != null &&
                    viewmodel.paramsSubject.value?.suggestion?.coordinates?.lat != 0.0 &&
                    viewmodel.paramsSubject.value?.suggestion?.coordinates?.lng != 0.0) {
                moveCameraToLatLng(LatLng(viewmodel.paramsSubject.value.suggestion.coordinates.lat,
                        viewmodel.paramsSubject.value.suggestion.coordinates.lng))
            } else if (viewmodel.paramsSubject.value?.suggestion?.regionNames?.shortName != null) {
                val BD_KEY = "geo_search"
                val bd = BackgroundDownloader.getInstance()
                bd.cancelDownload(BD_KEY)
                bd.startDownload(BD_KEY, mGeocodeDownload(viewmodel.paramsSubject.value.suggestion.regionNames.shortName), geoCallback())
            }
        }
    }

    private fun mGeocodeDownload(query: String): BackgroundDownloader.Download<List<Address>?> {
        return BackgroundDownloader.Download<kotlin.List<android.location.Address>?> {
            LocationServices.geocodeGoogle(context, query)
        }
    }

    private fun geoCallback(): BackgroundDownloader.OnDownloadComplete<List<Address>?> {
        return BackgroundDownloader.OnDownloadComplete<List<Address>?> { results ->
            if (results != null && results.isNotEmpty()) {
                if (results[0].latitude != 0.0 && results[0].longitude != 0.0) {
                    moveCameraToLatLng(LatLng(results[0].latitude, results[0].longitude))
                }
            }
        }
    }

    private fun moveCameraToLatLng(latLng: LatLng) {
        var cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(8f)
                .build();
        googleMap?.setPadding(0, 0, 0, 0)
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

}
