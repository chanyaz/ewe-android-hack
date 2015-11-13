package com.expedia.bookings.presenter.hotel

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.TransitionDrawable
import android.location.Location
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.bitmaps.PicassoScrollListener
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FilterButtonWithCountWidget
import com.expedia.bookings.widget.HotelCarouselRecycler
import com.expedia.bookings.widget.HotelFilterView
import com.expedia.bookings.widget.HotelListAdapter
import com.expedia.bookings.widget.HotelListRecyclerView
import com.expedia.bookings.widget.HotelMapCarouselAdapter
import com.expedia.bookings.widget.MapLoadingOverlayWidget
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.createHotelMarkerIcon
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelFilterViewModel
import com.expedia.vm.HotelResultsMapViewModel
import com.expedia.vm.HotelResultsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mobiata.android.LocationServices
import org.joda.time.DateTime
import rx.Observable
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.ArrayList
import kotlin.properties.Delegates

public class HotelResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), OnMapReadyCallback {

    //Views
    val recyclerView: HotelListRecyclerView by bindView(R.id.list_view)
    var mapView: MapView by Delegates.notNull()
    val loadingOverlay: MapLoadingOverlayWidget by bindView(R.id.map_loading_overlay)
    val filterView: HotelFilterView by bindView(R.id.filter_view)
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val toolbarTitle by lazy { toolbar.getChildAt(2) }
    val toolbarSubtitle by lazy { toolbar.getChildAt(3) }
    val recyclerTempBackground: View by bindView(R.id.recycler_view_temp_background)
    val mapCarouselContainer: View by bindView(R.id.hotel_carousel_container)
    val mapCarouselRecycler: HotelCarouselRecycler by bindView(R.id.hotel_carousel)
    val fab: FloatingActionButton by bindView(R.id.fab)
    var adapter: HotelListAdapter by Delegates.notNull()
    val filterBtn: LinearLayout by bindView(R.id.filter_btn)

    private val PICASSO_TAG = "HOTEL_RESULTS_LIST"
    private val DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION = 200L

    var screenHeight: Int = 0
    var screenWidth: Float = 0f
    var mapTransitionRunning: Boolean = false
    var hideFabAnimationRunning: Boolean = false

    var previousWasList: Boolean = true

    var navIcon: ArrowXDrawable

    val hotelSelectedSubject = PublishSubject.create<Hotel>()
    val headerClickedSubject = PublishSubject.create<Unit>()

    var googleMap: GoogleMap? = null

    val filterMenuItem by lazy { toolbar.menu.findItem(R.id.menu_filter) }
    val searchMenuItem by lazy { toolbar.menu.findItem(R.id.menu_open_search) }

    var filterCountText: TextView by Delegates.notNull()
    var filterPlaceholderImageView: ImageView by Delegates.notNull()
    val filterPlaceholderIcon by lazy {
        val sortDrawable = resources.getDrawable(R.drawable.sort).mutate()
        sortDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        sortDrawable
    }

    val searchOverlaySubject = PublishSubject.create<Unit>()

    var halfway = 0
    var threshold = 0

    val filterBtnWithCountWidget: FilterButtonWithCountWidget by bindView(R.id.sort_filter_button_container)
    val searchThisArea: Button by bindView(R.id.search_this_area)

    val mapViewModel = HotelResultsMapViewModel(resources, lastBestLocationSafe())
    var markers = arrayListOf<Marker>()

    private val ANIMATION_DURATION_FILTER = 500

    var viewmodel: HotelResultsViewModel by notNullAndObservable { vm ->
        vm.hotelResultsObservable.subscribe{
            filterBtnWithCountWidget.visibility = View.VISIBLE
            filterBtnWithCountWidget.translationY = 0f
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
            showLoading()
            if (params.suggestion.coordinates != null) {
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(params.suggestion.coordinates.lat, params.suggestion.coordinates.lng), 14.0f))
            }
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

    private fun resetListOffset() {
        val mover = ObjectAnimator.ofFloat(mapView, "translationY", mapView.translationY, -halfway.toFloat());
        mover.setDuration(300);
        mover.start();

        val view = recyclerView.findViewHolderForAdapterPosition(1)
        if (view != null) {
            var distance = view.itemView.top - halfway;
            recyclerView.smoothScrollBy(0, distance)
        } else {
            recyclerView.layoutManager.scrollToPositionWithOffset(1, halfway)
        }
    }

    private fun adjustGoogleMapLogo() {
        val view = recyclerView.getChildAt(1)
        val topOffset = if (view == null) 0 else view.top
        val bottom = recyclerView.height - topOffset
        googleMap?.setPadding(0, toolbar.height, 0, (bottom + mapView.translationY).toInt())
    }

    private fun fabShouldBeHiddenOnList(): Boolean {
        return recyclerView.layoutManager.findFirstVisibleItemPosition() == 0
    }

    private fun shouldBlockTransition(): Boolean {
        return (mapTransitionRunning || (recyclerView.adapter as HotelListAdapter).isLoading())
    }

    val listResultsObserver = endlessObserver<HotelSearchResponse> {
        filterView.viewmodel.setHotelList(it)
        loadingOverlay.animate(false)
        loadingOverlay.visibility = View.GONE
        adapter.resultsSubject.onNext(it)
        filterBtnWithCountWidget.visibility = View.VISIBLE
    }

    private fun lastBestLocationSafe(): Location {
        val minTime = DateTime.now().millis - DateUtils.HOUR_IN_MILLIS
        val loc = LocationServices.getLastBestLocation(context, minTime)
        val location = Location("lastBestLocationSafe")
        location.latitude = loc?.latitude ?: 0.0
        location.longitude = loc?.longitude ?: 0.0
        return location
    }

    val mapSelectedObserver: Observer<Unit> = endlessObserver {
        if (!shouldBlockTransition()) {
            show(ResultsMap())
        }
    }

    val filterObserver: Observer<HotelSearchResponse> = endlessObserver { response ->
        if (previousWasList) {
            show(ResultsList(), Presenter.FLAG_CLEAR_TOP)
            resetListOffset()
        } else {
            show(ResultsMap(), Presenter.FLAG_CLEAR_TOP)
        }

        (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setItems(response.hotelList)
        adapter.resultsSubject.onNext(response)
        mapViewModel.hotelResultsSubject.onNext(response)
    }

    override fun back(): Boolean {
        if (ResultsFilter().javaClass.name == currentState) {
            if (filterView.viewmodel.isFilteredToZeroResults()) {
                filterView.dynamicFeedbackWidget.animateDynamicFeedbackWidget()
                return true
            } else {
                filterView.viewmodel.doneObservable.onNext(Unit)
            }
        }
        return super.back()
    }

    init {
        View.inflate(getContext(), R.layout.widget_hotel_results, this)
        ViewCompat.setElevation(loadingOverlay, context.resources.getDimension(R.dimen.launch_tile_margin_side))
        headerClickedSubject.subscribe(mapSelectedObserver)
        adapter = HotelListAdapter(hotelSelectedSubject, headerClickedSubject)
        recyclerView.adapter = adapter
        filterView.viewmodel = HotelFilterViewModel()
        filterView.viewmodel.filterObservable.subscribe(filterObserver)
        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.setBackgroundColor(resources.getColor(R.color.hotels_primary_color))
        toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
        toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance)

        mapCarouselRecycler.adapter = HotelMapCarouselAdapter(emptyList(), hotelSelectedSubject)

        mapViewModel.markersObservable.subscribe {
            mapViewModel.selectMarker.onNext(null)
            val hotels = it
            Observable.just(it).subscribeOn(Schedulers.io())
                    .map {
                        var options = ArrayList<MarkerOptions>()
                        //createHotelMarkerIcon should run in a separate thread since its heavy and hangs on the UI thread
                        it.forEach {
                            hotel ->
                            val bitmap = createHotelMarkerIcon(resources, hotel, false, hotel.lowRateInfo.isShowAirAttached(), hotel.isSoldOut)
                            val option = MarkerOptions()
                                    .position(LatLng(hotel.latitude, hotel.longitude))
                                    .icon(bitmap)
                                    .title(hotel.hotelId)
                            options.add(option)
                        }
                        options
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    //add the markers on the UI thread
                    .subscribe {
                        markers.clear()
                        googleMap?.clear()

                        it.forEach {
                            val option = it
                            val marker = googleMap?.addMarker(option)
                            if (marker != null) markers.add(marker)
                        }

                        (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setItems(hotels)
                        mapCarouselRecycler.scrollToPosition(0)
                        val marker = markers.filter { it.title == hotels.first().hotelId }.first()
                        mapViewModel.selectMarker.onNext(Pair(marker, hotels.first()))
                    }
        }

        mapViewModel.sortedHotelsObservable.subscribe {
            val hotels = it
            (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setItems(hotels)
            mapCarouselRecycler.scrollToPosition(0)
            val markersForHotel = markers.filter { it.title == hotels.first().hotelId }
            if (markersForHotel.isNotEmpty()) {
                val marker = markersForHotel.first()
                val prevMarker = mapViewModel.selectMarker.value
                if (prevMarker != null) mapViewModel.unselectedMarker.onNext(prevMarker)
                mapViewModel.selectMarker.onNext(Pair(marker, hotels.first()))
            }
        }

        mapViewModel.soldOutHotel.subscribe { hotel ->
            val markersForHotel = markers.filter { it.title == hotel.hotelId }
            if (markersForHotel.isNotEmpty()) {
                val marker = markersForHotel.first()
                mapViewModel.soldOutMarker.onNext(Pair(marker, hotel))
            }
        }

        mapViewModel.carouselSwipedObservable.subscribe {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, googleMap?.cameraPosition?.zoom!!))
        }

        mapCarouselRecycler.addOnScrollListener(PicassoScrollListener(context, PICASSO_TAG))
    }

    override fun onFinishInflate() {

        //Store a pointer instead of invoking the getResources() function behind the resources property each usage
        val res = resources

        // add the view of same height as of status bar
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
            var lp = recyclerView.layoutParams as FrameLayout.LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
        }

        addDefaultTransition(defaultTransition)
        addTransition(fabTransition)
        addTransition(listFilterTransition)
        addTransition(mapFilterTransition)

        mapCarouselContainer.visibility = View.INVISIBLE
        val screen = Ui.getScreenSize(context)
        var lp = mapCarouselRecycler.layoutParams
        lp.width = screen.x

        recyclerView.addOnScrollListener(scrollListener)
        recyclerView.addOnItemTouchListener(touchListener)

        mapCarouselRecycler.mapSubject.subscribe { hotel ->
            val markersForHotel = markers.filter { it.title == hotel.hotelId }
            if (markersForHotel.isNotEmpty()) {
                val marker = markersForHotel.first()
                mapViewModel.carouselSwipedObservable.onNext(marker)
                HotelV2Tracking().trackHotelV2CarouselScroll()
            }
        }

        inflateAndSetupToolbarMenu()
        toolbar.setNavigationOnClickListener { view ->
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }

        filterView.viewmodel.filterCountObservable.subscribe(filterCountObserver)
        filterView.viewmodel.filterCountObservable.map { it.toString() }.subscribeText(filterCountText)
        filterView.viewmodel.filterCountObservable.map { it > 0 }.subscribeVisibility(filterCountText)
        filterView.viewmodel.filterCountObservable.map { it > 0 }.subscribeInverseVisibility(filterPlaceholderImageView)

        val fabDrawable: TransitionDrawable? = (fab.drawable as? TransitionDrawable)
        // Enabling crossfade prevents the icon ending up with a weird mishmash of both icons.
        fabDrawable?.isCrossFadeEnabled = true

        fab.setOnClickListener { view ->
            if (recyclerView.visibility == View.VISIBLE) {
                show(ResultsMap())
            } else {
                show(ResultsList(), Presenter.FLAG_CLEAR_BACKSTACK)
                HotelV2Tracking().trackHotelV2MapToList()
            }
        }

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

        filterBtn.setOnClickListener { view ->
            show(ResultsFilter())
            filterView.viewmodel.sortContainerObservable.onNext(false)
            filterView.toolbar.setTitle(getResources().getString(R.string.filter))
        }

        filterBtnWithCountWidget.setOnClickListener {
            show(ResultsFilter())
            filterView.viewmodel.sortContainerObservable.onNext(true)
            filterView.toolbar.setTitle(getResources().getString(R.string.Sort_and_Filter))
        }

        searchMenuItem.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
            override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                searchOverlaySubject.onNext(Unit)
                return true
            }
        })

        filterMenuItem.setVisible(false)
        var fabLp = fab.layoutParams as FrameLayout.LayoutParams
        fabLp.bottomMargin += resources.getDimension(R.dimen.hotel_filter_height).toInt()

        mapViewModel.newBoundsObservable.subscribe {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(it, resources.displayMetrics.density.toInt() * 50), object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    val center = googleMap?.cameraPosition?.target
                    val latLng = LatLng(center?.latitude!!, center?.longitude!!)
                    mapViewModel.mapBoundsSubject.onNext(latLng)
                }

                override fun onCancel() {
                }
            })

        }
    }

    public fun showDefault() {
        show(ResultsList())
    }

    private fun inflateAndSetupToolbarMenu() {
        toolbar.inflateMenu(R.menu.menu_filter_item)

        val toolbarFilterItemActionView = LayoutInflater.from(context).inflate(R.layout.toolbar_filter_item, null) as LinearLayout
        filterCountText = toolbarFilterItemActionView.findViewById(R.id.filter_count_text) as TextView
        filterPlaceholderImageView = toolbarFilterItemActionView.findViewById(R.id.filter_placeholder_icon) as ImageView
        filterPlaceholderImageView.setImageDrawable(filterPlaceholderIcon)

        toolbar.menu.findItem(R.id.menu_filter).setActionView(toolbarFilterItemActionView)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        val uiSettings = googleMap?.uiSettings
        //Explicitly disallow map-cluttering ui (but keep the gestures)
        if (uiSettings != null) {
            uiSettings.isCompassEnabled = false
            uiSettings.isMapToolbarEnabled = false
            uiSettings.isZoomControlsEnabled = false
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isIndoorLevelPickerEnabled = false
        }

        googleMap?.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {
                mapViewModel.carouselSwipedObservable.onNext(marker)
                mapViewModel.mapPinSelectSubject.onNext(marker)
                mapCarouselContainer.visibility = View.VISIBLE
                return true
            }
        })

        googleMap?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            override fun getInfoWindow(marker: Marker): View? {
                val activity = context as AppCompatActivity
                val v = activity.layoutInflater.inflate(R.layout.marker_window, null);
                return v;
            }

            override fun getInfoContents(marker: Marker): View? {
                return null
            }
        })
    }

    val scrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        var currentState = RecyclerView.SCROLL_STATE_IDLE
        var scrolledDistance = 0
        val heightOfButton = resources.getDimension(R.dimen.lx_sort_filter_container_height).toInt()

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            currentState = newState

            val manager = recyclerView.layoutManager as LinearLayoutManager
            val isAtBottom = manager.findLastCompletelyVisibleItemPosition() == (recyclerView.adapter.itemCount - 1)
            val topOffset = recyclerView.findViewHolderForAdapterPosition(1)?.itemView?.top ?: 0

            if (newState == RecyclerView.SCROLL_STATE_IDLE && ((topOffset >= threshold && isHeaderVisible()) || isHeaderCompletelyVisible())) {
                //view has passed threshold, show map
                show(ResultsMap())
            } else if (newState == RecyclerView.SCROLL_STATE_IDLE && topOffset < threshold && topOffset > halfway && isHeaderVisible() && !isAtBottom) {
                resetListOffset()
            }

            // Filter button translation
            if (!mapTransitionRunning && newState == RecyclerView.SCROLL_STATE_IDLE && !Strings.equals(ResultsMap::class.java.name, getCurrentState())) {
                if (topOffset == halfway) {
                    filterBtnWithCountWidget.animate().translationY(0f).setInterpolator(DecelerateInterpolator()).start()
                } else if (scrolledDistance > heightOfButton / 2) {
                    filterBtnWithCountWidget.animate().translationY(heightOfButton.toFloat()).setInterpolator(DecelerateInterpolator()).start()
                    fab.animate().translationY(heightOfButton.toFloat()).setInterpolator(DecelerateInterpolator()).start()
                } else {
                    filterBtnWithCountWidget.animate().translationY(0f).setInterpolator(DecelerateInterpolator()).start()
                    fab.animate().translationY(0f).setInterpolator(DecelerateInterpolator()).start()
                }
            }

        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (shouldBlockTransition() || getCurrentState()?.equals(ResultsMap::class.java.name) ?: false) {
                return
            }

            val y = mapView.translationY + (-dy * halfway / (recyclerView.height - halfway))
            mapView.translationY = y

            val topOffset = recyclerView.findViewHolderForAdapterPosition(1)?.itemView?.top ?: 0

            adjustGoogleMapLogo()

            if (currentState == RecyclerView.SCROLL_STATE_SETTLING && topOffset < threshold && topOffset > halfway && isHeaderVisible()) {
                recyclerView.translationY = 0f
                resetListOffset()
            } else if (currentState == RecyclerView.SCROLL_STATE_SETTLING && ((topOffset >= threshold && isHeaderVisible()) || isHeaderCompletelyVisible())) {
                show(ResultsMap())
            }

            if (dy > 0) {
                scrolledDistance = Math.min(heightOfButton, scrolledDistance + dy)
            } else {
                scrolledDistance = Math.max(0, scrolledDistance + dy)
            }

            if (!mapTransitionRunning) {
                if (!fabShouldBeHiddenOnList() && fab.visibility != View.VISIBLE) {
                    fab.visibility = View.VISIBLE
                    getFabAnimIn().start()
                } else if (fabShouldBeHiddenOnList() && fab.visibility == View.VISIBLE && !hideFabAnimationRunning) {
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

                // Filter button translation
                if (currentState != RecyclerView.SCROLL_STATE_SETTLING && currentState != RecyclerView.SCROLL_STATE_IDLE) {
                    if (topOffset > halfway) {
                        filterBtnWithCountWidget.translationY = 0f
                        fab.translationY = 0f
                    } else if (scrolledDistance > 0) {
                        filterBtnWithCountWidget.translationY = Math.min(heightOfButton, scrolledDistance).toFloat()
                        fab.translationY = Math.min(heightOfButton, scrolledDistance).toFloat()
                    } else {
                        filterBtnWithCountWidget.translationY = Math.min(scrolledDistance, 0).toFloat()
                        fab.translationY = Math.min(scrolledDistance, 0).toFloat()
                    }
                }

            }
        }

        fun isHeaderVisible(): Boolean {
            return recyclerView.layoutManager.findFirstVisibleItemPosition() == 0
        }

        fun isHeaderCompletelyVisible(): Boolean {
            return recyclerView.layoutManager.findFirstCompletelyVisibleItemPosition() == 0
        }
    }

    private val defaultTransition = object : Presenter.DefaultTransition(ResultsList::class.java.name) {

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            navIcon.parameter = if (forward) Math.abs(1 - f) else f
        }

        override fun finalizeTransition(forward: Boolean) {
            navIcon.parameter = ArrowXDrawableUtil.ArrowDrawableType.BACK.type.toFloat()
            recyclerView.translationY = 0f
            mapView.translationY = -halfway.toFloat()

            recyclerView.visibility = View.VISIBLE
            mapCarouselContainer.visibility = View.INVISIBLE

            if (recyclerView.visibility == View.INVISIBLE) {
                fab.visibility = View.VISIBLE
                getFabAnimIn().start()
            } else {
                fab.visibility = View.INVISIBLE
            }
            filterView.visibility = View.INVISIBLE
        }
    }

    private val adapterListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            screenHeight = if (ExpediaBookingApp.isAutomation()) { 0 } else { height }
            screenWidth = if (ExpediaBookingApp.isAutomation()) { 0f } else { width.toFloat() }

            halfway = (height / 4.1).toInt()
            threshold = (height / 2.2).toInt()

            resetListOffset()
        }
    }

    private val fabTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsList::class.java, LinearInterpolator(), 750) {

        private val listTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsList::class.java, DecelerateInterpolator(2f), duration * 2 / 3) {

            var fabShouldVisiblyMove: Boolean = true
            var mapTranslationStart: Float = 0f
            var initialListTranslation: Int = 0
            var toolbarTextOrigin: Float = 0f
            var toolbarTextGoal: Float = 0f
            var filterViewOrigin: Float = 0f
            var filterViewGoal: Float = 0f

            override fun startTransition(forward: Boolean) {
                super.startTransition(forward)
                toolbarTextOrigin = toolbarTitle.translationY

                if (forward) {
                    toolbarTextGoal = 0f //
                } else {
                    toolbarTextGoal = toolbarSubtitleTop.toFloat()
                }
                filterViewGoal = if (forward) 0f else filterBtnWithCountWidget.height.toFloat()
                recyclerView.visibility = View.VISIBLE
                previousWasList = forward
                fabShouldVisiblyMove = if (forward) !fabShouldBeHiddenOnList() else (fab.visibility == View.VISIBLE)
                initialListTranslation = recyclerView.getChildAt(1)?.top ?: 0
                if (forward) {
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
                    mapTranslationStart = mapView.translationY
                    if (fabShouldVisiblyMove) {
                        (fab.drawable as? TransitionDrawable)?.startTransition(duration)
                    } else {
                        //Since we're not moving it manually, let's jump it to where it belongs,
                        // and let's get it showing the right thing
                        fab.translationY = -(mapCarouselContainer.height.toFloat() - resources.getDimension(R.dimen.hotel_filter_height).toInt())
                        (fab.drawable as? TransitionDrawable)?.startTransition(0)
                        fab.visibility = View.VISIBLE
                        getFabAnimIn().start()
                    }
                }
            }

            override fun updateTransition(f: Float, forward: Boolean) {
                val hotelListDistance = if (forward) (screenHeight * (1 - f)) else ((screenHeight - initialListTranslation) * f);
                recyclerView.translationY = hotelListDistance
                navIcon.parameter = if (forward) Math.abs(1 - f) else f
                if (forward) {
                    mapView.translationY = f * -halfway
                } else {
                    mapView.translationY = (1 - f) * mapTranslationStart
                }

                if (fabShouldVisiblyMove) {
                    val fabDistance = if (forward) -(1 - f) * mapCarouselContainer.height else -f * (mapCarouselContainer.height - resources.getDimension(R.dimen.hotel_filter_height).toInt())
                    fab.translationY = fabDistance
                }
                //Title transition
                val toolbarYTransStep = toolbarTextOrigin + (f * (toolbarTextGoal - toolbarTextOrigin))
                toolbarTitle.translationY = toolbarYTransStep
                toolbarSubtitle.translationY = toolbarYTransStep
                toolbarSubtitle.alpha = if (forward) f else (1 - f)
                adjustGoogleMapLogo()
                //Filter transition
                filterBtnWithCountWidget.translationY = filterViewOrigin + (f * (filterViewGoal - filterViewOrigin))
            }

            override fun finalizeTransition(forward: Boolean) {
                navIcon.parameter = (if (forward) ArrowXDrawableUtil.ArrowDrawableType.BACK else ArrowXDrawableUtil.ArrowDrawableType.CLOSE).type.toFloat()

                searchMenuItem.setVisible(forward)
                filterMenuItem.setVisible(!forward)
                //When we are about to show the menu filter item, ensure that it is of sufficient height
                if (filterMenuItem.actionView.height != toolbar.height) {
                    filterMenuItem.actionView.layoutParams.height = toolbar.measuredHeight
                }

                recyclerView.visibility = if (forward) View.VISIBLE else View.INVISIBLE

                mapCarouselContainer.visibility = if (forward) View.INVISIBLE else View.VISIBLE

                if (forward) {
                    if (!fabShouldVisiblyMove) {
                        fab.translationY = 0f
                        (fab.drawable as? TransitionDrawable)?.reverseTransition(0)
                        fab.visibility = View.INVISIBLE
                    }
                    recyclerView.translationY = 0f
                    mapView.translationY = -halfway.toFloat()
                    adjustGoogleMapLogo()
                    filterBtnWithCountWidget.translationY = 0f
                } else {
                    fab.translationY = -(mapCarouselContainer.height.toFloat() - resources.getDimension(R.dimen.hotel_filter_height).toInt())
                    mapView.translationY = 0f
                    recyclerView.translationY = screenHeight.toFloat()
                    googleMap?.setPadding(0, toolbar.height, 0, mapCarouselContainer.height)
                    filterBtnWithCountWidget.translationY = resources.getDimension(R.dimen.hotel_filter_height)
                }
            }
        }

        private val carouselTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsList::class.java, DecelerateInterpolator(2f), duration / 3) {

            override fun startTransition(forward: Boolean) {
                mapCarouselContainer.visibility = View.VISIBLE
                if (forward) {
                    mapCarouselContainer.translationX = 0f
                    googleMap?.setOnCameraChangeListener(null)
                    hideSearchThisArea(duration.toLong())
                } else {
                    //If the user moves the map at all, make sure the button is showing.
                    googleMap?.setOnCameraChangeListener { position ->
                        if (Strings.equals(currentState, ResultsMap::class.java.name)) {
                            showSearchThisArea()
                        }
                    }
                    mapCarouselContainer.translationX = screenWidth
                }
            }

            override fun updateTransition(f: Float, forward: Boolean) {
                mapCarouselContainer.translationX = (if (forward) f else 1 - f) * screenWidth
            }

            override fun finalizeTransition(forward: Boolean) {
                if (forward) {
                    mapCarouselContainer.translationX = screenWidth
                    mapCarouselContainer.visibility = View.INVISIBLE
                } else {
                    mapCarouselContainer.translationX = 0f
                }
            }
        }

        var secondTransitionStartTime = .33f
        var currentTransition = 0

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            setupToolbarMeasurements()
            currentTransition = 0
            mapTransitionRunning = true


            if (forward) {
                //Let's be explicit despite it being the default
                secondTransitionStartTime = .33f
                carouselTransition.startTransition(forward)
            } else {
                secondTransitionStartTime = .66f
                listTransition.startTransition(forward)
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            if (forward) {
                if (f < secondTransitionStartTime) {
                    carouselTransition.updateTransition(carouselTransition.interpolator.getInterpolation(f / secondTransitionStartTime), forward)
                } else {
                    if (currentTransition == 0) {
                        currentTransition = 1
                        carouselTransition.finalizeTransition(forward)
                        listTransition.startTransition(forward)
                    }
                    listTransition.updateTransition(listTransition.interpolator.getInterpolation((f - secondTransitionStartTime) / (1 - secondTransitionStartTime)), forward)
                }
            } else {
                if (f < secondTransitionStartTime) {
                    listTransition.updateTransition(listTransition.interpolator.getInterpolation(f / secondTransitionStartTime), forward)
                } else {
                    if (currentTransition == 0) {
                        currentTransition = 1
                        listTransition.finalizeTransition(forward)
                        carouselTransition.startTransition(forward)
                    }
                    carouselTransition.updateTransition(carouselTransition.interpolator.getInterpolation((f - secondTransitionStartTime) / (1 - secondTransitionStartTime)), forward)
                }
            }
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)

            if (forward) {
                listTransition.finalizeTransition(forward)
            } else {
                carouselTransition.finalizeTransition(forward)
            }
            mapTransitionRunning = false
        }
    }

    private val listFilterTransition = object : Presenter.Transition(ResultsList::class.java, ResultsFilter::class.java, DecelerateInterpolator(2f), ANIMATION_DURATION_FILTER) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            filterView.visibility = View.VISIBLE
            if (forward) {
                fab.visibility = View.GONE
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            val translatePercentage = if (forward) 1 - f else f
            filterView.translationY = filterView.getHeight() * translatePercentage
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            filterView.visibility = if (forward) View.VISIBLE else View.GONE
            filterView.translationY = (if (forward) 0 else filterView.getHeight()).toFloat()
            if (!forward && !fabShouldBeHiddenOnList()) {
                fab.visibility = View.VISIBLE
                getFabAnimIn().start()
            }
        }
    }

    private val mapFilterTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsFilter::class.java, DecelerateInterpolator(2f), ANIMATION_DURATION_FILTER) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            filterView.visibility = View.VISIBLE
            if (forward) {
                fab.visibility = View.GONE
                searchThisArea.visibility = View.GONE
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            val translatePercentage = if (forward) 1 - f else f
            filterView.translationY = filterView.height * translatePercentage
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            filterView.visibility = if (forward) View.VISIBLE else View.GONE
            filterView.translationY = (if (forward) 0 else filterView.height).toFloat()
            if (!forward) {
                fab.visibility = View.VISIBLE
                searchThisArea.visibility = View.VISIBLE
            }
        }
    }

    val touchListener = object : RecyclerView.OnItemTouchListener {
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

        }

        override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent?): Boolean {
            if (mapTransitionRunning) {
                return true
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView?, e: MotionEvent?) {

        }
    }

    val filterCountObserver: Observer<Int> = endlessObserver { filterBtnWithCountWidget.showNumberOfFilters(it) }

    var yTranslationRecyclerTempBackground = 0f
    var yTranslationRecyclerView = 0f
    var toolbarTitleTop = 0
    var toolbarSubtitleTop = 0

    fun setupToolbarMeasurements() {
        if (yTranslationRecyclerTempBackground == 0f && recyclerView.getChildAt(1) != null) {
            yTranslationRecyclerTempBackground = (recyclerView.getChildAt(0).height + recyclerView.getChildAt(0).top + toolbar.height).toFloat()
            yTranslationRecyclerView = (recyclerView.getChildAt(0).height + recyclerView.getChildAt(0).top).toFloat()
            recyclerTempBackground.translationY = yTranslationRecyclerTempBackground
            toolbarTitleTop = (toolbarTitle.bottom - toolbarTitle.top) / 2
            toolbarSubtitleTop = (toolbarSubtitle.bottom - toolbarSubtitle.top) / 2
            toolbarTitle.translationY = toolbarTitleTop.toFloat()
            toolbarSubtitle.translationY = toolbarSubtitleTop.toFloat()
        }
    }

    fun animationStart() {
        recyclerTempBackground.visibility = View.VISIBLE
    }

    fun animationUpdate(f: Float, forward: Boolean) {
        setupToolbarMeasurements()
        var factor = if (forward) f else Math.abs(1 - f)
        recyclerView.translationY = factor * yTranslationRecyclerView
        navIcon.parameter = factor
        toolbarTitle.translationY = factor * toolbarTitleTop
        toolbarSubtitle.translationY = factor * toolbarSubtitleTop
    }

    fun animationFinalize(forward: Boolean) {
        recyclerTempBackground.visibility = View.GONE
        navIcon.parameter = ArrowXDrawableUtil.ArrowDrawableType.BACK.type.toFloat()
        googleMap?.isMyLocationEnabled = forward
    }

    //We use ObjectAnimators instead of Animation because Animation mucks with settings values outside of it, and Object
    // Animator lets us do that.
    fun getFabAnimIn(): Animator {
        val set = AnimatorSet()
        set.playTogether(
                ObjectAnimator.ofFloat(fab, "scaleX", 0f, 1f),
                ObjectAnimator.ofFloat(fab, "scaleY", 0f, 1f)
        )
        set.setDuration(DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION)
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
        set.setDuration(DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION)
        return set
    }

    // Classes for state
    public class ResultsList

    public class ResultsMap {
        init {
            HotelV2Tracking().trackHotelV2SearchMap()
        }
    }

    public class ResultsFilter {
        init {
            HotelV2Tracking().trackHotelV2Filter()
        }
    }

    fun doAreaSearch() {
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

    fun hideSearchThisArea(duration: Long = DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION) {
        if (searchThisArea.getVisibility() == View.VISIBLE) {
            val anim: Animator = ObjectAnimator.ofFloat(searchThisArea, "alpha", 1f, 0f)
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationCancel(animation: Animator?) {
                    //Do nothing
                }

                override fun onAnimationEnd(animator: Animator?) {
                    searchThisArea.setVisibility(View.GONE)
                }

                override fun onAnimationStart(animator: Animator?) {
                    //Do nothing
                }

                override fun onAnimationRepeat(animator: Animator?) {
                    //Do nothing
                }

            })
            anim.setDuration(duration)
            anim.start()
        }
    }

    fun showSearchThisArea(duration: Long = DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION) {
        if (currentState?.equals(javaClass<ResultsMap>().name) ?: false && searchThisArea.visibility == View.GONE) {
            searchThisArea.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(searchThisArea, "alpha", 0f, 1f).setDuration(duration).start()
        }
    }
}
