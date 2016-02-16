package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.utils.DefaultClusterRenderer
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ui.IconGenerator
import rx.subjects.PublishSubject
import kotlin.collections.filter
import kotlin.collections.first
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty

class HotelMapClusterRenderer(private val context: Context, private val map: GoogleMap?, private val clusterManager: ClusterManager<MapItem>?, private val isClusteringEnabled: Boolean, clusterChangeSubject: PublishSubject<Unit>) : DefaultClusterRenderer<MapItem>(context, map, clusterManager, clusterChangeSubject) {
    private val clusterIconGenerator = IconGenerator(context)
    private val clusterCountText: TextView
    private val clusterRangeText: TextView

    init {
        val multiHotel = LayoutInflater.from(context).inflate(R.layout.map_multi_hotel, null)
        clusterIconGenerator.setContentView(multiHotel)
        clusterIconGenerator.setTextAppearance(R.style.MarkerTextAppearance)
        clusterCountText = multiHotel.findViewById(R.id.hotel_count) as TextView
        clusterRangeText = multiHotel.findViewById(R.id.price_range) as TextView
    }

    override protected fun onBeforeClusterItemRendered(mapItem: MapItem, markerOptions: MarkerOptions?) {
        mapItem.isClustered = false
        if (mapItem.hotel.isSoldOut) {
            markerOptions?.icon(createHotelMarkerIcon(context, clusterIconGenerator, mapItem.hotel, false, mapItem.hotel.lowRateInfo.isShowAirAttached(), mapItem.hotel.isSoldOut))
        } else if (mapItem.isSelected) {
            markerOptions?.icon(mapItem.selectedIcon)?.title(mapItem.title)
        } else {
            markerOptions?.icon(mapItem.icon)?.title(mapItem.title)
        }
    }

    override protected fun onBeforeClusterRendered(cluster: Cluster<MapItem>, markerOptions: MarkerOptions) {
        val selected = cluster.items.filter { it.isSelected }
        selected.forEach { it.isClustered = false }
        val notSelected = cluster.items.filter { !it.isSelected }
        notSelected.forEach { it.isClustered = true }

        //This should work never be true when the algorithm works correctly.
        //This was happening due to caching. Can be removed once we come up with a custom non-caching Decorator.
        if (selected.isNotEmpty() && notSelected.isNotEmpty()) {
            Log.e("HotelMapClusterRenderer", "Selected and unselected marker cannot be in the same cluster: " + selected.first().hotel.localizedName)
        }
        // Note: this method runs on the UI thread. Don't spend too much time in here.
        val icon = createClusterMarkerIcon(context, clusterIconGenerator, cluster)
        markerOptions.icon(icon)
    }

    override protected fun shouldRenderAsCluster(cluster: Cluster<MapItem>): Boolean {
        return isClusteringEnabled && cluster.size > 10
    }

    fun createClusterMarkerIcon(context: Context, factory: IconGenerator, cluster: Cluster<MapItem>): BitmapDescriptor {
        var minPrice = Int.MAX_VALUE
        var minFormattedPrice: Money? = null
        cluster.items.forEach {
            val formattedPrice = HotelRate.getDisplayMoney(it.price, false)
            val price = formattedPrice.amount.toInt()
            if (minPrice > price) {
                minPrice = price
                minFormattedPrice = formattedPrice
            }
        }
        clusterCountText.text = context.getString(R.string.cluster_number_of_hotels_template, cluster.items.size.toString())
        clusterRangeText.text = context.getString(R.string.cluster_price_from_range_template, minFormattedPrice?.getFormattedMoney(Money.F_NO_DECIMAL))
        factory.setBackground(ContextCompat.getDrawable(context, R.drawable.hotel_tooltip))
        return BitmapDescriptorFactory.fromBitmap(factory.makeIcon())
    }

}