package com.expedia.bookings.presenter.hotel

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.TransitionDrawable
import android.location.Location
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
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
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.bitmaps.PicassoScrollListener
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelCarouselRecycler
import com.expedia.bookings.widget.HotelFilterView
import com.expedia.bookings.widget.HotelListAdapter
import com.expedia.bookings.widget.HotelListRecyclerView
import com.expedia.bookings.widget.HotelMarkerPreviewAdapter
import com.expedia.bookings.widget.createHotelMarker
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelFilterViewModel
import com.expedia.vm.HotelResultsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mobiata.android.LocationServices
import com.mobiata.android.Log
import org.joda.time.DateTime
import rx.Observer
import rx.subjects.PublishSubject
import java.util.ArrayList
import kotlin.properties.Delegates

public class HotelResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), OnMapReadyCallback {

    private val PICASSO_TAG = "HOTEL_RESULTS_LIST"
    private val DEFAULT_FAB_ANIM_DURATION = 200L

    var screenHeight: Int = 0
    var screenWidth: Float = 0f
    var mapTransitionRunning: Boolean = false
    var hideFabAnimationRunning: Boolean = false
    var markerList = ArrayList<Marker>()
    var previousWasList: Boolean = true

    val recyclerView: HotelListRecyclerView by bindView(R.id.list_view)
    val mapView: MapView by bindView(R.id.map_view)
    val filterView: HotelFilterView by bindView(R.id.filter_view)
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val toolbarTitle by lazy { toolbar.getChildAt(2) }
    val toolbarSubtitle by lazy { toolbar.getChildAt(3) }
    val recyclerTempBackground: View by bindView(R.id.recycler_view_temp_background)

    var navIcon: ArrowXDrawable

    val mapCarouselContainer: View by bindView(R.id.hotel_carousel_container)
    val mapCarouselRecycler: HotelCarouselRecycler by bindView(R.id.hotel_carousel)

    var mHotelList = ArrayList<MarkerDistance>()

    val hotelSelectedSubject = PublishSubject.create<Hotel>()
    val headerClickedSubject = PublishSubject.create<Unit>()

    var googleMap: GoogleMap? = null

    var menu: MenuItem? = null

    var halfway = 0
    var threshold = 0

    val fab: FloatingActionButton by bindView(R.id.fab)

    var adapter: HotelListAdapter by Delegates.notNull()
    val filterBtn: Button by bindView(R.id.filter_btn)

    var viewmodel: HotelResultsViewModel by notNullAndObservable { vm ->
        vm.hotelResultsObservable.subscribe(listResultsObserver)
        vm.hotelResultsObservable.subscribe(mapResultsObserver)

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
        }
    }

    public class MarkerDistance(marker: Marker, distance: Float, hotel: Hotel) : Comparable<MarkerDistance> {
        override fun compareTo(other: MarkerDistance): Int {
            return this.distance.compareTo(other.distance)
        }

        val marker: Marker = marker
        var distance: Float = distance
        val hotel: Hotel = hotel
    }

    fun showLoading() {
        adapter.showLoading()
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(adapterListener)
    }

    private fun resetListOffset() {
        val mover = ObjectAnimator.ofFloat(mapView, "translationY", mapView.translationY, -halfway.toFloat());
        mover.setDuration(300);
        mover.start();
        
        var listOffset = (height / 3.1).toInt()
        val view = recyclerView.getChildAt(adapter.numHeaderItemsInHotelsList())
        if (view != null) {
            var distance = view.top - listOffset;
            recyclerView.smoothScrollBy(0, distance)
        } else {
            recyclerView.layoutManager.scrollToPositionWithOffset(adapter.numHeaderItemsInHotelsList(), listOffset)
        }
    }

    private fun adjustGoogleMapLogo() {
        val view = recyclerView.getChildAt(1)
        val topOffset = if (view == null) { 0 } else {view.top
        }
        val bottom = recyclerView.height - topOffset
        googleMap?.setPadding(0, 0, 0, (bottom + mapView.translationY).toInt())
    }

    private fun fabShouldBeHiddenOnList(): Boolean {
        return recyclerView.layoutManager.findFirstVisibleItemPosition() == 0
    }

    private fun shouldBlockTransition(): Boolean {
        return (mapTransitionRunning || (recyclerView.adapter as HotelListAdapter).isLoading())
    }

    val listResultsObserver = endlessObserver<HotelSearchResponse> {
        adapter.resultsSubject.onNext(it)
        resetListOffset()
    }

    /**
     * Alternative to map.clear() to avoid undesired exceptions
     */
    public fun clearAllMapMarkers() {
        markerList.clear()
    }

    val mapResultsObserver: Observer<HotelSearchResponse> = endlessObserver { response ->
        val map = googleMap
        if (map != null) {
            clearAllMapMarkers()
            map.isMyLocationEnabled = true
            renderMarkers(map, response, false)
        }
        Log.d("Hotel Results Next")
    }

    fun renderMarkers(map: GoogleMap, response: HotelSearchResponse, isFilter: Boolean) {
        if (!isFilter) {
            filterView.viewmodel.setHotelList(response)
        }

        map.clear()
        mHotelList.clear()

        map.isMyLocationEnabled = true

        // Determine current location
        val minTime = DateTime.now().millis - DateUtils.HOUR_IN_MILLIS
        val loc = LocationServices.getLastBestLocation(context, minTime)
        val currentLat = loc?.latitude ?: 0.0
        val currentLong = loc?.longitude ?: 0.0

        var closestHotel: Hotel? = null
        var closestToCurrentLocationVal = Float.MAX_VALUE

        val allHotelsBox = LatLngBounds.Builder()

        for (hotel in response.hotelList) {
            // Add markers for all hotels
            val marker: Marker = map.addMarker(MarkerOptions()
                    .position(LatLng(hotel.latitude, hotel.longitude))
                    .icon(createHotelMarker(getResources(), hotel, false)))
            markerList.add(marker)

            val markerDistance = MarkerDistance(marker, -1f, hotel)
            mHotelList.add(markerDistance)

            allHotelsBox.include(LatLng(hotel.latitude, hotel.longitude))

            // Determine which neighbourhood is closest to current location
            if (hotel.locationId != null) {
                var a = Location("a")
                a.setLatitude(currentLat)
                a.setLongitude(currentLong)

                var b = Location("b")
                b.setLatitude(hotel.latitude)
                b.setLongitude(hotel.longitude)

                var distanceBetween = a.distanceTo(b)

                if (distanceBetween <= closestToCurrentLocationVal) {
                    closestToCurrentLocationVal = distanceBetween
                    closestHotel = hotel
                }
            }

        }

        if (mHotelList.size() > 0) {
            var mostInterestingNeighborhood: HotelSearchResponse.Neighborhood?

            var anyPointsIncludedInAllHotelsBox = (response.hotelList.size() > 0)
            if (anyPointsIncludedInAllHotelsBox && allHotelsBox.build().contains(LatLng(currentLat, currentLong)) && closestHotel != null && closestHotel.locationId != null) {
                mostInterestingNeighborhood = response.neighborhoodsMap.get(closestHotel.locationId)
            } else {
                mostInterestingNeighborhood = response.allNeighborhoodsInSearchRegion.reduce { left, right -> if (left.score >= right.score) left else right }
            }

            if (mostInterestingNeighborhood != null && mostInterestingNeighborhood.hotels.size() > 0) {
                val neighborhoodBox = LatLngBounds.Builder()
                for (hotel in mostInterestingNeighborhood.hotels) {
                    neighborhoodBox.include(LatLng(hotel.latitude, hotel.longitude))
                }
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(neighborhoodBox.build(), getResources().getDisplayMetrics().density.toInt() * 50))
            } else {
                //in case we don't get any neighborhood from response
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(allHotelsBox.build(), getResources().getDisplayMetrics().density.toInt() * 50))
            }

            var closestMarker: MarkerDistance? = null
            if (!mHotelList.isEmpty()) {
                closestMarker = mHotelList.get(0)
            }

            for (item in mHotelList) {
                var a = Location("a")
                a.latitude = map.cameraPosition.target.latitude
                a.longitude = map.cameraPosition.target.longitude

                var b = Location("b")
                b.latitude = item.hotel.latitude
                b.longitude = item.hotel.longitude

                var distanceBetween = a.distanceTo(b)

                if (distanceBetween <= closestToCurrentLocationVal) {
                    closestToCurrentLocationVal = distanceBetween
                    closestMarker = item
                }
            }

            if (closestMarker != null) {
                closestMarker.marker.showInfoWindow()
                closestMarker.marker.setIcon(createHotelMarker(resources, closestMarker.hotel, true))
                mapCarouselRecycler.addOnScrollListener(PicassoScrollListener(context, PICASSO_TAG))
                mapCarouselRecycler.adapter = HotelMarkerPreviewAdapter(mHotelList, closestMarker.marker, hotelSelectedSubject)
            }
        }
    }

    val mapSelectedObserver: Observer<Unit> = endlessObserver {
        if (!shouldBlockTransition()) {
            show(ResultsMap())
        }
    }

    val filterObserver: Observer<List<Hotel>> = endlessObserver {
        if (filterView.viewmodel.filteredResponse.hotelList != null && filterView.viewmodel.filteredResponse.hotelList.size() > 0) {
            adapter.resultsSubject.onNext(filterView.viewmodel.filteredResponse)
            if (previousWasList) {
                show(ResultsList())
            } else {
                show(ResultsMap())
            }
            val map = googleMap
            val response = filterView.viewmodel.filteredResponse
            if (map != null) {
                renderMarkers(map, response, true)
            }
        } else if (it == filterView.viewmodel.originalResponse?.hotelList) {
            adapter.resultsSubject.onNext(filterView.viewmodel.originalResponse!!)
            if (previousWasList) {
                show(ResultsList())
            } else {
                show(ResultsMap())
            }
        }
    }

    init {
        View.inflate(getContext(), R.layout.widget_hotel_results, this)

        headerClickedSubject.subscribe(mapSelectedObserver)
        adapter = HotelListAdapter(hotelSelectedSubject, headerClickedSubject)
        recyclerView.adapter = adapter
        filterView.subscribe(filterObserver)
        filterView.viewmodel = HotelFilterViewModel(getContext())
        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.setBackgroundColor(resources.getColor(R.color.hotels_primary_color))
        toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
        toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance)
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
        mapView.getMapAsync(this)

        mapCarouselContainer.visibility = View.INVISIBLE
        val screen = Ui.getScreenSize(context)
        var lp = mapCarouselRecycler.layoutParams
        lp.width = screen.x

        recyclerView.addOnScrollListener(scrollListener)
        recyclerView.addOnItemTouchListener(touchListener)
        mapCarouselRecycler.mapSubject.subscribe(markerObserver)

        toolbar.inflateMenu(R.menu.menu_filter_item)
        menu = toolbar.menu.findItem(R.id.menu_filter)
        var drawable = resources.getDrawable(R.drawable.sort).mutate()
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        menu?.setIcon(drawable)

        val button = LayoutInflater.from(context).inflate(R.layout.toolbar_filter_item, null) as Button
        val icon = resources.getDrawable(R.drawable.sort).mutate()
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        button.setTextColor(resources.getColor(android.R.color.white))

        toolbar.menu.findItem(R.id.menu_filter).setActionView(button)

        toolbar.setNavigationOnClickListener { view ->
            val activity = context as AppCompatActivity
            activity.onBackPressed()
            show(ResultsList())
        }

        val fabDrawable: TransitionDrawable? = (fab.drawable as? TransitionDrawable)
        // Enabling crossfade prevents the icon ending up with a weird mishmash of both icons.
        fabDrawable?.isCrossFadeEnabled = true

        fab.setOnClickListener { view ->
            if (recyclerView.visibility == View.VISIBLE) {
                show(ResultsMap())
            } else {
                show(ResultsList())
            }

        }


        show(ResultsList())

        filterBtn.setOnClickListener { view ->
            if (filterView.visibility != View.VISIBLE) {
                filterView.visibility = View.VISIBLE
                show(ResultsFilter())
            }
        }

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
                // Reset markers
                var markerHotel: Hotel? = null
                for (item in mHotelList) {
                    item.marker.setIcon(createHotelMarker(resources, item.hotel, false))

                    if (marker == item.marker) {
                        markerHotel = item.hotel
                    }
                }

                if (markerHotel != null) {
                    marker.setIcon(createHotelMarker(resources, markerHotel, true))
                }

                marker.showInfoWindow()

                mapCarouselContainer.visibility = View.VISIBLE

                mapCarouselRecycler.addOnScrollListener(PicassoScrollListener(context, PICASSO_TAG))

                mapCarouselRecycler.adapter = HotelMarkerPreviewAdapter(mHotelList, marker, hotelSelectedSubject)

                return true
            }
        })
    }

    val scrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        var currentState = RecyclerView.SCROLL_STATE_IDLE
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            currentState = newState

            val view = recyclerView.getChildAt(1)
            val topOffset = if (view == null) {
                0
            } else {
                view.top
            }

            if (halfway == 0 && threshold == 0 && view != null) {
                halfway = view.top
                threshold = view.top + (view.bottom / 1.9).toInt()
            }

            if (newState == RecyclerView.SCROLL_STATE_IDLE && ((topOffset >= threshold && isHeaderVisible()) || isHeaderCompletelyVisible())) {
                //view has passed threshold, show map
                show(ResultsMap())
            } else if (newState == RecyclerView.SCROLL_STATE_IDLE && topOffset < threshold && topOffset > halfway && isHeaderVisible()) {
                //view is between threshold and halfway, reset the list
                show(ResultsList())
                recyclerView.translationY = 0f
                resetListOffset()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (shouldBlockTransition() || getCurrentState()?.equals(ResultsMap::class.java.name) ?: false) {
                return
            }

            val y = mapView.translationY + (-dy * halfway/(recyclerView.height - halfway))
            mapView.translationY = y

            val view = recyclerView.getChildAt(1)
            val topOffset = if (view == null) {
                0
            } else {
                view.top
            }

            adjustGoogleMapLogo()

            if (currentState == RecyclerView.SCROLL_STATE_SETTLING && topOffset < threshold && topOffset > halfway && isHeaderVisible()) {
                show(ResultsList())
                mapView.translationY = 0f
                recyclerView.translationY = 0f
                resetListOffset()
            } else if (currentState == RecyclerView.SCROLL_STATE_SETTLING && ((topOffset >= threshold && isHeaderVisible()) || isHeaderCompletelyVisible())) {
                show(ResultsMap())
            }

            if (!fabShouldBeHiddenOnList() && fab.visibility == View.INVISIBLE) {
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

            menu?.setVisible(true)

            recyclerView.visibility = View.VISIBLE
            mapCarouselContainer.visibility = View.INVISIBLE

            if (recyclerView.visibility == View.INVISIBLE) {
                fab.visibility = View.VISIBLE
                getFabAnimIn().start()
            } else {
                fab.visibility = View.INVISIBLE
            }

            filterView.visibility = View.GONE
        }
    }

    private val adapterListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            screenHeight = if (ExpediaBookingApp.isAutomation()) { 0 } else {
                height
            }
            screenWidth = if (ExpediaBookingApp.isAutomation()) { 0f } else { width.toFloat() }
            val view = recyclerView.getChildAt(1)

            if (halfway == 0 && threshold == 0 && view != null) {
                halfway = view.top
                threshold = view.top + (view.bottom / 1.9).toInt()
            }

            resetListOffset()
        }
    }

    private val fabTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsList::class.java, LinearInterpolator(), 750) {

        private val listTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsList::class.java, DecelerateInterpolator(2f), duration * 2 / 3) {

            var fabShouldVisiblyMove: Boolean = true
            var mapTranslationStart: Float = 0f
            var toolbarTextOrigin: Float = 0f
            var toolbarTextGoal: Float = 0f

            override fun startTransition(forward: Boolean) {
                super.startTransition(forward)
                toolbarTextOrigin = toolbarTitle.translationY

                if (forward) {
                    toolbarTextGoal = 0f //
                } else {
                    toolbarTextGoal = toolbarSubtitleTop.toFloat()
                }
                recyclerView.visibility = View.VISIBLE
                previousWasList = forward
                fabShouldVisiblyMove = if (forward) !fabShouldBeHiddenOnList() else (fab.visibility == View.VISIBLE)
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
                        fab.translationY = -mapCarouselContainer.height.toFloat()
                        (fab.drawable as? TransitionDrawable)?.startTransition(0)
                        fab.visibility = View.VISIBLE
                        getFabAnimIn().start()
                    }
                }
            }

            override fun updateTransition(f: Float, forward: Boolean) {
                val hotelListDistance = if (forward) (screenHeight * (1 - f)) else (screenHeight * f);
                recyclerView.translationY = hotelListDistance
                navIcon.parameter = if (forward) Math.abs(1 - f) else f
                if (forward) {
                    mapView.translationY = f * -halfway
                }
                else {
                    mapView.translationY = (1-f) * mapTranslationStart
                }

                if (fabShouldVisiblyMove) {
                    val fabDistance = if (forward) -(1 - f) * mapCarouselContainer.height else -f * mapCarouselContainer.height
                    fab.translationY = fabDistance
                }
                //Title transition
                val toolbarYTransStep = toolbarTextOrigin + (f * (toolbarTextGoal - toolbarTextOrigin))
                toolbarTitle.translationY = toolbarYTransStep
                toolbarSubtitle.translationY = toolbarYTransStep
                toolbarSubtitle.alpha = if (forward) f else (1-f)
                adjustGoogleMapLogo()
            }

            override fun finalizeTransition(forward: Boolean) {
                navIcon.parameter = (if (forward) ArrowXDrawableUtil.ArrowDrawableType.BACK else ArrowXDrawableUtil.ArrowDrawableType.CLOSE).type.toFloat()

                menu?.setVisible(true)

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
                }
                else {
                    mapView.translationY = 0f
                    recyclerView.translationY = screenHeight.toFloat()
                    googleMap?.setPadding(0, 0, 0, mapCarouselContainer.height)
                }
            }
        }

        private val carouselTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsList::class.java, DecelerateInterpolator(2f), duration / 3) {

            override fun startTransition(forward: Boolean) {
                mapCarouselContainer.visibility = View.VISIBLE
                if (forward) {
                    mapCarouselContainer.translationX = 0f
                } else {
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

            menu?.setVisible(true)

            if (forward) {
                listTransition.finalizeTransition(forward)
            } else {
                carouselTransition.finalizeTransition(forward)
            }
            mapTransitionRunning = false
        }
    }

    private val listFilterTransition = object : Presenter.Transition(ResultsList::class.java, ResultsFilter::class.java, DecelerateInterpolator(), 500) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (forward) {
                fab.visibility = View.GONE
                filterView.visibility = View.VISIBLE
            } else {
                filterView.visibility = View.GONE
            }
        }
    }

    private val mapFilterTransition = object : Presenter.Transition(ResultsMap::class.java, ResultsFilter::class.java, DecelerateInterpolator(), 500) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (forward) {
                fab.visibility = View.GONE
                filterView.visibility = View.VISIBLE
            } else {
                fab.visibility = View.VISIBLE
                filterView.visibility = View.GONE
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

    val markerObserver: Observer<Marker> = endlessObserver { marker ->
        val map = googleMap
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, map.cameraPosition.zoom))
    }

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

    fun animationFinalize() {
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
        set.setDuration(DEFAULT_FAB_ANIM_DURATION)
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
        set.setDuration(DEFAULT_FAB_ANIM_DURATION)
        return set
    }

    // Classes for state
    public class ResultsList

    public class ResultsMap

    public class ResultsFilter
}
