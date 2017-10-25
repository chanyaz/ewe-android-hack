package com.expedia.bookings.itin.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.EBRequestParams
import com.expedia.bookings.data.trips.Event
import com.expedia.bookings.data.trips.EventbriteResponse
import com.expedia.bookings.data.trips.TcsData
import com.expedia.bookings.data.trips.TcsRequestParams
import com.expedia.bookings.data.trips.TcsResponse
import com.expedia.bookings.data.trips.Trail
import com.expedia.bookings.data.trips.TrailsRequestParams
import com.expedia.bookings.data.trips.YelpAccessToken
import com.expedia.bookings.data.trips.YelpBusiness
import com.expedia.bookings.data.trips.YelpRequestParams
import com.expedia.bookings.data.trips.YelpResponse
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.itin.widget.HotelItinToolbar
import com.expedia.bookings.itin.widget.ItinMapMarkerCard
import com.expedia.bookings.services.TripsHotelMapServices
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.PermissionsUtils.havePermissionToAccessLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import org.joda.time.format.ISODateTimeFormat
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription



class HotelItinExpandedMapActivity : HotelItinBaseActivity(), OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {
    override fun onMarkerClick(p0: Marker?): Boolean {
        val x = markerList[p0]
        selectedMarker = x!!
        when(x)
        {
            is Event -> {
                markerWidget.visibility = View.VISIBLE
                markerWidget.setTitle(x.name.text)
                markerWidget.setBody(x.description.text)
                markerWidget.setImage(x.logo.url)
                markerWidget.hideChev(false)
            }
            is TcsData -> {
                markerWidget.visibility = View.VISIBLE
                markerWidget.setTitle(x.images.data[0].alt)
                markerWidget.setBody(x.descriptions.data[0].value)
                markerWidget.setImage(x.images.data[0].url)
                markerWidget.hideChev(true)
            }
            is Trail -> {
                markerWidget.visibility = View.VISIBLE
                markerWidget.setTitle(x.name)
                markerWidget.setBody(x.description)
                markerWidget.hideImage(true)
                markerWidget.hideChev(true)
            }
            is ItinCardDataHotel -> {
                markerWidget.setTitle(x.propertyName)
                markerWidget.setBody(x.property.localPhone)
                markerWidget.setImage(x.headerImageUrls[0])
            }
            is YelpBusiness -> {
                markerWidget.visibility = View.VISIBLE
                markerWidget.setTitle(x.name)
                //markerWidget.setBody(x.snippet_text)
                markerWidget.setImage(x.image_url)
                markerWidget.hideChev(false)
            }

        }
        return false
    }
    lateinit var selectedMarker: Any

    override fun onCameraIdle() {
        if (moveStarted) {
            if (!zoomTracked) {
                if (googleMap?.cameraPosition?.zoom != MAP_ZOOM_LEVEL) {
                    zoomTracked = true
                    if (googleMap?.cameraPosition!!.zoom > MAP_ZOOM_LEVEL) {
                        OmnitureTracking.trackItinExpandedMapZoomIn()
                    } else {
                        OmnitureTracking.trackItinExpandedMapZoomOut()
                    }
                }
            }
            if (checkForPan()) {
                OmnitureTracking.trackItinExpandedMapZoomPan()
                panTracked = true

            }
            if (panTracked && zoomTracked) {
                fullyTracked = true
            }
            moveStarted = false

        }
    }

    fun checkForPan(): Boolean {
        return !panTracked && currentZoom == googleMap?.cameraPosition?.zoom
                && googleMap?.cameraPosition?.target != startPosition
    }

    override fun onCameraMoveStarted(reason: Int) {
        if (reason == OnCameraMoveStartedListener.REASON_GESTURE && !fullyTracked) {
            moveStarted = true
            currentZoom = googleMap?.cameraPosition!!.zoom
        }
    }


    lateinit var itinCardDataHotel: ItinCardDataHotel
    private val mapView: MapView by lazy {
        findViewById(R.id.expanded_map_view_hotel) as MapView
    }
//    val directionsButton: FrameLayout by lazy {
//        findViewById(R.id.directions_button) as FrameLayout
//    }
//    private val directionsButtonText: TextView by lazy {
//        findViewById(R.id.directions_button_text) as TextView
//    }

    val musicButton: LinearLayout by bindView(R.id.music_button)
    val musicList = mutableListOf<Event>()
    val musicText: TextView by bindView(R.id.music_text)
    val sportsButton: LinearLayout by bindView(R.id.sports_button)
    val sportsList = mutableListOf<Event>()
    val sportsText: TextView by bindView(R.id.sports_text)
    val trailsButton: LinearLayout by bindView(R.id.trails_button)
    val trailsList = mutableListOf<Trail>()
    val trailsText: TextView by bindView(R.id.trails_text)
    val poiButton: LinearLayout by bindView(R.id.poi_button)
    val poiList = mutableListOf<TcsData>()
    val poiText: TextView by bindView(R.id.poi_text)
    val foodButton: LinearLayout by bindView(R.id.food_button)
    val foodList = mutableListOf<YelpBusiness>()
    val foodText: TextView by bindView(R.id.food_text)
    val drinksButton: LinearLayout by bindView(R.id.drinks_button)
    val drinksList = mutableListOf<YelpBusiness>()
    val drinksText: TextView by bindView(R.id.drinks_text)

    //TODO
    //val activitiesButton: LinearLayout by bindView(R.id.activities_button)
    //val activitiesMarkerMap: MutableList<YelpBusiness>? = null

    private var googleMap: GoogleMap? = null
    private val MAP_ZOOM_LEVEL = 14f
    private lateinit var startPosition: LatLng
    private var fullyTracked = false
    private var zoomTracked = false
    private var panTracked = false
    private var moveStarted = false
    private var currentZoom = 0f
    private lateinit var markerList: HashMap<Marker?, Any>
    private lateinit var markerWidget: ItinMapMarkerCard
    private val toolbar: HotelItinToolbar by lazy {
        findViewById(R.id.widget_hotel_itin_toolbar) as HotelItinToolbar
    }

    private val tripsHotelMapServices: TripsHotelMapServices by lazy {
        TripsHotelMapServices(Schedulers.io(), AndroidSchedulers.mainThread())
    }
    var compositeSubscription: CompositeSubscription = CompositeSubscription()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultTripComponents()
        setContentView(R.layout.hotel_itin_expanded_map)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
        markerList = HashMap()
        markerWidget = findViewById(R.id.marker_card) as ItinMapMarkerCard

        musicButton.setOnClickListener {
            turnAllButtonsOff()
            changeButtonColorOn(musicButton, musicText)
            if (musicList.isNotEmpty()) {
                googleMap?.clear()
                markerList = HashMap()
                markerList.put(addMarker(R.drawable.ic_hotel_pin, getHotelLatLong(),itinCardDataHotel.propertyName), itinCardDataHotel)
                for (event in musicList) {
                    markerList.put(addMarker(R.drawable.ic_music_pin, LatLng(
                            event.venue.latitude,
                            event.venue.longitude
                    ),event.name.text),event)
                }
            }
        }
        sportsButton.setOnClickListener {
            turnAllButtonsOff()
            changeButtonColorOn(sportsButton, sportsText)
            if (sportsList.isNotEmpty()) {
                googleMap?.clear()
                markerList = HashMap()
                markerList.put(addMarker(R.drawable.ic_hotel_pin, getHotelLatLong(),
                        itinCardDataHotel.propertyName),itinCardDataHotel)
                for (event in sportsList) {
                    markerList.put(addMarker(R.drawable.ic_sports_pin, LatLng(
                            event.venue.latitude,
                            event.venue.longitude
                    ),event.name.text),event)
                }
            }
        }
        trailsButton.setOnClickListener {
            turnAllButtonsOff()
            changeButtonColorOn(trailsButton, trailsText)
            if (trailsList.isNotEmpty()) {
                googleMap?.clear()
                markerList = HashMap()
                markerList.put(addMarker(R.drawable.ic_hotel_pin, getHotelLatLong(),
                        itinCardDataHotel.propertyName),itinCardDataHotel)
                for (trail in trailsList) {
                    markerList.put(addMarker(R.drawable.ic_trail_pin, LatLng(
                            trail.latitude.toDouble(),
                            trail.longitude.toDouble()
                    ),trail.name),trail)
                }
            }
        }
        poiButton.setOnClickListener {
            turnAllButtonsOff()
            changeButtonColorOn(poiButton, poiText)
            if (poiList.isNotEmpty()) {
                googleMap?.clear()
                markerList = HashMap()
                markerList.put(addMarker(R.drawable.ic_hotel_pin, getHotelLatLong(),
                        itinCardDataHotel.propertyName),itinCardDataHotel)
                for (poi in poiList) {
                    markerList.put(addMarker(R.drawable.ic_landmark_pin, LatLng(
                            poi.geo.latitude.toDouble(),
                            poi.geo.longitude.toDouble()
                    ),poi.images.data[0].alt),poi)
                }
            }
        }
        foodButton.setOnClickListener {
            turnAllButtonsOff()
            changeButtonColorOn(foodButton, foodText)
            if (foodList.isNotEmpty()) {
                googleMap?.clear()
                markerList = HashMap()
                markerList.put(addMarker(R.drawable.ic_hotel_pin, getHotelLatLong(),
                        itinCardDataHotel.propertyName),itinCardDataHotel)
                for (food in foodList) {
                    markerList.put(addMarker(R.drawable.ic_restaurant_pin, LatLng(
                            food.coordinates.latitude,
                            food.coordinates.longitude
                    ),food.name), food)
                }
            }
        }
        drinksButton.setOnClickListener {
            turnAllButtonsOff()
            changeButtonColorOn(drinksButton, drinksText)
            if (drinksList.isNotEmpty()) {
                googleMap?.clear()
                markerList = HashMap()
                markerList.put(addMarker(R.drawable.ic_hotel_pin, getHotelLatLong(),
                        itinCardDataHotel.propertyName),itinCardDataHotel)
                for (drink in drinksList) {
                    markerList.put(addMarker(R.drawable.ic_restaurant_pin, LatLng(
                            drink.coordinates.latitude,
                            drink.coordinates.longitude
                    ),drink.name),drink)
                }
            }
        }
    }

    fun changeButtonColorOff(button: LinearLayout, textView: TextView) {
        button.background = getDrawable(R.drawable.trips_hotel_maps_filter_button_background_off)
        textView.setTextColor(ContextCompat.getColor(this, R.color.exp_launch_blue))
    }

    fun changeButtonColorOn(button: LinearLayout, textView: TextView) {
        button.background = getDrawable(R.drawable.trips_hotel_maps_filter_button_background_on)
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    fun turnAllButtonsOff() {
        val buttonTexts = listOf(
                Pair(musicButton, musicText),
                Pair(sportsButton, sportsText),
                Pair(poiButton, poiText),
                Pair(trailsButton, trailsText),
                Pair(foodButton, foodText),
                Pair(drinksButton, drinksText)
        )
        for (buttonText in buttonTexts) {
            changeButtonColorOff(buttonText.first, buttonText.second)
        }

        markerWidget.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val focused = selectedMarker
                when(focused) {
                    is Event -> startActivity(buildWebViewIntent(focused.name.text, focused.url).intent)
                    is YelpBusiness ->  startActivity(buildWebViewIntent(focused.name, focused.url).intent)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        updateItinCardDataHotel()

        compositeSubscription.add(tripsHotelMapServices.getPoiNearby(
                TcsRequestParams(itinCardDataHotel.propertyLocation.latitude.toString(),
                        itinCardDataHotel.propertyLocation.longitude.toString(),
                        TripsHotelMapServices.Keys.TCS.value,
                        "EN",
                        arrayOf("POI"),
                        2,
                        true),
                poiObserver))

        compositeSubscription.add(tripsHotelMapServices.getEvents(EBRequestParams(
                itinCardDataHotel.propertyLocation.latitude,
                itinCardDataHotel.propertyLocation.longitude,
                "5mi",
                itinCardDataHotel.startDate.toString(ISODateTimeFormat.dateHourMinuteSecond()),
                itinCardDataHotel.endDate.plusDays(1).toString(ISODateTimeFormat.dateHourMinuteSecond()),
                "venue",
                "103,108"),
                ebObserver))

        compositeSubscription.add(tripsHotelMapServices.getTrails(
                TrailsRequestParams(
                        itinCardDataHotel.propertyLocation.latitude.toString(),
                        itinCardDataHotel.propertyLocation.longitude.toString(),
                        TripsHotelMapServices.Keys.TRAILS.value,
                        "50",
                        "75"
                ), trailsObserver)
        )
        compositeSubscription.add(tripsHotelMapServices.getYelpAccessToken(yelpAccessTokenObserver))
    }

    companion object {
        private const val ID_EXTRA = "ITINID"

        fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, HotelItinExpandedMapActivity::class.java)
            i.putExtra(HotelItinExpandedMapActivity.ID_EXTRA, id)
            return i
        }
    }

    fun setUpWidgets(itinCardDataHotel: ItinCardDataHotel) {
        toolbar.setUpWidget(itinCardDataHotel, itinCardDataHotel.propertyName, itinCardDataHotel.propertyLocation.toCityStateCountryAddressFormattedString())
        toolbar.setNavigationOnClickListener {
            super.finish()
            overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
        }
//        directionsButtonText.setCompoundDrawablesTint(ContextCompat.getColor(this, R.color.white))
//        AccessibilityUtil.appendRoleContDesc(directionsButton, directionsButtonText.text.toString(), R.string.accessibility_cont_desc_role_button)
//        directionsButton.setOnClickListener {
//            val hotelLat = itinCardDataHotel.propertyLocation.latitude
//            val hotelLong = itinCardDataHotel.propertyLocation.longitude
//            val propertyName = itinCardDataHotel.propertyName
//
//            val uri = String.format(Locale.getDefault(), "geo:0,0?q=") + android.net.Uri.encode(String.format("%s@%f,%f", propertyName, hotelLat, hotelLong), "UTF-8")
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
//            intent.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
//            intent.flags = Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
//            intent.data = Uri.parse(uri)
//            this.startActivity(intent)
//
//            OmnitureTracking.trackItinHotelDirectionsButton()
//        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap?.isMyLocationEnabled = havePermissionToAccessLocation(this)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(getHotelLatLong(), MAP_ZOOM_LEVEL))
        markerList.put(addMarker(R.drawable.ic_hotel_pin, getHotelLatLong(), itinCardDataHotel.propertyName),itinCardDataHotel)
        googleMap?.setOnCameraMoveStartedListener(this)
        googleMap?.setOnCameraIdleListener(this)
        startPosition = googleMap?.cameraPosition!!.target
        currentZoom = MAP_ZOOM_LEVEL
        googleMap?.setOnMarkerClickListener(this)

    }

    override fun updateItinCardDataHotel() {
        val freshItinCardDataHotel = getItineraryManager().getItinCardDataFromItinId(intent.getStringExtra(ID_EXTRA)) as ItinCardDataHotel?
        if (freshItinCardDataHotel == null) {
            finish()
        } else {
            itinCardDataHotel = freshItinCardDataHotel
            setUpWidgets(itinCardDataHotel)
        }
    }


    private fun addMarker(icon: Int, latlong: LatLng, title: String): Marker? {
        val marker = MarkerOptions()
        marker.position(latlong)
        marker.icon(bitmapDescriptorFromVector(this, icon))
        marker.title(title)
        return googleMap?.addMarker(marker)
    }

    private fun getHotelLatLong(): LatLng {
        val hotelLat = itinCardDataHotel.propertyLocation.latitude
        val hotelLong = itinCardDataHotel.propertyLocation.longitude
        return LatLng(hotelLat, hotelLong)
    }

    private val poiObserver: Observer<TcsResponse> = object : Observer<TcsResponse> {
        override fun onNext(t: TcsResponse?) {
            //Example: fetching the description of the first item from the response
            if (t != null) {
                val pointsOfInterest = t.sections.poi.data
                if (pointsOfInterest.isNotEmpty()) poiButton.visibility = View.VISIBLE
                for (poi in pointsOfInterest) {
                    poiList.add(poi)
                }
                Log.d("TCSRESPONSE: ", t.sections.poi.data[0].descriptions.data[0].value)
            }
        }

        override fun onError(e: Throwable?) {
            Log.d("TCSRESPONSE: ", e?.toString())
        }

        override fun onCompleted() {
        }
    }

    private val ebObserver: Observer<EventbriteResponse> = object : Observer<EventbriteResponse> {
        override fun onError(e: Throwable?) {
            Log.d("EBRESPONSE: ", e?.toString())
        }

        override fun onNext(t: EventbriteResponse?) {
            if (t != null) {
                val events = t.events
                for (event in events) {
                    when (event.category_id) {
                        "103" -> {
                            musicButton.visibility = View.VISIBLE
                            musicList.add(event)
                        }
                        "108" -> {
                            sportsButton.visibility = View.VISIBLE
                            sportsList.add(event)
                        }
                    }
                }
                Log.d("EBRESPONSE: ", t.events[0].name.text)
            }
        }

        override fun onCompleted() {
        }
    }

    private val trailsObserver: Observer<Array<Trail>> = object : Observer<Array<Trail>> {
        override fun onError(e: Throwable?) {
            Log.d("TRAILSRESPONSE: ", e?.toString())
        }

        override fun onNext(trails: Array<Trail>?) {
            if (trails != null) {
                if (trails.isNotEmpty()) trailsButton.visibility = View.VISIBLE
                for (trail: Trail in trails) {
                    trailsList.add(trail)
                    Log.d("TRAILSRESPONSE: ", trail.id)
                }
            }
        }

        override fun onCompleted() {
        }

    }

    private val yelpAccessTokenObserver: Observer<YelpAccessToken> = object : Observer<YelpAccessToken> {
        override fun onNext(t: YelpAccessToken?) {
            if (t != null) {
                compositeSubscription.add(tripsHotelMapServices.getYelpSearchResponse(
                        YelpRequestParams(
                                t.access_token,
                                "restaurants",
                                itinCardDataHotel.propertyLocation.latitude.toString(),
                                itinCardDataHotel.propertyLocation.longitude.toString(),
                                50,
                                40000,
                                "rating"
                        ),
                        yelpRestaurantResponseObserver
                ))
                compositeSubscription.add(tripsHotelMapServices.getYelpSearchResponse(
                        YelpRequestParams(
                                t.access_token,
                                "bars",
                                itinCardDataHotel.propertyLocation.latitude.toString(),
                                itinCardDataHotel.propertyLocation.longitude.toString(),
                                50,
                                40000,
                                "rating"
                        ),
                        yelpBarsResponseObserver
                ))
            }
        }

        override fun onError(e: Throwable?) {
            Log.d("YELPACCESSTOKEN: ", e?.toString())
        }

        override fun onCompleted() {
        }

    }

    private val yelpRestaurantResponseObserver: Observer<YelpResponse> = object : Observer<YelpResponse> {
        override fun onCompleted() {
        }

        override fun onNext(response: YelpResponse?) {
            if (response != null) {
                val businesses = response.businesses
                if (businesses.isNotEmpty()) foodButton.visibility = View.VISIBLE
                for (business in businesses) {
                    foodList.add(business)
                    Log.d("YELPRESTAURANT: ", business.toString())
                }
            }
        }

        override fun onError(e: Throwable?) {
            Log.d("YELPRESTAURANT: ", e?.toString())
        }

    }

    private val yelpBarsResponseObserver: Observer<YelpResponse> = object : Observer<YelpResponse> {
        override fun onCompleted() {
        }

        override fun onNext(t: YelpResponse?) {
            if (t != null) {
                val businesses = t.businesses
                if (businesses.isNotEmpty()) drinksButton.visibility = View.VISIBLE
                for (business in businesses) {
                    drinksList.add(business)
                    Log.d("YELPBARS: ", business.toString())
                }
            }
        }

        override fun onError(e: Throwable?) {
            Log.d("YELPBARS: ", e?.toString())
        }

    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        val bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    override fun onDestroy() {
        super.onDestroy()
        compositeSubscription.unsubscribe()
    }

    private fun buildWebViewIntent(title: String, url: String): WebViewActivity.IntentBuilder {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(this)
        builder.setUrl(url)
        builder.setTitle(title)
        builder.setAllowMobileRedirects(false)
        return builder
    }
}
