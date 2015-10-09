package com.expedia.bookings.widget

import android.content.res.Resources
import android.graphics.Paint
import android.location.Location
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.presenter.hotel.HotelResultsPresenter
import com.expedia.bookings.utils.Akeakamai
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribe
import com.google.android.gms.maps.model.Marker
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.Collections
import kotlin.properties.Delegates

public class HotelMarkerPreviewAdapter(var hotels: ArrayList<HotelResultsPresenter.MarkerDistance>, val marker: Marker, val hotelSubject: PublishSubject<Hotel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var sortedHotelList = sortHotelList()

    override fun getItemCount(): Int {
        return sortedHotelList.size()
    }

    override fun onBindViewHolder(given: RecyclerView.ViewHolder?, position: Int) {
        val holder: HotelViewHolder = given as HotelViewHolder

        val viewModel = HotelViewModel(sortedHotelList.get(position).hotel, holder.resources)

        holder.bind(viewModel)
        holder.itemView.setOnClickListener(holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_marker_preview_cell, parent, false)
        val screen = Ui.getScreenSize(parent.getContext())
        var lp = view.findViewById(R.id.root).getLayoutParams()
        lp.width = screen.x
        return HotelViewHolder(view as ViewGroup)
    }

    // Create a sorted hotel list of the markers closest to the specified marker in increasing order
    fun sortHotelList(): ArrayList<HotelResultsPresenter.MarkerDistance> {
        var modifiedHotels = hotels

        var markerLat = marker.getPosition().latitude
        var markerLong = marker.getPosition().longitude

        for (item in modifiedHotels) {
            var a = Location("a")
            a.setLatitude(markerLat)
            a.setLongitude(markerLong)

            var b = Location("b")

            b.setLatitude(item.hotel.latitude)
            b.setLongitude(item.hotel.longitude)

            item.distance = a.distanceTo(b)
        }

        // Sort this list
        Collections.sort(modifiedHotels)
        return modifiedHotels
    }

    public inner class HotelViewHolder(root: ViewGroup) : RecyclerView.ViewHolder(root), View.OnClickListener {

        val resources: Resources by Delegates.lazy {
            itemView.getResources()
        }

        override fun onClick(view: View) {
            val hotel: Hotel = sortedHotelList.get(getAdapterPosition()).hotel
            hotelSubject.onNext(hotel)
        }

        val hotelPreviewImage: ImageView by bindView(R.id.hotel_preview_image)
        val hotelPreviewText: TextView by bindView(R.id.hotel_preview_text)
        val hotelPricePerNight: TextView by bindView(R.id.hotel_price_per_night)
        val hotelStrikeThroughPrice: TextView by bindView(R.id.hotel_strike_through_price)
        val hotelGuestRating: TextView by bindView(R.id.hotel_guest_rating)
        val hotelGuestRecommend: TextView by bindView(R.id.hotel_guest_recommend)
        val hotelPreviewRating: RatingBar by bindView(R.id.hotel_preview_rating)

        public fun bind(viewModel: HotelViewModel) {
            viewModel.hotelThumbnailUrlObservable.subscribe { url ->
                val imageUrl = Akeakamai(url).resizeExactly(100, 100).build()

                PicassoHelper.Builder(hotelPreviewImage)
                        .setError(R.drawable.cars_fallback)
                        .fade()
                        .build()
                        .load(imageUrl)
            }

            viewModel.hotelNameObservable.subscribe(hotelPreviewText)

            viewModel.hotelPreviewRatingObservable.subscribe {
                hotelPreviewRating.setRating(it)
            }

            viewModel.hotelPriceObservable.subscribe(hotelPricePerNight)
            viewModel.hotelStrikeThroughPriceObservable.subscribe(hotelStrikeThroughPrice)
            viewModel.hotelGuestRatingObservable.subscribe(hotelGuestRating)

            hotelPreviewText.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM))

            hotelPricePerNight.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_BOLD))

            hotelStrikeThroughPrice.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG)
            hotelStrikeThroughPrice.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))

            hotelGuestRating.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM))

            hotelGuestRecommend.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        }
    }
}

public fun priceFormatter(hotel: Hotel, strikeThrough: Boolean): String {
    var hotelPrice = if (strikeThrough)
        Money(Math.round(hotel.lowRateInfo.strikethroughPriceToShowUsers).toString(), hotel.lowRateInfo.currencyCode)
    else Money(Math.round(hotel.lowRateInfo.priceToShowUsers).toString(), hotel.lowRateInfo.currencyCode)
    return hotelPrice.getFormattedMoney()
}