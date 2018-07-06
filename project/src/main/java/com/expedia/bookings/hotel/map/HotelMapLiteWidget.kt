package com.expedia.bookings.hotel.map

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.subjects.PublishSubject

class HotelMapLiteWidget(context: Context, attrs: AttributeSet?) :
        FrameLayout(context, attrs),
        OnMapReadyCallback {

    val hotelMapClickedSubject = PublishSubject.create<Unit>()

    private val liteMapView: MapView by bindView(R.id.lite_map_view)
    private lateinit var googleMap: GoogleMap
    private var queuedLatLng: LatLng? = null

    init {
        View.inflate(context, R.layout.widget_hotel_map_lite, this)

        with(liteMapView) {
            onCreate(null)
            getMapAsync(this@HotelMapLiteWidget)
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        MapsInitializer.initialize(context.applicationContext)
        googleMap = map ?: return

        googleMap.uiSettings?.isMapToolbarEnabled = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelMiniMapToolbar)
        queuedLatLng?.let { latLng -> placeMarker(latLng) }
        queuedLatLng = null
    }

    fun setLocation(latLong: LatLng) {
        if (!::googleMap.isInitialized) {
            queuedLatLng = latLong
            return
        }
        placeMarker(latLong)
    }

    fun reset() {
        if (!::googleMap.isInitialized) return
        with (googleMap) {
            clear()
            mapType = GoogleMap.MAP_TYPE_NONE
        }
    }

    private fun placeMarker(latLong: LatLng) {
        with(googleMap) {
            moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 13f))
            val marker = MarkerOptions()
            val drawableId = Ui.obtainThemeResID(context, R.attr.map_pin_drawable)
            marker.icon(BitmapDescriptorFactory.fromResource(drawableId))

            addMarker(marker.position(latLong))
            mapType = GoogleMap.MAP_TYPE_NORMAL
            setOnMapClickListener {
                hotelMapClickedSubject.onNext(Unit)
            }
        }
    }
}
