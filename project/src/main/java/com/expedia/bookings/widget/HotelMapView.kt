package com.expedia.bookings.widget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
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
import rx.Observable
import rx.subjects.PublishSubject

public class HotelMapView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs), OnMapReadyCallback {
    val MAP_ZOOM_LEVEL = 15f

    val mapView: MapView by bindView(R.id.detailed_map_view)
    val selectRoomContainer: View by bindView(R.id.map_view_select_room_container)
    val selectRoomStrikethroughPrice: TextView by bindView(R.id.map_view_select_room_strikethrough_price)
    val selectRoomPrice: TextView by bindView(R.id.map_view_select_room_price)
    val selectRoomLabel: TextView by bindView(R.id.map_view_select_room)

    val toolBar: Toolbar by bindView(R.id.toolbar)
    val toolBarTitle: TextView by bindView(R.id.hotel_name_text)
    val toolBarRating: StarRatingBar by bindView(R.id.hotel_star_rating_bar)
    val toolBarBackground: View by bindView(R.id.toolbar_background)

    val mapReady = PublishSubject.create<GoogleMap>()

    init {
        View.inflate(context, R.layout.widget_hotel_map, this)
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            toolBar.setPadding(0, statusBarHeight, 0, 0)
        }
        Ui.showTransparentStatusBar(context)
        toolBar.setBackgroundColor(resources.getColor(android.R.color.transparent))
        toolBarBackground.layoutParams.height += statusBarHeight
        toolBar.setTitleTextAppearance(context, R.style.CarsToolbarTitleTextAppearance)

        val navIcon: ArrowXDrawable = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.BACK)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolBar.navigationIcon = navIcon

        toolBar.setNavigationOnClickListener { view ->
            (context as Activity).onBackPressed()
        }
    }

    var viewmodel: HotelMapViewModel by notNullAndObservable { vm ->
        //Hook outputs for View
        vm.hotelName.subscribeText(toolBarTitle)
        vm.hotelStarRating.subscribeRating(toolBarRating)
        vm.hotelStarRatingVisibility.subscribeVisibility(toolBarRating)
        vm.strikethroughPrice.subscribeText(selectRoomStrikethroughPrice)
        vm.strikethroughPriceVisibility.subscribeVisibility(selectRoomStrikethroughPrice)
        vm.fromPrice.subscribeText(selectRoomPrice)

        Observable.combineLatest(mapReady, vm.resetCameraPosition, vm.hotelLatLng) {
            googleMap, unit, hotelLatLng -> object {
                val googleMap = googleMap
                val hotelLatLng = hotelLatLng
            }
        }.subscribe {
            it.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.hotelLatLng[0], it.hotelLatLng[1]), MAP_ZOOM_LEVEL))
            it.googleMap.clear()
            addMarker(it.googleMap, it.hotelLatLng)
        }

        //Hook inputs from View
        selectRoomContainer.subscribeOnClick(endlessObserver<Unit> {
            (context as Activity).onBackPressed()
            vm.selectARoomObserver.onNext(Unit)
            HotelV2Tracking().trackLinkHotelV2MapSelectRoom()
        })

        mapView.getMapAsync(this);
    }

    private fun addMarker(googleMap:GoogleMap, hotelLatLng: DoubleArray) {
        val marker = MarkerOptions()
        marker.position(LatLng(hotelLatLng[0], hotelLatLng[1]))
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.hotels_pin))
        googleMap.addMarker(marker)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        MapsInitializer.initialize(context)
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.uiSettings.isZoomControlsEnabled = false

        mapReady.onNext(googleMap)
    }
}
