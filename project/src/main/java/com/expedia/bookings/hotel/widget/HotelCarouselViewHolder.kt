package com.expedia.bookings.hotel.widget

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.extensions.setInverseVisibility
import com.expedia.bookings.extensions.setTextAndVisibility
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.hotel.util.shouldShowCircleForRatings
import com.expedia.bookings.hotel.vm.HotelViewModel
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.StarRatingBar
import com.expedia.bookings.widget.TextView
import io.reactivex.subjects.PublishSubject
import kotlin.properties.Delegates

class HotelCarouselViewHolder(root: ViewGroup) : RecyclerView.ViewHolder(root), View.OnClickListener {

    val hotelClickedSubject = PublishSubject.create<Int>()

    var hotelId: String by Delegates.notNull()
    val viewModel: HotelViewModel by lazy {
        HotelViewModel(itemView.context)
    }
    val hotelPreviewImage: ImageView by bindView(R.id.hotel_preview_image)
    val hotelPreviewText: TextView by bindView(R.id.hotel_preview_text)
    val hotelPricePerNight: TextView by bindView(R.id.hotel_price_per_night)
    val hotelSoldOut: TextView by bindView(R.id.hotel_sold_out)
    val hotelStrikeThroughPrice: TextView by bindView(R.id.hotel_strike_through_price)
    val hotelGuestRating: TextView by bindView(R.id.hotel_guest_rating)
    val hotelGuestRecommend: TextView by bindView(R.id.hotel_guest_recommend)
    val hotelNoGuestRating: TextView by bindView(R.id.no_guest_rating)
    val loyaltyAppliedMessageContainer: LinearLayout by bindView(R.id.map_loyalty_message_container)
    val loyaltyAppliedMessage: TextView by bindView(R.id.map_loyalty_applied_message)
    val shadowOnLoyaltyMessageContainer: View by bindView(R.id.shadow_on_loyalty_message_container)
    val shadowOnHotelCell: View by bindView(R.id.shadow_on_hotel_preview_cell)
    val loyaltyEarnMessage: TextView by bindView(R.id.hotel_loyalty_earn_message)

    var hotelPreviewRating: StarRatingBar by Delegates.notNull()

    init {
        hotelPreviewRating = root.findViewById(if (shouldShowCircleForRatings()) R.id.hotel_preview_circle_rating else R.id.hotel_preview_star_rating)
        hotelPreviewRating.visibility = View.VISIBLE
    }

    override fun onClick(view: View) {
        hotelClickedSubject.onNext(adapterPosition)
    }

    fun bindHotelData(hotel: Hotel, shopWithPoints: Boolean = false) {
        viewModel.bindHotelData(hotel)
        hotelId = viewModel.hotelId

        val url = viewModel.getHotelLargeThumbnailUrl()
        if (url.isNotBlank()) {
            PicassoHelper.Builder(hotelPreviewImage)
                    .setError(R.drawable.room_fallback)
                    .build()
                    .load(url)
        }

        hotelPreviewText.text = viewModel.hotelName
        hotelPreviewImage.colorFilter = viewModel.getImageColorFilter()

        updateHotelRating(viewModel)
        updatePricing(viewModel)

        loyaltyAppliedMessageContainer.visibility = viewModel.getMapLoyaltyMessageVisibility(shopWithPoints)

        shadowOnLoyaltyMessageContainer.setVisibility(viewModel.loyaltyAvailable)
        shadowOnHotelCell.setVisibility(!viewModel.loyaltyAvailable && !hotel.isPackage)
        loyaltyAppliedMessage.text = viewModel.getMapLoyaltyMessageText()
        loyaltyEarnMessage.text = viewModel.earnMessage
        loyaltyEarnMessage.visibility = viewModel.earnMessageVisibility

        updateFonts()
    }

    private fun updatePricing(viewModel: HotelViewModel) {
        hotelStrikeThroughPrice.setTextAndVisibility(viewModel.hotelStrikeThroughPriceFormatted)
        hotelPricePerNight.text = viewModel.hotelPriceFormatted
        hotelPricePerNight.setTextColor(viewModel.pricePerNightColor)
        hotelPricePerNight.setInverseVisibility(viewModel.isHotelSoldOut)
        hotelSoldOut.setVisibility(viewModel.isHotelSoldOut)
    }

    private fun updateFonts() {
        hotelPreviewText.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM)
        hotelPricePerNight.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_BOLD)
        hotelStrikeThroughPrice.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)
        hotelGuestRating.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM)
        hotelGuestRecommend.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)
    }

    private fun updateHotelRating(viewModel: HotelViewModel) {
        hotelPreviewRating.setStarColor(viewModel.getStarRatingColor())
        hotelPreviewRating.setRating(viewModel.hotelStarRating)
        hotelPreviewRating.setVisibility(viewModel.showHotelPreviewRating)

        hotelGuestRating.text = viewModel.hotelGuestRating.toString()
        hotelGuestRating.setVisibility(viewModel.isHotelGuestRatingAvailable)
        hotelGuestRecommend.setVisibility(viewModel.isHotelGuestRatingAvailable)
        hotelNoGuestRating.setVisibility(viewModel.showNoGuestRating)
    }
}
