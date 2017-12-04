package com.expedia.bookings.presenter.hotel

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.drawable.TransitionDrawable
import android.location.Address
import android.location.Location
import android.support.annotation.CallSuper
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewStub
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.hotel.animation.transition.HorizontalTranslateTransition
import com.expedia.bookings.hotel.animation.transition.VerticalFadeTransition
import com.expedia.bookings.hotel.animation.transition.VerticalTranslateTransition
import com.expedia.bookings.hotel.map.CleanHotelMapView
import com.expedia.bookings.hotel.map.HotelMapClusterRenderer
import com.expedia.bookings.hotel.map.HotelMarkerIconGenerator
import com.expedia.bookings.hotel.map.HotelMapMarker
import com.expedia.bookings.hotel.vm.BaseHotelResultsViewModel
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseHotelFilterView
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.bookings.widget.HotelListRecyclerView
import com.expedia.bookings.widget.MapLoadingOverlayWidget
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.hotel.HotelResultsSortFaqWebView
import com.expedia.util.endlessObserver
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import com.google.maps.android.clustering.ClusterManager
import com.mobiata.android.BackgroundDownloader
import com.mobiata.android.LocationServices
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import rx.Observer
import rx.subjects.PublishSubject
import kotlin.properties.Delegates
import kotlin.properties.Delegates.notNull

abstract class BaseHotelResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    //Views
    var filterButtonText: TextView by Delegates.notNull()
    val recyclerView: HotelListRecyclerView by bindView(R.id.list_view)
    open val loadingOverlay: MapLoadingOverlayWidget? = null

    val toolbar: Toolbar by bindView(R.id.hotel_results_toolbar)
    val toolbarTitle: android.widget.TextView by bindView(R.id.title)
    val toolbarSubtitle: android.widget.TextView by bindView(R.id.subtitle)
    val recyclerTempBackground: View by bindView(R.id.recycler_view_temp_background)
    val cleanMapView: CleanHotelMapView by bindView(R.id.clean_hotel_map_view)

    val fab: FloatingActionButton by bindView(R.id.fab)

    open val filterMenuItem by lazy { toolbar.menu.findItem(R.id.menu_filter) }

    var filterCountText: TextView by Delegates.notNull()
    var filterPlaceholderImageView: ImageView by Delegates.notNull()

    val filterView: BaseHotelFilterView by lazy {
        inflateFilterView(filterViewStub)
    }
    private val filterViewStub: ViewStub by bindView(R.id.hotel_filter_view_stub)

    private val sortFaqWebView: HotelResultsSortFaqWebView by lazy {
        val webView = findViewById<HotelResultsSortFaqWebView>(R.id.sort_faq_web_view)
        webView.setExitButtonOnClickListener(View.OnClickListener { this.back() })
        webView
    }

    var adapter: BaseHotelListAdapter by Delegates.notNull()
    open val searchThisArea: Button? = null
    var isMapReady = false

    var clusterManager: ClusterManager<HotelMapMarker>? = null

    val DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION = 200L

    open val filterHeight by lazy { resources.getDimension(R.dimen.hotel_filter_height) }

    var screenHeight: Int = 0
    var screenWidth: Float = 0f

    var filterScreenShown = false
    var transitionRunning = false
    var hideFabAnimationRunning = false

    var previousWasList = true

    var navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)

    val hotelSelectedSubject = PublishSubject.create<Hotel>()
    val headerClickedSubject = PublishSubject.create<Unit>()
    val pricingHeaderSelectedSubject = PublishSubject.create<Unit>()
    val hideBundlePriceOverviewSubject = PublishSubject.create<Boolean>()

    var filterBtn: LinearLayout? = null

    var hotelMapClusterRenderer: HotelMapClusterRenderer by Delegates.notNull()

    var mapListSplitAnchor = 0
    var snapToFullScreenMapThreshold = 0

    val hotelIconFactory = HotelMarkerIconGenerator(context)

    var mapItems = arrayListOf<HotelMapMarker>()
    var hotels = emptyList<Hotel>()

    private val ANIMATION_DURATION_FILTER = 500

    protected var sortFilterButtonTransition: VerticalTranslateTransition? = null
    private var toolbarTitleTransition: VerticalTranslateTransition? = null
    private var subTitleTransition: VerticalFadeTransition? = null
    private var mapCarouselTransition: HorizontalTranslateTransition? = null

    var baseViewModel: BaseHotelResultsViewModel by notNull()

    fun resetListOffset() {
        val mover = ObjectAnimator.ofFloat(cleanMapView, "translationY",
                cleanMapView.translationY, -mapListSplitAnchor.toFloat())
        mover.duration = 300
        mover.start()

        val view = recyclerView.findViewHolderForAdapterPosition(1)
        if (view != null) {
            val distance = view.itemView.top - mapListSplitAnchor
            recyclerView.smoothScrollBy(0, distance)
        } else {
            recyclerView.layoutManager.scrollToPositionWithOffset(1, mapListSplitAnchor)
        }
    }

    private fun animateFab(newTranslationY: Float) {
        fab.animate().translationY(newTranslationY).setInterpolator(DecelerateInterpolator()).start()
    }

    private fun getRequiredMapPadding() : Int {
        val firstVisibleView = recyclerView.getChildAt(1)
        if (firstVisibleView != null) {
            val topOffset = firstVisibleView.top
            val bottom = recyclerView.height - topOffset
            return (bottom + cleanMapView.translationY).toInt()
        }
        return 0
    }
    private fun adjustGoogleMapLogo() {
        // TODO fixme
//        val view = recyclerView.getChildAt(1)
//        if (view != null) {
//            googleMap?.setPadding(0, toolbar.height, 0, (bottom + mapView.translationY).toInt())
//        }
    }

    private fun fabShouldBeHiddenOnList(): Boolean {
        return recyclerView.layoutManager.findFirstVisibleItemPosition() == 0
    }

    private fun shouldBlockTransition(): Boolean {
        return (transitionRunning || (recyclerView.adapter as BaseHotelListAdapter).isLoading())
    }

    val listResultsObserver = endlessObserver<HotelSearchResponse> { response ->
        hideMapLoadingOverlay()
        adapter.resultsSubject.onNext(response)
        showFilterMenuItem(currentState == ResultsList::class.java.name)
        // show fab button always in case of AB test or shitty device
        if (ExpediaBookingApp.isDeviceShitty()) {
            fab.visibility = View.VISIBLE
            getFabAnimIn().start()
        }
        if (ExpediaBookingApp.isDeviceShitty() && response.hotelList.size <= 3) {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context, R.color.hotel_result_background))
        } else {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }
        if (!response.isFilteredResponse) {
            filterView.viewModel.setHotelList(response)
        }

        cleanMapView.newResults(response, updateBounds = true)
    }

    fun lastBestLocationSafe(): Location {
        val minTime = DateTime.now().millis - DateUtils.HOUR_IN_MILLIS
        val loc = LocationServices.getLastBestLocation(context, minTime)
        val location = Location("lastBestLocationSafe")
        location.latitude = loc?.latitude ?: 0.0
        location.longitude = loc?.longitude ?: 0.0
        return location
    }

    val mapSelectedObserver: Observer<Unit> = endlessObserver {
        if (!shouldBlockTransition()) {
            showWithTracking(ResultsMap())
        }
    }

    val filterObserver: Observer<HotelSearchResponse> = endlessObserver { response ->
        if (previousWasList) {
            show(ResultsList(), Presenter.FLAG_CLEAR_TOP)
            resetListOffset()
        } else {
            show(ResultsMap(), Presenter.FLAG_CLEAR_TOP)
        }
        adapter.resultsSubject.onNext(response)

        if (ExpediaBookingApp.isDeviceShitty() && response.hotelList.size <= 3 && previousWasList) {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context, R.color.hotel_result_background))
        } else {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }
    }

    override fun back(): Boolean {
        if (ResultsFilter().javaClass.name == currentState) {
            if (filterView.viewModel.isFilteredToZeroResults()) {
                filterView.shakeForError()
                return true
            } else {
                filterView.viewModel.doneObservable.onNext(Unit)
                return true
            }
        } else if (ResultsList().javaClass.name == currentState) {
//            clearMarkers(false) todo fix me
        } else if (ResultsMap().javaClass.name == currentState) {
            trackMapToList()
        }

        return super.back()
    }

    init {
        inflate()

        fab.contentDescription = if (recyclerView.visibility == View.VISIBLE) context.getString(R.string.show_list) else context.getString(R.string.show_map)
        headerClickedSubject.subscribe(mapSelectedObserver)
        adapter = getHotelListAdapter()

        recyclerView.adapter = adapter
        filterView.viewModel = createFilterViewModel()
        filterView.viewModel.filterObservable.subscribe(filterObserver)

        filterView.viewModel.showPreviousResultsObservable.subscribe {
            if (previousWasList) {
                show(ResultsList(), Presenter.FLAG_CLEAR_TOP)
                resetListOffset()
            } else {
                show(ResultsMap(), Presenter.FLAG_CLEAR_TOP)
//                animateMapCarouselOut() todo again do we need this?
            }
        }

        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_cont_desc)

        pricingHeaderSelectedSubject.subscribe {
            sortFaqWebView.loadUrl()
            show(ResultsSortFaqWebView())
        }

        cleanMapView.carouselShownSubject.subscribe { carouselHeight ->
            if (currentState == ResultsMap::class.java.name) {
                val offset = context.resources.getDimension(R.dimen.hotel_carousel_fab_vertical_offset)
                fab.animate().translationY(-(carouselHeight - offset))
                        .setInterpolator(DecelerateInterpolator())
                        .start()
            }
        }
    }

    protected fun hideMapLoadingOverlay() {
        if (loadingOverlay != null && loadingOverlay?.visibility == View.VISIBLE) {
            loadingOverlay?.animate(false)
            loadingOverlay?.visibility = View.GONE
        }
    }

    override fun onFinishInflate() {
        // add the view of same height as of status bar
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
            val lp = recyclerView.layoutParams as FrameLayout.LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
        }

        addDefaultTransition(defaultTransition)
        addTransition(mapToResultsTransition)
        addTransition(listFilterTransition)
        addTransition(mapFilterTransition)
        addTransition(sortFaqWebViewTransition)

        recyclerView.addOnScrollListener(scrollListener)
        recyclerView.addOnItemTouchListener(touchListener)
        toolbar.inflateMenu(R.menu.menu_filter_item)

        filterMenuItem.isVisible = false

        toolbar.setNavigationOnClickListener { view ->
            if (!transitionRunning) {
                val activity = context as AppCompatActivity
                activity.onBackPressed()
            }
        }


        val fabDrawable: TransitionDrawable? = (fab.drawable as? TransitionDrawable)
        // Enabling crossfade prevents the icon ending up with a weird mishmash of both icons.
        fabDrawable?.isCrossFadeEnabled = true

        fab.setOnClickListener { view ->
            if (!transitionRunning) {
                if (recyclerView.visibility == View.VISIBLE) {
                    fab.contentDescription = context.getString(R.string.show_list)
                    showWithTracking(ResultsMap())
                } else {
                    fab.contentDescription = context.getString(R.string.show_map)
                    show(ResultsList(), Presenter.FLAG_CLEAR_BACKSTACK)
                    trackMapToList()
                }
            }
        }

        inflateAndSetupToolbarMenu()

        filterView.viewModel.filterCountObservable.subscribe { filterCount ->
            updateFilterCount(filterCount)
        }
    }

    private fun inflateAndSetupToolbarMenu() {
        val toolbarFilterItemActionView = LayoutInflater.from(context).inflate(R.layout.toolbar_filter_item, null) as LinearLayout
        filterCountText = toolbarFilterItemActionView.findViewById<TextView>(R.id.filter_count_text)
        filterPlaceholderImageView = toolbarFilterItemActionView.findViewById<ImageView>(R.id.filter_placeholder_icon)
        filterPlaceholderImageView.setColorFilter(ContextCompat.getColor(context, R.color.toolbar_icon))
        filterBtn = toolbarFilterItemActionView.findViewById<LinearLayout>(R.id.filter_btn)
        filterMenuItem.actionView = toolbarFilterItemActionView
        toolbarFilterItemActionView.setOnLongClickListener {
            val size = Point()
            display.getSize(size)
            val width = size.x
            val toast = Toast.makeText(context, context.getString(R.string.sort_and_filter), Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, width - toolbarFilterItemActionView.width, toolbarFilterItemActionView.height)
            toast.show()
            true
        }
        filterBtn?.setOnClickListener { view ->
            showWithTracking(ResultsFilter())
            val isResults = currentState == ResultsList::class.java.name
            previousWasList = isResults
            filterView.viewModel.sortContainerVisibilityObservable.onNext(isResults)
            filterView.toolbar.title = if (isResults) resources.getString(R.string.sort_and_filter) else resources.getString(R.string.filter)
        }
        filterButtonText = filterMenuItem.actionView.findViewById<TextView>(R.id.filter_text)
        filterButtonText.visibility = GONE
    }

    private fun updateFilterCount(filterCount: Int) {
        filterCountText.text = filterCount.toString()
        val hasFilters = filterCount > 0
        filterCountText.visibility = if (hasFilters) View.VISIBLE else View.GONE
        filterPlaceholderImageView.visibility = if (hasFilters) View.GONE else View.VISIBLE

        val contDescBuilder = StringBuilder()
        if (hasFilters) {
            val announcementString = Phrase.from(context.resources.getQuantityString(R.plurals.number_results_announcement_text_TEMPLATE, filterCount))
                    .put("number", filterCount).format().toString()
            contDescBuilder.append(announcementString).append(". ").append(resources.getString(R.string.sort_and_filter))
            AccessibilityUtil.appendRoleContDesc(filterBtn as View, contDescBuilder.toString(), R.string.accessibility_cont_desc_role_button)
        }
    }

    fun showDefault() {
        show(ResultsList())
    }

    @CallSuper
    open fun showLoading() {
        adapter.showLoading()
    }

    val scrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            val firstItem = recyclerView.findViewHolderForAdapterPosition(1)

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val manager = recyclerView.layoutManager as LinearLayoutManager
                val displayingLastItemInList = manager.findLastCompletelyVisibleItemPosition() == (recyclerView.adapter.itemCount - 1)
                if (isHeaderVisible() && !displayingLastItemInList) {
                    if (firstItem == null) {
                        showWithTracking(ResultsMap())
                    } else {
                        val firstItemTop = firstItem.itemView?.top ?: 0
                        val manager = recyclerView.layoutManager as HotelListRecyclerView.PreCachingLayoutManager
                        val displayingLastItemInList = manager.findLastCompletelyVisibleItemPosition() == (recyclerView.adapter.itemCount - 1)

                        if (!displayingLastItemInList && firstItemTop > mapListSplitAnchor && firstItemTop < snapToFullScreenMapThreshold) {
                            recyclerView.translationY = 0f
                            resetListOffset()
                        } else if (firstItemTop >= snapToFullScreenMapThreshold) {
                            showWithTracking(ResultsMap())
                        }
                    }
                }

                if (!transitionRunning && !filterScreenShown) {
                    if (fab.visibility != View.VISIBLE && !fabShouldBeHiddenOnList()) {
                        fab.visibility = View.VISIBLE
                        fab.translationY = -filterHeight
                        getFabAnimIn().start()
                    } else if (fab.visibility == View.VISIBLE && !hideFabAnimationRunning && fabShouldBeHiddenOnList()) {
                        hideFabAnimationRunning = true
                        val outAnim = getFabAnimOut()
                        outAnim.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animator: Animator) {
                                fab.visibility = View.INVISIBLE
                                hideFabAnimationRunning = false
                            }
                        })
                        outAnim.start()
                    }
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (shouldBlockTransition() || currentState?.equals(ResultsMap::class.java.name) ?: false) {
                return
            }

            if (isHeaderVisible()) {
                val y = cleanMapView.translationY + (-dy * mapListSplitAnchor / (recyclerView.height - mapListSplitAnchor))
                cleanMapView.translationY = y

                adjustGoogleMapLogo()
            }
        }

        fun isHeaderVisible(): Boolean {
            return recyclerView.layoutManager.findFirstVisibleItemPosition() == 0
        }
    }

    private val defaultTransition = object : Presenter.DefaultTransition(ResultsList::class.java.name) {

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            navIcon.parameter = if (forward) Math.abs(1 - f) else f
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            navIcon.parameter = ArrowXDrawableUtil.ArrowDrawableType.BACK.type.toFloat()
            recyclerView.translationY = 0f
            cleanMapView.translationY = -mapListSplitAnchor.toFloat()
            cleanMapView.toSplitView(getRequiredMapPadding())

            recyclerView.visibility = View.VISIBLE
            fab.visibility = View.INVISIBLE
            filterView.visibility = View.GONE

            sortFaqWebView.visibility = View.GONE
            postDelayed({ AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar) }, 50L)
            updateViewModelState(ResultsList::class.java.name)
        }
    }

    val adapterListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            if (ExpediaBookingApp.isAutomation()) {
                screenHeight = 0
                screenWidth = 0f
                // todo be nice if the tests could reflect actual behavior of fab aka need scroll b4 visible
                fab.visibility = View.VISIBLE
                val fabLp = fab.layoutParams as FrameLayout.LayoutParams
                fabLp.bottomMargin += filterHeight.toInt()
            } else {
                screenHeight = height
                screenWidth = width.toFloat()
            }

            mapListSplitAnchor = (height / 4.1).toInt()
            snapToFullScreenMapThreshold = (height / 2.2).toInt()

            resetListOffset()
        }
    }

    private val mapToResultsTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsList::class.java, LinearInterpolator(), 750) {
        var firstStepTransitionTime = .33f

        var firstTransition: Presenter.Transition? = null
        var secondTransition: Presenter.Transition? = null

        var secondTransitionStarted = false

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            transitionRunning = true
            secondTransitionStarted = false

            setupToolbarMeasurements()

            if (forward) {
                firstStepTransitionTime = .33f
                secondTransition = listTransition
                hideSearchThisArea()
                previousWasList = true
            } else {
                firstStepTransitionTime = .66f
                firstTransition = listTransition
                previousWasList = false
            }
            firstTransition?.startTransition(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            if (f < firstStepTransitionTime) {
                val acceleratedTransitionTime = f / firstStepTransitionTime
                firstTransition?.updateTransition(firstTransition?.interpolator?.getInterpolation(acceleratedTransitionTime)!!, forward)
            } else {
                if (!secondTransitionStarted) {
                    firstTransition?.endTransition(forward)
                    secondTransition?.startTransition(forward)
                    secondTransitionStarted = true
                }
                val acceleratedTransitionTime = (f - firstStepTransitionTime) / (1 - firstStepTransitionTime)
                secondTransition?.updateTransition(secondTransition!!.interpolator.getInterpolation(acceleratedTransitionTime), forward)
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            secondTransition?.endTransition(forward)
            transitionRunning = false
            if (forward) {
                updateViewModelState(ResultsList::class.java.name)
            } else {
                updateViewModelState(ResultsMap::class.java.name)
                AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar)
            }
        }
    }

    private val listTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsList::class.java, DecelerateInterpolator(2f), 750 * 2 / 3) {

        var fabShouldVisiblyMove: Boolean = true
        var mapTranslationStart: Float = 0f
        var initialListTranslation: Int = 0
        var startingFabTranslation: Float = 0f
        var finalFabTranslation: Float = 0f

        override fun startTransition(forwardToList: Boolean) {
            super.startTransition(forwardToList)
            transitionRunning = true

            recyclerView.visibility = View.VISIBLE
            fabShouldVisiblyMove = if (forwardToList) !fabShouldBeHiddenOnList() else (fab.visibility == View.VISIBLE)
            initialListTranslation = if (recyclerView.layoutManager.findFirstVisibleItemPosition() == 0) recyclerView.getChildAt(1)?.top ?: 0 else 0
            if (forwardToList) {
                previousWasList = true
                //If the fab is visible we want to do the transition - but if we're just hiding it, don't confuse the
                // user with an unnecessary icon swap
                if (fabShouldVisiblyMove) {
                    (fab.drawable as? TransitionDrawable)?.reverseTransition(duration)
                } else {
                    resetListOffset()

                    //Let's start hiding the fab
                    getFabAnimOut().start()
                }
            } else {
                previousWasList = false
                mapTranslationStart = cleanMapView.translationY
                if (fabShouldVisiblyMove) {
                    (fab.drawable as? TransitionDrawable)?.startTransition(duration)
                } else {
                    (fab.drawable as? TransitionDrawable)?.startTransition(0)
                    fab.visibility = View.VISIBLE
                    getFabAnimIn().start()
                }
            }
            startingFabTranslation = fab.translationY
            if (forwardToList) {
                finalFabTranslation = -filterHeight
            }
            hideBundlePriceOverview(!forwardToList)
            updateFilterButtonTextVisibility(forwardToList)
            showFilterMenuItem(forwardToList)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val hotelListDistance = if (forward) (screenHeight * (1 - f)) else ((screenHeight - initialListTranslation) * f)
            recyclerView.translationY = hotelListDistance
            navIcon.parameter = if (forward) Math.abs(1 - f) else f
            if (forward) {
                cleanMapView.translationY = f * -mapListSplitAnchor
            } else {
                cleanMapView.translationY = (1 - f) * mapTranslationStart
            }

            if (fabShouldVisiblyMove) {
                fab.translationY = startingFabTranslation - f * (startingFabTranslation - finalFabTranslation)
            }
            if (forward) {
                toolbarTitleTransition?.toOrigin(f)
                subTitleTransition?.fadeIn(f)
                sortFilterButtonTransition?.toOrigin(f)
            } else {
                toolbarTitleTransition?.toTarget(f)
                subTitleTransition?.fadeOut(f)
                sortFilterButtonTransition?.toTarget(f)
            }
            adjustGoogleMapLogo()
        }

        override fun endTransition(forward: Boolean) {
            transitionRunning = false
            navIcon.parameter = (if (forward) ArrowXDrawableUtil.ArrowDrawableType.BACK else ArrowXDrawableUtil.ArrowDrawableType.CLOSE).type.toFloat()
            recyclerView.visibility = if (forward) View.VISIBLE else View.INVISIBLE

            if (forward) {
                if (!fabShouldVisiblyMove) {
                    fab.translationY = 0f
                    (fab.drawable as? TransitionDrawable)?.reverseTransition(0)
                    fab.visibility = View.INVISIBLE
                }
                recyclerView.translationY = 0f
                cleanMapView.translationY = -mapListSplitAnchor.toFloat()
                sortFilterButtonTransition?.jumpToOrigin()

                cleanMapView.toSplitView(getRequiredMapPadding())
                adjustGoogleMapLogo()
                updateViewModelState(ResultsList::class.java.name)
            } else {
                cleanMapView.translationY = 0f
                recyclerView.translationY = screenHeight.toFloat()
                sortFilterButtonTransition?.jumpToTarget()
                cleanMapView.toFullScreen()

                updateViewModelState(ResultsMap::class.java.name)
            }
            fab.translationY = finalFabTranslation
        }
    }

    private val listFilterTransition = object : Presenter.Transition(ResultsList::class.java, ResultsFilter::class.java, DecelerateInterpolator(2f), ANIMATION_DURATION_FILTER) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            previousWasList = true
            transitionRunning = true
            filterView.show()
            filterScreenShown = forward
            if (forward) {
                fab.visibility = View.GONE
            } else {
                recyclerView.visibility = View.VISIBLE
                toolbar.visibility = View.VISIBLE
                cleanMapView.visibility = View.VISIBLE
            }
            hideBundlePriceOverview(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            val translatePercentage = if (forward) 1 - f else f
            filterView.translationY = filterView.height * translatePercentage
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            transitionRunning = false
            if (!forward) filterView.visibility = View.GONE
            filterView.translationY = (if (forward) 0 else filterView.height).toFloat()
            if (!forward && !fabShouldBeHiddenOnList()) {
                fab.visibility = View.VISIBLE
                getFabAnimIn().start()
            }
            if (forward) {
                filterView.toolbar.requestFocus()
                recyclerView.visibility = View.GONE
                toolbar.visibility = View.GONE
                cleanMapView.visibility = View.GONE
                updateViewModelState(ResultsFilter::class.java.name)
            } else {
                updateViewModelState(ResultsList::class.java.name)
            }
        }
    }

    private val mapFilterTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsFilter::class.java, DecelerateInterpolator(2f), ANIMATION_DURATION_FILTER) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            previousWasList = false
            transitionRunning = true
            filterView.show()
            if (forward) {
                fab.visibility = View.GONE
                searchThisArea?.visibility = View.GONE
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            val translatePercentage = if (forward) 1 - f else f
            filterView.translationY = filterView.height * translatePercentage
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            transitionRunning = false
            filterView.translationY = (if (forward) 0 else filterView.height).toFloat()
            if (forward) {
                filterView.toolbar.requestFocus()
                updateViewModelState(ResultsFilter::class.java.name)
            } else {
                filterView.visibility = View.GONE
                fab.visibility = View.VISIBLE
                searchThisArea?.visibility = View.VISIBLE
                updateViewModelState(ResultsMap::class.java.name)
            }
        }
    }

    private val sortFaqWebViewTransition = object : ScaleTransition(this, recyclerView, sortFaqWebView, ResultsList::class.java, ResultsSortFaqWebView::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            previousWasList = true
            transitionRunning = true
            if (forward) {
                fab.visibility = View.GONE
            } else {
                toolbar.visibility = View.VISIBLE
                cleanMapView.visibility = View.VISIBLE
            }
            hideBundlePriceOverview(forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            transitionRunning = false
            if (forward) {
                toolbar.visibility = View.GONE
                cleanMapView.visibility = View.GONE
                updateViewModelState(ResultsSortFaqWebView::class.java.name)
            } else {
                if (!fabShouldBeHiddenOnList()) {
                    fab.visibility = View.VISIBLE
                    getFabAnimIn().start()
                }
                updateViewModelState(ResultsList::class.java.name)
            }
        }
    }

    val touchListener = object : RecyclerView.OnItemTouchListener {
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

        }

        override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent?): Boolean {
            if (transitionRunning) {
                return true
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView?, e: MotionEvent?) {

        }
    }

    var yTranslationRecyclerTempBackground = 0f
    var yTranslationRecyclerView = 0f

    fun setupToolbarMeasurements() {
        if (yTranslationRecyclerTempBackground == 0f && recyclerView.getChildAt(1) != null) {
            yTranslationRecyclerTempBackground = (recyclerView.getChildAt(0).height + recyclerView.getChildAt(0).top + toolbar.height).toFloat()
            yTranslationRecyclerView = (recyclerView.getChildAt(0).height + recyclerView.getChildAt(0).top).toFloat()
            recyclerTempBackground.translationY = yTranslationRecyclerTempBackground

            toolbarTitleTransition = VerticalTranslateTransition(toolbarTitle, toolbarTitle.top,
                    (toolbarSubtitle.bottom - toolbarSubtitle.top) / 2)

            subTitleTransition = VerticalFadeTransition(toolbarSubtitle, 0, (toolbarSubtitle.bottom - toolbarSubtitle.top))
        }
    }

    fun animationStart() {
        recyclerTempBackground.visibility = View.VISIBLE
    }

    fun animationUpdate(f: Float, forward: Boolean) {
        setupToolbarMeasurements()
        val factor = if (forward) f else Math.abs(1 - f)
        navIcon.parameter = factor
    }

    fun animationFinalize(enableLocation: Boolean) {
        recyclerTempBackground.visibility = View.GONE
        navIcon.parameter = ArrowXDrawableUtil.ArrowDrawableType.BACK.type.toFloat()
    }

    //We use ObjectAnimators instead of Animation because Animation mucks with settings values outside of it, and Object
    // Animator lets us do that.
    fun getFabAnimIn(): Animator {
        val set = AnimatorSet()
        set.playTogether(
                ObjectAnimator.ofFloat(fab, "scaleX", 0f, 1f),
                ObjectAnimator.ofFloat(fab, "scaleY", 0f, 1f)
        )
        set.duration = DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION
        set.interpolator = DecelerateInterpolator()
        return set
    }

    fun getFabAnimOut(): Animator {
        val set = AnimatorSet()
        set.playTogether(
                ObjectAnimator.ofFloat(fab, "scaleX", 1f, 0f),
                ObjectAnimator.ofFloat(fab, "scaleY", 1f, 0f)
        )
        set.interpolator = AccelerateInterpolator()
        set.duration = DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION
        return set
    }

    fun updateFilterButtonTextVisibility(isResults: Boolean) {
        if (isResults) {
            filterButtonText.visibility = GONE
        } else {
            filterButtonText.visibility = VISIBLE
        }
    }

    open fun hideBundlePriceOverview(hide: Boolean) {
        //
    }

    fun showFilterMenuItem(isResults: Boolean) {
        if (getLineOfBusiness() == LineOfBusiness.PACKAGES) {
            filterMenuItem.isVisible = true
        } else {
            filterMenuItem.isVisible = !isResults
        }
    }

    fun showWithTracking(newState: Any) {
        if (!transitionRunning) {
            when (newState) {
                is ResultsMap -> trackSearchMap()
                is ResultsFilter -> trackFilterShown()
            }
            show(newState)
        }
    }

    // Classes for state
    class ResultsList

    class ResultsMap
    class ResultsFilter
    class ResultsSortFaqWebView

    fun moveMapToDestination(suggestion: SuggestionV4?) {
        if (suggestion != null) {
            if (suggestion.coordinates != null &&
                    suggestion.coordinates?.lat != 0.0 &&
                    suggestion.coordinates?.lng != 0.0) {
                cleanMapView.moveCamera(suggestion.coordinates.lat, suggestion.coordinates.lng)
            } else if (suggestion.regionNames?.fullName != null) {
                val BD_KEY = "geo_search"
                val bd = BackgroundDownloader.getInstance()
                bd.cancelDownload(BD_KEY)
                bd.startDownload(BD_KEY, mGeocodeDownload(suggestion.regionNames.fullName), geoCallback())
            }
        }
    }

    private fun mGeocodeDownload(query: String): BackgroundDownloader.Download<List<Address>?> {
        return BackgroundDownloader.Download<List<android.location.Address>?> {
            LocationServices.geocodeGoogle(context, query)
        }
    }

    private fun geoCallback(): BackgroundDownloader.OnDownloadComplete<List<Address>?> {
        return BackgroundDownloader.OnDownloadComplete<List<Address>?> { results ->
            if (results != null && results.isNotEmpty()) {
                if (results[0].latitude != 0.0 && results[0].longitude != 0.0) {
                    cleanMapView.moveCamera(results[0].latitude, results[0].longitude)
                }
            }
        }
    }

    private fun updateViewModelState(newState: String) {
        baseViewModel.resultStateParamsSubject.onNext(newState)
    }

    abstract fun inflate()
    abstract fun inflateFilterView(viewStub: ViewStub): BaseHotelFilterView
    abstract fun doAreaSearch()
    abstract fun hideSearchThisArea()
    abstract fun showSearchThisArea()
    abstract fun createFilterViewModel(): BaseHotelFilterViewModel
    abstract fun trackSearchMap()
    abstract fun trackMapToList()
    abstract fun trackMapPinTap()
    abstract fun trackFilterShown()
    abstract fun trackMapSearchAreaClick()
    abstract fun getLineOfBusiness(): LineOfBusiness
    abstract fun getHotelListAdapter(): BaseHotelListAdapter
}
