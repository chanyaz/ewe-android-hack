package com.expedia.bookings.presenter.hotel

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.view.ViewStub
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.hotel.animation.ScaleInRunnable
import com.expedia.bookings.hotel.animation.ScaleOutRunnable
import com.expedia.bookings.hotel.animation.VerticalTranslateTransition
import com.expedia.bookings.hotel.vm.HotelResultsViewModel
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.urgency.UrgencyServices
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseHotelFilterView
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.bookings.widget.FilterButtonWithCountWidget
import com.expedia.bookings.widget.HotelMapCarouselAdapter
import com.expedia.bookings.widget.HotelServerFilterView
import com.expedia.bookings.widget.MapLoadingOverlayWidget
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.hotel.HotelListAdapter
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelClientFilterViewModel
import com.expedia.vm.ShopWithPointsViewModel
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import com.expedia.vm.hotel.HotelServerFilterViewModel
import com.expedia.vm.hotel.UrgencyViewModel
import rx.Observer
import java.lang.ref.WeakReference
import javax.inject.Inject

class HotelResultsPresenter(context: Context, attrs: AttributeSet) : BaseHotelResultsPresenter(context, attrs) {
    val urgencyDropDownContainer: LinearLayout by bindView(R.id.hotel_urgency_container)
    val urgencyPercentBookedView: TextView by bindView(R.id.urgency_percentage_view)
    val urgencyDescriptionView: TextView by bindView(R.id.urgency_destination_description_view)
    val toolbarShadow: View by bindView(R.id.toolbar_dropshadow)
    val filterBtnWithCountWidget: FilterButtonWithCountWidget by bindView(R.id.sort_filter_button_container)
    override val searchThisArea: Button by bindView(R.id.search_this_area)
    override val loadingOverlay: MapLoadingOverlayWidget by bindView(R.id.map_loading_overlay)

    val searchMenu: MenuItem by lazy {
        val searchMenu = toolbar.menu.findItem(R.id.menu_open_search)
        searchMenu
    }

    val filterCountObserver: Observer<Int> = endlessObserver { numberOfFilters ->
        filterBtnWithCountWidget.showNumberOfFilters(numberOfFilters)
    }

    lateinit var shopWithPointsViewModel: ShopWithPointsViewModel
        @Inject set

    lateinit var urgencyServices: UrgencyServices
        @Inject set

    lateinit var urgencyViewModel: UrgencyViewModel

    init {
        showSearchMenu.subscribe { searchMenu.isVisible = it }

        if (!filterView.viewModel.isClientSideFiltering()) {
            filterView.viewModel.filterByParamsObservable.subscribe { params ->
                viewModel.filterParamsSubject.onNext(params)
            }
        }
    }

    var viewModel: HotelResultsViewModel by notNullAndObservable { vm ->
        mapViewModel.mapInitializedObservable.subscribe {
            setMapToInitialState(viewModel.getSearchParams()?.suggestion)
        }
        vm.hotelResultsObservable.subscribe(listResultsObserver)

        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelUrgencyMessage)) {
            vm.hotelResultsObservable.subscribe { response ->
                vm.getSearchParams()?.let { params ->
                    urgencyViewModel.fetchCompressionScore(response.searchRegionId, params.checkIn, params.checkOut)
                }
            }
        }

        vm.hotelResultsObservable.subscribe(mapViewModel.hotelResultsSubject)
        vm.hotelResultsObservable.subscribe {
            filterBtnWithCountWidget.visibility = View.VISIBLE
            filterBtnWithCountWidget.translationY = 0f
        }
        vm.mapResultsObservable.subscribe(listResultsObserver)
        vm.mapResultsObservable.subscribe(mapViewModel.mapResultsSubject)
        vm.mapResultsObservable.subscribe {
            val latLng = googleMap?.projection?.visibleRegion?.latLngBounds?.center
            mapViewModel.mapBoundsSubject.onNext(latLng)
            fab.isEnabled = true
        }

        vm.filterResultsObservable.subscribe(listResultsObserver)
        vm.filterResultsObservable.subscribe(mapViewModel.hotelResultsSubject)
        vm.filterResultsObservable.subscribe {
            if (previousWasList) {
                filterBtnWithCountWidget.visibility = View.VISIBLE
                filterBtnWithCountWidget.translationY = 0f
            } else {
                fab.isEnabled = true
            }
        }

        vm.titleSubject.subscribe {
            toolbarTitle.text = it
        }

        vm.subtitleSubject.subscribe {
            toolbarSubtitle.text = it
        }

        vm.paramsSubject.subscribe { params -> resetForNewSearch(params) }

        vm.sortByDeepLinkSubject.subscribe { sortType ->
            filterView.viewModel.sortByObservable.onNext(sortType)
        }

        vm.locationParamsSubject.subscribe { params ->
            showMapLoadingOverlay()
            filterView.sortByObserver.onNext(params.isCurrentLocationSearch && !params.isGoogleSuggestionSearch)
            filterView.viewModel.clearObservable.onNext(Unit)
        }

        vm.filterParamsSubject.subscribe {
            if (previousWasList) {
                showLoading()
                show(ResultsList(), Presenter.FLAG_CLEAR_TOP)
                resetListOffset()
            } else {
                show(ResultsMap(), Presenter.FLAG_CLEAR_TOP)
                fab.isEnabled = false
                animateMapCarouselOut()
                clearMarkers()
                showMapLoadingOverlay()
            }
        }
        vm.paramsSubject.map { it.isCurrentLocationSearch() }.subscribe(filterView.viewModel.isCurrentLocationSearch)

        vm.errorObservable.subscribe { hideMapLoadingOverlay() }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        Ui.getApplication(context).hotelComponent().inject(this)
        urgencyViewModel = UrgencyViewModel(context, urgencyServices)
        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelUrgencyMessage)) {
            urgencyViewModel.percentSoldOutTextSubject.zipWith(urgencyViewModel.urgencyDescriptionSubject,
                    { soldOutPercentText, urgencyDescription ->
                        urgencyPercentBookedView.text = soldOutPercentText
                        urgencyDescriptionView.text = urgencyDescription
                    }).subscribe {
                UrgencyAnimation(urgencyDropDownContainer, toolbarShadow).animate()
            }
        }

        searchMenu.setOnMenuItemClickListener({
            if (!transitionRunning) {
                searchOverlaySubject.onNext(Unit)
                true
            } else {
                false
            }
        })
        ViewCompat.setElevation(loadingOverlay, context.resources.getDimension(R.dimen.launch_tile_margin_side))
        //Fetch, color, and slightly resize the searchThisArea location pin drawable
        val icon = ContextCompat.getDrawable(context, R.drawable.ic_material_location_pin).mutate()
        icon.setColorFilter(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)), PorterDuff.Mode.SRC_IN)
        icon.bounds = Rect(icon.bounds.left, icon.bounds.top, (icon.bounds.right * 1.1).toInt(), (icon.bounds.bottom * 1.1).toInt())
        searchThisArea.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)

        //We don't want to show the searchThisArea button unless the map has just moved.
        searchThisArea.visibility = View.GONE
        searchThisArea.setOnClickListener({ view ->
            fab.isEnabled = false
            animateMapCarouselOut()
            clearMarkers()
            hideSearchThisArea()
            doAreaSearch()
            trackMapSearchAreaClick()
        })

        searchMenu.isVisible = true
        filterView.shopWithPointsViewModel = shopWithPointsViewModel

        sortFilterButtonTransition = VerticalTranslateTransition(filterBtnWithCountWidget, 0, filterHeight.toInt())
        filterBtnWithCountWidget.setOnClickListener {
            previousWasList = currentState == ResultsList::class.java.name
            showWithTracking(ResultsFilter())
            filterView.viewModel.sortContainerVisibilityObservable.onNext(true)
            filterView.toolbar.title = resources.getString(R.string.sort_and_filter)
        }
        filterView.viewModel.filterCountObservable.subscribe(filterCountObserver)
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_hotel_results, this)
    }

    override fun inflateFilterView(viewStub: ViewStub): BaseHotelFilterView {
        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelServerSideFilter)) {
            viewStub.layoutResource = R.layout.hotel_server_filter_view_stub;
            return viewStub.inflate() as HotelServerFilterView
        }

        return inflateClientFilterView(viewStub)
    }

    override fun back(): Boolean {
        if (navIcon.parameter.toInt() == ArrowXDrawableUtil.ArrowDrawableType.BACK.type) {
            viewModel.unsubscribeSearchResponse()
        }
        return super.back()
    }

    override fun showLoading() {
        super.showLoading()
        filterBtnWithCountWidget?.visibility = View.GONE
        urgencyDropDownContainer.visibility = View.GONE
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
        viewModel.locationParamsSubject.onNext(location)
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
        HotelTracking.trackHotelSearchMap()
    }

    override fun trackMapToList() {
        HotelTracking.trackHotelMapToList()
    }

    override fun trackCarouselScroll() {
        HotelTracking.trackHotelCarouselScroll()
    }

    override fun trackMapPinTap() {
        HotelTracking.trackHotelMapTapPin()
    }

    override fun trackFilterShown() {
        HotelTracking.trackHotelFilter()
    }

    override fun trackMapSearchAreaClick() {
        HotelTracking.trackHotelsSearchAreaClick()
    }

    override fun getHotelListAdapter(): BaseHotelListAdapter {
        return HotelListAdapter(hotelSelectedSubject, headerClickedSubject, pricingHeaderSelectedSubject)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.HOTELS
    }

    override fun createFilterViewModel(): BaseHotelFilterViewModel {
        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelServerSideFilter)) {
            return HotelServerFilterViewModel(context)
        }
        return HotelClientFilterViewModel(context)
    }

    fun showCachedResults() {
        filterView.viewModel.clearObservable.onNext(Unit)
        val cachedResponse = filterView.viewModel.originalResponse ?: adapter.resultsSubject.value
        if (previousWasList) {
            viewModel.hotelResultsObservable.onNext(cachedResponse)
        } else {
            viewModel.mapResultsObservable.onNext(cachedResponse)
        }
    }

    private fun resetForNewSearch(params: HotelSearchParams) {
        (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).shopWithPoints = params.shopWithPoints

        setMapToInitialState(params.suggestion)
        showLoading()
        show(ResultsList())

        filterView.sortByObserver.onNext(params.isCurrentLocationSearch() && !params.suggestion.isGoogleSuggestionSearch)
        filterView.viewModel.clearObservable.onNext(Unit)
        if (params.suggestion.gaiaId != null) {
            filterView.viewModel.setSearchLocationId(params.suggestion.gaiaId)
        }
    }

    private class UrgencyAnimation(urgencyContainer: LinearLayout, toolbarShadow: View) {
        private val duration = 500L
        private val shadowViewRef: WeakReference<View>
        private val scaleInRunnable: ScaleInRunnable
        private val scaleOutRunnable: ScaleOutRunnable

        init {
            shadowViewRef = WeakReference(toolbarShadow)
            scaleInRunnable = ScaleInRunnable(urgencyContainer, duration, 0L)
            scaleOutRunnable = ScaleOutRunnable(urgencyContainer, duration, 5000L)

            scaleInRunnable.endSubject.subscribe {
                shadowViewRef.get()?.visibility = VISIBLE
                scaleOutRunnable.run()
            }

            scaleOutRunnable.startSubject.subscribe {
                shadowViewRef.get()?.visibility = GONE
            }
            scaleOutRunnable.endSubject.subscribe {
               shadowViewRef.get()?.visibility = VISIBLE
            }
        }

        fun animate() {
            shadowViewRef.get()?.visibility = GONE
            Handler(Looper.getMainLooper()).post(scaleInRunnable)
        }
    }

    private fun showMapLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay?.animate(true)
            loadingOverlay?.visibility = View.VISIBLE
        }
    }
}
