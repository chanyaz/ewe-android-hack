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
import android.support.annotation.CallSuper
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.bitmaps.PicassoScrollListener
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.extensions.setFocusForView
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.animation.transition.HorizontalTranslateTransition
import com.expedia.bookings.hotel.animation.transition.VerticalFadeTransition
import com.expedia.bookings.hotel.animation.transition.VerticalTranslateTransition
import com.expedia.bookings.hotel.map.HotelResultsMapWidget
import com.expedia.bookings.hotel.vm.BaseHotelResultsViewModel
import com.expedia.bookings.hotel.widget.HotelSearchFloatingActionPill
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBreadcrumbsMoveBundleOverviewPackagesEnabled
import com.expedia.bookings.widget.BaseHotelFilterView
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.bookings.widget.HotelCarouselRecycler
import com.expedia.bookings.widget.HotelListRecyclerView
import com.expedia.bookings.hotel.widget.adapter.HotelMapCarouselAdapter
import com.expedia.bookings.widget.MapLoadingOverlayWidget
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.hotel.HotelResultsSortFaqWebView
import com.expedia.util.endlessObserver
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import com.mobiata.android.BackgroundDownloader
import com.mobiata.android.LocationServices
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import kotlin.properties.Delegates
import kotlin.properties.Delegates.notNull

abstract class BaseHotelResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    //Views
    var filterButtonText: TextView by Delegates.notNull()
    val recyclerView: HotelListRecyclerView by bindView(R.id.list_view)
    val mapWidget: HotelResultsMapWidget by bindView(R.id.results_map_container)
    open val loadingOverlay: MapLoadingOverlayWidget? = null

    val toolbar: Toolbar by bindView(R.id.hotel_results_toolbar)
    val toolbarTitle: android.widget.TextView by bindView(R.id.title)
    val toolbarSubtitle: android.widget.TextView by bindView(R.id.subtitle)
    val recyclerTempBackground: View by bindView(R.id.recycler_view_temp_background)
    val mapCarouselContainer: ViewGroup by bindView(R.id.hotel_carousel_container)
    val mapCarouselRecycler: HotelCarouselRecycler by bindView(R.id.hotel_carousel)
    val fab: FloatingActionButton by bindView(R.id.fab)
    val filterButtonOnClickObservable = PublishSubject.create<Unit>()
    open val floatingPill: HotelSearchFloatingActionPill? = null

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
    open val searchThisArea: TextView? = null

    private val PICASSO_TAG = "HOTEL_RESULTS_LIST"
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

    var mapListSplitAnchor = 0
    var snapToFullScreenMapThreshold = 0

    private val ANIMATION_DURATION_FILTER = 500

    protected var sortFilterButtonTransition: VerticalTranslateTransition? = null
    private var toolbarTitleTransition: VerticalTranslateTransition? = null
    private var subTitleTransition: VerticalFadeTransition? = null
    private var mapCarouselTransition: HorizontalTranslateTransition? = null

    var baseViewModel: BaseHotelResultsViewModel by notNull()

    protected val filterViewModel by lazy<BaseHotelFilterViewModel> { createFilterViewModel() }

    protected fun animateMapCarouselIn() {
        if (mapCarouselContainer.visibility != View.VISIBLE) {
            val carouselAnimation = mapCarouselContainer.animate().translationX(0f)
                    .setInterpolator(DecelerateInterpolator()).setStartDelay(400)
                    .withEndAction {
                        mapWidget.adjustPadding(mapCarouselContainer.height)
                        mapCarouselRecycler.setFocusForView()
                    }
            mapCarouselContainer.translationX = screenWidth

            if (mapCarouselContainer.height == 0) {
                var onLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null //need to know carousel height before fab can properly animate.
                onLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
                    getFloatingButton().animate().translationY(-fabHeightOffset()).setInterpolator(DecelerateInterpolator()).withEndAction {
                        carouselAnimation.start()
                    }.start()
                    mapCarouselContainer.viewTreeObserver.removeOnGlobalLayoutListener(onLayoutListener)
                }
                mapCarouselContainer.viewTreeObserver.addOnGlobalLayoutListener(onLayoutListener)
            } else {
                getFloatingButton().animate().translationY(-fabHeightOffset()).setInterpolator(DecelerateInterpolator())
                        .withEndAction { carouselAnimation.start() }
                        .start()
            }

            mapCarouselContainer.visibility = View.VISIBLE
        } else {
            mapCarouselRecycler.setFocusForView()
        }
    }

    protected fun animateMapCarouselOut() {
        if (mapCarouselContainer.visibility != View.INVISIBLE) {
            val carouselAnimation = mapCarouselContainer.animate().translationX(screenWidth).setInterpolator(DecelerateInterpolator())
            carouselAnimation.withEndAction {
                mapCarouselContainer.visibility = View.INVISIBLE
                animateFab(0f)
                mapWidget.adjustPadding(bottomTranslation = 0)
            }
            carouselAnimation.start()
        }
    }

    protected fun resetListOffset() {
        val mover = ObjectAnimator.ofFloat(mapWidget, "translationY", mapWidget.translationY, -mapListSplitAnchor.toFloat())
        mover.duration = 300
        mover.start()

        val view = recyclerView.findViewHolderForAdapterPosition(1)
        if (view != null) {
            val distance = view.itemView.top - mapListSplitAnchor
            recyclerView.smoothScrollBy(0, distance)
        } else {
            recyclerView.layoutManager.scrollToPositionWithOffset(1, mapListSplitAnchor)
        }
        mapWidget.adjustPadding(getRequiredMapPadding())
    }

    protected fun isBucketedToShowChangeDate(): Boolean {
        return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelResultChangeDate)
    }

    private fun animateFab(newTranslationY: Float) {
        getFloatingButton().animate().translationY(newTranslationY).setInterpolator(DecelerateInterpolator()).start()
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
        if (ExpediaBookingApp.isDeviceShitty() && !shouldUsePill()) {
            fab.visibility = View.VISIBLE
            getFabAnimIn().start()
        }
        if (ExpediaBookingApp.isDeviceShitty() && response.hotelList.size <= 3) {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context, R.color.hotel_result_background))
        } else {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }
        if (!response.isFilteredResponse) {
            filterViewModel.setHotelList(response)
        }
        filterViewModel.availableAmenityOptionsObservable.onNext(response.amenityFilterOptions.keys)
        mapWidget.newResults(response, updateBounds = true)
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
            animateMapCarouselOut()
        }

        mapWidget.newResults(response, updateBounds = true)
        adapter.resultsSubject.onNext(response)
        if (ExpediaBookingApp.isDeviceShitty() && response.hotelList.size <= 3 && previousWasList) {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context, R.color.hotel_result_background))
        } else {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }
    }

    override fun back(): Boolean {
        if (ResultsFilter().javaClass.name == currentState) {
            if (filterViewModel.isFilteredToZeroResults()) {
                filterView.shakeForError()
                return true
            } else {
                filterViewModel.doneObservable.onNext(Unit)
                return true
            }
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
        filterView.setViewModel(filterViewModel)
        filterViewModel.filterObservable.subscribe(filterObserver)

        filterViewModel.showPreviousResultsObservable.subscribe {
            if (previousWasList) {
                show(ResultsList(), Presenter.FLAG_CLEAR_TOP)
                resetListOffset()
            } else {
                show(ResultsMap(), Presenter.FLAG_CLEAR_TOP)
                animateMapCarouselOut()
            }
        }

        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_cont_desc)

        mapCarouselRecycler.adapter = HotelMapCarouselAdapter(emptyList(), hotelSelectedSubject)
        mapCarouselRecycler.addOnScrollListener(PicassoScrollListener(context, PICASSO_TAG))

        pricingHeaderSelectedSubject.subscribe {
            sortFaqWebView.loadUrl()
            show(ResultsSortFaqWebView())
        }

        filterButtonOnClickObservable.subscribe {
            showWithTracking(ResultsFilter())
            filterViewModel.sortContainerVisibilityObservable.onNext(true)
            filterView.toolbar.title = resources.getString(R.string.sort_and_filter)
        }
    }

    protected fun hideMapLoadingOverlay() {
        if (loadingOverlay != null && loadingOverlay?.visibility == View.VISIBLE) {
            loadingOverlay?.animate(false)
            loadingOverlay?.visibility = View.GONE
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
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

        animateMapCarouselOut()
        val screen = Ui.getScreenSize(context)
        val lp = mapCarouselRecycler.layoutParams
        lp.width = screen.x

        recyclerView.addOnScrollListener(getScrollListener())
        recyclerView.addOnItemTouchListener(touchListener)

        mapCarouselRecycler.carouselSwipedSubject.subscribe { hotel ->
            mapWidget.selectNewHotel(hotel)
        }
        toolbar.inflateMenu(R.menu.menu_filter_item)

        filterMenuItem.isVisible = false

        toolbar.setNavigationOnClickListener {
            if (!transitionRunning) {
                val activity = context as AppCompatActivity
                activity.onBackPressed()
            }
        }

        val fabDrawable: TransitionDrawable? = (fab.drawable as? TransitionDrawable)
        // Enabling crossfade prevents the icon ending up with a weird mishmash of both icons.
        fabDrawable?.isCrossFadeEnabled = true

        fab.setOnClickListener {
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
        floatingPill?.visibility = View.GONE

        inflateAndSetupToolbarMenu()

        filterViewModel.filterCountObservable.subscribe { filterCount ->
            updateFilterCount(filterCount)
        }

        mapWidget.markerClickedSubject.subscribe {
            trackMapPinTap()
            animateMapCarouselIn()
        }
        mapWidget.hotelsForCarouselSubject.subscribe { hotels ->
            (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setItems(hotels)
            mapCarouselRecycler.scrollToPosition(0)
        }
        mapWidget.clusterClickedSubject.subscribe {
            animateMapCarouselOut()
        }

        toolbar.viewTreeObserver.addOnGlobalLayoutListener {
            setupToolbarTransition()
            if (isBucketedToShowChangeDate() && getLineOfBusiness() == LineOfBusiness.HOTELS) {
                toolbarTitleTransition?.jumpToTarget()
                subTitleTransition?.fadeOut(1.0f)
            }
        }

        searchThisArea?.let {
            AccessibilityUtil.appendRoleContDesc(it as View, it.text.toString(), R.string.accessibility_cont_desc_role_button)
        }
    }

    fun getFloatingButton(): View = if (shouldUsePill() && floatingPill != null) floatingPill!! else fab

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
        filterBtn?.setOnClickListener {
            showWithTracking(ResultsFilter())
            val isResults = currentState == ResultsList::class.java.name
            previousWasList = isResults
            filterViewModel.sortContainerVisibilityObservable.onNext(isResults)
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

    protected open inner class BaseHotelResultsScrollListener : RecyclerView.OnScrollListener() {

        @CallSuper
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            val firstItem = recyclerView.findViewHolderForAdapterPosition(1)

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                val isLastItemInListDisplayed = linearLayoutManager.findLastCompletelyVisibleItemPosition() == (recyclerView.adapter.itemCount - 1)
                if (isHeaderVisible() && !isLastItemInListDisplayed) {
                    if (firstItem == null) {
                        showWithTracking(ResultsMap())
                    } else {
                        val firstItemTop = firstItem.itemView?.top ?: 0
                        val preCachingLayoutManager = recyclerView.layoutManager as HotelListRecyclerView.PreCachingLayoutManager
                        val isDisplayingLastItemInList = preCachingLayoutManager.findLastCompletelyVisibleItemPosition() == (recyclerView.adapter.itemCount - 1)

                        if (!isDisplayingLastItemInList && firstItemTop > mapListSplitAnchor && firstItemTop < snapToFullScreenMapThreshold) {
                            recyclerView.translationY = 0f
                            resetListOffset()
                        } else if (firstItemTop >= snapToFullScreenMapThreshold) {
                            showWithTracking(ResultsMap())
                        }
                    }
                }

                if (shouldUsePill()) {
                    return
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

        @CallSuper
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (shouldBlockTransition() || currentState?.equals(ResultsMap::class.java.name) ?: false) {
                return
            }

            if (isHeaderVisible()) {
                val y = mapWidget.translationY + (-dy * mapListSplitAnchor / (recyclerView.height - mapListSplitAnchor))
                mapWidget.translationY = y
                mapWidget.adjustPadding(getRequiredMapPadding())
            }
        }

        fun isHeaderVisible(): Boolean {
            return recyclerView.layoutManager.findFirstVisibleItemPosition() == 0
        }
    }

    private val defaultTransition = object : Presenter.DefaultTransition(ResultsList::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            navIcon.parameter = ArrowXDrawableUtil.ArrowDrawableType.BACK.type.toFloat()
            recyclerView.translationY = 0f
            mapWidget.translationY = -mapListSplitAnchor.toFloat()

            recyclerView.visibility = View.VISIBLE
            resetListOffset()

            loadingOverlay?.visibility = View.GONE
            mapCarouselContainer.visibility = View.INVISIBLE
            searchThisArea?.visibility = View.GONE
            fab.visibility = View.INVISIBLE
            filterView.visibility = View.GONE
            sortFaqWebView.visibility = View.GONE
            postDelayed({ AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar) }, 50L)
            showFilterMenuItem(true)
        }
    }

    val adapterListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (height == 0) {
                return
            }
            recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            if (ExpediaBookingApp.isAutomation() && !shouldUsePill()) {
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

            if (forward) {
                firstStepTransitionTime = .33f
                firstTransition = carouselTransition
                secondTransition = listTransition
                hideSearchThisArea()
                previousWasList = true
            } else {
                if (mapWidget.hasSelectedMarker()) {
                    mapWidget.adjustPadding(mapCarouselContainer.height)
                } else {
                    mapWidget.adjustPadding(0)
                }
                firstStepTransitionTime = .66f
                firstTransition = listTransition
                secondTransition = carouselTransition
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
            toolbar.navigationContentDescription = if (forward) context.getString(R.string.toolbar_nav_icon_cont_desc) else context.getString(R.string.toolbar_nav_icon_close_cont_desc)
            if (!forward) {
                AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar)
            }
        }
    }

    private val carouselTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsList::class.java, DecelerateInterpolator(2f), 750 / 3) {
        override fun startTransition(forward: Boolean) {
            transitionRunning = true
            mapCarouselTransition = HorizontalTranslateTransition(mapCarouselContainer, 0, screenWidth.toInt())
            if (mapWidget.hasSelectedMarker()) {
                mapCarouselContainer.visibility = View.VISIBLE
            } else {
                mapCarouselContainer.visibility = View.INVISIBLE
            }
            if (forward) {
                mapCarouselTransition?.toOrigin()
            } else {
                mapCarouselTransition?.toTarget()
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            if (forward) {
                mapCarouselTransition?.toTarget(f)
            } else {
                mapCarouselTransition?.toOrigin(f)
            }
        }

        override fun endTransition(forward: Boolean) {
            transitionRunning = false
            if (forward) {
                mapCarouselTransition?.toTarget()
                mapCarouselContainer.visibility = View.INVISIBLE
            } else {
                mapCarouselTransition?.toOrigin()
            }
        }
    }

    private val listTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsList::class.java, DecelerateInterpolator(2f), 750 * 2 / 3) {

        var fabShouldVisiblyMove: Boolean = true
        var mapTranslationStart: Float = 0f
        var initialListTranslation: Int = 0
        var startingFabTranslation: Float = 0f
        var finalFabTranslation: Float = 0f

        val shouldAnimateTitleSubtitle = !isBucketedToShowChangeDate() || getLineOfBusiness() != LineOfBusiness.HOTELS

        override fun startTransition(forwardToList: Boolean) {
            super.startTransition(forwardToList)
            transitionRunning = true
            val isMapPinSelected = mapWidget.hasSelectedMarker()

            recyclerView.visibility = View.VISIBLE
            fabShouldVisiblyMove = if (forwardToList) !fabShouldBeHiddenOnList() else (getFloatingButton().visibility == View.VISIBLE)
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
                mapTranslationStart = mapWidget.translationY
                if (fabShouldVisiblyMove) {
                    (fab.drawable as? TransitionDrawable)?.startTransition(duration)
                } else {
                    //Since we're not moving it manually, let's jump it to where it belongs,
                    // and let's get it showing the right thing
                    if (isMapPinSelected) {
                        getFloatingButton().translationY = -fabHeightOffset()
                    } else {
                        getFloatingButton().translationY = 0f
                    }
                    (fab.drawable as? TransitionDrawable)?.startTransition(0)
                    if (!shouldUsePill()) {
                        fab.visibility = View.VISIBLE
                    }
                    getFabAnimIn().start()
                }
            }
            startingFabTranslation = getFloatingButton().translationY
            if (forwardToList) {
                if (shouldUsePill()) {
                    finalFabTranslation = 0f
                } else {
                    finalFabTranslation = -filterHeight
                }
            } else {
                finalFabTranslation = if (isMapPinSelected) -fabHeightOffset() else 0f
            }
            hideBundlePriceOverview(!forwardToList)
            updateFilterButtonTextVisibility(forwardToList)
            showFilterMenuItem(forwardToList)
            if (!forwardToList) {
                showChangeDateBanner()
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val hotelListDistance = if (forward) (screenHeight * (1 - f)) else ((screenHeight - initialListTranslation) * f)
            recyclerView.translationY = hotelListDistance
            navIcon.parameter = if (forward) Math.abs(1 - f) else f
            if (forward) {
                mapWidget.translationY = f * -mapListSplitAnchor
            } else {
                mapWidget.translationY = (1 - f) * mapTranslationStart
            }

            if (f > .5) {
                // Half-way through the transition, toggle the pill. We can't toggle it immediately because then it
                // changes before the rest of the UI. We can't change it on endTransition because that feels like lag.
                floatingPill?.setToggleState(forward)
            }

            if (fabShouldVisiblyMove) {
                fab.translationY = startingFabTranslation - f * (startingFabTranslation - finalFabTranslation)
            }
            floatingPill?.translationY = startingFabTranslation - f * (startingFabTranslation - finalFabTranslation)

            if (forward) {
                if (shouldAnimateTitleSubtitle) {
                    toolbarTitleTransition?.toOrigin(f)
                    subTitleTransition?.fadeIn(f)
                }
                sortFilterButtonTransition?.toOrigin(f)
            } else {
                if (shouldAnimateTitleSubtitle) {
                    toolbarTitleTransition?.toTarget(f)
                    subTitleTransition?.fadeOut(f)
                }
                sortFilterButtonTransition?.toTarget(f)
            }
        }

        override fun endTransition(forward: Boolean) {
            transitionRunning = false
            navIcon.parameter = (if (forward) ArrowXDrawableUtil.ArrowDrawableType.BACK else ArrowXDrawableUtil.ArrowDrawableType.CLOSE).type.toFloat()
            recyclerView.visibility = if (forward) View.VISIBLE else View.INVISIBLE

            mapCarouselContainer.visibility = if (forward) {
                View.INVISIBLE
            } else {
                if (mapWidget.hasSelectedMarker()) {
                    animateMapCarouselOut()
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
            }

            if (forward) {
                floatingPill?.translationY = 0f
                if (!fabShouldVisiblyMove) {
                    fab.translationY = 0f
                    (fab.drawable as? TransitionDrawable)?.reverseTransition(0)
                    fab.visibility = View.INVISIBLE
                }
                recyclerView.translationY = 0f
                mapWidget.translationY = -mapListSplitAnchor.toFloat()
                mapWidget.toSplitView(getRequiredMapPadding())

                sortFilterButtonTransition?.jumpToOrigin()
            } else {
                mapWidget.translationY = 0f
                recyclerView.translationY = screenHeight.toFloat()
                sortFilterButtonTransition?.jumpToTarget()

                mapWidget.toFullScreen()
            }
            getFloatingButton().translationY = finalFabTranslation
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
                mapWidget.visibility = View.VISIBLE
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
            if (!forward && !fabShouldBeHiddenOnList() && !shouldUsePill()) {
                fab.visibility = View.VISIBLE
                getFabAnimIn().start()
            }
            if (forward) {
                filterView.toolbar.requestFocus()
                recyclerView.visibility = View.GONE
                toolbar.visibility = View.GONE
                mapWidget.visibility = View.GONE
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
            } else {
                filterView.visibility = View.GONE
                if (!shouldUsePill()) {
                    fab.visibility = View.VISIBLE
                }
                searchThisArea?.visibility = View.VISIBLE
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
                mapWidget.visibility = View.VISIBLE
            }
            hideBundlePriceOverview(forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            transitionRunning = false
            if (forward) {
                toolbar.visibility = View.GONE
                mapWidget.visibility = View.GONE
            } else {
                if (!fabShouldBeHiddenOnList() && !shouldUsePill()) {
                    fab.visibility = View.VISIBLE
                    getFabAnimIn().start()
                }
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

    fun animationStart(forward: Boolean) {
        if (forward) {
            showChangeDateBanner()
        }
        recyclerTempBackground.visibility = View.VISIBLE
    }

    fun animationUpdate(f: Float, forward: Boolean) {
        val factor = if (forward) f else Math.abs(1 - f)
        navIcon.parameter = factor
    }

    fun animationFinalize() {
        recyclerTempBackground.visibility = View.GONE
        navIcon.parameter = ArrowXDrawableUtil.ArrowDrawableType.BACK.type.toFloat()
    }

    // We use ObjectAnimators instead of Animation because Animation mucks with settings values outside of it, and Object
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

    fun shouldUsePill(): Boolean = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelSearchResultsFloatingActionPill) && getLineOfBusiness() == LineOfBusiness.HOTELS

    fun showFilterMenuItem(isResults: Boolean) {
        if (shouldUsePill()) {
            return
        }
        if (getLineOfBusiness() == LineOfBusiness.PACKAGES && !isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
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

    open fun showChangeDateBanner() {
        //
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
                mapWidget.moveCamera(suggestion.coordinates.lat, suggestion.coordinates.lng)
            } else if (suggestion.regionNames?.fullName != null) {
                val BD_KEY = "geo_search"
                val bd = BackgroundDownloader.getInstance()
                bd.cancelDownload(BD_KEY)
                bd.startDownload(BD_KEY, mGeocodeDownload(suggestion.regionNames.fullName), geoCallback())
            }
        }
    }

    private fun setupToolbarTransition() {
        toolbarTitleTransition = VerticalTranslateTransition(toolbarTitle, toolbarTitle.top,
                (toolbarSubtitle.bottom - toolbarSubtitle.top) / 2)

        subTitleTransition = VerticalFadeTransition(toolbarSubtitle, 0, (toolbarSubtitle.bottom - toolbarSubtitle.top))
    }

    private fun getRequiredMapPadding(): Int {
        val firstVisibleView = recyclerView.getChildAt(1)
        if (firstVisibleView != null) {
            val topOffset = firstVisibleView.top
            val bottom = recyclerView.height - topOffset
            return (bottom + mapWidget.translationY).toInt()
        }
        return 0
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
                    mapWidget.moveCamera(results[0].latitude, results[0].longitude)
                }
            }
        }
    }

    private fun fabHeightOffset(): Float {
        val offset = context.resources.getDimension(R.dimen.hotel_carousel_fab_vertical_offset)
        return mapCarouselContainer.height.toFloat() - offset
    }

    abstract fun inflate()
    abstract fun inflateFilterView(viewStub: ViewStub): BaseHotelFilterView
    abstract fun hideSearchThisArea()
    abstract fun createFilterViewModel(): BaseHotelFilterViewModel
    abstract fun trackSearchMap()
    abstract fun trackMapToList()
    abstract fun trackMapPinTap()
    abstract fun trackFilterShown()
    abstract fun trackMapSearchAreaClick()
    abstract fun getLineOfBusiness(): LineOfBusiness
    abstract fun getHotelListAdapter(): BaseHotelListAdapter
    protected abstract fun getScrollListener(): BaseHotelResultsScrollListener
}
