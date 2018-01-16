package com.expedia.bookings.hotel.util

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.CurrencyUtils
import com.squareup.phrase.Phrase
import java.math.BigDecimal
import java.text.DecimalFormat

class HotelResortFeeFormatter {
    fun getResortFee(context: Context, roomResponse: HotelOffersResponse.HotelRoomResponse?,
                     isPackage: Boolean, hotelCountry: String) : String {
        var resortText = ""
        if (roomResponse?.rateInfo?.chargeableRateInfo?.showResortFeeMessage == true) {
            val rate = roomResponse.rateInfo.chargeableRateInfo

            if (isPackage && PointOfSale.getPointOfSale().showResortFeesInHotelLocalCurrency()) {
                val df = DecimalFormat("#.00")
                val resortFees = Money(BigDecimal(rate.totalMandatoryFees.toDouble()),
                        CurrencyUtils.currencyForLocale(hotelCountry))
                resortText = Phrase.from(context, R.string.non_us_resort_fee_format_TEMPLATE)
                        .put("amount", df.format(resortFees.amount))
                        .put("currency", resortFees.currencyCode)
                        .format().toString()
            } else {
                if (!rate.currencyCodePOSu.isNullOrEmpty() && AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.HotelNewCurrencyPOSFees)) {
                    val resortFees = Money(BigDecimal(rate.totalMandatoryFees.toDouble()), rate.currencyCodePOSu)
                    resortText = resortFees.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL)
                } else {
                    val resortFees = Money(BigDecimal(rate.totalMandatoryFees.toDouble()), rate.currencyCode)
                    resortText = resortFees.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL)
                }
            }
        }

        return resortText
    }

    fun getResortFeeInclusionText(context: Context,
                                 roomResponse: HotelOffersResponse.HotelRoomResponse?) : String {
        var feeInclusionText = ""
        if (roomResponse?.rateInfo?.chargeableRateInfo?.showResortFeeMessage == true) {
            val rate = roomResponse.rateInfo.chargeableRateInfo
            if (rate.resortFeeInclusion) {
                feeInclusionText = context.getString(R.string.included_in_the_price)
            } else {
                feeInclusionText = context.getString(R.string.not_included_in_the_price)
            }
        }
        return feeInclusionText
    }
}
