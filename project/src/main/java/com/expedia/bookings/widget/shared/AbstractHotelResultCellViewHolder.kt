package com.expedia.bookings.widget.shared

import android.content.Context
import android.support.annotation.CallSuper
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.extensions.setInverseVisibility
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.features.Features
import com.expedia.util.getGuestRatingText
import com.expedia.vm.hotel.HotelViewModel
import kotlin.properties.Delegates

abstract class AbstractHotelResultCellViewHolder(val root: ViewGroup) : AbstractHotelCellViewHolder(root) {

    abstract fun createHotelViewModel(context: Context): HotelViewModel

    var hotelId: String by Delegates.notNull()
    val viewModel = createHotelViewModel(itemView.context)

    @CallSuper
    open fun bindHotelData(hotel: Hotel) {
        viewModel.bindHotelData(hotel)

        this.hotelId = hotel.hotelId

        hotelNameStarAmenityDistance.update(viewModel)
        hotelPriceTopAmenity.update(viewModel)
        vipMessageContainer.update(viewModel)
        urgencyMessageContainer.update(viewModel)

        imageView.colorFilter = viewModel.getImageColorFilter()

        updateDiscountPercentage()
        updateHotelGuestRating()

        updateAirAttach()

        earnMessagingText.text = viewModel.earnMessage
        earnMessagingText.setVisibility(viewModel.showEarnMessage)

        ratingPointsContainer.setVisibility(viewModel.showRatingPointsContainer())

        loadHotelImage()

        soldOutOverlay.setVisibility(viewModel.showSoldOutOverlay)

        cardView.contentDescription = viewModel.getHotelContentDesc()
    }

    private fun isGenericAttachEnabled(): Boolean {
        return Features.all.genericAttach.enabled()
    }

    private fun updateAirAttach() {
        airAttachContainer.setVisibility(!isGenericAttachEnabled() && viewModel.showAirAttachWithDiscountLabel)
        airAttachSWPImage.setVisibility(!isGenericAttachEnabled() && viewModel.showAirAttachIconWithoutDiscountLabel)
        airAttachDiscount.text = viewModel.hotelDiscountPercentage
    }

    private fun updateDiscountPercentage() {
        discountPercentage.text = viewModel.hotelDiscountPercentage
        if (viewModel.hasMemberDeal() || (isGenericAttachEnabled() && viewModel.hasAttach)) {
            discountPercentage.setBackgroundResource(R.drawable.member_only_discount_percentage_background)
            discountPercentage.setTextColor(ContextCompat.getColor(itemView.context, R.color.member_pricing_text_color))
        } else {
            discountPercentage.setBackgroundResource(R.drawable.discount_percentage_background)
            discountPercentage.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
        }
        discountPercentage.setVisibility(viewModel.showDiscount)
    }

    private fun loadHotelImage() {
        val url = viewModel.getHotelLargeThumbnailUrl()
        loadHotelImage(url)
    }

    private fun updateHotelGuestRating() {
        if (viewModel.isHotelGuestRatingAvailable) {
            val rating = viewModel.hotelGuestRating
            guestRating.text = rating.toString()
            guestRatingRecommendedText.text = getGuestRatingText(rating, itemView.resources)
        }

        guestRating.setVisibility(viewModel.isHotelGuestRatingAvailable)
        guestRatingRecommendedText.setVisibility(viewModel.isHotelGuestRatingAvailable)
        noGuestRating.setInverseVisibility(viewModel.isHotelGuestRatingAvailable)
    }
}
