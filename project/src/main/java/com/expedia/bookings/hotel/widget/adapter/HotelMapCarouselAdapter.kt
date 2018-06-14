package com.expedia.bookings.hotel.widget.adapter

import android.content.Context
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.widget.HotelCarouselViewHolder
import com.expedia.bookings.hotel.widget.HotelMapCellViewHolder
import com.expedia.bookings.text.HtmlCompat
import com.expedia.util.endlessObserver
import com.mobiata.android.text.StrikethroughTagHandler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

class HotelMapCarouselAdapter(var hotels: List<Hotel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val hotelSubject = PublishSubject.create<Hotel>()
    val favoriteAddedSubject = PublishSubject.create<String>()
    val favoriteRemovedSubject = PublishSubject.create<String>()

    val hotelSoldOut = endlessObserver<String> { soldOutHotelId ->
        hotels.firstOrNull { it.hotelId == soldOutHotelId }?.isSoldOut = true
        hotelListItemsMetadata.firstOrNull { it.hotelId == soldOutHotelId }?.hotelSoldOut?.onNext(true)
    }

    var shopWithPoints = false

    private data class HotelListItemMetadata(val hotelId: String, val hotelSoldOut: BehaviorSubject<Boolean>)

    private val hotelListItemsMetadata: MutableList<HotelListItemMetadata> = ArrayList()

    override fun getItemCount(): Int {
        return hotels.size
    }

    fun setItems(newHotels: List<Hotel>) {
        hotels = newHotels
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val hotel = hotels[position]

        if (AbacusFeatureConfigManager.isBucketedForTest(holder.itemView.context, AbacusUtils.HotelResultsCellOnMapCarousel)) {
            holder as HotelMapCellViewHolder
            holder.bindHotelData(hotel, shopWithPoints)

            hotelListItemsMetadata.add(HotelListItemMetadata(holder.hotelId, holder.viewModel.soldOut))
        } else {
            holder as HotelCarouselViewHolder
            holder.bindHotelData(hotel, shopWithPoints)

            hotelListItemsMetadata.add(HotelListItemMetadata(holder.hotelId, holder.viewModel.soldOut))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val holder: RecyclerView.ViewHolder
        if (isBucketedToShowResultsCellOnMap(parent.context)) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.horizontal_hotel_cell, parent, false)
            holder = HotelMapCellViewHolder(view as ViewGroup)

            holder.hotelClickedSubject.subscribe {
                if (holder.adapterPosition >= 0 && holder.adapterPosition < hotels.count()) {
                    val hotel = hotels[holder.adapterPosition]
                    hotelSubject.onNext(hotel)
                }
            }

            holder.favoriteAddedSubject.subscribe(favoriteAddedSubject)
            holder.favoriteRemovedSubject.subscribe(favoriteRemovedSubject)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_marker_preview_cell, parent, false)
            holder = HotelCarouselViewHolder(view as ViewGroup)

            holder.hotelClickedSubject.subscribe {
                val hotel = hotels[holder.adapterPosition]
                hotelSubject.onNext(hotel)
            }
        }
        return holder
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        val hotelItemIndex = if (isBucketedToShowResultsCellOnMap(holder.itemView.context)) {
            holder as HotelMapCellViewHolder
            hotelListItemsMetadata.indexOfFirst { it.hotelId == holder.hotelId }
        } else {
            holder as HotelCarouselViewHolder
            hotelListItemsMetadata.indexOfFirst { it.hotelId == holder.hotelId }
        }

        if (hotelItemIndex != -1) {
            hotelListItemsMetadata.removeAt(hotelItemIndex)
        }
        super.onViewRecycled(holder)
    }

    private fun isBucketedToShowResultsCellOnMap(context: Context): Boolean {
        return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelResultsCellOnMapCarousel)
    }
}

// TODO: move this function to be a fun in HotelRate
fun priceFormatter(resources: Resources, rate: HotelRate?, strikeThrough: Boolean, shouldFallbackToZeroIfNegative: Boolean): CharSequence {

    if (rate == null) {
        return ""
    } else if (strikeThrough && !rate.isStrikeThroughPriceValid) { // #6801 - strikethrough price now optional from API
        return ""
    } else {
        val hotelPrice = rate.getDisplayMoney(strikeThrough, shouldFallbackToZeroIfNegative).getFormattedMoney(Money.F_NO_DECIMAL)
        return if (strikeThrough) HtmlCompat.fromHtml(resources.getString(R.string.strike_template, hotelPrice), null, StrikethroughTagHandler()) else hotelPrice
    }
}
