package com.expedia.bookings.hotel.map

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ui.IconGenerator
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject

class HotelMapClusterRenderer(private val context: Context, map: GoogleMap?, clusterManager: ClusterManager<HotelMapMarker>?, clusterChangeSubject: PublishSubject<Unit>) : DefaultClusterRenderer<HotelMapMarker>(context, map, clusterManager, clusterChangeSubject) {
    private val clusterIconGenerator = IconGenerator(context)
    private val clusterCountText: TextView
    private val clusterRangeText: TextView

    init {
        val multiHotel = LayoutInflater.from(context).inflate(R.layout.map_multi_hotel, null)
        clusterIconGenerator.setContentView(multiHotel)
        clusterIconGenerator.setTextAppearance(R.style.MarkerTextAppearance)
        clusterCountText = multiHotel.findViewById<TextView>(R.id.hotel_count)
        clusterRangeText = multiHotel.findViewById<TextView>(R.id.price_range)
    }

    override fun onBeforeClusterItemRendered(hotelMapMarker: HotelMapMarker, markerOptions: MarkerOptions?) {
        hotelMapMarker.isClustered = false
        val icon = hotelMapMarker.getHotelMarkerIcon()
        val formattedPrice = hotelMapMarker.price?.getDisplayMoney(false, !hotelMapMarker.hotel.isPackage)

        val contDesc = Phrase.from(context, R.string.hotel_map_pin_price_cont_desc_TEMPLATE)
                .put("price" , formattedPrice?.formattedMoney ?: "")
                .format().toString()
        markerOptions?.icon(icon)?.title(contDesc)
    }

    override fun onBeforeClusterRendered(cluster: Cluster<HotelMapMarker>, markerOptions: MarkerOptions) {
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

        val clusterTitleContDesc = Phrase.from(context, R.string.hotel_map_pin_cluster_cont_desc_TEMPLATE)
                .put("hotel_count", clusterCountText.text.toString())
                .format().toString()
        markerOptions.icon(icon)?.title(clusterTitleContDesc)
        markerOptions.icon(icon)?.snippet(getButtonContDesc(clusterRangeText.text.toString()))
        markerOptions.icon(icon)
    }

    override fun shouldRenderAsCluster(cluster: Cluster<HotelMapMarker>): Boolean {
        return cluster.size > 10
    }

    fun createClusterMarkerIcon(context: Context, factory: IconGenerator, cluster: Cluster<HotelMapMarker>): BitmapDescriptor {
        var minPrice = Int.MAX_VALUE
        var minFormattedPrice: Money? = null
        val clusterExcludingSoldOut = cluster.items.filter { !it.hotel.isSoldOut }
        val isSoldOutCluster = clusterExcludingSoldOut.isEmpty()
        clusterExcludingSoldOut.forEach {
            val formattedPrice = it.price?.getDisplayMoney(false, !it.hotel.isPackage)
            val price = formattedPrice?.amount?.toInt() ?: 0
            if (minPrice > price) {
                minPrice = price
                minFormattedPrice = formattedPrice
            }
        }

        val template = if (cluster.items.first().hotel.isPackage) R.string.cluster_number_of_packages_template else R.string.cluster_number_of_hotels_template
        clusterCountText.text = context.getString(template, cluster.items.size.toString())
        clusterRangeText.text = if (isSoldOutCluster) context.getString(R.string.sold_out) else context.getString(R.string.cluster_price_from_range_template, minFormattedPrice?.getFormattedMoney(Money.F_NO_DECIMAL))
        factory.setBackground(getClusterBackground(isSoldOutCluster))
        return BitmapDescriptorFactory.fromBitmap(factory.makeIcon())
    }

    private fun getClusterBackground(isSoldOutCluster: Boolean) : Drawable {
        if (isSoldOutCluster) {
            return ContextCompat.getDrawable(context, R.drawable.sold_out_pin)
        } else {
            return ContextCompat.getDrawable(context, Ui.obtainThemeResID(context, R.attr.hotel_map_tooltip_drawable))
        }
    }

    private fun getButtonContDesc(description: String) : String {
        return Phrase.from(context, R.string.a11y_button_TEMPLATE)
                .put("description", description)
                .format().toString()
    }
}