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
import com.expedia.util.subscribeText
import com.google.android.gms.maps.model.Marker
import rx.subjects.PublishSubject
import java.util.Collections

public class HotelMapCarouselAdapter(var hotels: List<Hotel>, val hotelSubject: PublishSubject<Hotel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return hotels.size()
    }

    override fun onBindViewHolder(given: RecyclerView.ViewHolder?, position: Int) {
        val holder: HotelViewHolder = given as HotelViewHolder
        val viewModel = HotelViewModel(holder.itemView.context, hotels.get(position))
        holder.bind(viewModel)
        holder.itemView.setOnClickListener(holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_marker_preview_cell, parent, false)
        val screen = Ui.getScreenSize(parent.context)
        var lp = view.findViewById(R.id.root).layoutParams
        lp.width = screen.x
        return HotelViewHolder(view as ViewGroup)
    }

    public inner class HotelViewHolder(root: ViewGroup) : RecyclerView.ViewHolder(root), View.OnClickListener {

        val resources: Resources by lazy {
            itemView.resources
        }

        override fun onClick(view: View) {
            val hotel: Hotel = hotels.get(adapterPosition)
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

            viewModel.hotelNameObservable.subscribeText(hotelPreviewText)

            viewModel.hotelPreviewRatingObservable.subscribe {
                hotelPreviewRating.rating = it
            }

            viewModel.hotelPriceObservable.subscribeText(hotelPricePerNight)
            viewModel.hotelStrikeThroughPriceObservable.subscribeText(hotelStrikeThroughPrice)
            viewModel.hotelGuestRatingObservable.subscribeText(hotelGuestRating)

            hotelPreviewText.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM)

            hotelPricePerNight.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_BOLD)

            hotelStrikeThroughPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            hotelStrikeThroughPrice.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)

            hotelGuestRating.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM)

            hotelGuestRecommend.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)
        }
    }
}

public fun priceFormatter(rate: HotelRate?, strikeThrough: Boolean): String {
    if (rate == null) return ""
    var hotelPrice = if (strikeThrough)
        Money(Math.round(rate.strikethroughPriceToShowUsers).toString(), rate.currencyCode)
    else Money(Math.round(rate.priceToShowUsers).toString(), rate.currencyCode)
    return hotelPrice.formattedMoney
}
