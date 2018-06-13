package com.expedia.bookings.hotel.widget.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.extensions.setInverseVisibility
import com.expedia.bookings.extensions.setTextAndVisibility
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.NumberUtils
import com.expedia.bookings.widget.shared.AbstractHotelCellViewHolder
import com.expedia.util.getGuestRatingText
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject
import org.joda.time.LocalDate

class HotelFavoritesItemViewHolder(root: ViewGroup) : AbstractHotelCellViewHolder(root) {

    val favoriteButtonClickedSubject = PublishSubject.create<Int>()

    fun bind(item: HotelShortlistItem) {
        hotelNameStarAmenityDistance.hotelNameTextView.setTextAndVisibility(item.name)
        item.media?.let { media -> loadHotelImage(Images.getMediaHost() + media) }

        updateHotelGuestRating(item)
        updatePricePerNight(item)
        updateCheckInOutDate(item)
        updateAirAttach()

        vipMessageContainer.visibility = View.GONE
        hotelNameStarAmenityDistance.starRatingBar.visibility = View.GONE
        hotelPriceTopAmenity.soldOutTextView.visibility = View.GONE

        favoriteIcon.setImageResource(R.drawable.ic_favorite_active)
        favoriteTouchTarget.setOnClickListener { favoriteButtonClickedSubject.onNext(adapterPosition) }
        favoriteTouchTarget.setVisibility(true)
    }

    companion object {
        fun create(parent: ViewGroup): HotelFavoritesItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_cell, parent, false)
            return HotelFavoritesItemViewHolder(view as ViewGroup)
        }
    }

    private fun updateHotelGuestRating(item: HotelShortlistItem) {
        if (item.isHotelGuestRatingAvailable()) {
            val roundedGuestRating = NumberUtils.round(item.guestRating!!.toFloat(), Money.F_NO_DECIMAL)
            guestRating.text = roundedGuestRating.toString()
            guestRatingRecommendedText.text = getGuestRatingText(roundedGuestRating, itemView.resources)
        }
        guestRating.setVisibility(item.isHotelGuestRatingAvailable())
        guestRatingRecommendedText.setVisibility(item.isHotelGuestRatingAvailable())
        noGuestRating.setInverseVisibility(item.isHotelGuestRatingAvailable())
    }

    private fun updateAirAttach() {
        airAttachContainer.visibility = View.GONE
        airAttachSWPImage.visibility = View.GONE
    }

    private fun updateCheckInOutDate(item: HotelShortlistItem) {
        val checkInDate = item.shortlistItem?.metaData?.getCheckInLocalDate()
        val checkOutDate = item.shortlistItem?.metaData?.getCheckOutLocalDate()
        val date = if (checkInDate != null && !checkInDate.isBefore(LocalDate.now()) && checkOutDate != null) {
            Phrase.from(itemView.context, R.string.start_dash_end_date_range_TEMPLATE).put("startdate",
                    LocaleBasedDateFormatUtils.localDateToMMMd(checkInDate)).put("enddate",
                    LocaleBasedDateFormatUtils.localDateToMMMd(checkOutDate))
                    .format()
                    .toString()
        } else {
            ""
        }

        hotelPriceTopAmenity.checkInOutDateTextView.setTextAndVisibility(date)
    }

    private fun updatePricePerNight(item: HotelShortlistItem) {
        val pricePerNight = if (item.price.isNullOrBlank() || item.price!!.toFloatOrNull() == null || item.price.equals("0")) "" else Money(item.price, item.currency).getFormattedMoneyFromAmountAndCurrencyCode(Money.F_NO_DECIMAL)
        hotelPriceTopAmenity.pricePerNightTextView.setTextAndVisibility(pricePerNight)
    }
}
