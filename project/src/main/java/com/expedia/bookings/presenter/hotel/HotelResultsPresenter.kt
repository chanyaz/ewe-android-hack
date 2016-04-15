package com.expedia.bookings.presenter.hotel

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FilterButtonWithCountWidget
import com.expedia.bookings.widget.MapLoadingOverlayWidget
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.hotel.HotelResultsViewModel
import com.expedia.vm.ShopWithPointsViewModel
import javax.inject.Inject

class HotelResultsPresenter(context: Context, attrs: AttributeSet) : BaseHotelResultsPresenter(context, attrs) {
    override val filterBtnWithCountWidget: FilterButtonWithCountWidget by bindView(R.id.sort_filter_button_container)
    override val searchThisArea: Button by bindView(R.id.search_this_area)
    override val loadingOverlay: MapLoadingOverlayWidget by bindView(R.id.map_loading_overlay)
    var filterBtn: LinearLayout? = null

    lateinit var shopWithPointsViewModel: ShopWithPointsViewModel
        @Inject set

    var viewmodel: HotelResultsViewModel by notNullAndObservable { vm ->
        vm.hotelResultsObservable.subscribe {
            filterBtnWithCountWidget.visibility = View.VISIBLE
            filterBtnWithCountWidget.translationY = 0f
        }
        mapViewModel.mapInitializedObservable.subscribe{
            setMapToInitialState(viewmodel.paramsSubject.value?.suggestion)
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
            setMapToInitialState(params.suggestion)
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
        Ui.getApplication(getContext()).hotelComponent().inject(this)

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
            animateMapCarouselVisibility(false)
            clearMarkers()
            hideSearchThisArea()
            doAreaSearch()
            trackMapSearchAreaClick()
        })

        inflateAndSetupToolbarMenu()
        filterView.shopWithPointsViewModel = shopWithPointsViewModel
        filterView.viewmodel.filterCountObservable.map { it.toString() }.subscribeText(filterCountText)
        filterView.viewmodel.filterCountObservable.map { it > 0 }.subscribeVisibility(filterCountText)
        filterView.viewmodel.filterCountObservable.map { it > 0 }.subscribeInverseVisibility(filterPlaceholderImageView)

        filterBtn?.setOnClickListener { view ->
            showWithTracking(ResultsFilter())
            filterView.viewmodel.sortContainerObservable.onNext(false)
            filterView.toolbar.title = resources.getString(R.string.filter)
        }

        filterBtnWithCountWidget?.setOnClickListener {
            showWithTracking(ResultsFilter())
            filterView.viewmodel.sortContainerObservable.onNext(true)
            filterView.toolbar.title = resources.getString(R.string.Sort_and_Filter)
        }
    }

    private fun inflateAndSetupToolbarMenu() {
        val toolbarFilterItemActionView = LayoutInflater.from(context).inflate(R.layout.toolbar_filter_item, null) as LinearLayout
        filterCountText = toolbarFilterItemActionView.findViewById(R.id.filter_count_text) as TextView
        filterPlaceholderImageView = toolbarFilterItemActionView.findViewById(R.id.filter_placeholder_icon) as ImageView
        filterPlaceholderImageView.setImageDrawable(filterPlaceholderIcon)
        filterBtn = toolbarFilterItemActionView.findViewById(R.id.filter_btn) as LinearLayout
        filterMenuItem.actionView = toolbarFilterItemActionView
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_hotel_results, this)
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
        if (searchThisArea.visibility == View.VISIBLE) {
            val anim: Animator = ObjectAnimator.ofFloat(searchThisArea, "alpha", 1f, 0f)
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationCancel(animation: Animator?) {
                    //Do nothing
                }

                override fun onAnimationEnd(animator: Animator?) {
                    searchThisArea.visibility = View.GONE
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
        if (currentState?.equals(ResultsMap::class.java.name) ?: false && searchThisArea.visibility == View.GONE) {
            searchThisArea.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(searchThisArea, "alpha", 0f, 1f).setDuration(DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION).start()
        }
    }

    override fun trackSearchMap() {
        HotelV2Tracking().trackHotelV2SearchMap()
    }

    override fun trackMapToList() {
        HotelV2Tracking().trackHotelV2MapToList()
    }

    override fun trackCarouselScroll() {
        HotelV2Tracking().trackHotelV2CarouselScroll()
    }

    override fun trackMapPinTap() {
        HotelV2Tracking().trackHotelV2MapTapPin()
    }

    override fun trackFilterShown() {
        HotelV2Tracking().trackHotelV2Filter()
    }

    override fun trackMapSearchAreaClick() {
        HotelV2Tracking().trackHotelsV2SearchAreaClick()
    }

    override fun isMapClusteringEnabled(): Boolean {
        return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHSRMapClusteringTest)
    }

    override fun isUserBucketedSearchScreenTest(): Boolean {
        return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelsSearchScreenTest)
    }
}
