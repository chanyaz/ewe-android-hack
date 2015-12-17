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
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.bitmaps.PicassoScrollListener
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelMarkerPreviewRecycler
import com.expedia.bookings.widget.HotelListAdapter
import com.expedia.bookings.widget.createHotelMarker
import com.expedia.bookings.widget.HotelMarkerPreviewAdapter
import com.expedia.bookings.widget.RecyclerDividerDecoration
import com.expedia.util.endlessObserver
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
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import rx.Observer
import rx.Subscription
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.PublishSubject
import java.util.ArrayList
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), OnMapReadyCallback {

    private val PICASSO_TAG = "HOTEL_RESULTS_LIST"

    var hotelServices: HotelServices by Delegates.notNull()
        @Inject set

    var downloadSubscription: Subscription? = null
    var screenHeight: Int = 0
    var mapTransitionRunning: Boolean = false

    val recyclerView: RecyclerView by bindView(R.id.list_view)
    val mapView: MapView by bindView(R.id.map_view)
    val toolbar: Toolbar by bindView(R.id.toolbar)

    val hotelMarkerPreview: HotelMarkerPreviewRecycler by bindView(R.id.hotel_marker_preview)
    val markerPreviewLayout: View by bindView(R.id.preview_layout)

    var mHotelList = ArrayList<MarkerDistance>()

    val hotelSubject = PublishSubject.create<Hotel>()

    val layoutManager = LinearLayoutManager(context)

    var googleMap: GoogleMap? = null
    var subtitle: CharSequence? = null

    var listOffset: Int = 0
    var menu: MenuItem? = null

    var halfway = 0
    var threshold = 0

    val mapFab: FloatingActionButton by bindView(R.id.map_fab)
    val listFab: FloatingActionButton by bindView(R.id.list_fab)

    var showMapFab: Boolean = false

    var fabAnim : Animation? = null

    public class MarkerDistance(marker: Marker, distance: Float, hotel: Hotel) : Comparable<MarkerDistance> {
        override fun compareTo(other: MarkerDistance): Int {
            if ( this.distance < other.distance ) {
                return -1
            } else if (this.distance > other.distance ) {
                return 1
            } else {
                return 0
            }
        }

        val marker: Marker = marker
        var distance: Float = distance
        val hotel: Hotel = hotel

    }

    val resultsSubject: PublishSubject<HotelSearchResponse> = PublishSubject.create()

    val listResultsObserver: Observer<HotelSearchResponse> = object : Observer<HotelSearchResponse> {
        override fun onNext(response: HotelSearchResponse) {
            recyclerView.setAdapter(HotelListAdapter(response.hotelList, hotelSubject))
            listOffset = (screenHeight - (screenHeight / 2.7f)).toInt()
            layoutManager.scrollToPositionWithOffset(1, listOffset)
            show(ResultsList())
        }

        override fun onCompleted() {
            throw OnErrorNotImplementedException(RuntimeException("Completed called"))
        }

        override fun onError(e: Throwable?) {
            throw OnErrorNotImplementedException(e)
        }
    }

    val mapResultsObserver: Observer<HotelSearchResponse> = object : Observer<HotelSearchResponse> {
        override fun onNext(response: HotelSearchResponse) {
            val map = googleMap
            map ?: return

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

            for (hotel in response.hotelList) {
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

            var mostInterestingNeighborhood: HotelSearchResponse.Neighborhood?

            if (allHotelsBox.build().contains(LatLng(currentLat, currentLong)) && closestHotel != null && closestHotel.locationId != null) {
                mostInterestingNeighborhood = response.neighborhoodsMap.get(closestHotel.locationId)
            } else {
                mostInterestingNeighborhood = response.allNeighborhoodsInSearchRegion.reduce { left, right -> if (left.score >= right.score) left else right }
            }

            if (mostInterestingNeighborhood != null) {
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
                markerPreviewLayout.setVisibility(View.INVISIBLE)
                hotelMarkerPreview.addOnScrollListener(PicassoScrollListener(getContext(), PICASSO_TAG))
                hotelMarkerPreview.setAdapter(HotelMarkerPreviewAdapter(mHotelList, closestMarker.marker, hotelSubject))
            }

            Log.d("Hotel Results Next")
        }

        override fun onCompleted() {
            Log.d("Hotel Results Completed")
        }

        override fun onError(e: Throwable?) {
            Log.d("Hotel Results Error", e)
        }
    }

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        View.inflate(getContext(), R.layout.widget_hotel_results, this)

        resultsSubject.subscribe(listResultsObserver)
        resultsSubject.subscribe(mapResultsObserver)

        fabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fab_in)
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
        addTransition(mapTransition)
        mapView.getMapAsync(this)

        markerPreviewLayout.setVisibility(View.INVISIBLE)
        val screen = Ui.getScreenSize(getContext())
        var lp = hotelMarkerPreview.getLayoutParams()
        lp.width = screen.x

        recyclerView.setLayoutManager(layoutManager)
        recyclerView.addOnScrollListener(PicassoScrollListener(getContext(), PICASSO_TAG))
        recyclerView.addOnScrollListener(scrollListener)
        recyclerView.addOnItemTouchListener(touchListener)
        hotelMarkerPreview.mapSubject.subscribe(markerObserver)

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

        resetToolbar()

        toolbar.setNavigationOnClickListener { view ->
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
            resetToolbar()
        }

        mapFab.setOnClickListener { view ->
            changeToolbar()
        }

        listFab.setOnClickListener { view ->
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
            resetToolbar()
        }

    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super<Presenter>.onVisibilityChanged(changedView, visibility)
        if (changedView == this && visibility == View.VISIBLE) {
            getViewTreeObserver().addOnGlobalLayoutListener(layoutListener)
        }
    }

    fun doSearch(params: HotelSearchParams) {
        downloadSubscription = hotelServices.regionSearch(params, object : Observer<HotelSearchResponse> {
            override fun onNext(response: HotelSearchResponse?) {
                resultsSubject.onNext(response)
            }

            override fun onCompleted() {
                // ignore
            }

            override fun onError(e: Throwable?) {
                Log.d("Hotel Results Error", e)
            }
        })

        toolbar.setTitle(params.suggestion.regionNames.shortName)
        subtitle = Phrase.from(getContext(), R.string.calendar_instructions_date_range_with_guests_TEMPLATE).put("startdate", DateUtils.localDateToMMMd(params.checkIn)).put("enddate", DateUtils.localDateToMMMd(params.checkOut)).put("guests", params.children.size() + 1).format()
        toolbar.setSubtitle(subtitle)

        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(params.suggestion.coordinates.lat, params.suggestion.coordinates.lng), 14.0f))

        resetToolbar()
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

                markerPreviewLayout.setVisibility(View.VISIBLE)

                hotelMarkerPreview.addOnScrollListener(PicassoScrollListener(getContext(), PICASSO_TAG))

                hotelMarkerPreview.setAdapter(HotelMarkerPreviewAdapter(mHotelList, marker, hotelSubject))

                return true
            }
        })
    }

    val scrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (halfway == 0 && threshold == 0) {
                halfway = recyclerView.getChildAt(0).getTop()
                threshold = halfway + recyclerView.getChildAt(0).getHeight()
            }

            val topOffset = recyclerView.getChildAt(0).getTop()

            if (newState == RecyclerView.SCROLL_STATE_IDLE && topOffset >= threshold) {
                changeToolbar()
                show(ResultsMap())
            } else if (newState == RecyclerView.SCROLL_STATE_IDLE && topOffset < threshold && topOffset >= halfway) {
                resetToolbar()
                recyclerView.setTranslationY(0f)
                recyclerView.smoothScrollBy(0, topOffset - halfway)
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (mapTransitionRunning || getCurrentState().equals(javaClass<ResultsMap>().getName())) {
                return
            }

            val y = mapView.getTranslationY() + (-dy * .5f)

            if (y <= 0) {
                mapView.setTranslationY(y)
            }

            val topOffset = recyclerView.getChildAt(0).getTop()
            if (topOffset <= 0) {
                showMapFab = true
            } else {
                showMapFab = false
            }
            resetToolbar()
        }
    }

    fun resetToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setBackgroundColor(getResources().getColor(R.color.hotels_primary_color))
        toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
        toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance)
        toolbar.setSubtitle(subtitle)

        menu?.setVisible(true)

        recyclerView.setVisibility(View.VISIBLE)
        markerPreviewLayout.setVisibility(View.INVISIBLE)

        listFab.setVisibility(View.INVISIBLE)

        if (showMapFab && mapFab.getVisibility() == View.INVISIBLE) {
            mapFab.setVisibility(View.VISIBLE)
            mapFab.startAnimation(fabAnim)

        } else if (!showMapFab && mapFab.getVisibility() == View.VISIBLE) {
            mapFab.setVisibility(View.INVISIBLE)
        }

        mapView.setTranslationY(0f)
    }

    fun changeToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        toolbar.setBackgroundColor(getResources().getColor(R.color.hotels_primary_color))
        toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
        toolbar.setSubtitle(null)

        menu?.setVisible(true)

        recyclerView.setVisibility(View.INVISIBLE)
        markerPreviewLayout.setVisibility(View.VISIBLE)

        listFab.setVisibility(View.VISIBLE)
        listFab.startAnimation(fabAnim)

        mapFab.setVisibility(View.INVISIBLE)

        mapView.setTranslationY(0f)
    }

    private val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            getViewTreeObserver().removeOnGlobalLayoutListener(this)
            screenHeight = if (ExpediaBookingApp.isAutomation()) { 0 } else { getHeight() }
            recyclerView.addItemDecoration(RecyclerDividerDecoration(getContext(), 0, 0, 0, 0, screenHeight, 0, false))
        }
    }

    private val defaultTransition = object : Presenter.DefaultTransition(javaClass<ResultsList>().getName()) {
        override fun finalizeTransition(forward: Boolean) {
            recyclerView.setTranslationY(0f)
            mapView.setTranslationY(0f)
        }
    }

    private val mapTransition = object : Presenter.Transition(javaClass<ResultsList>(), javaClass<ResultsMap>(), DecelerateInterpolator(), 500) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            mapTransitionRunning = true
            if (!forward) {
                layoutManager.scrollToPositionWithOffset(1, listOffset)
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            var distance: Float = 0f

            if (forward) {
                distance = (screenHeight * f)
            }

            recyclerView.setTranslationY(distance)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            mapView.setTranslationY(0f)
            if (!forward) {
                recyclerView.setTranslationY(0f)
                layoutManager.scrollToPositionWithOffset(1, listOffset)
            } else {
                recyclerView.setTranslationY(screenHeight.toFloat())
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

    // Classes for state
    public class ResultsList

    public class ResultsMap
}
