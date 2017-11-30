package com.expedia.bookings.hotel.map

import android.content.Context
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.util.AttributeSet
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
import com.google.maps.android.clustering.ClusterManager
import com.mobiata.android.LocationServices
import org.joda.time.DateTime
import rx.Completable
import rx.subjects.PublishSubject

class CleanHotelMapView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
        OnMapReadyCallback {

    //todo fix tracking.
    val mapPinClickedSubject = PublishSubject.create<Unit>()
    val hotelSelectedSubject = PublishSubject.create<Hotel>()

    private val mapContainer: FrameLayout by bindView(R.id.clean_map_container)
    private val mapCarouselContainer: ViewGroup by bindView(R.id.hotel_carousel_container)
    private val mapCarouselRecycler: HotelCarouselRecycler by bindView(R.id.hotel_carousel)

    private lateinit var mapView: MapView

    private var googleMap: GoogleMap? = null
    private var clusterManager: ClusterManager<MapItem>? = null
    private lateinit var hotelMapClusterRenderer: HotelMapClusterRenderer
    private val hotelIconFactory = HotelMarkerIconGenerator(context)

    private var mapItems = arrayListOf<MapItem>()

    val viewModel: HotelResultsMapViewModel

    private var mapReady = false

    private var orderedCarouselHotels = emptyList<Hotel>()
    private var currentHotels = emptyList<Hotel>()
    private var currentBounds: LatLngBounds? = null

    private val PICASSO_TAG = "HOTEL_RESULTS_LIST"

    init {
        View.inflate(context, R.layout.clean_hotel_results_map, this)
        viewModel = HotelResultsMapViewModel(context, lastBestLocationSafe())
        viewModel.clusterChangeSubject.subscribe {
            updateCarouselItems()
        }

        mapCarouselRecycler.adapter = HotelMapCarouselAdapter(emptyList(), hotelSelectedSubject)
        mapCarouselRecycler.addOnScrollListener(PicassoScrollListener(context, PICASSO_TAG))

        mapCarouselRecycler.showingHotelSubject.subscribe { hotel ->
            val markersForHotel = mapItems.filter { it.hotel.hotelId == hotel.hotelId }
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
                changeBounds()
            }
        }
    }

    fun clearMarkers() {
        clusterManager?.let { clusterManager ->
            mapItems.clear()
            clusterManager.clearItems()
            clusterManager.cluster()
            mapCarouselContainer.visibility = View.INVISIBLE
        }
    }

    fun moveCamera(lat: Double, lng: Double) {
        val latLng = LatLng(lat, lng)
        val cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(8f)
                .build()
        googleMap?.setPadding(0, 0, 0, 0)
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun createNewMarkers() {
        if (clusterManager == null) {
            return
        }
        if (currentHotels.isEmpty()) {
            return
        }
        //createHotelMarkerIcon should run in a separate thread since its heavy and hangs on the UI thread
        currentHotels.forEach { hotel ->
            val mapItem = MapItem(context, LatLng(hotel.latitude, hotel.longitude), hotel, hotelIconFactory)
            mapItems.add(mapItem)
            clusterManager!!.addItem(mapItem)
        }
        clusterManager?.cluster()
    }

    private fun changeBounds() {
        currentBounds?.let { bounds ->
            val center = bounds.center
            moveCamera(center.latitude, center.longitude)

            val location = Location("currentRegion")
            location.latitude = center.latitude
            location.longitude = center.longitude
            orderedCarouselHotels = viewModel.sortByLocation(location, currentHotels)
        }
    }

    private fun selectMarker(mapItem: MapItem, shouldZoom: Boolean = false, animateCarousel: Boolean = true) {
        if (clusterManager == null) {
            return
        }
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
        viewModel.selectedMapMarker = mapItem

        //todo fix me
//        if (animateCarousel && currentState == BaseHotelResultsPresenter.ResultsMap::class.java.name) {
//            animateMapCarouselIn()
//        }
    }

    private fun clearPreviousMarker() {
        val prevMapItem = viewModel.selectedMapMarker
        if (prevMapItem != null) {
            prevMapItem.isSelected = false
            if (!prevMapItem.hotel.isSoldOut) {
                hotelMapClusterRenderer.getMarker(prevMapItem)?.setIcon(prevMapItem.getHotelMarkerIcon())
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

        clusterManager!!.setOnClusterItemClickListener {
            mapPinClickedSubject.onNext(Unit)
            selectMarker(it)
            updateCarouselItems()
            true
        }

        clusterManager!!.setOnClusterClickListener {
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

    private fun updateCarouselItems() {
        //todo share some of this with changeBounds()
        val selectedHotels = mapItems.filter { it.isSelected }.map { it.hotel }
        var hotelItems = mapItems.filter { !it.isClustered }.map { it.hotel }
        if (!selectedHotels.isEmpty()) {
            val hotel = selectedHotels.first()
            val hotelLocation = Location("selected")
            hotelLocation.latitude = hotel.latitude
            hotelLocation.longitude = hotel.longitude
            hotelItems = viewModel.sortByLocation(hotelLocation, hotelItems)
            (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setItems(hotelItems)
            mapCarouselRecycler.scrollToPosition(0)
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

    private class CreateMarkersCompletable : Completable() {

    }

    private fun lastBestLocationSafe(): Location {
        val minTime = DateTime.now().millis - DateUtils.HOUR_IN_MILLIS
        val loc = LocationServices.getLastBestLocation(context, minTime)
        val location = Location("lastBestLocationSafe")
        location.latitude = loc?.latitude ?: 0.0
        location.longitude = loc?.longitude ?: 0.0
        return location
    }

    /*
        TODO KILL ME
     */
    protected fun animateMapCarouselIn() {
//        if (mapCarouselContainer.visibility != View.VISIBLE) {
//            val carouselAnimation = mapCarouselContainer.animate().translationX(0f).setInterpolator(DecelerateInterpolator()).setStartDelay(400)
//            mapCarouselContainer.translationX = screenWidth
//
//            var onLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null //need to know carousel height before fab can properly animate.
//            onLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
//                fab.animate().translationY(-fabHeightOffset()).setInterpolator(DecelerateInterpolator()).withEndAction {
//                    carouselAnimation.start()
//                }.start()
//                mapCarouselContainer.viewTreeObserver.removeOnGlobalLayoutListener(onLayoutListener)
//            }
//
//            mapCarouselContainer.viewTreeObserver.addOnGlobalLayoutListener(onLayoutListener)
//            mapCarouselContainer.visibility = View.VISIBLE
//        }
    }

    protected fun animateMapCarouselOut() {
//        if (mapCarouselContainer.visibility != View.INVISIBLE) {
//            val carouselAnimation = mapCarouselContainer.animate().translationX(screenWidth).setInterpolator(DecelerateInterpolator())
//            carouselAnimation.withEndAction {
//                mapCarouselContainer.visibility = View.INVISIBLE
//                animateFab(0f)
//            }
//            carouselAnimation.start()
//        }
    }
}