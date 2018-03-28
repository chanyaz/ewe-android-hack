package com.expedia.bookings.presenter.hotel

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Rect
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.extensions.subscribeContentDescription
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.animation.AnimationRunner
import com.expedia.bookings.hotel.animation.transition.VerticalTranslateTransition
import com.expedia.bookings.hotel.fragment.ChangeDatesDialogFragment
import com.expedia.bookings.hotel.vm.HotelResultsViewModel
import com.expedia.bookings.hotel.widget.HotelSearchFloatingActionPill
import com.expedia.bookings.model.HotelStayDates
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.urgency.UrgencyServices
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseHotelFilterView
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.bookings.widget.FilterButtonWithCountWidget
import com.expedia.bookings.hotel.widget.adapter.HotelMapCarouselAdapter
import com.expedia.bookings.widget.HotelResultsChangeDateView
import com.expedia.bookings.widget.HotelServerFilterView
import com.expedia.bookings.widget.MapLoadingOverlayWidget
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.hotel.widget.adapter.HotelListAdapter
import com.expedia.bookings.utils.FontCache
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.ShopWithPointsViewModel
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import com.expedia.vm.hotel.HotelFilterViewModel
import com.expedia.vm.hotel.UrgencyViewModel
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class HotelResultsPresenter(context: Context, attrs: AttributeSet) : BaseHotelResultsPresenter(context, attrs) {
    override val floatingPill: HotelSearchFloatingActionPill by bindView(R.id.hotel_results_floating_pill)

    override fun getRecyclerYTranslation(): Float {
        return 0f
    }

    private val hotelResultChangeDateView: HotelResultsChangeDateView by bindView(R.id.hotel_result_change_date_container)
    private val toolbarShadow: View by bindView(R.id.toolbar_dropshadow)
    val filterBtnWithCountWidget: FilterButtonWithCountWidget by bindView(R.id.sort_filter_button_container)
    private val narrowResultsPromptView: TextView by bindView(R.id.narrow_result_prompt)
    override val searchThisArea: TextView by bindView(if (shouldUsePill()) R.id.search_this_area_pill else R.id.search_this_area)
    override val loadingOverlay: MapLoadingOverlayWidget by bindView(R.id.map_loading_overlay)
    private var narrowFilterPromptSubscription: Disposable? = null
    private var swpEnabled = false

    val filterCountObserver: Observer<Int> = endlessObserver { numberOfFilters ->
        filterBtnWithCountWidget.showNumberOfFilters(numberOfFilters)
        floatingPill.setFilterCount(numberOfFilters)
    }

    lateinit var shopWithPointsViewModel: ShopWithPointsViewModel
        @Inject set

    lateinit var urgencyServices: UrgencyServices
        @Inject set

    lateinit var urgencyViewModel: UrgencyViewModel

    init {
        filterViewModel.filterByParamsObservable.subscribe { params ->
            viewModel.filterParamsSubject.onNext(params)
        }

        floatingPill.filterButton.setOnClickListener {
            show(ResultsFilter())
            trackFilterShown()
            filterViewModel.sortContainerVisibilityObservable.onNext(currentState == ResultsList::class.java.name)
            filterView.toolbar.title = resources.getString(R.string.sort_and_filter)
        }

        floatingPill.toggleViewButton.setOnClickListener {
            if (floatingPill.showMap) {
                showWithTracking(ResultsMap())
            } else {
                show(ResultsList(), Presenter.FLAG_CLEAR_BACKSTACK)
                trackMapToList()
            }
        }

        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(adapterListener)

        hotelResultChangeDateView.calendarClickedSubject.subscribe {
            if (!adapter.isLoading() && loadingOverlay.visibility != View.VISIBLE) {
                showChangeDatesDialog()
            }
        }

        if (isBucketedToShowChangeDate()) {
            hotelResultChangeDateView.visibility = View.VISIBLE
            toolbarShadow.visibility = View.GONE
        }
    }

    var viewModel: HotelResultsViewModel by notNullAndObservable { vm ->
        baseViewModel = vm
        vm.hotelResultsObservable.subscribe(listResultsObserver)

        if (shouldFetchUrgency()) {
            vm.hotelResultsObservable.subscribe { response ->
                vm.getSearchParams()?.let { params ->
                    adapter.clearUrgency()
                    urgencyViewModel.fetchCompressionScore(response.searchRegionId, params.checkIn, params.checkOut)
                }
            }
        }

        initSortFilterCallToAction()

        vm.hotelResultsObservable.subscribe {
            if (shouldUsePill()) {
                floatingPill.visibility = View.VISIBLE
            }
            if (previousWasList && filterBtnWithCountWidget.translationY != 0f) {
                showSortAndFilter()
            } else {
                fab.isEnabled = true
            }
        }

        vm.filterResultsObservable.subscribe(listResultsObserver)
        vm.filterResultsObservable.subscribe {
            if (shouldUsePill()) {
                floatingPill.visibility = View.VISIBLE
            }
            if (previousWasList && filterBtnWithCountWidget.translationY != 0f) {
                showSortAndFilter()
            } else {
                fab.isEnabled = true
            }
        }

        vm.titleSubject.subscribe { titleString ->
            toolbarTitle.text = titleString
        }

        vm.subtitleSubject.subscribe { subtitleString ->
            toolbarSubtitle.text = subtitleString
        }
        vm.subtitleContDescSubject.subscribeContentDescription(toolbarSubtitle)

        vm.paramsSubject.subscribe { newParams(it) }
        vm.searchInProgressSubject.subscribe { resetForNewSearch() }
        vm.hotelResultsObservable.subscribe {
            if (previousWasList) {
                show(ResultsList())
            } else {
                show(ResultsMap())
            }
        }

        vm.locationParamsSubject.subscribe { params ->
            filterView.sortByObserver.onNext(params.isCurrentLocationSearch && !params.isGoogleSuggestionSearch)
            filterViewModel.clearObservable.onNext(Unit)
            viewModel.clearCachedParamsFilterOptions()
        }

        vm.filterParamsSubject.subscribe {
            if (previousWasList) {
                show(ResultsList(), Presenter.FLAG_CLEAR_TOP)
                resetListOffset()
            } else {
                show(ResultsMap(), Presenter.FLAG_CLEAR_TOP)
                getFloatingButton().isEnabled = false
                animateMapCarouselOut()
            }
        }
        vm.paramsSubject.map { it.isCurrentLocationSearch() }.subscribe(filterViewModel.isCurrentLocationSearch)

        vm.errorObservable.subscribe { hideMapLoadingOverlay() }

        vm.changeDateStringSubject.subscribe(hotelResultChangeDateView.changeDateStringSubject)
        vm.guestStringSubject.subscribe(hotelResultChangeDateView.guestStringSubject)
    }

    private fun showSortAndFilter() {
        val anim = ValueAnimator.ofFloat(0f, 1f).setDuration(500)
        anim.interpolator = DecelerateInterpolator(2f)
        anim.addUpdateListener({ animation ->
            sortFilterButtonTransition?.toOrigin(animation.animatedValue as Float)
        })
        anim.start()
    }

    private fun initSortFilterCallToAction() {
        if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelNewFilterCtaText)) {
            narrowResultsPromptView.setText(R.string.new_filters_available)
        } else {
            narrowResultsPromptView.setText(R.string.narrow_your_results)
        }

        viewModel.hotelResultsObservable.subscribe {
            narrowResultsPromptView.visibility = View.GONE
            narrowFilterPromptSubscription = adapter.filterPromptSubject.subscribe {
                val animationRunner = AnimationRunner(narrowResultsPromptView, context)
                if (!shouldUsePill()) {
                    narrowResultsPromptView.visibility = View.VISIBLE
                }
                animationRunner.animIn(R.anim.filter_prompt_in)
                        .animOut(R.anim.filter_prompt_out)
                        .afterAction({ narrowResultsPromptView.visibility = View.GONE })
                        .duration(500L).outDelay(3000L)
                        .run()
                HotelTracking.trackHotelNarrowPrompt()
                narrowFilterPromptSubscription?.dispose()
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        Ui.getApplication(context).hotelComponent().inject(this)
        urgencyViewModel = UrgencyViewModel(context, urgencyServices)
        if (shouldFetchUrgency()) {
            urgencyViewModel.urgencyTextSubject.subscribe { text -> adapter.addUrgency(text) }
        }
        ViewCompat.setElevation(loadingOverlay, context.resources.getDimension(R.dimen.launch_tile_margin_side))
        val iconColor = ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color))
        FontCache.setTypeface(searchThisArea, FontCache.Font.ROBOTO_MEDIUM)
        //Fetch, color, and slightly resize the searchThisArea location pin drawable
        if (shouldUsePill()) {
            searchThisArea.compoundDrawables[0]?.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
        } else {
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_material_location_pin).mutate()
            icon.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
            icon.bounds = Rect(icon.bounds.left, icon.bounds.top, (icon.bounds.right * 1.1).toInt(), (icon.bounds.bottom * 1.1).toInt())
            searchThisArea.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        }

        //We don't want to show the searchThisArea button unless the map has just moved.
        searchThisArea.visibility = View.GONE
        searchThisArea.setOnClickListener {
            getFloatingButton().isEnabled = false
            animateMapCarouselOut()
            hideSearchThisArea()
            doAreaSearch()
            trackMapSearchAreaClick()
        }

        filterView.shopWithPointsViewModel = shopWithPointsViewModel

        if (shouldUsePill()) {
            filterBtnWithCountWidget.visibility = View.GONE
        } else {
            sortFilterButtonTransition = VerticalTranslateTransition(filterBtnWithCountWidget, 0, filterHeight.toInt())
            sortFilterButtonTransition?.reachedTargetSubject?.subscribe {
                narrowResultsPromptView.clearAnimation()
                narrowResultsPromptView.visibility = View.GONE
            }
        }

        filterBtnWithCountWidget.subscribeOnClick(filterButtonOnClickObservable)
        filterViewModel.filterCountObservable.subscribe(filterCountObserver)

        mapWidget.cameraChangeSubject.subscribe {
            if (currentState?.equals(ResultsMap::class.java.name) == true
                    && searchThisArea.visibility == View.GONE) {
                searchThisArea.visibility = View.VISIBLE
                ObjectAnimator.ofFloat(searchThisArea, "alpha", 0f, 1f).setDuration(DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION).start()
            }
        }
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_hotel_results, this)
    }

    override fun inflateFilterView(viewStub: ViewStub): BaseHotelFilterView {
        viewStub.layoutResource = R.layout.hotel_server_filter_view_stub
        return viewStub.inflate() as HotelServerFilterView
    }

    override fun back(): Boolean {
        if (navIcon.parameter.toInt() == ArrowXDrawableUtil.ArrowDrawableType.BACK.type) {
            viewModel.unsubscribeSearchResponse()
        }
        return super.back()
    }

    override fun showLoading() {
        super.showLoading()
        resetListOffset()
        sortFilterButtonTransition?.jumpToTarget()
        floatingPill.visibility = View.GONE
        narrowResultsPromptView.visibility = View.GONE
        narrowResultsPromptView.clearAnimation()
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

    override fun trackSearchMap() {
        HotelTracking.trackHotelSearchMap(swpEnabled)
    }

    override fun trackMapToList() {
        HotelTracking.trackHotelMapToList(swpEnabled)
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

    private fun trackChangeDateClick(isMap: Boolean) {
        HotelTracking.trackChangeDateClick(isMap)
    }

    override fun getHotelListAdapter(): BaseHotelListAdapter {
        val canShow2xMessaging = AbacusFeatureConfigManager.Companion.isBucketedForTest(context, AbacusUtils.HotelEarn2xMessaging)
                && Ui.getApplication(context).appComponent().userStateManager().isUserAuthenticated()
        return HotelListAdapter(hotelSelectedSubject, headerClickedSubject, pricingHeaderSelectedSubject, canShow2xMessaging)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.HOTELS
    }

    override fun createFilterViewModel(): BaseHotelFilterViewModel {
        return HotelFilterViewModel(context)
    }

    override fun showChangeDateBanner() {
        if (isBucketedToShowChangeDate()) {
            hotelResultChangeDateView.animateIn()
        }
    }

    override fun getScrollListener(): BaseHotelResultsScrollListener {
        return HotelResultsScrollListener()
    }

    fun handleSoldOutHotel(hotelId: String) {
        // When createTrip/CKO give sold out update everywhere, this is stupid. I hate the people that built this into our product.
        (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).hotelSoldOut.onNext(hotelId)
        adapter.hotelSoldOut.onNext(hotelId)
        mapWidget.markSoldOutHotel(hotelId)
    }

    fun showFilterCachedResults() {
        filterViewModel.clearObservable.onNext(Unit)
        viewModel.clearCachedParamsFilterOptions()
        val cachedFilterResponse = filterViewModel.originalResponse ?: adapter.resultsSubject.value
        viewModel.hotelResultsObservable.onNext(cachedFilterResponse)
    }

    fun showCachedResults() {
        viewModel.cachedResponse?.let {
            viewModel.hotelResultsObservable.onNext(it)
        }
    }

    private fun newParams(params: HotelSearchParams) {
        filterViewModel.resetPriceSliderFilterTracking()
        (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).shopWithPoints = params.shopWithPoints
        if (currentState == ResultsList::class.java.name) {
            moveMapToDestination(params.suggestion)
        }
        filterView.sortByObserver.onNext(params.isCurrentLocationSearch() && !params.suggestion.isGoogleSuggestionSearch)

        filterViewModel.clearObservable.onNext(Unit)
        if (params.suggestion.gaiaId != null) {
            filterViewModel.setSearchLocationId(params.suggestion.gaiaId)
        }
        filterViewModel.sortSpinnerObservable.onNext(DisplaySort.fromServerSort(params.getSortOrder()))
        params.filterOptions?.let { filterOptions ->
            filterViewModel.updatePresetOptions(filterOptions)
        }

        swpEnabled = params.shopWithPoints
    }

    private fun resetForNewSearch() {
        if (previousWasList) {
            showLoading()
        } else {
            showMapLoadingOverlay()
        }
        mapWidget.clearMarkers()
    }

    private fun showMapLoadingOverlay() {
        loadingOverlay.animate(true)
        loadingOverlay.visibility = View.VISIBLE
    }

    private fun doAreaSearch() {
        mapWidget.getCameraCenter()?.let { center ->
            val location = SuggestionV4()
            location.isSearchThisArea = true
            val region = SuggestionV4.RegionNames()
            region.displayName = context.getString(R.string.visible_map_area)
            region.shortName = context.getString(R.string.visible_map_area)
            location.regionNames = region
            val coordinate = SuggestionV4.LatLng()
            coordinate.lat = center.latitude
            coordinate.lng = center.longitude
            location.coordinates = coordinate
            viewModel.locationParamsSubject.onNext(location)
        }
    }

    private fun showChangeDatesDialog() {
        val params = viewModel.getSearchParams()
        if (params == null) {
            return
        }

        trackChangeDateClick(currentState == BaseHotelResultsPresenter.ResultsMap::class.java.name)

        val dialogFragment = ChangeDatesDialogFragment()
        dialogFragment.datesChangedSubject.subscribe { stayDates ->
            viewModel.dateChagedParamsSubject.onNext(stayDates)
        }
        val fragmentManager = (context as FragmentActivity).supportFragmentManager

        dialogFragment.presetDates(HotelStayDates(params.checkIn, params.checkOut))
        dialogFragment.show(fragmentManager, Constants.TAG_CALENDAR_DIALOG)
    }

    private fun shouldFetchUrgency(): Boolean {
        return AbacusFeatureConfigManager.isBucketedInAnyVariant(context, AbacusUtils.HotelUrgencyV2)
    }

    private inner class HotelResultsScrollListener : BaseHotelResultsScrollListener() {
        private var changeDateSensitivity = context.resources.getDimensionPixelSize(R.dimen.hotel_results_change_date_sensitivity)

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (isBucketedToShowChangeDate()) {
                if (dy < -changeDateSensitivity) {
                    hotelResultChangeDateView.animateIn()
                } else if (dy > changeDateSensitivity && !isHeaderVisible()) {
                    hotelResultChangeDateView.animateOut()
                }
            }
        }
    }
}
