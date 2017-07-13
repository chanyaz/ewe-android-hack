package com.expedia.vm

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotel.HotelValueAdd
import com.expedia.bookings.data.hotel.ValueAddsEnum
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.HotelUtils
import com.expedia.util.LoyaltyUtil
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import java.math.BigDecimal
import java.util.ArrayList
import java.util.TreeSet

class HotelRoomDetailViewModel(val context: Context, val hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, val hotelId: String, val rowIndex: Int, val optionIndex: Int, val hasETP: Boolean) {

    val isPackage: Boolean get() = hotelRoomResponse.isPackage

    val optionString: SpannableString? get() = createOptionString()

    val cancellationString: String
        get() = if (hotelRoomResponse.hasFreeCancellation) {
            context.resources.getString(R.string.free_cancellation)
        } else {
            context.resources.getString(R.string.non_refundable)
        }

    val cancellationTimeString: String? get() = createCancellationTimeString()

    val earnMessageString: String? get() = createEarnMessageString()

    val mandatoryFeeString: String? get() = createMandatoryFeeString()

    val discountPercentageString: String?
        get() = if (!isPackage && chargeableRateInfo.isDiscountPercentNotZero
                && !(hotelLoyaltyInfo?.isBurnApplied ?: false) && !chargeableRateInfo.isShowAirAttached()) {
            context.resources.getString(R.string.percent_off_TEMPLATE, HotelUtils.getDiscountPercent(chargeableRateInfo))
        } else {
            null
        }

    val payLaterPriceString: String? get() = createPayLaterPriceString()

    val showDepositTerm: Boolean get() = isPayLater && haveDepositTerm

    val strikeThroughString: String?
        get() = if (!isPayLater && chargeableRateInfo.isStrikeThroughPriceValid) {
            chargeableRateInfo.getDisplayMoney(true, true).formattedMoney
        } else {
            null
        }

    val pricePerNightString: String? get() = createPricePerNightString()

    val showPerNight: Boolean get() = !isPayLater && !isTotalPrice

    val roomLeftString: String? get() = createRoomLeftString()

    val roomSoldOut = BehaviorSubject.create<Boolean>(false)

    private val chargeableRateInfo = hotelRoomResponse.rateInfo.chargeableRateInfo
    private val hotelLoyaltyInfo: LoyaltyInformation? = chargeableRateInfo.loyaltyInfo

    private val currencyCode = chargeableRateInfo.currencyCode
    private val isPayLater = hotelRoomResponse.isPayLater
    private val priceToShowUser = chargeableRateInfo.getDisplayMoney(false, true).formattedMoney
    private val isTotalPrice = chargeableRateInfo.getUserPriceType() == HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES
    private val haveDepositTerm = hotelRoomResponse.depositPolicy != null && !hotelRoomResponse.depositPolicy.isEmpty()

    fun getValueAdds(): List<HotelValueAdd> {
        val valueAddsTreeSet = TreeSet<HotelValueAdd>()
        if (CollectionUtils.isNotEmpty(hotelRoomResponse.valueAdds)) {
            val valueAdds = hotelRoomResponse.valueAdds
            valueAdds.forEach { valueAdd ->
                when (valueAdd.id.toInt()) {
                // taken from https://confluence/pages/viewpage.action?pageId=587907920 and
                // https://opengrok/source/xref/expweb-trunk/bizconfig/com/expedia/www/domain/services/hotel/config/AmenityConfig.properties.xml
                    512, 2, 8, 4, 8192, 4096, 16777216, 33554432, 67108864, 1073742786, 1073742857, 2111, 2085, 4363,
                    2102, 2207, 2206, 2194, 2193, 2205, 2103, 2105, 2104, 3969, 4647, 4646, 4648, 4649, 4650, 4651, 2001
                    -> valueAddsTreeSet.add(HotelValueAdd(context, ValueAddsEnum.BREAKFAST, valueAdd.description))
                    2048, 1024, 1073742787, 4347, 2403, 4345, 2405, 2407, 4154, 2191, 2192, 2404, 2406
                    -> valueAddsTreeSet.add(HotelValueAdd(context, ValueAddsEnum.INTERNET, context.resources.getString(ValueAddsEnum.INTERNET.descriptionId)))
                    16384, 128, 2195, 2109, 4449, 4447, 4445, 4443, 3863, 3861, 2011
                    -> valueAddsTreeSet.add(HotelValueAdd(context, ValueAddsEnum.PARKING, valueAdd.description))
                    2196, 32768, 10
                    -> valueAddsTreeSet.add(HotelValueAdd(context, ValueAddsEnum.FREE_AIRPORT_SHUTTLE, valueAdd.description))
                }
            }
        }

        return ArrayList<HotelValueAdd>(valueAddsTreeSet)
    }

    private fun createOptionString(): SpannableString? {
        if (optionIndex >= 0) {
            val optionString = Phrase.from(context, R.string.option_TEMPLATE).put("number", optionIndex + 1).format().toString()

            val largeTextSize = context.resources.getDimensionPixelSize(R.dimen.large_text_size)
            val smallTextSize = context.resources.getDimensionPixelSize(R.dimen.small_text_size)

            var detailString = hotelRoomResponse.roomTypeDescriptionDetail

            if (!detailString.isNullOrBlank()) {
                detailString = "  (" + detailString + ")"
                val span = SpannableString(optionString + detailString)

                span.setSpan(AbsoluteSizeSpan(smallTextSize), optionString.length, optionString.length + detailString.length, SPAN_INCLUSIVE_INCLUSIVE)
                span.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.light_text_color)), optionString.length, optionString.length + detailString.length, SPAN_INCLUSIVE_INCLUSIVE)

                return span
            } else {
                val span = SpannableString(optionString)
                span.setSpan(AbsoluteSizeSpan(largeTextSize), 0, optionString.length, SPAN_INCLUSIVE_INCLUSIVE)
                span.setSpan(StyleSpan(Typeface.BOLD), 0, optionString.length, SPAN_INCLUSIVE_INCLUSIVE)

                return span
            }
        }
        return null
    }

    private fun createCancellationTimeString(): String? {
        if (hotelRoomResponse.hasFreeCancellation && hotelRoomResponse.freeCancellationWindowDate != null) {
            val dateTime = DateUtils.yyyyMMddHHmmToDateTime(hotelRoomResponse.freeCancellationWindowDate).toLocalDate()
            val cancellationDate = DateUtils.localDateToEEEMMMd(dateTime)
            return Phrase.from(context, R.string.before_TEMPLATE).put("date", cancellationDate).format().toString()
        }
        return null
    }

    private fun createEarnMessageString(): String? {
        val packageLoyaltyInfo: LoyaltyInformation? = hotelRoomResponse.packageLoyaltyInformation
        val earnMessage = LoyaltyUtil.getEarnMessagingString(context, isPackage, hotelLoyaltyInfo?.earn, packageLoyaltyInfo?.earn)
        if (LoyaltyUtil.shouldShowEarnMessage(earnMessage, isPackage)) {
            return earnMessage
        }
        return null
    }

    private fun createMandatoryFeeString(): String? {
        val dailyMandatoryFee = Money(BigDecimal(chargeableRateInfo.dailyMandatoryFee.toDouble()), currencyCode)
        if (!dailyMandatoryFee.isZero && !isPackage) {
            return Phrase.from(context, R.string.excludes_mandatory_fee_TEMPLATE).put("amount", dailyMandatoryFee.formattedMoney).format().toString()
        }
        return null
    }

    private fun createPayLaterPriceString(): String? {
        if (isPayLater) {
            val sb = StringBuilder(priceToShowUser)

            if (!isTotalPrice) {
                sb.append(context.resources.getString(R.string.per_night))
            }
            return sb.toString()
        }
        return null
    }

    private fun createPricePerNightString(): String? {
        if (isPayLater) {
            if (haveDepositTerm) {
                return null
            } else {
                val depositAmount = chargeableRateInfo.depositAmountToShowUsers?.toDouble() ?: 0.0
                val depositAmountMoney = Money(BigDecimal(depositAmount), currencyCode)
                return Phrase.from(context, R.string.room_rate_pay_later_due_now).put("amount", depositAmountMoney.formattedMoney).format().toString()
            }
        } else {
            return priceToShowUser
        }
    }

    private fun createRoomLeftString(): String? {
        try {
            val roomLeft = hotelRoomResponse.currentAllotment.toInt()
            if (roomLeft > 0 && roomLeft <= ROOMS_LEFT_CUTOFF) {
                return context.resources.getQuantityString(R.plurals.num_rooms_left, roomLeft, roomLeft)
            }
        } catch (e: NumberFormatException) {
            return null
        }
        return null
    }
}
