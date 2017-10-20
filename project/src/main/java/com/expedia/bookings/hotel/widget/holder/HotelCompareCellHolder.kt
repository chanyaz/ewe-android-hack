package com.expedia.bookings.hotel.widget.holder

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.StarRatingBar
import com.expedia.util.subscribeOnClick
import com.squareup.phrase.Phrase
import com.squareup.picasso.Picasso
import rx.subjects.PublishSubject
import java.math.BigDecimal

class HotelCompareCellHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    val selectRoomClickSubject = PublishSubject.create<Unit>()

    private val hotelImage: ImageView by bindView(R.id.detailed_compare_hotel_image)
    private val hotelName: TextView by bindView(R.id.detailed_compare_hotel_name)
    private val starRatingBar: StarRatingBar by bindView(R.id.detailed_compare_star_rating)
    private val guestRating: TextView by bindView(R.id.detailed_compare_guest_rating)
    private val superlativeGuestRating: TextView by bindView(R.id.detailed_compare_guest_rating_superlative)
    private val guestsRecommendPercent: TextView by bindView(R.id.detailed_compare_recommend_rating)
    private val ratingCountView: TextView by bindView(R.id.detailed_compare_rating_count)
    private val amenityContainer: TableRow by bindView(R.id.amenities_table_row)
    private val priceTextView: TextView by bindView(R.id.detailed_compare_price)
    private val selectRoomButton: TextView by bindView(R.id.detailed_compare_select_button)

    init {
        selectRoomButton.subscribeOnClick(selectRoomClickSubject)
    }

    fun bind(offer: HotelOffersResponse) {
        hotelName.text = offer.hotelName
        starRatingBar.setRating(offer.hotelStarRating.toFloat())
        guestRating.text = offer.hotelGuestRating.toString()
        superlativeGuestRating.text = getSuperlative(view.context, offer.hotelGuestRating.toFloat())
        ratingCountView.text = Phrase.from(view.context, R.string.n_reviews_TEMPLATE)
                .put("review_count", HotelUtils.formattedReviewCount(offer.totalReviews)).toString()

        guestsRecommendPercent.text = Phrase.from(view.context, R.string.hotel_percentage_TEMPLATE)
                .put("discount", offer.percentRecommended).format().toString()

        val amenityList = arrayListOf<Amenity>()
        if (offer.hotelAmenities != null) {
            amenityList.addAll(Amenity.amenitiesToShow(offer.hotelAmenities))
        }
        Amenity.addHotelAmenity(amenityContainer, amenityList)
        val background = ContextCompat.getDrawable(view.context, R.drawable.generic_pattern_background)

        Picasso.with(view.context)
                .load(getHotelImage(offer.photos[0]).getUrl(HotelMedia.Size.BIG))
                .placeholder(background)
                .error(background)
                .into(hotelImage)

        setPrice(offer.hotelRoomResponse)
    }

    private fun setPrice(rooms: List<HotelOffersResponse.HotelRoomResponse>?) {
        if (rooms != null && rooms.isNotEmpty()) {
            priceTextView.visibility = View.VISIBLE
            val cheapestRate = rooms[0].rateInfo.chargeableRateInfo
            val priceDisplayText = Money(BigDecimal(cheapestRate.averageRate.toDouble()),
                    cheapestRate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)
            priceTextView.text = priceDisplayText
        }
    }

    private fun getHotelImage(photo: HotelOffersResponse.Photos) : HotelMedia {
        return HotelMedia(Images.getMediaHost() + photo.url, photo.displayText)
    }

    private fun getSuperlative(context: Context, rating: Float) : String {
        return when {
            rating < 3.5f -> context.getString(R.string.generic_guest_rating_description)
            rating < 4f -> context.getString(R.string.good_guest_rating_description)
            rating < 4.3f -> context.getString(R.string.very_good_guest_rating_description)
            rating < 4.5f -> context.getString(R.string.excellent_guest_rating_description)
            rating < 4.7f -> context.getString(R.string.wonderful_guest_rating_description)
            else -> context.getString(R.string.exceptional_guest_rating_description)
        }
    }
}