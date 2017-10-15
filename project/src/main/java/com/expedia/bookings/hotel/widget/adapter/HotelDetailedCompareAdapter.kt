package com.expedia.bookings.hotel.widget.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.StarRatingBar
import com.expedia.util.getGuestRatingText
import com.squareup.picasso.Picasso
import rx.subjects.PublishSubject

class HotelDetailedCompareAdapter(private val context: Context)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val hotelSelectedSubject = PublishSubject.create<String>()
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
            holder.itemView.setOnClickListener {
                hotelSelectedSubject.onNext(hotels[position].hotelId)
            }
        }
    }

    override fun getItemCount(): Int {
        return hotels.size
    }

    class DetailedViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val hotelImage: ImageView by bindView(R.id.detailed_compare_hotel_image)
        val hotelName: TextView by bindView(R.id.detailed_compare_hotel_name)
        val starRatingBar: StarRatingBar by bindView(R.id.detailed_compare_star_rating)
        val guestRating: TextView by bindView(R.id.detailed_compare_guest_rating)

        private val amenityContainer: TableRow by bindView(R.id.amenities_table_row)

        fun bind(offer: HotelOffersResponse) {
            hotelName.text = offer.hotelName
            starRatingBar.setRating(offer.hotelStarRating.toFloat())
            guestRating.text = "${offer.hotelGuestRating.toString()} ${getGuestRatingText(offer.hotelStarRating.toFloat(),
                    view.context.resources)}"

            val amenityList = arrayListOf<Amenity>()
            if (offer.hotelAmenities != null) {
                amenityList.addAll(Amenity.amenitiesToShow(offer.hotelAmenities))
            }
            Amenity.addHotelAmenity(amenityContainer, amenityList)
            val background = ContextCompat.getDrawable(view.context, R.drawable.confirmation_background)

            Picasso.with(view.context)
                    .load(getHotelImage(offer.photos[0]).getUrl(HotelMedia.Size.SMALL))
                    .placeholder(background)
                    .error(background)
                    .into(hotelImage)
        }

        private fun getHotelImage(photo: HotelOffersResponse.Photos) : HotelMedia {
            return HotelMedia(Images.getMediaHost() + photo.url, photo.displayText)
        }
    }
}