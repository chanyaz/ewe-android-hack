package com.expedia.bookings.presenter.hotel

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.tracking.HotelTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.bookings.widget.FilterButtonWithCountWidget
import com.expedia.bookings.widget.MapLoadingOverlayWidget
import com.expedia.bookings.widget.hotel.HotelListAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.AbstractHotelFilterViewModel
import com.expedia.vm.HotelFilterViewModel
import com.expedia.vm.ShopWithPointsViewModel
import com.expedia.vm.hotel.HotelResultsViewModel
import javax.inject.Inject

class HotelResultsPresenter(context: Context, attrs: AttributeSet) : BaseHotelResultsPresenter(context, attrs) {

    override val filterBtnWithCountWidget: FilterButtonWithCountWidget by bindView(R.id.sort_filter_button_container)
    override val searchThisArea: Button by bindView(R.id.search_this_area)
    override val loadingOverlay: MapLoadingOverlayWidget by bindView(R.id.map_loading_overlay)
    val searchMenu: MenuItem by lazy {
        val searchMenu = toolbar.menu.findItem(R.id.menu_open_search)
        searchMenu
    }

    lateinit var shopWithPointsViewModel: ShopWithPointsViewModel
        @Inject set

    init {
        showSearchMenu.subscribe { searchMenu.isVisible = it }
    }

    var viewmodel: HotelResultsViewModel by notNullAndObservable { vm ->
        mapViewModel.mapInitializedObservable.subscribe{
            setMapToInitialState(viewmodel.paramsSubject.value?.suggestion)
        }
        vm.hotelResultsObservable.subscribe(listResultsObserver)
        vm.addHotelResultsObservable.subscribe(addListResultsObserver)

        vm.hotelResultsObservable.subscribe(mapViewModel.hotelResultsSubject)
        vm.addHotelResultsObservable.subscribe(mapViewModel.hotelResultsSubject)
        vm.hotelResultsObservable.subscribe {
            if (shouldShowHotelFilterProminenceTest && isFilterInNavBar()) {
                filterBtnWithCountWidget.visibility = View.INVISIBLE
                filterMenuItem.isVisible = true
            }
            else {
                filterBtnWithCountWidget.visibility = View.VISIBLE
            }

            filterBtnWithCountWidget.translationY = 0f
        }
        vm.mapResultsObservable.subscribe(listResultsObserver)
        vm.mapResultsObservable.subscribe(mapViewModel.mapResultsSubject)
        vm.mapResultsObservable.subscribe {
            val latLng = googleMap?.projection?.visibleRegion?.latLngBounds?.center
            mapViewModel.mapBoundsSubject.onNext(latLng)
            fab.isEnabled = true
        }

        vm.titleSubject.subscribe {
            toolbarTitle.text = it
        }

        vm.subtitleSubject.subscribe {
            toolbarSubtitle.text = it
        }

        vm.paramsSubject.subscribe { params ->
            setMapToInitialState(params.suggestion)
            showLoading()
            show(ResultsList())
            filterView.sortByObserver.onNext(params.isCurrentLocationSearch() && !params.suggestion.isGoogleSuggestionSearch)
            filterView.viewmodel.clearObservable.onNext(Unit)
        }

        vm.locationParamsSubject.subscribe { params ->
            loadingOverlay.animate(true)
            loadingOverlay.visibility = View.VISIBLE
            filterView.sortByObserver.onNext(params.isCurrentLocationSearch && !params.isGoogleSuggestionSearch)
            filterView.viewmodel.clearObservable.onNext(Unit)
        }

        vm.paramsSubject.map { it.isCurrentLocationSearch() }.subscribe(filterView.viewmodel.isCurrentLocationSearch)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        Ui.getApplication(context).hotelComponent().inject(this)
        searchMenu.setOnMenuItemClickListener({
            searchOverlaySubject.onNext(Unit)
            true
        })
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

        searchMenu.isVisible = true
        filterView.shopWithPointsViewModel = shopWithPointsViewModel

        filterBtnWithCountWidget.setOnClickListener {
            showWithTracking(ResultsFilter())
            filterView.viewmodel.sortContainerObservable.onNext(true)
            filterView.toolbar.title = resources.getString(R.string.sort_and_filter)
        }
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
            anim.duration = DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION
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
        HotelTracking().trackHotelSearchMap()
    }

    override fun trackMapToList() {
        HotelTracking().trackHotelMapToList()
    }

    override fun trackCarouselScroll() {
        HotelTracking().trackHotelCarouselScroll()
    }

    override fun trackMapPinTap() {
        HotelTracking().trackHotelMapTapPin()
    }

    override fun trackFilterShown() {
        HotelTracking().trackHotelFilter()
    }

    override fun trackMapSearchAreaClick() {
        HotelTracking().trackHotelsSearchAreaClick()
    }

    override fun getHotelListAdapter(): BaseHotelListAdapter {
        return HotelListAdapter(hotelSelectedSubject, headerClickedSubject)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.HOTELS
    }

    override fun createFilterViewModel(): AbstractHotelFilterViewModel {
        return HotelFilterViewModel(context)
    }
}
