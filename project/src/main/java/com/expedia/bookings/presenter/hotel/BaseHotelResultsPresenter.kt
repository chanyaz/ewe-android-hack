package com.expedia.bookings.presenter.hotel

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.TransitionDrawable
import android.location.Address
import android.location.Location
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.bitmaps.PicassoScrollListener
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.HotelMapClusterAlgorithm
import com.expedia.bookings.utils.HotelMapClusterRenderer
import com.expedia.bookings.utils.MapItem
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
import com.expedia.util.havePermissionToAccessLocation
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelFilterViewModel
import com.expedia.vm.HotelResultsMapViewModel
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
import com.google.maps.android.ui.IconGenerator
import com.mobiata.android.BackgroundDownloader
import com.mobiata.android.LocationServices
import org.joda.time.DateTime
import rx.Observer
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

public abstract class BaseHotelResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), OnMapReadyCallback {

    //Views
    val recyclerView: HotelListRecyclerView by bindView(R.id.list_view)
    var mapView: MapView by Delegates.notNull()
    open val loadingOverlay: MapLoadingOverlayWidget? = null
    val filterView: HotelFilterView by bindView(R.id.filter_view)
    val toolbar: Toolbar by bindView(R.id.hotel_results_toolbar)
    val toolbarTitle by lazy { toolbar.getChildAt(2) }
    val toolbarSubtitle by lazy { toolbar.getChildAt(3) }
    val recyclerTempBackground: View by bindView(R.id.recycler_view_temp_background)
    val mapCarouselContainer: ViewGroup by bindView(R.id.hotel_carousel_container)
    val mapCarouselRecycler: HotelCarouselRecycler by bindView(R.id.hotel_carousel)
    val fab: FloatingActionButton by bindView(R.id.fab)
    var adapter: HotelListAdapter by Delegates.notNull()
    open val filterBtnWithCountWidget: FilterButtonWithCountWidget? = null
    open val searchThisArea: Button? = null
    var isMapReady = false
    val isBucketedForResultMap = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelResultMapTest)
    val isUserBucketedSearchScreenTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelsSearchScreenTest)

    var clusterManager: ClusterManager<MapItem> by Delegates.notNull()

    private val PICASSO_TAG = "HOTEL_RESULTS_LIST"
    val DEFAULT_UI_ELEMENT_APPEAR_ANIM_DURATION = 200L

    val filterHeight by lazy { resources.getDimension(R.dimen.hotel_filter_height) }
    var screenHeight: Int = 0
    var screenWidth: Float = 0f
    var mapTransitionRunning: Boolean = false
    var hideFabAnimationRunning: Boolean = false

    var previousWasList: Boolean = true

    var navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)

    val hotelSelectedSubject = PublishSubject.create<Hotel>()
    val headerClickedSubject = PublishSubject.create<Unit>()
    val hideBundlePriceOverviewSubject = PublishSubject.create<Boolean>()

    var googleMap: GoogleMap? = null

    open val filterMenuItem by lazy { toolbar.menu.findItem(R.id.menu_filter) }
    val searchMenuItem by lazy { toolbar.menu.findItem(R.id.menu_open_search) }

    var filterCountText: TextView by Delegates.notNull()
    var filterPlaceholderImageView: ImageView by Delegates.notNull()
    val filterPlaceholderIcon by lazy {
        val sortDrawable = ContextCompat.getDrawable(context, R.drawable.sort).mutate()
        sortDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        sortDrawable
    }

    val searchOverlaySubject = PublishSubject.create<Unit>()

    var hotelMapClusterRenderer: HotelMapClusterRenderer by Delegates.notNull()

    var halfway = 0
    var threshold = 0

    val iconFactory = IconGenerator(context)
    var mapItems = arrayListOf<MapItem>()
    private val ANIMATION_DURATION_FILTER = 500
    var hotels = emptyList<Hotel>()

    var mapViewModel: HotelResultsMapViewModel by notNullAndObservable { vm ->

        vm.sortedHotelsObservable.subscribe {
            hotels = it
            updateMarkers()
        }

        vm.soldOutHotel.subscribe { hotel->
            mapItems.filter { it.hotel.hotelId == hotel.hotelId }.first().hotel.isSoldOut = true
            clusterMarkers()
        }

        vm.carouselSwipedObservable.subscribe {
            selectMarker(it, true)
        }
    }

    private fun selectMarker(mapItem: MapItem, shouldZoom: Boolean = false) {
        //To prevent caching, we have to clear items and add again. TODO- Try with a custom decorator which doesnt cache anything at any zoom level.
        clusterManager.clearItems()
        clusterManager.addItems(mapItems)
        clearPreviousMarker()
        mapItem.isSelected = true
        val selectedMarker = hotelMapClusterRenderer.getMarker(mapItem)
        selectedMarker?.setIcon(mapItem.selectedIcon)
        selectedMarker?.showInfoWindow()
        if (shouldZoom) {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(mapItem.position, googleMap?.cameraPosition?.zoom!!))
        }
        mapViewModel.mapPinSelectSubject.onNext(mapItem)
        animateMapCarouselVisibility(true)
    }

    private fun animateMapCarouselVisibility(visible: Boolean) {
        val mapCarouselHeight = mapCarouselContainer.height.toFloat()
        if (visible) {
            if (mapCarouselContainer.visibility != View.VISIBLE) {
                AnimUtils.slideUp(mapCarouselContainer)
                mapCarouselContainer.visibility = View.VISIBLE
            }
            fab.animate().translationY(filterHeight - mapCarouselHeight).setInterpolator(DecelerateInterpolator())?.start()
            searchThisArea?.animate()?.translationY(0f)?.setInterpolator(DecelerateInterpolator())?.start()
        } else {
            if (mapCarouselContainer.visibility != View.INVISIBLE) {
                AnimUtils.slideDown(mapCarouselContainer)
                mapCarouselContainer.visibility = View.INVISIBLE
            }
            fab.animate().translationY(filterHeight).setInterpolator(DecelerateInterpolator())?.start()
            searchThisArea?.animate()?.translationY(mapCarouselHeight)?.setInterpolator(DecelerateInterpolator())?.start()
        }
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
        loadingOverlay?.animate(false)
        loadingOverlay?.visibility = View.GONE
        adapter.resultsSubject.onNext(Pair(it.hotelList, it.userPriceType))

        // show fab button always in case of AB test or shitty device
        if (ExpediaBookingApp.isDeviceShitty() || isBucketedForResultMap) {
            fab.visibility = View.VISIBLE
            getFabAnimIn().start()
        }
        if ((ExpediaBookingApp.isDeviceShitty() || isBucketedForResultMap) && it.hotelList.size <= 3) {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context, R.color.hotel_result_background))
        } else {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }
        filterBtnWithCountWidget?.visibility = View.VISIBLE
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
        adapter.resultsSubject.onNext(Pair(response.hotelList, response.userPriceType))
        mapViewModel.hotelResultsSubject.onNext(response)
        if ((ExpediaBookingApp.isDeviceShitty() || isBucketedForResultMap) && response.hotelList.size <= 3 && previousWasList) {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context, R.color.hotel_result_background))
        } else {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }
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
        inflate()
        mapViewModel = HotelResultsMapViewModel(context, lastBestLocationSafe())
        mapViewModel.clusterChangeSubject.subscribe {
            updateCarouselItems()
        }
        headerClickedSubject.subscribe(mapSelectedObserver)
        adapter = HotelListAdapter(hotelSelectedSubject, headerClickedSubject)
        recyclerView.adapter = adapter
        filterView.viewmodel = HotelFilterViewModel()
        filterView.viewmodel.filterObservable.subscribe(filterObserver)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.setTitleTextAppearance(getContext(), R.style.ToolbarTitleTextAppearance)
        toolbar.setSubtitleTextAppearance(getContext(), R.style.ToolbarSubtitleTextAppearance)

        mapCarouselRecycler.adapter = HotelMapCarouselAdapter(emptyList(), hotelSelectedSubject)
        mapCarouselRecycler.addOnScrollListener(PicassoScrollListener(context, PICASSO_TAG))

        mapViewModel.newBoundsObservable.subscribe {
            if(isMapReady) {
                val center = it.center
                val latLng = LatLng(center.latitude, center.longitude)
                mapViewModel.mapBoundsSubject.onNext(latLng)
                val padding = 60
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(it, resources.displayMetrics.density.toInt() * padding), object : GoogleMap.CancelableCallback {
                    override fun onFinish() {

                    }

                    override fun onCancel() {
                    }
                })
            }
        }

    }

    fun clearMarkers() {
        mapItems.clear()
        clusterManager.clearItems()
        clusterMarkers()
        if (mapViewModel.isClusteringEnabled) mapCarouselContainer.visibility = View.INVISIBLE
        setUpMap()
    }

    fun createMarkers() {
        clearMarkers()
        if (hotels.isEmpty()) {
            return
        }
        //createHotelMarkerIcon should run in a separate thread since its heavy and hangs on the UI thread
        hotels.forEach {
            hotel ->
            val bitmap = createHotelMarkerIcon(context, iconFactory, hotel, false, hotel.lowRateInfo.isShowAirAttached(), hotel.isSoldOut)
            val selectedBitmap = createHotelMarkerIcon(context, iconFactory, hotel, true, hotel.lowRateInfo.isShowAirAttached(), hotel.isSoldOut)
            val mapItem = MapItem(LatLng(hotel.latitude, hotel.longitude), hotel.hotelId, bitmap, selectedBitmap, hotel.lowRateInfo, hotel)
            mapItems.add(mapItem)
            clusterManager.addItem(mapItem)
        }
        clusterMarkers()
        if (!mapViewModel.isClusteringEnabled) {
            selectMarker(mapItems.first())
        }

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
            var lp = recyclerView.layoutParams as FrameLayout.LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
        }

        addDefaultTransition(defaultTransition)
        addTransition(fabTransition)
        addTransition(listFilterTransition)
        addTransition(mapFilterTransition)

        animateMapCarouselVisibility(false)
        val screen = Ui.getScreenSize(context)
        var lp = mapCarouselRecycler.layoutParams
        lp.width = screen.x

        recyclerView.addOnScrollListener(scrollListener)
        recyclerView.addOnItemTouchListener(touchListener)

        mapCarouselRecycler.mapSubject.subscribe { hotel ->
            val markersForHotel = mapItems.filter { it.title == hotel.hotelId }
            if (markersForHotel.isNotEmpty()) {
                val marker = markersForHotel.first()
                mapViewModel.carouselSwipedObservable.onNext(marker)
                HotelV2Tracking().trackHotelV2CarouselScroll()
            }
        }

        toolbar.inflateMenu(R.menu.menu_filter_item)

        toolbar.setNavigationOnClickListener { view ->
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }

        filterView.viewmodel.filterCountObservable.subscribe(filterCountObserver)

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

        filterBtnWithCountWidget?.setOnClickListener {
            show(ResultsFilter())
            filterView.viewmodel.sortContainerObservable.onNext(true)
            filterView.toolbar.title = resources.getString(R.string.Sort_and_Filter)
        }

        searchMenuItem.setOnMenuItemClickListener({
            searchOverlaySubject.onNext(Unit)
            true
        })

        filterMenuItem.setVisible(false)
        var fabLp = fab.layoutParams as FrameLayout.LayoutParams
        fabLp.bottomMargin += resources.getDimension(R.dimen.hotel_filter_height).toInt()
    }

    public fun showDefault() {
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
                val v = activity.layoutInflater.inflate(R.layout.marker_window, null);
                return v;
            }

            override fun getInfoContents(marker: Marker): View? {
                return null
            }
        })

        mapView.viewTreeObserver.addOnGlobalLayoutListener(mapViewLayoutReadyListener)
    }

    private val mapViewLayoutReadyListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            isMapReady = true
            mapView.viewTreeObserver.removeOnGlobalLayoutListener(this);
            mapViewModel.mapInitializedObservable.onNext(Unit)
            mapViewModel.createMarkersObservable.onNext(Unit)

        }
    }

    private fun setUpMap() {
        clusterManager = ClusterManager(context, googleMap)
        clusterManager.setAlgorithm(HotelMapClusterAlgorithm())
        hotelMapClusterRenderer = HotelMapClusterRenderer(context, googleMap, clusterManager, mapViewModel.isClusteringEnabled, mapViewModel.clusterChangeSubject)
        clusterManager.setRenderer(hotelMapClusterRenderer)
        var currentZoom = -1f

        googleMap?.setOnCameraChangeListener() { position ->
            synchronized(currentZoom) {
                if (mapViewModel.isClusteringEnabled && Math.abs(currentZoom) != Math.abs(position.zoom)) {
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
            HotelV2Tracking().trackHotelV2MapTapPin()
            selectMarker(it)
            updateCarouselItems()
            true
        }

        clusterManager.setOnClusterClickListener {
            animateMapCarouselVisibility(false)
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
            hotelMapClusterRenderer.getMarker(prevMapItem)?.setIcon(prevMapItem.icon)
        }
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
                    filterBtnWithCountWidget?.animate()?.translationY(0f)?.setInterpolator(DecelerateInterpolator())?.start()
                } else if (scrolledDistance > heightOfButton / 2) {
                    filterBtnWithCountWidget?.animate()?.translationY(heightOfButton.toFloat())?.setInterpolator(DecelerateInterpolator())?.start()
                    fab.animate().translationY(heightOfButton.toFloat()).setInterpolator(DecelerateInterpolator()).start()
                } else {
                    filterBtnWithCountWidget?.animate()?.translationY(0f)?.setInterpolator(DecelerateInterpolator())?.start()
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
                        filterBtnWithCountWidget?.translationY = 0f
                        fab.translationY = 0f
                    } else if (scrolledDistance > 0) {
                        filterBtnWithCountWidget?.translationY = Math.min(heightOfButton, scrolledDistance).toFloat()
                        fab.translationY = Math.min(heightOfButton, scrolledDistance).toFloat()
                    } else {
                        filterBtnWithCountWidget?.translationY = Math.min(scrolledDistance, 0).toFloat()
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

    val adapterListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            screenHeight = if (ExpediaBookingApp.isAutomation()) {
                0
            } else {
                height
            }
            screenWidth = if (ExpediaBookingApp.isAutomation()) {
                0f
            } else {
                width.toFloat()
            }

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
                    googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                }
                if (filterBtnWithCountWidget != null) {
                    filterViewGoal = if (forward) 0f else filterBtnWithCountWidget?.height?.toFloat()!!
                }
                recyclerView.visibility = View.VISIBLE
                previousWasList = forward
                fabShouldVisiblyMove = if (forward) !fabShouldBeHiddenOnList() else (fab.visibility == View.VISIBLE)
                initialListTranslation = if (recyclerView.layoutManager.findFirstVisibleItemPosition() == 0) recyclerView.getChildAt(1)?.top ?: 0 else 0
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
                hideBundlePriceOverview(!forward)
                toolbarTitle.translationY = 0f
                toolbarSubtitle.translationY = 0f
                updateFilterButtonText(forward)
                searchMenuItem.setVisible(forward)
                filterMenuItem.setVisible(!forward)
                showMenuItem(forward)
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
                filterBtnWithCountWidget?.translationY = filterViewOrigin + (f * (filterViewGoal - filterViewOrigin))
            }

            override fun finalizeTransition(forward: Boolean) {
                navIcon.parameter = (if (forward) ArrowXDrawableUtil.ArrowDrawableType.BACK else ArrowXDrawableUtil.ArrowDrawableType.CLOSE).type.toFloat()

                //When we are about to show the menu filter item, ensure that it is of sufficient height
                if (filterMenuItem.actionView.height != toolbar.height) {
                    filterMenuItem.actionView.layoutParams.height = toolbar.measuredHeight
                }

                recyclerView.visibility = if (forward) View.VISIBLE else View.INVISIBLE

                mapCarouselContainer.visibility = if (forward) {
                    View.INVISIBLE
                } else {
                    if (mapViewModel.isClusteringEnabled && mapItems.filter { it.isSelected }.isEmpty()) {
                        animateMapCarouselVisibility(false)
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
                    mapView.translationY = -halfway.toFloat()
                    adjustGoogleMapLogo()
                    filterBtnWithCountWidget?.translationY = 0f
                    if (isBucketedForResultMap || ExpediaBookingApp.isDeviceShitty()) {
                        lazyLoadMapAndMarkers()
                    }
                } else {
                    fab.translationY = -(mapCarouselContainer.height.toFloat() - resources.getDimension(R.dimen.hotel_filter_height).toInt())
                    mapView.translationY = 0f
                    recyclerView.translationY = screenHeight.toFloat()
                    googleMap?.setPadding(0, toolbar.height, 0, mapCarouselContainer.height)
                    filterBtnWithCountWidget?.translationY = resources.getDimension(R.dimen.hotel_filter_height)
                    if (isBucketedForResultMap || ExpediaBookingApp.isDeviceShitty()) {
                        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                        createMarkers()
                    }
                }
            }
        }

        private val carouselTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsList::class.java, DecelerateInterpolator(2f), duration / 3) {

            override fun startTransition(forward: Boolean) {
                if (mapViewModel.isClusteringEnabled && mapItems.filter { it.isSelected }.isEmpty()) {
                    mapCarouselContainer.translationX = screenWidth
                    mapCarouselContainer.visibility = View.INVISIBLE
                } else {
                    mapCarouselContainer.visibility = View.VISIBLE
                }
                if (forward) {
                    mapCarouselContainer.translationX = 0f
                    hideSearchThisArea()
                } else {
                    mapCarouselContainer.translationX = screenWidth
                }
                updateFilterButtonText(forward)
                showMenuItem(forward)
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

    fun lazyLoadMapAndMarkers() {
        googleMap?.mapType = GoogleMap.MAP_TYPE_NONE
        clearMarkers()
    }

    private val listFilterTransition = object : Presenter.Transition(ResultsList::class.java, ResultsFilter::class.java, DecelerateInterpolator(2f), ANIMATION_DURATION_FILTER) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            filterView.visibility = View.VISIBLE
            if (forward) {
                fab.visibility = View.GONE
            }
            hideBundlePriceOverview(forward)
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
                searchThisArea?.visibility = View.GONE
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
                searchThisArea?.visibility = View.VISIBLE
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

    val filterCountObserver: Observer<Int> = endlessObserver { filterBtnWithCountWidget?.showNumberOfFilters(it) }

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
        if(!isUserBucketedSearchScreenTest) {
            recyclerView.translationY = factor * yTranslationRecyclerView
        }
        navIcon.parameter = factor
        toolbarTitle.translationY = factor * toolbarTitleTop
        toolbarSubtitle.translationY = factor * toolbarSubtitleTop
    }

    fun animationFinalize(forward: Boolean) {
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

    open fun updateFilterButtonText(forward: Boolean) {
        //
    }

    open fun hideBundlePriceOverview(forward: Boolean) {
        //
    }

    open fun showMenuItem(forward: Boolean) {
        //
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
        var cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(8f)
                .build();
        googleMap?.setPadding(0, 0, 0, 0)
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    abstract fun inflate()

    abstract fun doAreaSearch()

    abstract fun hideSearchThisArea()

    abstract fun showSearchThisArea()
}
