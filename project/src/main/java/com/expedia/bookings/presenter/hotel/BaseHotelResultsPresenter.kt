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
import android.support.v4.view.ViewCompat
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
import android.view.ViewGroup
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
import com.expedia.bookings.bitmaps.PicassoScrollListener
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.hotel.animation.HorizontalTranslateTransition
import com.expedia.bookings.hotel.animation.VerticalFadeTransition
import com.expedia.bookings.hotel.animation.VerticalTranslateTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.HotelMapClusterAlgorithm
import com.expedia.bookings.utils.HotelMapClusterRenderer
import com.expedia.bookings.utils.MapItem
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseHotelFilterView
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.bookings.widget.HotelCarouselRecycler
import com.expedia.bookings.widget.HotelClientFilterView
import com.expedia.bookings.widget.HotelListRecyclerView
import com.expedia.bookings.widget.HotelMapCarouselAdapter
import com.expedia.bookings.widget.HotelMarkerIconGenerator
import com.expedia.bookings.widget.MapLoadingOverlayWidget
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.hotel.HotelResultsSortFaqWebView
import com.expedia.util.endlessObserver
import com.expedia.util.havePermissionToAccessLocation
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import com.expedia.vm.hotel.HotelResultsMapViewModel
import com.expedia.vm.hotel.HotelResultsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterManager
import com.mobiata.android.BackgroundDownloader
import com.mobiata.android.LocationServices
import org.joda.time.DateTime
import rx.Observer
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

abstract class BaseHotelResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), OnMapReadyCallback {

    abstract var viewModel: HotelResultsViewModel

    //Views
    var filterButtonText: TextView by Delegates.notNull()
    val recyclerView: HotelListRecyclerView by bindView(R.id.list_view)
    var mapView: MapView by Delegates.notNull()
    open val loadingOverlay: MapLoadingOverlayWidget? = null

    val toolbar: Toolbar by bindView(R.id.hotel_results_toolbar)
    val toolbarTitle: android.widget.TextView by bindView(R.id.title)
    val toolbarSubtitle: android.widget.TextView by bindView(R.id.subtitle)
    val recyclerTempBackground: View by bindView(R.id.recycler_view_temp_background)
    val mapCarouselContainer: ViewGroup by bindView(R.id.hotel_carousel_container)
    val mapCarouselRecycler: HotelCarouselRecycler by bindView(R.id.hotel_carousel)
    val fab: FloatingActionButton by bindView(R.id.fab)

    open val filterMenuItem by lazy { toolbar.menu.findItem(R.id.menu_filter) }

    var filterCountText: TextView by Delegates.notNull()
    var filterPlaceholderImageView: ImageView by Delegates.notNull()

    protected val filterView: BaseHotelFilterView by lazy {
        inflateFilterView(filterViewStub)
    }
    private val filterViewStub: ViewStub by bindView(R.id.hotel_filter_view_stub)

    private val sortFaqWebView: HotelResultsSortFaqWebView by lazy {
        val webView = findViewById(R.id.sort_faq_web_view) as HotelResultsSortFaqWebView
        webView.setExitButtonOnClickListener(View.OnClickListener { this.back() })
        webView
    }

    var adapter: BaseHotelListAdapter by Delegates.notNull()
    open val searchThisArea: Button? = null
    var isMapReady = false

    var clusterManager: ClusterManager<MapItem> by Delegates.notNull()

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
    val showSearchMenu = PublishSubject.create<Boolean>()
    val hideBundlePriceOverviewSubject = PublishSubject.create<Boolean>()

    var googleMap: GoogleMap? = null

    var filterBtn: LinearLayout? = null

    val searchOverlaySubject = PublishSubject.create<Unit>()

    var hotelMapClusterRenderer: HotelMapClusterRenderer by Delegates.notNull()

    var mapListSplitAnchor = 0
    var snapToFullScreenMapThreshold = 0

    val hotelIconFactory = HotelMarkerIconGenerator(context)

    var mapItems = arrayListOf<MapItem>()
    var hotels = emptyList<Hotel>()

    private val ANIMATION_DURATION_FILTER = 500

    protected var sortFilterButtonTransition: VerticalTranslateTransition? = null
    private var toolbarTitleTransition: VerticalTranslateTransition? = null
    private var subTitleTransition: VerticalFadeTransition? = null
    private var mapCarouselTransition: HorizontalTranslateTransition? = null

    var mapViewModel: HotelResultsMapViewModel by notNullAndObservable { vm ->

        vm.sortedHotelsObservable.subscribe {

            hotels = it
            updateMarkers()
        }

        vm.soldOutHotel.subscribe { hotel ->
            val mapItem = mapItems.firstOrNull { it.hotel.hotelId == hotel.hotelId }
            if (mapItem != null) {
                hotelMapClusterRenderer.getMarker(mapItem)?.setIcon(mapItem.getHotelMarkerIcon())
                clusterMarkers()
            }
        }
        vm.carouselSwipedObservable.subscribe {
            selectMarker(it, true)
        }
    }

    private fun selectMarker(mapItem: MapItem, shouldZoom: Boolean = false, animateCarousel: Boolean = true) {
        clusterManager.clearItems()
        clusterManager.addItems(mapItems)
        clearPreviousMarker()
        mapItem.isSelected = true
        val selectedMarker = hotelMapClusterRenderer.getMarker(mapItem)
        if (!mapItem.hotel.isSoldOut) {
            selectedMarker?.setIcon(mapItem.getHotelMarkerIcon())
        }
        selectedMarker?.showInfoWindow()
        if (shouldZoom) {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(mapItem.position, googleMap?.cameraPosition?.zoom!!))
        }
        mapViewModel.mapPinSelectSubject.onNext(mapItem)
        if (animateCarousel && currentState == ResultsMap::class.java.name) {
            animateMapCarouselIn()
        }
    }

    protected fun inflateClientFilterView(viewStub: ViewStub) : HotelClientFilterView {
        viewStub.layoutResource = R.layout.hotel_client_filter_stub;
        return viewStub.inflate() as HotelClientFilterView
    }

    protected fun animateMapCarouselIn() {
        if (mapCarouselContainer.visibility != View.VISIBLE) {
            val carouselAnimation = mapCarouselContainer.animate().translationX(0f).setInterpolator(DecelerateInterpolator()).setStartDelay(400)
            mapCarouselContainer.translationX = screenWidth

            var onLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null //need to know carousel height before fab can properly animate.
            onLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
                fab.animate().translationY(-fabHeightOffset()).setInterpolator(DecelerateInterpolator()).withEndAction {
                    carouselAnimation.start()
                }.start()
                mapCarouselContainer.viewTreeObserver.removeOnGlobalLayoutListener(onLayoutListener)
            }

            mapCarouselContainer.viewTreeObserver.addOnGlobalLayoutListener(onLayoutListener)
            mapCarouselContainer.visibility = View.VISIBLE
        }
    }

    protected fun animateMapCarouselOut() {
        if (mapCarouselContainer.visibility != View.INVISIBLE) {
            val carouselAnimation = mapCarouselContainer.animate().translationX(screenWidth).setInterpolator(DecelerateInterpolator())
            carouselAnimation.withEndAction {
                mapCarouselContainer.visibility = View.INVISIBLE
                animateFab(0f)
            }
            carouselAnimation.start()
        }
    }

    protected fun resetListOffset() {
        val mover = ObjectAnimator.ofFloat(mapView, "translationY", mapView.translationY, -mapListSplitAnchor.toFloat())
        mover.duration = 300
        mover.start()

        val view = recyclerView.findViewHolderForAdapterPosition(1)
        if (view != null) {
            var distance = view.itemView.top - mapListSplitAnchor
            recyclerView.smoothScrollBy(0, distance)
        } else {
            recyclerView.layoutManager.scrollToPositionWithOffset(1, mapListSplitAnchor)
        }
    }

    private fun animateFab(newTranslationY: Float) {
        fab.animate().translationY(newTranslationY).setInterpolator(DecelerateInterpolator()).start()
    }

    private fun adjustGoogleMapLogo() {
        val view = recyclerView.getChildAt(1)
        if (view != null) {
            val topOffset = view.top
            val bottom = recyclerView.height - topOffset
            googleMap?.setPadding(0, toolbar.height, 0, (bottom + mapView.translationY).toInt())
        }
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
            animateMapCarouselOut()
        }

        (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setItems(response.hotelList)
        adapter.resultsSubject.onNext(response)
        mapViewModel.hotelResultsSubject.onNext(response)
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
            }
        } else if (ResultsList().javaClass.name == currentState) {
            clearMarkers(false)
        }

        return super.back()
    }

    init {
        inflate()

        mapViewModel = HotelResultsMapViewModel(context, lastBestLocationSafe())
        mapViewModel.clusterChangeSubject.subscribe {
            updateCarouselItems()
        }
        headerClickedSubject.subscribe(mapSelectedObserver)
        adapter = getHotelListAdapter()

        recyclerView.adapter = adapter
        filterView.viewModel = createFilterViewModel()
        filterView.viewModel.filterObservable.subscribe(filterObserver)

        if (!filterView.viewModel.isClientSideFiltering()) {
            filterView.viewModel.filterByParamsObservable.subscribe { params ->
                viewModel.filterParamsSubject.onNext(params)
            }
        }

        filterView.viewModel.showPreviousResultsObservable.subscribe {
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

        mapViewModel.newBoundsObservable.subscribe {
            if (isMapReady) {
                val center = it.center
                val latLng = LatLng(center.latitude, center.longitude)
                mapViewModel.mapBoundsSubject.onNext(latLng)
                val padding = 60
                if (ViewCompat.isLaidOut(mapView)) {
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(it, resources.displayMetrics.density.toInt() * padding), object : GoogleMap.CancelableCallback {
                        override fun onFinish() {
                        }

                        override fun onCancel() {
                        }
                    })
                }
            }
        }

        pricingHeaderSelectedSubject.subscribe {
            sortFaqWebView.loadUrl()
            show(ResultsSortFaqWebView())
        }
    }

    protected fun hideMapLoadingOverlay() {
        if (loadingOverlay != null && loadingOverlay?.visibility == View.VISIBLE) {
            loadingOverlay?.animate(false)
            loadingOverlay?.visibility = View.GONE
        }
    }

    fun clearMarkers(setUpMap: Boolean = true) {
        mapItems.clear()
        clusterManager.clearItems()
        clusterMarkers()
        mapCarouselContainer.visibility = View.INVISIBLE
        if (setUpMap) setUpMap()
    }

    fun createMarkers() {
        clearMarkers()
        if (hotels.isEmpty()) {
            return
        }
        //createHotelMarkerIcon should run in a separate thread since its heavy and hangs on the UI thread
        hotels.forEach { hotel ->
            val mapItem = MapItem(context, LatLng(hotel.latitude, hotel.longitude), hotel, hotelIconFactory)
            mapItems.add(mapItem)
            clusterManager.addItem(mapItem)
        }
        clusterMarkers()
    }

    fun updateCarouselItems() {
        val selectedHotels = mapItems.filter { it.isSelected }.map { it.hotel }
        var hotelItems = mapItems.filter { !it.isClustered }.map { it.hotel }
        if (!selectedHotels.isEmpty()) {
            val hotel = selectedHotels.first()
            val hotelLocation = Location("selected")
            hotelLocation.latitude = hotel.latitude
            hotelLocation.longitude = hotel.longitude
            hotelItems = sortByLocation(hotelLocation, hotelItems)
            (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setItems(hotelItems)
            mapCarouselRecycler.scrollToPosition(0)
        }
    }

    fun sortByLocation(location: Location, hotels: List<Hotel>): List<Hotel> {
        val hotelLocation = Location("other")
        val sortedHotels = hotels.sortedBy { h ->
            hotelLocation.latitude = h.latitude
            hotelLocation.longitude = h.longitude
            location.distanceTo(hotelLocation)
        }
        return sortedHotels
    }

    fun clusterMarkers() {
        clusterManager.cluster()
    }

    fun updateMarkers() {
        (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setItems(hotels)
        if (!ExpediaBookingApp.isDeviceShitty() || Strings.equals(currentState, ResultsMap::class.java.name)) {
            googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            createMarkers()
        } else {
            googleMap?.mapType = GoogleMap.MAP_TYPE_NONE
            clearMarkers()
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

        animateMapCarouselOut()
        val screen = Ui.getScreenSize(context)
        val lp = mapCarouselRecycler.layoutParams
        lp.width = screen.x

        recyclerView.addOnScrollListener(scrollListener)
        recyclerView.addOnItemTouchListener(touchListener)

        mapCarouselRecycler.mapSubject.subscribe { hotel ->
            val markersForHotel = mapItems.filter { it.hotel.hotelId == hotel.hotelId }
            if (markersForHotel.isNotEmpty()) {
                val marker = markersForHotel.first()
                mapViewModel.carouselSwipedObservable.onNext(marker)
                trackCarouselScroll()
            }
        }

        if (getLineOfBusiness() == LineOfBusiness.HOTELS) {
            toolbar.inflateMenu(R.menu.menu_search_item)
        }

        toolbar.inflateMenu(R.menu.menu_filter_item)

        filterMenuItem.isVisible = getLineOfBusiness() == LineOfBusiness.PACKAGES

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
                    showWithTracking(ResultsMap())
                    trackSearchMap()
                } else {
                    show(ResultsList(), Presenter.FLAG_CLEAR_BACKSTACK)
                    trackMapToList()
                }
            }
        }

        inflateAndSetupToolbarMenu()

        filterView.viewModel.filterCountObservable.map { it.toString() }.subscribeText(filterCountText)
        filterView.viewModel.filterCountObservable.map { it > 0 }.subscribeVisibility(filterCountText)
        filterView.viewModel.filterCountObservable.map { it > 0 }.subscribeInverseVisibility(filterPlaceholderImageView)
    }

    private fun inflateAndSetupToolbarMenu() {
        val toolbarFilterItemActionView = LayoutInflater.from(context).inflate(R.layout.toolbar_filter_item, null) as LinearLayout
        filterCountText = toolbarFilterItemActionView.findViewById(R.id.filter_count_text) as TextView
        filterPlaceholderImageView = toolbarFilterItemActionView.findViewById(R.id.filter_placeholder_icon) as ImageView
        filterBtn = toolbarFilterItemActionView.findViewById(R.id.filter_btn) as LinearLayout
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
        filterButtonText = filterMenuItem.actionView.findViewById(R.id.filter_text) as TextView
        filterButtonText.visibility = GONE
    }

    fun showDefault() {
        show(ResultsList())
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        MapsInitializer.initialize(context)
        this.googleMap = googleMap
        setUpMap()
        if (havePermissionToAccessLocation(context)) {
            googleMap?.isMyLocationEnabled = true
        }
        val uiSettings = googleMap?.uiSettings
        //Explicitly disallow map-cluttering ui (but keep the gestures)
        if (uiSettings != null) {
            uiSettings.isCompassEnabled = false
            uiSettings.isMapToolbarEnabled = false
            uiSettings.isZoomControlsEnabled = false
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isIndoorLevelPickerEnabled = false
        }

        googleMap?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            override fun getInfoWindow(marker: Marker): View? {
                val activity = context as AppCompatActivity
                val v = activity.layoutInflater.inflate(R.layout.marker_window, null)
                return v
            }

            override fun getInfoContents(marker: Marker): View? {
                return null
            }
        })
        mapView.viewTreeObserver.addOnGlobalLayoutListener(mapViewLayoutReadyListener)
    }

    @CallSuper
    open fun showLoading() {
        adapter.showLoading()
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(adapterListener)
    }

    private val mapViewLayoutReadyListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            isMapReady = true
            mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            mapViewModel.mapInitializedObservable.onNext(Unit)
            mapViewModel.createMarkersObservable.onNext(Unit)
        }
    }

    private fun setUpMap() {
        clusterManager = ClusterManager(context, googleMap)
        clusterManager.setAlgorithm(HotelMapClusterAlgorithm())
        hotelMapClusterRenderer = HotelMapClusterRenderer(context, googleMap, clusterManager, mapViewModel.clusterChangeSubject)
        clusterManager.setRenderer(hotelMapClusterRenderer)
        var currentZoom = -1f

        googleMap?.setOnCameraChangeListener() { position ->
            synchronized(currentZoom) {
                if (Math.abs(currentZoom) != Math.abs(position.zoom)) {
                    val selectedHotels = mapItems.filter { it.isSelected }.map { it.hotel }
                    (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setItems(selectedHotels)
                    clusterMarkers()
                    currentZoom = position.zoom
                }
                if (Strings.equals(currentState, ResultsMap::class.java.name)) {
                    showSearchThisArea()
                }
            }
        }
        googleMap?.setOnMarkerClickListener(clusterManager)

        clusterManager.setOnClusterItemClickListener {
            trackMapPinTap()
            selectMarker(it)
            updateCarouselItems()
            true
        }

        clusterManager.setOnClusterClickListener {
            animateMapCarouselOut()
            clearPreviousMarker()
            val builder = LatLngBounds.builder()
            it.items.forEach { item ->
                builder.include(item.pos)
            }
            val bounds = builder.build()

            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, resources.displayMetrics.density.toInt() * 50), object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                }

                override fun onCancel() {
                }
            })
            true
        }
    }

    private fun clearPreviousMarker() {
        val prevMapItem = mapViewModel.mapPinSelectSubject.value
        if (prevMapItem != null) {
            prevMapItem.isSelected = false
            if (!prevMapItem.hotel.isSoldOut) {
                hotelMapClusterRenderer.getMarker(prevMapItem)?.setIcon(prevMapItem.getHotelMarkerIcon())
            }
        }
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
                        val firstItemTop = firstItem?.itemView?.top ?: 0
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
            if (shouldBlockTransition() || getCurrentState()?.equals(ResultsMap::class.java.name) ?: false) {
                return
            }

            if (isHeaderVisible()) {
                val y = mapView.translationY + (-dy * mapListSplitAnchor / (recyclerView.height - mapListSplitAnchor))
                mapView.translationY = y

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
            mapView.translationY = -mapListSplitAnchor.toFloat()

            recyclerView.visibility = View.VISIBLE
            mapCarouselContainer.visibility = View.INVISIBLE

            if (recyclerView.visibility == View.INVISIBLE) {
                fab.visibility = View.VISIBLE
                getFabAnimIn().start()
            } else {
                fab.visibility = View.INVISIBLE
            }
            filterView.visibility = View.GONE

            sortFaqWebView.visibility = View.GONE

            postDelayed({ AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar) }, 50L)
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
                firstTransition = carouselTransition
                secondTransition = listTransition
                hideSearchThisArea()
            } else {
                firstStepTransitionTime = .66f
                firstTransition = listTransition
                secondTransition = carouselTransition
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
        }
    }

    private val carouselTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsList::class.java, DecelerateInterpolator(2f), 750 / 3) {
        override fun startTransition(forward: Boolean) {
            transitionRunning = true
            mapCarouselTransition = HorizontalTranslateTransition(mapCarouselContainer, 0, screenWidth.toInt())
            if (mapItems.filter { it.isSelected }.isEmpty()) {
                mapCarouselContainer.visibility = View.INVISIBLE
            } else {
                mapCarouselContainer.visibility = View.VISIBLE
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

        override fun startTransition(forwardToList: Boolean) {
            super.startTransition(forwardToList)
            transitionRunning = true
            //Map pin will always be selected for non-clustering behavior eventually
            val isMapPinSelected = mapItems.filter { it.isSelected }.isNotEmpty()

            recyclerView.visibility = View.VISIBLE
            fabShouldVisiblyMove = if (forwardToList) !fabShouldBeHiddenOnList() else (fab.visibility == View.VISIBLE)
            initialListTranslation = if (recyclerView.layoutManager.findFirstVisibleItemPosition() == 0) recyclerView.getChildAt(1)?.top ?: 0 else 0
            if (forwardToList) {
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
                googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                mapTranslationStart = mapView.translationY
                if (fabShouldVisiblyMove) {
                    (fab.drawable as? TransitionDrawable)?.startTransition(duration)
                } else {
                    //Since we're not moving it manually, let's jump it to where it belongs,
                    // and let's get it showing the right thing
                    if (isMapPinSelected) {
                        fab.translationY = -fabHeightOffset()
                    } else {
                        fab.translationY = 0f
                    }
                    (fab.drawable as? TransitionDrawable)?.startTransition(0)
                    fab.visibility = View.VISIBLE
                    getFabAnimIn().start()
                }
            }
            startingFabTranslation = fab.translationY
            if (forwardToList) {
                finalFabTranslation = -filterHeight
            } else {
                finalFabTranslation = if (isMapPinSelected) -fabHeightOffset() else 0f
            }
            hideBundlePriceOverview(!forwardToList)
            updateFilterButtonText(forwardToList)
            showSearchMenu.onNext(forwardToList)
            showMenuItem(forwardToList)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val hotelListDistance = if (forward) (screenHeight * (1 - f)) else ((screenHeight - initialListTranslation) * f)
            recyclerView.translationY = hotelListDistance
            navIcon.parameter = if (forward) Math.abs(1 - f) else f
            if (forward) {
                mapView.translationY = f * -mapListSplitAnchor
            } else {
                mapView.translationY = (1 - f) * mapTranslationStart
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

            mapCarouselContainer.visibility = if (forward) {
                View.INVISIBLE
            } else {
                if (mapItems.filter { it.isSelected }.isEmpty()) {
                    animateMapCarouselOut()
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
            }

            if (forward) {
                if (!fabShouldVisiblyMove) {
                    fab.translationY = 0f
                    (fab.drawable as? TransitionDrawable)?.reverseTransition(0)
                    fab.visibility = View.INVISIBLE
                }
                recyclerView.translationY = 0f
                mapView.translationY = -mapListSplitAnchor.toFloat()
                sortFilterButtonTransition?.jumpToOrigin()

                adjustGoogleMapLogo()
                if (ExpediaBookingApp.isDeviceShitty()) {
                    lazyLoadMapAndMarkers()
                }
            } else {
                mapView.translationY = 0f
                recyclerView.translationY = screenHeight.toFloat()
                sortFilterButtonTransition?.jumpToTarget()

                googleMap?.setPadding(0, toolbar.height, 0, fabHeightOffset().toInt())
                if (ExpediaBookingApp.isDeviceShitty()) {
                    googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                    createMarkers()
                }
            }
            fab.translationY = finalFabTranslation
        }
    }

    fun lazyLoadMapAndMarkers() {
        googleMap?.mapType = GoogleMap.MAP_TYPE_NONE
        clearMarkers()
    }

    private val listFilterTransition = object : Presenter.Transition(ResultsList::class.java, ResultsFilter::class.java, DecelerateInterpolator(2f), ANIMATION_DURATION_FILTER) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            transitionRunning = true
            filterView.show()
            filterScreenShown = forward
            if (forward) {
                sortFilterButtonTransition?.jumpToTarget()
                fab.visibility = View.GONE
            } else {
                recyclerView.visibility = View.VISIBLE
                toolbar.visibility = View.VISIBLE
                mapView.visibility = View.VISIBLE
            }
            hideBundlePriceOverview(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            val translatePercentage = if (forward) 1 - f else f
            filterView.translationY = filterView.height * translatePercentage
            if (!forward) {
                sortFilterButtonTransition?.toOrigin(f)
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            transitionRunning = false
            if (!forward) filterView.visibility = View.GONE
            filterView.translationY = (if (forward) 0 else filterView.height).toFloat()
            if (!forward) sortFilterButtonTransition?.jumpToOrigin()
            if (!forward && !fabShouldBeHiddenOnList()) {
                fab.visibility = View.VISIBLE
                getFabAnimIn().start()
            }
            if (forward) {
                recyclerView.visibility = View.GONE
                toolbar.visibility = View.GONE
                mapView.visibility = View.GONE
            }
        }
    }

    private val mapFilterTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsFilter::class.java, DecelerateInterpolator(2f), ANIMATION_DURATION_FILTER) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
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
            if (!forward) filterView.visibility = View.GONE
            filterView.translationY = (if (forward) 0 else filterView.height).toFloat()
            if (!forward) {
                fab.visibility = View.VISIBLE
                searchThisArea?.visibility = View.VISIBLE
            }
        }
    }

    private val sortFaqWebViewTransition = object : ScaleTransition(this, recyclerView, sortFaqWebView, ResultsList::class.java, ResultsSortFaqWebView::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            transitionRunning = true
            if (forward) {
                fab.visibility = View.GONE
            } else {
                toolbar.visibility = View.VISIBLE
                mapView.visibility = View.VISIBLE
            }
            hideBundlePriceOverview(forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            transitionRunning = false
            if (forward) {
                toolbar.visibility = View.GONE
                mapView.visibility = View.GONE
            } else {
                if (!fabShouldBeHiddenOnList()) {
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
        var factor = if (forward) f else Math.abs(1 - f)
        navIcon.parameter = factor
    }

    fun animationFinalize(forward: Boolean, isSearchToResultsTransition: Boolean = false) {
        recyclerTempBackground.visibility = View.GONE
        navIcon.parameter = ArrowXDrawableUtil.ArrowDrawableType.BACK.type.toFloat()
        if (havePermissionToAccessLocation(context)) {
            googleMap?.isMyLocationEnabled = forward
        }
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

    fun updateFilterButtonText(isResults: Boolean) {
        if (isResults) {
            filterButtonText.visibility = GONE
        } else {
            filterButtonText.visibility = VISIBLE
        }
    }

    open fun hideBundlePriceOverview(hide: Boolean) {
        //
    }

    fun showMenuItem(isResults: Boolean) {
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

    fun setMapToInitialState(suggestion: SuggestionV4?) {
        if (isMapReady) {
            if (suggestion != null) {
                if (suggestion.coordinates != null &&
                        suggestion.coordinates?.lat != 0.0 &&
                        suggestion.coordinates?.lng != 0.0) {
                    moveCameraToLatLng(LatLng(suggestion.coordinates.lat,
                            suggestion.coordinates.lng))
                } else if (suggestion.regionNames?.fullName != null) {
                    val BD_KEY = "geo_search"
                    val bd = BackgroundDownloader.getInstance()
                    bd.cancelDownload(BD_KEY)
                    bd.startDownload(BD_KEY, mGeocodeDownload(suggestion.regionNames.fullName), geoCallback())
                }
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
                    moveCameraToLatLng(LatLng(results[0].latitude, results[0].longitude))
                }
            }
        }
    }

    private fun moveCameraToLatLng(latLng: LatLng) {
        val cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(8f)
                .build()
        googleMap?.setPadding(0, 0, 0, 0)
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun fabHeightOffset(): Float {
        val offset = context.resources.getDimension(R.dimen.hotel_carousel_fab_vertical_offset)
        return mapCarouselContainer.height.toFloat() - offset
    }

    abstract fun inflate()
    abstract fun inflateFilterView(viewStub: ViewStub) : BaseHotelFilterView
    abstract fun doAreaSearch()
    abstract fun hideSearchThisArea()
    abstract fun showSearchThisArea()
    abstract fun createFilterViewModel(): BaseHotelFilterViewModel
    abstract fun trackSearchMap()
    abstract fun trackMapToList()
    abstract fun trackCarouselScroll()
    abstract fun trackMapPinTap()
    abstract fun trackFilterShown()
    abstract fun trackMapSearchAreaClick()
    abstract fun getLineOfBusiness(): LineOfBusiness
    abstract fun getHotelListAdapter(): BaseHotelListAdapter
}
