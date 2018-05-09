package com.expedia.bookings.hotel.widget

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.hotel.widget.adapter.priceFormatter
import com.expedia.util.LoyaltyUtil
import com.squareup.phrase.Phrase
import java.math.BigDecimal

class HotelSelectARoomBarViewModel(private val context: Context) {

    var response: HotelOffersResponse? = null

    fun getStrikeThroughPriceString(): CharSequence {
        val isShopWithPoints = LoyaltyUtil.isShopWithPoints(getChargeableRate())
        val isAirAttached = getChargeableRate()?.airAttached ?: false

        return if (getPriceString().isNotEmpty() && (isShopWithPoints || !isAirAttached)) {
            priceFormatter(context.resources, getChargeableRate(), true, response?.isPackage == false)
        } else {
            ""
        }
    }

    fun getPriceString(): String {
        getChargeableRate()?.let { chargeableRate ->
            return Money(BigDecimal(chargeableRate.displayPrice.toDouble()), chargeableRate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)
        }
        return ""
    }

    fun getContainerContentDescription(): String = Phrase.from(context, R.string.hotel_select_a_room_cont_desc_TEMPLATE).put("price", getPriceString()).format().toString()

    private fun getChargeableRate(): HotelRate? = response?.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo
}
