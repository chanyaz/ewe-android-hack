package com.expedia.bookings.hotel.map

import android.content.Context
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoScrollListener
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.HotelCarouselRecycler
import com.expedia.bookings.widget.HotelMapCarouselAdapter
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
import org.joda.time.DateTime
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.concurrent.Callable

class CleanHotelMapView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
        OnMapReadyCallback {

    //todo fix tracking.
    val mapPinClickedSubject = PublishSubject.create<Unit>()
    val hotelSelectedSubject = PublishSubject.create<Hotel>()

    val carouselShownSubject = PublishSubject.create<Int>()

    private val mapContainer: FrameLayout by bindView(R.id.clean_map_container)
    private val mapCarouselContainer: ViewGroup by bindView(R.id.hotel_carousel_container)
    private val mapCarouselRecycler: HotelCarouselRecycler by bindView(R.id.hotel_carousel)

    private lateinit var mapView: MapView

    private var googleMap: GoogleMap? = null
    private var clusterManager: ClusterManager<HotelMapMarker>? = null
    private lateinit var hotelMapClusterRenderer: HotelMapClusterRenderer
    private val hotelIconFactory = HotelMarkerIconGenerator(context)

    private var hotelMapMarkers = arrayListOf<HotelMapMarker>()

    val viewModel: HotelResultsMapViewModel

    private var mapReady = false

    private var orderedCarouselHotels = emptyList<Hotel>()
    private var currentHotels = emptyList<Hotel>()
    private var currentBounds: LatLngBounds? = null

    private var queuedCameraPosition: CameraPosition? = null

    private val PICASSO_TAG = "HOTEL_RESULTS_LIST"

    private val DEFAULT_ZOOM = resources.displayMetrics.density.toInt() * 50

    private val clusterClickListener by lazy { ClusterClickListener() }
    private val markerClickListener by lazy { MarkerClickListener() }

    init {
        View.inflate(context, R.layout.clean_hotel_results_map, this)
        viewModel = HotelResultsMapViewModel(context, lastBestLocationSafe())
        viewModel.clusterChangeSubject.subscribe {
            updateCarouselItems()
        }

        mapCarouselRecycler.adapter = HotelMapCarouselAdapter(emptyList(), hotelSelectedSubject)
        mapCarouselRecycler.addOnScrollListener(PicassoScrollListener(context, PICASSO_TAG))

        mapCarouselRecycler.showingHotelSubject.subscribe { hotel ->
            val markersForHotel = hotelMapMarkers.filter { it.hotel.hotelId == hotel.hotelId }
            if (markersForHotel.isNotEmpty()) {
                val newMarker = markersForHotel.first()
                viewModel.carouselSwipedObservable.onNext(newMarker)
            }
        }

    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        val screen = Ui.getScreenSize(context)
        val lp = mapCarouselRecycler.layoutParams
        lp.width = screen.x
    }

    override fun onMapReady(map: GoogleMap?) {
        MapsInitializer.initialize(context)
        this.googleMap = map

        initMapSettings()
        initClusterManagement()
        addMapListeners()
        queuedCameraPosition?.let { position ->
            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(queuedCameraPosition))
        }
        queuedCameraPosition = null
        mapReady = true

        //todo wtf
//        mapView.viewTreeObserver.addOnGlobalLayoutListener(mapViewLayoutReadyListener)
    }

    fun setMapView(mapView: MapView) {
        this.mapView = mapView
        mapContainer.addView(mapView)
        mapView.getMapAsync(this)
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

    fun clearMarkers() {
        clusterManager?.let { clusterManager ->
            hotelMapMarkers.clear()
            clusterManager.clearItems()
            clusterManager.cluster()
            hideCarousel()
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
//            googleMap?.setPadding(0, 0, 0, 0)
            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    fun toFullScreen() {
        clusterManager?.setOnClusterItemClickListener(markerClickListener)
        clusterManager?.setOnClusterClickListener(clusterClickListener)

        if (hotelMapMarkers.filter { it.isSelected }.isEmpty()) {
            hideCarousel()
        } else {
            showCarousel()
        }
    }

    fun toSplitView(bottomTranslation: Int) {
        clusterManager?.setOnClusterItemClickListener(null)
        clusterManager?.setOnClusterClickListener(null)
        hideCarousel()
        googleMap?.setPadding(0, getToolbarHeight(), 0, bottomTranslation)
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

    private fun animateBounds() {
        currentBounds?.let { bounds ->
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(currentBounds, DEFAULT_ZOOM))

            val center = bounds.center
            val location = Location("currentRegion")
            location.latitude = center.latitude
            location.longitude = center.longitude
            viewModel.asyncSortByLocation(location, currentHotels).subscribe { orderedHotels ->
                orderedCarouselHotels = orderedHotels
                (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setItems(orderedCarouselHotels)
            }
        }
    }

    private fun selectMarker(hotelMapMarker: HotelMapMarker, shouldZoom: Boolean = false, animateCarousel: Boolean = true) {
        if (clusterManager == null) {
            return
        }
        clearPreviousMarker()
        hotelMapMarker.isSelected = true
        val selectedMarker = hotelMapClusterRenderer.getMarker(hotelMapMarker)
        if (!hotelMapMarker.hotel.isSoldOut) {
            selectedMarker?.setIcon(hotelMapMarker.getHotelMarkerIcon())
        }
        selectedMarker?.showInfoWindow()
        if (shouldZoom) {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(hotelMapMarker.position,
                    googleMap?.cameraPosition?.zoom!!))
        }
        viewModel.selectedMapMarker = hotelMapMarker
        showCarousel()
    }

    private fun clearPreviousMarker() {
        val prevHotelMapMarker = viewModel.selectedMapMarker
        if (prevHotelMapMarker != null) {
            prevHotelMapMarker.isSelected = false
            if (!prevHotelMapMarker.hotel.isSoldOut) {
                hotelMapClusterRenderer.getMarker(prevHotelMapMarker)?.setIcon(prevHotelMapMarker.getHotelMarkerIcon())
            }
        }
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

        // todo find out what this is for.
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

    private fun initClusterManagement() {
        clusterManager = ClusterManager(context, googleMap)
        clusterManager!!.setAlgorithm(HotelMapClusterAlgorithm())
        hotelMapClusterRenderer = HotelMapClusterRenderer(context, googleMap, clusterManager!!,
                viewModel.clusterChangeSubject)
        clusterManager!!.setRenderer(hotelMapClusterRenderer)
        clusterManager!!.setOnClusterItemClickListener(markerClickListener)
        clusterManager!!.setOnClusterClickListener(clusterClickListener)
    }

    private inner class MarkerClickListener : ClusterManager.OnClusterItemClickListener<HotelMapMarker> {
        override fun onClusterItemClick(marker: HotelMapMarker): Boolean {
            mapPinClickedSubject.onNext(Unit)
            selectMarker(marker)
            updateCarouselItems()
            return true
        }
    }

    private inner class ClusterClickListener : ClusterManager.OnClusterClickListener<HotelMapMarker> {
        override fun onClusterClick(cluster: Cluster<HotelMapMarker>): Boolean {
            hideCarousel()
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

    private fun updateCarouselItems() {
        //todo share some of this with jumpToBounds()
        val selectedHotels = hotelMapMarkers.filter { it.isSelected }.map { it.hotel }
        var hotelItems = hotelMapMarkers.filter { !it.isClustered }.map { it.hotel }
        if (!selectedHotels.isEmpty()) {
            val hotel = selectedHotels.first()
            val hotelLocation = Location("selected")
            hotelLocation.latitude = hotel.latitude
            hotelLocation.longitude = hotel.longitude
            viewModel.asyncSortByLocation(hotelLocation, hotelItems).subscribe { sortedHotels ->
                (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setItems(sortedHotels)
                mapCarouselRecycler.scrollToPosition(0)
            }
        }
    }

    private fun addMapListeners() {
        var currentZoom = -1f

        googleMap?.setOnCameraChangeListener { position ->
            synchronized(currentZoom) {
                if (Math.abs(currentZoom - position.zoom) > .5) {
                    clusterManager?.cluster()
                    currentZoom = position.zoom
                }

                //todo for full screen this should be always.
//                if (Strings.equals(currentState, ResultsMap::class.java.name)) {
//                    showSearchThisArea()
//                }
            }
        }
        googleMap?.setOnMarkerClickListener(clusterManager!!)

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

    private fun lastBestLocationSafe(): Location {
        val minTime = DateTime.now().millis - DateUtils.HOUR_IN_MILLIS
        val loc = LocationServices.getLastBestLocation(context, minTime)
        val location = Location("lastBestLocationSafe")
        location.latitude = loc?.latitude ?: 0.0
        location.longitude = loc?.longitude ?: 0.0
        return location
    }

    private fun showCarousel() {
        mapCarouselContainer.visibility = View.VISIBLE
        googleMap?.setPadding(0, getToolbarHeight(), 0, mapCarouselContainer.height)
        carouselShownSubject.onNext(mapCarouselContainer.height)
    }

    private fun hideCarousel() {
        mapCarouselContainer.visibility = View.INVISIBLE
        googleMap?.setPadding(0, getToolbarHeight(), 0, 0)
        carouselShownSubject.onNext(0)
    }

    private fun getToolbarHeight(): Int {
        val tv = TypedValue();
        context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
        return TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
    }
}