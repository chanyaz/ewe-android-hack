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
import android.text.format
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
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelCarouselRecycler
import com.expedia.bookings.widget.HotelListAdapter
import com.expedia.bookings.widget.HotelListRecyclerView
import com.expedia.bookings.widget.HotelMarkerPreviewAdapter
import com.expedia.bookings.widget.createHotelMarker
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
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
import rx.exceptions.OnErrorNotImplementedException
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

    val recyclerView: HotelListRecyclerView by bindView(R.id.list_view)
    val mapView: MapView by bindView(R.id.map_view)
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val toolbarTitle by Delegates.lazy { toolbar.getChildAt(2) }
    val toolbarSubtitle by Delegates.lazy { toolbar.getChildAt(3) }
    val recyclerTempBackground: View by bindView(R.id.recycler_view_temp_background)

    var navIcon: ArrowXDrawable

    val mapCarouselContainer: View by bindView(R.id.hotel_carousel_container)
    val mapCarouselRecycler: HotelCarouselRecycler by bindView(R.id.hotel_carousel)

    var mHotelList = ArrayList<MarkerDistance>()

    val hotelSubject = PublishSubject.create<Hotel>()
    val headerClickedSubject = PublishSubject.create<Unit>()

    var googleMap: GoogleMap? = null
    var subtitle: CharSequence? = null

    var listOffset: Int = 0
    var menu: MenuItem? = null

    var halfway = 0
    var threshold = 0
    var totalDistance = 0

    val fab: FloatingActionButton by bindView(R.id.fab)

    var adapter : HotelListAdapter by Delegates.notNull()

    var viewmodel: HotelResultsViewModel by notNullAndObservable { vm ->
        vm.hotelResultsObservable.subscribe(listResultsObserver)
        vm.hotelResultsObservable.subscribe(mapResultsObserver)

        vm.titleSubject.subscribe {
            toolbar.setTitle(it)
        }

        vm.subtitleSubject.subscribe {
            toolbar.setSubtitle(it)
        }

        vm.paramsSubject.subscribe { params ->
            showLoading()

            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(params.suggestion.coordinates.lat, params.suggestion.coordinates.lng), 14.0f))
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
        val elements = createDummyListForAnimation()
        adapter.setData(elements, HotelRate.UserPriceType.UNKNOWN, true)
        adapter.notifyDataSetChanged()
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(adapterListener)
    }

    private fun resetListOffset() {
        listOffset = (getHeight() - (getHeight() / 2.7f)).toInt()
        recyclerView.layoutManager.scrollToPositionWithOffset(adapter.numHeaderItemsInHotelsList() + 1, listOffset)
        totalDistance = halfway;
    }

    private fun fabShouldBeHiddenOnList(): Boolean {
        return recyclerView.layoutManager.findFirstVisibleItemPosition() == 0
    }

    private fun shouldBlockTransition(): Boolean {
        return (mapTransitionRunning || (recyclerView.getAdapter() as HotelListAdapter).isLoading())
    }

    // Create list to show cards for loading animation
    private fun createDummyListForAnimation(): List<Hotel> {
        val elements = ArrayList<Hotel>(2)
        for (i in 0..1) {
            elements.add(Hotel())
        }
        return elements
    }

    val listResultsObserver: Observer<HotelSearchResponse> = object : Observer<HotelSearchResponse> {
        override fun onNext(response: HotelSearchResponse) {
            adapter.setData(response.hotelList, response.userPriceType, false)
            adapter.notifyDataSetChanged()
            resetListOffset()
            AdImpressionTracking.trackAdClickOrImpression(getContext(), response.pageViewBeaconPixelUrl, null)
        }

        override fun onCompleted() {
            throw OnErrorNotImplementedException(RuntimeException("Completed called"))
        }

        override fun onError(e: Throwable?) {
            throw OnErrorNotImplementedException(e)
        }
    }

    val mapResultsObserver: Observer<HotelSearchResponse> = endlessObserver { response ->
        val map = googleMap
        if (map != null) {

            map.clear()

            map.setMyLocationEnabled(true)

            // Determine current location
            val MINIMUM_TIME_AGO = format.DateUtils.HOUR_IN_MILLIS
            val minTime = DateTime.now().getMillis() - MINIMUM_TIME_AGO
            val loc = LocationServices.getLastBestLocation(context, minTime)
            val currentLat = loc?.getLatitude() ?: 0.0
            val currentLong = loc?.getLongitude() ?: 0.0

            var closestHotel: Hotel? = null
            var closestToCurrentLocationVal: Float = Float.MAX_VALUE

            val allHotelsBox = LatLngBounds.Builder()

            var skipFirstHotel = true

            for (hotel in response.hotelList) {
                if (!skipFirstHotel) {
                    // Add markers for all hotels
                    val marker: Marker = map.addMarker(MarkerOptions()
                            .position(LatLng(hotel.latitude, hotel.longitude))
                            .icon(createHotelMarker(getResources(), hotel, false)))

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

                skipFirstHotel = false
            }

            var mostInterestingNeighborhood: HotelSearchResponse.Neighborhood?

            var anyPointsIncludedInAllHotelsBox = if (skipFirstHotel) (response.hotelList.size() > 1) else (response.hotelList.size() > 0)
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
            }

            var closestMarker: MarkerDistance? = null
            for (item in mHotelList) {
                var a = Location("a")
                a.setLatitude(map.getCameraPosition().target.latitude)
                a.setLongitude(map.getCameraPosition().target.longitude)

                var b = Location("b")
                b.setLatitude(item.hotel.latitude)
                b.setLongitude(item.hotel.longitude)

                var distanceBetween = a.distanceTo(b)

                if (distanceBetween <= closestToCurrentLocationVal) {
                    closestToCurrentLocationVal = distanceBetween
                    closestMarker = item
                }
            }

            if (closestMarker != null) {
                closestMarker.marker.showInfoWindow()
                closestMarker.marker.setIcon(createHotelMarker(getResources(), closestMarker.hotel, true))
                mapCarouselContainer.setVisibility(View.INVISIBLE)
                mapCarouselRecycler.addOnScrollListener(PicassoScrollListener(getContext(), PICASSO_TAG))
                mapCarouselRecycler.setAdapter(HotelMarkerPreviewAdapter(mHotelList, closestMarker.marker, hotelSubject))
            }
        }
        Log.d("Hotel Results Next")
    }

    val mapSelectedObserver: Observer<Unit> = endlessObserver {
        if (!shouldBlockTransition()) {
            show(ResultsMap())
        }
    }

    init {
        View.inflate(getContext(), R.layout.widget_hotel_results, this)

        headerClickedSubject.subscribe(mapSelectedObserver)
        adapter = HotelListAdapter(ArrayList<Hotel>(), HotelRate.UserPriceType.UNKNOWN, hotelSubject, headerClickedSubject)
        recyclerView.setAdapter(adapter)
        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.setNavigationIcon(navIcon)
    }

    override fun onFinishInflate() {
        // add the view of same height as of status bar
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
            var lp = recyclerView.getLayoutParams() as FrameLayout.LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
        }

        addDefaultTransition(defaultTransition)
        addTransition(fabTransition)
        mapView.getMapAsync(this)

        mapCarouselContainer.setVisibility(View.INVISIBLE)
        val screen = Ui.getScreenSize(getContext())
        var lp = mapCarouselRecycler.getLayoutParams()
        lp.width = screen.x

        recyclerView.addOnScrollListener(scrollListener)
        recyclerView.addOnItemTouchListener(touchListener)
        mapCarouselRecycler.mapSubject.subscribe(markerObserver)

        toolbar.inflateMenu(R.menu.menu_filter_item)
        menu = toolbar.getMenu().findItem(R.id.menu_filter)
        var drawable = getResources().getDrawable(R.drawable.sort).mutate()
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        menu?.setIcon(drawable)

        val button = LayoutInflater.from(getContext()).inflate(R.layout.toolbar_filter_item, null) as Button
        val navIcon = getResources().getDrawable(R.drawable.sort).mutate()
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        button.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null)
        button.setTextColor(getResources().getColor(android.R.color.white))

        toolbar.getMenu().findItem(R.id.menu_filter).setActionView(button)

        toolbar.setNavigationOnClickListener { view ->
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
            show(ResultsList())
        }

        val fabDrawable: TransitionDrawable? = (fab.getDrawable() as? TransitionDrawable)
        // Enabling crossfade prevents the icon ending up with a weird mishmash of both icons.
        fabDrawable?.setCrossFadeEnabled(true)

        fab.setOnClickListener { view ->
            if (recyclerView.getVisibility() == View.VISIBLE) {
                show(ResultsMap())
            }
            else {
                val activity = getContext() as AppCompatActivity
                activity.onBackPressed()
            }

        }

        show(ResultsList())
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap

        googleMap?.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {
                // Reset markers
                var markerHotel: Hotel? = null
                for (item in mHotelList) {
                    item.marker.setIcon(createHotelMarker(getResources(), item.hotel, false))

                    if (marker == item.marker) {
                        markerHotel = item.hotel
                    }
                }

                if (markerHotel != null) {
                    marker.setIcon(createHotelMarker(getResources(), markerHotel, true))
                }

                marker.showInfoWindow()

                mapCarouselContainer.setVisibility(View.VISIBLE)

                mapCarouselRecycler.addOnScrollListener(PicassoScrollListener(getContext(), PICASSO_TAG))

                mapCarouselRecycler.setAdapter(HotelMarkerPreviewAdapter(mHotelList, marker, hotelSubject))

                return true
            }
        })
    }

    val scrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        var currentState = RecyclerView.SCROLL_STATE_IDLE
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            currentState = newState

            val view = recyclerView.getChildAt(1)
            val holder = recyclerView.findViewHolderForAdapterPosition(2)
            view ?: holder ?: return

            if (halfway == 0 && threshold == 0 && holder is HotelListAdapter.HotelViewHolder) {
                halfway = view.getTop()
                totalDistance = halfway
                threshold = halfway + view.getHeight() + holder.imageView.getHeight()
            }

            val topOffset = totalDistance

            if (newState == RecyclerView.SCROLL_STATE_SETTLING ) {
                //ignore
            } else if (newState == RecyclerView.SCROLL_STATE_IDLE && topOffset >= threshold) {
                show(ResultsMap())
            } else if (newState == RecyclerView.SCROLL_STATE_IDLE && topOffset < threshold && topOffset >= halfway) {
                show(ResultsList())
                recyclerView.setTranslationY(0f)
                resetListOffset()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            totalDistance += -dy
            if (shouldBlockTransition() || getCurrentState()?.equals(javaClass<ResultsMap>().getName()) ?: false) {
                return
            }

            val y = mapView.getTranslationY() + (-dy * .5f)

            if (y <= 0) {
                mapView.setTranslationY(y)
            }

            if (currentState == RecyclerView.SCROLL_STATE_SETTLING && totalDistance >= halfway) {
                show(ResultsList())
                mapView.setTranslationY(0f)
                recyclerView.setTranslationY(0f)
                resetListOffset()
            }

            if (!fabShouldBeHiddenOnList() && fab.getVisibility() == View.INVISIBLE) {
                fab.setVisibility(View.VISIBLE)
                getFabAnimIn().start()
            } else if (fabShouldBeHiddenOnList() && fab.getVisibility() == View.VISIBLE && !hideFabAnimationRunning) {
                hideFabAnimationRunning = true
                val outAnim = getFabAnimOut()
                outAnim.addListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animator: Animator) {
                        fab.setVisibility(View.INVISIBLE)
                        hideFabAnimationRunning = false
                    }
                })
                outAnim.start()
            }
        }
    }

    private val defaultTransition = object : Presenter.DefaultTransition(javaClass<ResultsList>().getName()) {

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            navIcon.setParameter(if (forward) Math.abs(1 - f) else f)
        }

        override fun finalizeTransition(forward: Boolean) {
            navIcon.setParameter(ArrowXDrawableUtil.ArrowDrawableType.BACK.getType().toFloat())
            toolbar.setBackgroundColor(getResources().getColor(R.color.hotels_primary_color))
            toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
            toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance)
            toolbar.setSubtitle(subtitle)
            recyclerView.setTranslationY(0f)
            mapView.setTranslationY(0f)

            menu?.setVisible(true)

            recyclerView.setVisibility(View.VISIBLE)
            mapCarouselContainer.setVisibility(View.INVISIBLE)

            if (recyclerView.getVisibility() == View.INVISIBLE) {
                fab.setVisibility(View.VISIBLE)
                getFabAnimIn().start()
            } else {
                fab.setVisibility(View.INVISIBLE)
            }
        }
    }

    private val adapterListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this)
            screenHeight = if (ExpediaBookingApp.isAutomation()) { 0 } else { getHeight() }
            screenWidth = if (ExpediaBookingApp.isAutomation()) { 0f } else { getWidth().toFloat() }
            resetListOffset()
        }
    }

    private val fabTransition = object : Presenter.Transition(javaClass<ResultsMap>(), javaClass<ResultsList>(), LinearInterpolator(), 750) {

        private val listTransition = object : Presenter.Transition(javaClass<ResultsMap>(), javaClass<ResultsList>(), DecelerateInterpolator(2f), duration * 2/3) {

            var fabShouldVisiblyMove: Boolean = true
            var mapTranslationStart: Float = 0f

            override fun startTransition(forward: Boolean) {
                super.startTransition(forward)
                recyclerView.setVisibility(View.VISIBLE)
                fabShouldVisiblyMove = if (forward) !fabShouldBeHiddenOnList() else (fab.getVisibility() == View.VISIBLE)
                if (forward) {
                    //If the fab is visible we want to do the transition - but if we're just hiding it, don't confuse the
                    // user with an unnecessary icon swap
                    if (fabShouldVisiblyMove) {
                        (fab.getDrawable() as? TransitionDrawable)?.reverseTransition(duration)
                    } else {
                        resetListOffset()

                        //Let's start hiding the fab
                        getFabAnimOut().start()
                    }
                }
                else {
                    mapTranslationStart = mapView.getTranslationY()
                    if (fabShouldVisiblyMove) {
                        (fab.getDrawable() as? TransitionDrawable)?.startTransition(duration)
                    } else {
                        //Since we're not moving it manually, let's jump it to where it belongs,
                        // and let's get it showing the right thing
                        fab.setTranslationY(-mapCarouselContainer.getHeight().toFloat())
                        (fab.getDrawable() as? TransitionDrawable)?.startTransition(0)
                        fab.setVisibility(View.VISIBLE)
                        getFabAnimIn().start()
                    }
                }
            }

            override fun updateTransition(f: Float, forward: Boolean) {
                val hotelListDistance = if (forward) (screenHeight * (1 - f)) else (screenHeight * f);
                recyclerView.setTranslationY(hotelListDistance)
                if (forward) {
                    mapView.setTranslationY(f * mapTranslationStart)
                }
                else {
                    mapView.setTranslationY((1-f) * mapTranslationStart)
                }

                if (fabShouldVisiblyMove) {
                    val fabDistance = if (forward) -(1 - f) * mapCarouselContainer.getHeight() else -f * mapCarouselContainer.getHeight()
                    fab.setTranslationY(fabDistance)
                }
            }

            override fun finalizeTransition(forward: Boolean) {
                toolbar.setNavigationIcon(if (forward) R.drawable.ic_arrow_back_white_24dp else R.drawable.ic_close_white_24dp)
                toolbar.setBackgroundColor(getResources().getColor(R.color.hotels_primary_color))
                toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
                toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance)
                toolbar.setSubtitle(if (forward) subtitle else null)

                menu?.setVisible(true)

                recyclerView.setVisibility(if (forward) View.VISIBLE else View.INVISIBLE)
                mapCarouselContainer.setVisibility(if (forward) View.INVISIBLE else View.VISIBLE)

                if (forward) {
                    if (!fabShouldVisiblyMove) {
                        fab.setTranslationY(0f)
                        (fab.getDrawable() as? TransitionDrawable)?.reverseTransition(0)
                        fab.setVisibility(View.INVISIBLE)
                    }
                    recyclerView.setTranslationY(0f)
                    mapView.setTranslationY(mapTranslationStart)
                }
                else {
                    mapView.setTranslationY(0f)
                    recyclerView.setTranslationY(screenHeight.toFloat())
                }
            }
        }

        private val carouselTransition = object : Presenter.Transition(javaClass<ResultsMap>(), javaClass<ResultsList>(), DecelerateInterpolator(2f), duration/3) {

            override fun startTransition(forward: Boolean) {
                mapCarouselContainer.setVisibility(View.VISIBLE)
                if (forward) {
                    mapCarouselContainer.setTranslationX(0f)
                }
                else {
                    mapCarouselContainer.setTranslationX(screenWidth)
                }
            }

            override fun updateTransition(f: Float, forward: Boolean) {
                mapCarouselContainer.setTranslationX((if (forward) f else 1 - f) * screenWidth)
            }

            override fun finalizeTransition(forward: Boolean) {
                if (forward) {
                    mapCarouselContainer.setTranslationX(screenWidth)
                    mapCarouselContainer.setVisibility(View.INVISIBLE)
                }
                else {
                    mapCarouselContainer.setTranslationX(0f)
                }
            }
        }

        var secondTransitionStartTime = .33f
        var currentTransition = 0

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            currentTransition = 0
            mapTransitionRunning = true


            if (forward) {
                //Let's be explicit despite it being the default
                secondTransitionStartTime = .33f
                carouselTransition.startTransition(forward)
            }
            else {
                secondTransitionStartTime = .66f
                listTransition.startTransition(forward)
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            navIcon.setParameter(if (forward) Math.abs(1 - f) else f)
            if (forward) {
                if (f < secondTransitionStartTime) {
                    carouselTransition.updateTransition(carouselTransition.interpolator.getInterpolation(f / secondTransitionStartTime), forward)
                }
                else {
                    if (currentTransition == 0) {
                        currentTransition = 1
                        carouselTransition.finalizeTransition(forward)
                        listTransition.startTransition(forward)
                    }
                    listTransition.updateTransition(listTransition.interpolator.getInterpolation((f - secondTransitionStartTime) / (1 - secondTransitionStartTime)), forward)
                }
            }
            else {
                if (f < secondTransitionStartTime) {
                    listTransition.updateTransition(listTransition.interpolator.getInterpolation(f / secondTransitionStartTime), forward)
                }
                else {
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
            navIcon.setParameter(if (forward) ArrowXDrawableUtil.ArrowDrawableType.BACK.getType().toFloat() else ArrowXDrawableUtil.ArrowDrawableType.CLOSE.getType().toFloat())
            toolbar.setBackgroundColor(getResources().getColor(R.color.hotels_primary_color))
            toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
            toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance)
            toolbar.setSubtitle(if (forward) subtitle else null)

            menu?.setVisible(true)

            if (forward) {
                listTransition.finalizeTransition(forward)
            }
            else {
                carouselTransition.finalizeTransition(forward)
            }
            mapTransitionRunning = false
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
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), map.getCameraPosition().zoom))
    }

    var yTranslationRecyclerTempBackground = 0f
    var yTranslationRecyclerView = 0f
    var toolbarTitleTop = 0
    var toolbarSubtitleTop = 0

    fun animationStart() {
        recyclerTempBackground.setVisibility(View.VISIBLE)
    }

    fun animationUpdate(f : Float, forward : Boolean) {
        if (yTranslationRecyclerTempBackground == 0f && recyclerView.getChildAt(1) != null) {
            yTranslationRecyclerTempBackground = (recyclerView.getChildAt(0).getHeight() + recyclerView.getChildAt(0).getTop() + toolbar.getHeight()).toFloat()
            yTranslationRecyclerView = (recyclerView.getChildAt(0).getHeight() + recyclerView.getChildAt(0).getTop()).toFloat()
            recyclerTempBackground.setTranslationY(yTranslationRecyclerTempBackground)
            toolbarTitleTop =  (toolbarTitle.getHeight() - toolbarTitle.getTop())/2
            toolbarSubtitleTop = (toolbarSubtitle.getTop() - toolbarSubtitle.getHeight())/2
            toolbarTitle.setTranslationY(toolbarTitleTop.toFloat())
            toolbarSubtitle.setTranslationY(toolbarSubtitleTop.toFloat())
        }
        var factor = if (forward) f else Math.abs(1 - f)
        recyclerView.setTranslationY(factor * yTranslationRecyclerView)
        navIcon.setParameter(factor)
        toolbarTitle.setTranslationY(factor * toolbarTitleTop)
        toolbarSubtitle.setTranslationY(factor * toolbarSubtitleTop)
    }

    fun animationFinalize() {
        recyclerTempBackground.setVisibility(View.GONE)
        navIcon.setParameter(ArrowXDrawableUtil.ArrowDrawableType.BACK.getType().toFloat())
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
        set.setInterpolator(DecelerateInterpolator())
        return set
    }

    fun getFabAnimOut(): Animator {
        val set = AnimatorSet()
        set.playTogether(
                ObjectAnimator.ofFloat(fab, "scaleX", 1f, 0f),
                ObjectAnimator.ofFloat(fab, "scaleY", 1f, 0f)
        )
        set.setInterpolator(AccelerateInterpolator())
        set.setDuration(DEFAULT_FAB_ANIM_DURATION)
        return set
    }

    // Classes for state
    public class ResultsList

    public class ResultsMap
}
