package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Location
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.format
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
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
import com.expedia.bookings.widget.HotelMarkerPreviewAdapter
import com.expedia.bookings.widget.RecyclerDividerDecoration
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

    var screenHeight: Int = 0
    var mapTransitionRunning: Boolean = false

    val recyclerView: RecyclerView by bindView(R.id.list_view)
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

    val listLayoutManager = LinearLayoutManager(context)

    var googleMap: GoogleMap? = null
    var subtitle: CharSequence? = null

    var listOffset: Int = 0
    var menu: MenuItem? = null

    var halfway = 0
    var threshold = 0
    var totalDistance = 0

    val mapFab: FloatingActionButton by bindView(R.id.map_fab)
    val listFab: FloatingActionButton by bindView(R.id.list_fab)

    var fabAnim : Animation? = null

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
        listLayoutManager.scrollToPositionWithOffset(adapter.numHeaderItemsInHotelsList() + 1, listOffset)
        totalDistance = halfway;
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

            if (allHotelsBox.build().contains(LatLng(currentLat, currentLong)) && closestHotel != null && closestHotel.locationId != null) {
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
        show(ResultsMap())
    }

    init {
        View.inflate(getContext(), R.layout.widget_hotel_results, this)

        headerClickedSubject.subscribe(mapSelectedObserver)
        adapter = HotelListAdapter(ArrayList<Hotel>(), HotelRate.UserPriceType.UNKNOWN, hotelSubject, headerClickedSubject)
        recyclerView.setAdapter(adapter)
        fabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fab_in)
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

        recyclerView.setLayoutManager(listLayoutManager)
        recyclerView.addOnScrollListener(PicassoScrollListener(getContext(), PICASSO_TAG))
        recyclerView.addOnScrollListener(scrollListener)
        recyclerView.addOnItemTouchListener(touchListener)
        recyclerView.addItemDecoration(RecyclerDividerDecoration(getContext(), 0, 0, 0, 0, 0, 0, false))
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

        mapFab.setOnClickListener { view ->
            show(ResultsMap())
        }

        listFab.setOnClickListener { view ->
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
            show(ResultsList())
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
            view ?: return

            if (halfway == 0 && threshold == 0) {
                halfway = view.getTop()
                totalDistance = halfway
                threshold = halfway + view.getHeight() + recyclerView.getChildAt(2).getHeight()
            }

            val topOffset = view.getTop()

            if (newState == RecyclerView.SCROLL_STATE_SETTLING ) {
                //ignore
            } else if (newState == RecyclerView.SCROLL_STATE_IDLE && topOffset >= threshold) {
                show(ResultsMap())
            } else if (newState == RecyclerView.SCROLL_STATE_IDLE && topOffset <= threshold && topOffset >= halfway) {
                show(ResultsList())
                recyclerView.setTranslationY(0f)
                resetListOffset()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            totalDistance += -dy
            if (mapTransitionRunning || (recyclerView.getAdapter() as HotelListAdapter).isLoading() || getCurrentState().equals(javaClass<ResultsMap>().getName())) {
                return
            }

            val y = mapView.getTranslationY() + (-dy * .5f)

            if (y <= 0) {
                mapView.setTranslationY(y)
            }

            var linearLayoutManager : LinearLayoutManager = recyclerView.getLayoutManager() as LinearLayoutManager
            if (currentState == RecyclerView.SCROLL_STATE_SETTLING && totalDistance >= halfway) {
                show(ResultsList())
                mapView.setTranslationY(0f)
                recyclerView.setTranslationY(0f)
                resetListOffset()
            }

            if (linearLayoutManager.findFirstVisibleItemPosition() > 0 && mapFab.getVisibility() == View.INVISIBLE) {
                mapFab.setVisibility(View.VISIBLE)
                mapFab.startAnimation(fabAnim)
            } else if (linearLayoutManager.findFirstVisibleItemPosition() == 0 && mapFab.getVisibility() == View.VISIBLE) {
                mapFab.setVisibility(View.INVISIBLE)
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

            listFab.setVisibility(View.INVISIBLE)

            if (recyclerView.getVisibility() == View.INVISIBLE && mapFab.getVisibility() == View.INVISIBLE && listFab.getVisibility() == View.INVISIBLE) {
                mapFab.setVisibility(View.VISIBLE)
                mapFab.startAnimation(fabAnim)
            } else {
                mapFab.setVisibility(View.INVISIBLE)
            }
        }
    }

    private val adapterListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this)
            resetListOffset()
        }
    }

    private val fabTransition = object : Presenter.Transition(javaClass<ResultsMap>(), javaClass<ResultsList>(), DecelerateInterpolator(), 500) {
        override fun finalizeTransition(forward: Boolean) {
            navIcon.setParameter(if (forward) ArrowXDrawableUtil.ArrowDrawableType.BACK.getType().toFloat() else ArrowXDrawableUtil.ArrowDrawableType.CLOSE.getType().toFloat())
            toolbar.setBackgroundColor(getResources().getColor(R.color.hotels_primary_color))
            toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
            toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance)
            toolbar.setSubtitle(if (forward) subtitle else null)

            menu?.setVisible(true)

            recyclerView.setVisibility(if (forward) View.VISIBLE else View.INVISIBLE)
            mapCarouselContainer.setVisibility(if (forward) View.INVISIBLE else View.VISIBLE)

            listFab.setVisibility(if (forward) View.INVISIBLE else View.VISIBLE)

            if (recyclerView.getVisibility() == View.INVISIBLE && mapFab.getVisibility() == View.INVISIBLE && listFab.getVisibility() == View.INVISIBLE) {
                mapFab.setVisibility(View.VISIBLE)
                mapFab.startAnimation(fabAnim)
            } else {
                mapFab.setVisibility(View.INVISIBLE)
            }

            mapView.setTranslationY(0f)
            if (forward) {
                recyclerView.setTranslationY(0f)
                resetListOffset()
            } else {
                recyclerView.setTranslationY(screenHeight.toFloat())
            }
            mapTransitionRunning = false
        }

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            mapTransitionRunning = true
            if (forward) {
                resetListOffset()
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            if (!forward) {
                var distance = (screenHeight * f)
                recyclerView.setTranslationY(distance)
            }
            navIcon.setParameter(if (forward) Math.abs(1 - f) else f)
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

    // Classes for state
    public class ResultsList

    public class ResultsMap
}
