package com.expedia.bookings.widget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.extension.shouldShowCircleForRatings
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeContentDescription
import com.expedia.util.PermissionsUtils.havePermissionToAccessLocation
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeRating
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelMapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.properties.Delegates

class HotelMapView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), OnMapReadyCallback {
    val MAP_ZOOM_LEVEL = 15f

    var mapView: MapView by Delegates.notNull()
    val selectRoomContainer: View by bindView(R.id.map_view_select_room_container)
    val selectRoomStrikethroughPrice: TextView by bindView(R.id.map_view_select_room_strikethrough_price)
    val selectRoomPrice: TextView by bindView(R.id.map_view_select_room_price)
    val selectRoomLabel: TextView by bindView(R.id.map_view_select_room)

    val toolBar: Toolbar by bindView(R.id.infosite_map_toolbar)
    val toolBarTitle: TextView by bindView(R.id.hotel_name_text)
    var toolBarRating: StarRatingBar by Delegates.notNull()

    var googleMap: GoogleMap? = null

    private var mapReady = false
    private var queuedLatLng: LatLng? = null

    init {
        View.inflate(context, R.layout.widget_hotel_map, this)

        if (shouldShowCircleForRatings()) {
            toolBarRating = findViewById<StarRatingBar>(R.id.hotel_map_circle_rating_bar)
        } else {
            toolBarRating = findViewById<StarRatingBar>(R.id.hotel_map_star_rating_bar)
        }
        toolBarRating.visibility = View.VISIBLE

        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            toolBar.setPadding(0, statusBarHeight, 0, 0)
        }
        Ui.showTransparentStatusBar(context)
        toolBar.setTitleTextAppearance(context, R.style.ToolbarTitleTextAppearance)

        val navIcon: ArrowXDrawable = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.BACK)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolBar.navigationIcon = navIcon
        toolBar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_cont_desc)

        toolBar.setNavigationOnClickListener { view ->
            (context as Activity).onBackPressed()
        }
    }

    var viewmodel: HotelMapViewModel by notNullAndObservable { vm ->
        //Hook outputs for View
        vm.hotelName.subscribeText(toolBarTitle)
        vm.hotelStarRating.subscribeRating(toolBarRating)
        vm.hotelStarRatingContentDescription.subscribeContentDescription(toolBarRating)
        vm.hotelStarRatingVisibility.subscribeVisibility(toolBarRating)
        vm.strikethroughPrice.subscribeText(selectRoomStrikethroughPrice)
        vm.strikethroughPriceVisibility.subscribeVisibility(selectRoomStrikethroughPrice)
        vm.fromPrice.subscribeText(selectRoomPrice)
        vm.fromPriceVisibility.subscribeVisibility(selectRoomPrice)
        vm.selectARoomInvisibility.subscribeInverseVisibility(selectRoomContainer)
        vm.selectRoomContDescription.subscribe { it ->
            selectRoomContainer.contentDescription = it
        }
        //Hook inputs from View
        selectRoomContainer.subscribeOnClick(endlessObserver<Unit> {
            (context as Activity).onBackPressed()
            vm.selectARoomObserver.onNext(Unit)
            if (viewmodel.lob == LineOfBusiness.PACKAGES) {
                PackagesTracking().trackHotelMapViewSelectRoomClick()
            } else {
                HotelTracking.trackLinkHotelMapSelectRoom()
            }
        })

        vm.hotelLatLng.subscribe { latLngArray ->
            val latLng = LatLng(latLngArray[0], latLngArray[1])
            if (mapReady) {
                placeMarker(latLng)
            } else {
                queuedLatLng = latLng
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        MapsInitializer.initialize(context)
        googleMap = map
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.mapType = GoogleMap.MAP_TYPE_NONE
        googleMap?.isMyLocationEnabled = havePermissionToAccessLocation(context)
        mapReady = true

        queuedLatLng?.let { latLng -> placeMarker(latLng) }
        queuedLatLng = null
    }

    private fun placeMarker(latLng: LatLng) {
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM_LEVEL))
        googleMap?.clear()
        addMarker(googleMap, latLng)
    }

    private fun addMarker(googleMap: GoogleMap?, hotelLatLng: LatLng) {
        googleMap ?: return
        val marker = MarkerOptions()
        marker.position(hotelLatLng)
        val drawableId = Ui.obtainThemeResID(context, R.attr.map_pin_drawable)
        marker.icon(BitmapDescriptorFactory.fromResource(drawableId))
        googleMap.addMarker(marker)
    }
}
