package com.expedia.bookings.hotel.widget.adapter

import android.content.Context
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.StarRatingBar
import com.expedia.util.getGuestRatingText
import com.expedia.util.getSuperlative
import com.squareup.picasso.Picasso
import rx.subjects.PublishSubject
import java.math.BigDecimal

class HotelDetailedCompareAdapter(private val context: Context)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val hotelSelectedSubject = PublishSubject.create<String>()
    val removeHotelSubject = PublishSubject.create<String>()

    val hotels : ArrayList<HotelOffersResponse> = ArrayList()

    fun addHotel(offer: HotelOffersResponse) {
        hotels.add(offer)
        notifyItemInserted(hotels.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.hotel_detailed_compare_cell, parent, false)

        return DetailedViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is DetailedViewHolder) {
            holder.bind(hotels[position])
            holder.selectRoomButton.setOnClickListener {
                hotelSelectedSubject.onNext(hotels[holder.adapterPosition].hotelId)
            }

            holder.removeHotelView.setOnClickListener {
                // order matters
                val removedHotel = hotels.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
                removeHotelSubject.onNext(removedHotel.hotelId)
            }
        }
    }

    override fun getItemCount(): Int {
        return hotels.size
    }

    class DetailedViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val removeHotelView: ImageView by bindView(R.id.detailed_compare_remove_hotel)
        private val hotelImage: ImageView by bindView(R.id.detailed_compare_hotel_image)
        private val hotelName: TextView by bindView(R.id.detailed_compare_hotel_name)
        private val starRatingBar: StarRatingBar by bindView(R.id.detailed_compare_star_rating)
        private val guestRating: TextView by bindView(R.id.detailed_compare_guest_rating)
        private val superlativeGuestRating: TextView by bindView(R.id.detailed_compare_guest_rating_superlative)

        private val guestsRecommendPercent: TextView by bindView(R.id.detailed_compare_recommend_rating)
        private val ratingCountView: TextView by bindView(R.id.detailed_compare_rating_count)

        private val amenityContainer: TableRow by bindView(R.id.amenities_table_row)
        private val priceTextView: TextView by bindView(R.id.detailed_compare_price)
        val selectRoomButton: TextView by bindView(R.id.detailed_compare_select_button)

        fun bind(offer: HotelOffersResponse) {
            hotelName.text = offer.hotelName
            starRatingBar.setRating(offer.hotelStarRating.toFloat())
            guestRating.text = offer.hotelGuestRating.toString()
            superlativeGuestRating.text = getSuperlative(offer.hotelGuestRating.toFloat())
            ratingCountView.text = "${offer.totalReviews.toString()} Reviews"

            guestsRecommendPercent.text = "${offer.percentRecommended}%"

            val amenityList = arrayListOf<Amenity>()
            if (offer.hotelAmenities != null) {
                amenityList.addAll(Amenity.amenitiesToShow(offer.hotelAmenities))
            }
            Amenity.addHotelAmenity(amenityContainer, amenityList)
            val background = ContextCompat.getDrawable(view.context, R.drawable.confirmation_background)

            Picasso.with(view.context)
                    .load(getHotelImage(offer.photos[0]).getUrl(HotelMedia.Size.BIG))
                    .placeholder(background)
                    .error(background)
                    .into(hotelImage)

            if (offer.hotelRoomResponse != null
                    && offer.hotelRoomResponse.isNotEmpty()) {
                priceTextView.visibility = View.VISIBLE
                val rate = offer.hotelRoomResponse[0].rateInfo.chargeableRateInfo
                val displayText = Money(BigDecimal(rate.averageRate.toDouble()),
                        rate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)
                priceTextView.text = displayText
            }
        }

        private fun getHotelImage(photo: HotelOffersResponse.Photos) : HotelMedia {
            return HotelMedia(Images.getMediaHost() + photo.url, photo.displayText)
        }
    }

//    class DividerItemDecoration : RecyclerView.ItemDecoration() {
//
//        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
//            super.getItemOffsets(outRect, view, parent, state)
//
//            if (parent.getChildAdapterPosition(view) == 0) {
//                return
//            }
//
//            outRect.right = outRect.right + 1
//        }
//    }
}