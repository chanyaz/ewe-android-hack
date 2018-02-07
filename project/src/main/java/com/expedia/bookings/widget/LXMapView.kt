package com.expedia.bookings.widget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.LatLong
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.vm.LXMapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.properties.Delegates

class LXMapView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs), OnMapReadyCallback {
    val MAP_ZOOM_LEVEL = 15f

    var mapView: MapView by Delegates.notNull()
    val selectActivityContainer: View by bindView(R.id.map_view_select_activity_container)
    val selectActivityPrice: TextView by bindView(R.id.map_view_select_activity_price)

    val toolBar: Toolbar by bindView(R.id.map_toolbar)
    val toolBarTitle: TextView by bindView(R.id.activity_detail_text)
    val toolBarSubtitle: TextView by bindView(R.id.activity_subtitle_text)
    val toolBarBackground: View by bindView(R.id.map_toolbar_background)

    var googleMap: GoogleMap? = null

    init {
        View.inflate(context, R.layout.activity_infosite_map, this)

        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            toolBar.setPadding(0, statusBarHeight, 0, 0)
        }
        Ui.showTransparentStatusBar(context)
        toolBar.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        toolBarBackground.layoutParams.height += statusBarHeight
        toolBar.setTitleTextAppearance(context, R.style.ToolbarTitleTextAppearance)
        val navIcon: ArrowXDrawable = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.BACK)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolBar.navigationIcon = navIcon
        toolBar.setNavigationOnClickListener { view ->
            (context as Activity).onBackPressed()
        }
    }

    var viewmodel: LXMapViewModel by notNullAndObservable { vm ->
        //Hook outputs for View
        vm.toolbarDetailText.subscribeText(toolBarTitle)
        vm.activityPrice.subscribeText(selectActivityPrice)
        vm.toolbarSubtitleText.subscribeText(toolBarSubtitle)

        //Hook inputs from View
        selectActivityContainer.subscribeOnClick(endlessObserver<Unit> {
            (context as Activity).onBackPressed()
        })

        vm.eventLatLng.subscribe {
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), MAP_ZOOM_LEVEL))
            googleMap?.clear()
            addMarker(googleMap, it)
        }

        vm.redemptionLocationsLatLng.subscribe {
            it ->
            it.forEach { addMarker(googleMap, it) }
        }
    }

    fun setMap(map: MapView) {
        mapView = map
    }

    fun addMarker(googleMap: GoogleMap?, hotelLatLng: LatLong) {
        if (googleMap != null) {
            val marker = MarkerOptions()
            marker.position(LatLng(hotelLatLng.latitude, hotelLatLng.longitude))
            val drawableId = Ui.obtainThemeResID(context, R.attr.map_pin_drawable)
            marker.icon(BitmapDescriptorFactory.fromResource(drawableId))
            googleMap.addMarker(marker)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        MapsInitializer.initialize(context)
        googleMap = map
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.mapType = GoogleMap.MAP_TYPE_NONE
    }
}
