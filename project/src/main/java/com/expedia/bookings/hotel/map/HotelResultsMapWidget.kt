package com.expedia.bookings.hotel.map

import android.content.Context
import android.location.Location
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.widget.FrameLayout
import com.expedia.util.PermissionsUtils
import com.expedia.vm.hotel.HotelResultsMapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.mobiata.android.LocationServices
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class HotelResultsMapWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
        OnMapReadyCallback {

    val clusterClickedSubject = PublishSubject.create<Unit>()
    val markerClickedSubject = PublishSubject.create<Unit>()
    val hotelsForCarouselSubject = PublishSubject.create<List<Hotel>>()
    val cameraChangeSubject = PublishSubject.create<Unit>()

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null

    private val viewModel: HotelResultsMapViewModel
    private var currentHotels = emptyList<Hotel>()

    private var hotelMapMarkers = arrayListOf<HotelMapMarker>()
    private var clusterManager: ClusterManager<HotelMapMarker>? = null
    private var hotelMapClusterRenderer: HotelMapClusterRenderer? = null
    private val hotelIconFactory = HotelMarkerIconGenerator(context)
    private val clusterClickListener by lazy { ClusterClickListener() }
    private val markerClickListener by lazy { MarkerClickListener() }

    private var currentBounds: LatLngBounds? = null
    private var queuedCameraPosition: CameraPosition? = null
    private var mapReady = false

    private val clusterChangeObserver = PublishSubject.create<Unit>()

    private val DEFAULT_ZOOM = resources.displayMetrics.density.toInt() * 50

    init {
        viewModel = HotelResultsMapViewModel(context, lastBestLocationSafe())
        clusterChangeObserver.subscribe {
            hotelsForCarouselSubject.onNext(getHotelsForCarousel())
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        MapsInitializer.initialize(context)
        this.googleMap = googleMap
        initMapClusterManagement()
        initMapListeners()
        initMapSettings()

        if (currentHotels.isNotEmpty() && hotelMapMarkers.isEmpty()) {
            createNewMarkers()
        }
        queuedCameraPosition?.let { position ->
            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(queuedCameraPosition))
        }
        queuedCameraPosition = null
        mapReady = true
    }

    fun newResults(response: HotelSearchResponse, updateBounds: Boolean = false) {
        currentHotels = response.hotelList
        if (updateBounds) { currentBounds = viewModel.getMapBounds(response) }
        if (mapReady) {
            clearMarkers()
            createNewMarkers()
            if (updateBounds) {
                animateBounds()
            }
        }
    }

    fun setMapView(mapView: MapView) {
        this.mapView = mapView
        addView(mapView)
        mapView.getMapAsync(this)
    }

    fun toSplitView(bottomTranslation: Int) {
        clusterManager?.setOnClusterItemClickListener(null)
        clusterManager?.setOnClusterClickListener(null)
        adjustPadding(bottomTranslation)
    }

    fun toFullScreen() {
        clusterManager?.setOnClusterItemClickListener(markerClickListener)
        clusterManager?.setOnClusterClickListener(clusterClickListener)
    }

    fun adjustPadding(bottomTranslation: Int) {
        googleMap?.setPadding(0, getToolbarHeight(), 0, bottomTranslation)
    }

    fun selectNewHotel(hotel: Hotel) {
        hotelMapMarkers.find { it.hotel.hotelId == hotel.hotelId }?.let { matchingMarker ->
            selectMarker(matchingMarker, zoom = true)
        }
    }

    fun markSoldOutHotel(hotelId: String) {
        currentHotels.firstOrNull { it.hotelId == hotelId }?.let { hotel ->
            hotel.isSoldOut = true
            hotelMapMarkers.firstOrNull { it.hotel.hotelId == hotel.hotelId }?.let { marker ->
                hotelMapClusterRenderer?.getMarker(marker)?.setIcon(marker.getHotelMarkerIcon())
                clusterManager?.cluster()
            }
        }
    }

    fun moveCamera(lat: Double, lng: Double) {
        val latLng = LatLng(lat, lng)
        val cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(8f)
                .build()
        if (!mapReady) {
            queuedCameraPosition = cameraPosition
        } else {
            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    fun clearMarkers() {
        clusterManager?.let { clusterManager ->
            hotelMapMarkers.clear()
            clusterManager.clearItems()
            clusterManager.cluster()
        }
    }

    fun hasSelectedMarker() : Boolean {
        return hotelMapMarkers.filter { it.isSelected }.isNotEmpty()
    }

    fun getCameraCenter(): LatLng? {
        return googleMap?.cameraPosition?.target
    }

    private fun initMapClusterManagement() {
        clusterManager = ClusterManager(context, googleMap)
        clusterManager!!.setAlgorithm(HotelMapClusterAlgorithm())
        hotelMapClusterRenderer = HotelMapClusterRenderer(context, googleMap, clusterManager!!,
                clusterChangeObserver)
        clusterManager!!.setRenderer(hotelMapClusterRenderer)
        clusterManager!!.setOnClusterItemClickListener(markerClickListener)
        clusterManager!!.setOnClusterClickListener(clusterClickListener)
    }

    private fun initMapListeners() {
        var currentZoom = -1f

        googleMap?.setOnCameraChangeListener { position ->
            synchronized(currentZoom) {
                if (Math.abs(currentZoom - position.zoom) > .5) {
                    clusterManager?.cluster()
                    currentZoom = position.zoom
                }
            }
            cameraChangeSubject.onNext(Unit)
        }
        googleMap?.setOnMarkerClickListener(clusterManager!!)
    }

    private fun initMapSettings() {
        if (PermissionsUtils.havePermissionToAccessLocation(context)) {
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
    }

    private fun animateBounds() {
        currentBounds?.let { bounds ->
            if (ViewCompat.isLaidOut(mapView)) {
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(currentBounds, DEFAULT_ZOOM))
            }

            val center = bounds.center
            val location = Location("currentRegion")
            location.latitude = center.latitude
            location.longitude = center.longitude
        }
    }

    private fun createNewMarkers() {
        if (clusterManager == null) {
            return
        }
        if (currentHotels.isEmpty()) {
            return
        }

        Observable.fromCallable(CreateMarkersCallable(currentHotels))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { hotelMapMarkers ->
                    this.hotelMapMarkers = hotelMapMarkers
                    clusterManager!!.addItems(hotelMapMarkers)
                    clusterManager?.cluster()
                }
    }

    private fun selectMarker(hotelMapMarker: HotelMapMarker, zoom: Boolean = false,
                             refreshCarousel: Boolean = false) {
        if (clusterManager == null) {
            return
        }
        clearPreviousMarker()
        hotelMapMarker.isSelected = true
        viewModel.selectedMapMarker = hotelMapMarker

        val selectedMarker = hotelMapClusterRenderer?.getMarker(hotelMapMarker)
        if (!hotelMapMarker.hotel.isSoldOut) {
            selectedMarker?.setIcon(hotelMapMarker.getHotelMarkerIcon())
        }
        selectedMarker?.showInfoWindow()
        if (zoom) {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(hotelMapMarker.position,
                    googleMap?.cameraPosition?.zoom!!))
        }
        if (refreshCarousel) {
            hotelsForCarouselSubject.onNext(getHotelsForCarousel())
        }
        markerClickedSubject.onNext(Unit)
    }

    private fun clearPreviousMarker() {
        val prevHotelMapMarker = viewModel.selectedMapMarker
        if (prevHotelMapMarker != null) {
            prevHotelMapMarker.isSelected = false
            if (!prevHotelMapMarker.hotel.isSoldOut) {
                hotelMapClusterRenderer?.getMarker(prevHotelMapMarker)?.setIcon(prevHotelMapMarker.getHotelMarkerIcon())
            }
        }
    }

    private fun lastBestLocationSafe(): Location {
        val minTime = DateTime.now().millis - TimeUnit.HOURS.toMillis(1)
        val loc = LocationServices.getLastBestLocation(context, minTime)
        val location = Location("lastBestLocationSafe")
        location.latitude = loc?.latitude ?: 0.0
        location.longitude = loc?.longitude ?: 0.0
        return location
    }

    private fun getHotelsForCarousel(): List<Hotel> {
        // todo this can get called ALOT as a user zooms w/ map move off main thread
        val unClusteredHotels = hotelMapMarkers.filter { !it.isClustered }.map { it.hotel }
        viewModel.selectedMapMarker?.let { marker ->
            val hotel = marker.hotel
            val hotelLocation = Location("selected")
            hotelLocation.latitude = hotel.latitude
            hotelLocation.longitude = hotel.longitude
            var distanceSortedHotels = viewModel.sortByLocation(hotelLocation, unClusteredHotels)
            return distanceSortedHotels
        }
        return unClusteredHotels
    }

    private fun getToolbarHeight(): Int {
        val tv = TypedValue();
        context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
        return TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
    }

    private inner class MarkerClickListener : ClusterManager.OnClusterItemClickListener<HotelMapMarker> {
        override fun onClusterItemClick(marker: HotelMapMarker): Boolean {
            selectMarker(marker, zoom = false, refreshCarousel = true)
            return true
        }
    }

    private inner class ClusterClickListener : ClusterManager.OnClusterClickListener<HotelMapMarker> {
        override fun onClusterClick(cluster: Cluster<HotelMapMarker>): Boolean {
            clusterClickedSubject.onNext(Unit)
            clearPreviousMarker()
            val builder = LatLngBounds.builder()
            cluster.items.forEach { item ->
                builder.include(item.pos)
            }
            val bounds = builder.build()
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, resources.displayMetrics.density.toInt() * 50))
            return true
        }
    }

    private inner class CreateMarkersCallable(private val hotels: List<Hotel>) : Callable<ArrayList<HotelMapMarker>> {
        override fun call(): ArrayList<HotelMapMarker> {
            var hotelMapMarkers = arrayListOf<HotelMapMarker>()
            hotels.forEach { hotel ->
                val hotelMapMarker = HotelMapMarker(context, LatLng(hotel.latitude, hotel.longitude), hotel, hotelIconFactory)
                hotelMapMarkers.add(hotelMapMarker)
            }
            return hotelMapMarkers
        }
    }
}
