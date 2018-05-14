package com.expedia.bookings.hotel.widget

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
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

    fun getPriceString(): SpannableString {
        getChargeableRate()?.let { chargeableRate ->
            val roomDailyPrice = Money(BigDecimal(chargeableRate.displayPrice.toDouble()), chargeableRate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)

            val fromPriceString = context.getString(R.string.from_price_TEMPLATE, roomDailyPrice)
            val fromPriceStyledString = SpannableString(fromPriceString)
            val startIndex = fromPriceString.indexOf(roomDailyPrice)
            val endIndex = startIndex + roomDailyPrice.length
            fromPriceStyledString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            fromPriceStyledString.setSpan(RelativeSizeSpan(1.4f), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            return fromPriceStyledString
        }
        return SpannableString("")
    }

    fun getContainerContentDescription(): String = Phrase.from(context, R.string.hotel_select_a_room_cont_desc_TEMPLATE).put("price", getPriceString()).format().toString()

    private fun getChargeableRate(): HotelRate? = response?.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo
}
