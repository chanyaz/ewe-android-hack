package com.expedia.bookings.mia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.enums.DiscountColors

class MemberDealsCardViewModel(context: Context, leadingHotel: DealsDestination.Hotel, currency: String?) : BaseDealsCardViewModel(context, leadingHotel, currency) {

    override val numberOfTravelers = 1

    override val title = cityName

    override val subtitle: String? = context.getString(R.string.deals_hotel_only)

    override val hotelId: String? = null

    override val prioritizedBackgroundImageUrls = listOf(getDestinationBackgroundImageUrl())

    override val discountColors = DiscountColors.MEMBER_DEALS

    override val strikeOutPriceText: CharSequence by lazy {
        getFormattedPriceText(context.resources, leadingHotel.hotelPricingInfo?.crossOutPriceValue, true)
    }

    override fun getCardContentDescription(): String {
        return super.getCardContentDescription() + context.getString(R.string.deals_hotel_only)
    }
}
