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
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.presenter.hotel.HotelResultsPresenter
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribe
import com.google.android.gms.maps.model.Marker
import rx.subjects.PublishSubject
import java.util.Collections

public class HotelMarkerPreviewAdapter(var hotelMarkerDistances: List<HotelResultsPresenter.MarkerDistance>, val marker: Marker, val hotelSubject: PublishSubject<Hotel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var sortedHotelMarkerDistanceList = sortHotelMarkerDistanceList()

    override fun getItemCount(): Int {
        return sortedHotelMarkerDistanceList.size()
    }

    override fun onBindViewHolder(given: RecyclerView.ViewHolder?, position: Int) {
        val holder: HotelViewHolder = given as HotelViewHolder
        val viewModel = HotelViewModel(holder.itemView.context, sortedHotelMarkerDistanceList.get(position).hotel)
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
    fun sortHotelMarkerDistanceList(): List<HotelResultsPresenter.MarkerDistance> {
        var modifiedHotelMarkerDistances = hotelMarkerDistances

        var markerLocation = Location("specifiedMarker")
        markerLocation.latitude = marker.position.latitude
        markerLocation.longitude = marker.position.longitude

        for (hotelMarkerDistance in modifiedHotelMarkerDistances) {
            var hotelLocation = Location("hotelLocation")
            hotelLocation.setLatitude(hotelMarkerDistance.hotel.latitude)
            hotelLocation.setLongitude(hotelMarkerDistance.hotel.longitude)

            hotelMarkerDistance.distance = markerLocation.distanceTo(hotelLocation)
        }

        // Sort this list
        Collections.sort(modifiedHotelMarkerDistances)
        return modifiedHotelMarkerDistances
    }

    public inner class HotelViewHolder(root: ViewGroup) : RecyclerView.ViewHolder(root), View.OnClickListener {

        val resources: Resources by lazy {
            itemView.getResources()
        }

        override fun onClick(view: View) {
            val hotel: Hotel = sortedHotelMarkerDistanceList.get(getAdapterPosition()).hotel
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
            viewModel.hotelLargeThumbnailUrlObservable.subscribe {
                PicassoHelper.Builder(hotelPreviewImage)
                        .setError(R.drawable.cars_fallback)
                        .build()
                        .load(it)
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

public fun priceFormatter(rate: HotelRate?, strikeThrough: Boolean): String {
    if (rate == null) return ""
    var hotelPrice = if (strikeThrough)
        Money(Math.round(rate.strikethroughPriceToShowUsers).toString(), rate.currencyCode)
    else Money(Math.round(rate.priceToShowUsers).toString(), rate.currencyCode)
    return hotelPrice.getFormattedMoney()
}
