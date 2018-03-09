package com.expedia.bookings.mia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.enums.DiscountColors
import com.expedia.bookings.utils.isBrandColorEnabled

class MemberDealsCardViewModel(context: Context, leadingHotel: DealsDestination.Hotel, currency: String?) : BaseDealsCardViewModel(context, leadingHotel, currency) {

    override val numberOfTravelers = 1

    override val title = cityName

    override val subtitle: String? = context.getString(R.string.deals_hotel_only)

    override val hotelId: String? = null

    override val prioritizedBackgroundImageUrls = listOf(getDestinationBackgroundImageUrl())

    override val discountColors = if (isBrandColorEnabled(context)) DiscountColors.MEMBER_DEALS else DiscountColors.DEFAULT

    override fun getCardContentDescription(): String {
        return super.getCardContentDescription() + context.getString(R.string.deals_hotel_only)
    }
}
