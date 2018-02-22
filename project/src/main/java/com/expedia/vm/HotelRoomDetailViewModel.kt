package com.expedia.vm

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotel.HotelValueAdd
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.extensions.isShowAirAttached
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isMidAPIEnabled
import com.expedia.util.LoyaltyUtil
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import java.math.BigDecimal
import java.util.ArrayList
import java.util.TreeSet

class HotelRoomDetailViewModel(val context: Context, val hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, val hotelId: String, val rowIndex: Int, val optionIndex: Int, val hasETP: Boolean) {

    val isPackage: Boolean get() = hotelRoomResponse.isPackage

    val optionString: SpannableString? get() = createOptionString()

    val cancellationString: String get() = createCancellationString()

    val cancellationTimeString: String? get() = createCancellationTimeString()

    val earnMessageString: String? get() = createEarnMessageString()

    val mandatoryFeeString: String? get() = createMandatoryFeeString()

    val showMemberOnlyDealTag: Boolean get() = hotelRoomResponse.isMemberDeal && Ui.getApplication(context).appComponent().userStateManager().isUserAuthenticated()

    val discountPercentageString: String? get() = createDiscountPercentageString()

    val discountPercentageBackground: Drawable
        get() = if (showMemberOnlyDealTag) {
            ContextCompat.getDrawable(context, R.drawable.member_only_discount_percentage_background)
        } else {
            ContextCompat.getDrawable(context, R.drawable.discount_percentage_background)
        }

    val discountPercentageTextColor: Int
        get() = if (showMemberOnlyDealTag) {
            ContextCompat.getColor(context, R.color.member_pricing_text_color)
        } else {
            ContextCompat.getColor(context, R.color.white)
        }

    val payLaterPriceString: String? get() = createPayLaterPriceString()

    val showDepositTerm: Boolean get() = isPayLater && haveDepositTerm

    val strikeThroughString: String? get() = createStrikeThroughString()

    val showPerNight: Boolean get() = !isPayLater && !isTotalPrice

    val pricePerDescriptorString: String get() = if (isPackage && isMidAPIEnabled(context)) context.getString(R.string.price_per_person) else context.getString(R.string.per_night)

    val priceString: String? get() = createPriceString()

    val hotelRoomRowButtonString: String get() = if (isPackage) context.getString(R.string.book_room_button_text) else context.getString(R.string.select)

    val roomLeftString: String? get() = createRoomLeftString()

    val bookButtonContentDescriptionString: String get() = getBookButtonContentDescription()

    val roomSoldOut = BehaviorSubject.createDefault<Boolean>(false)

    private val chargeableRateInfo = hotelRoomResponse.rateInfo.chargeableRateInfo
    private val hotelLoyaltyInfo: LoyaltyInformation? = chargeableRateInfo.loyaltyInfo

    private val currencyCode = chargeableRateInfo.currencyCode
    private val isPayLater = hotelRoomResponse.isPayLater
    private val moneyToShowUser = chargeableRateInfo.getDisplayMoney(false, !isPackage)
    private val isTotalPrice = chargeableRateInfo.getUserPriceType() == HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES
    private val haveDepositTerm = hotelRoomResponse.depositPolicy != null && !hotelRoomResponse.depositPolicy.isEmpty()

    fun getValueAdds(): List<HotelValueAdd> {
        val valueAddsTreeSet = TreeSet<HotelValueAdd>()
        if (CollectionUtils.isNotEmpty(hotelRoomResponse.valueAdds)) {
            val valueAdds = hotelRoomResponse.valueAdds

            valueAdds.forEach { valueAdd ->
                val hotelValueAdd = HotelValueAdd.getHotelValueAdd(context, valueAdd)
                if (hotelValueAdd != null) {
                    valueAddsTreeSet.add(hotelValueAdd)
                }
            }
        }

        return ArrayList<HotelValueAdd>(valueAddsTreeSet)
    }

    fun getRoomPriceContentDescription(): String? {
        val perNightString = if (showPerNight) context.getString(R.string.per_night) else ""
        val discountPercentageString = if (!discountPercentageString.isNullOrBlank()) {
            Phrase.from(context, R.string.hotel_price_discount_percent_cont_desc_TEMPLATE)
                    .put("percentage", discountPercentageString).format().toString()
        } else {
            ""
        }
        return if (!strikeThroughString.isNullOrBlank()) {
            Phrase.from(context, R.string.hotel_price_strike_through_cont_desc_TEMPLATE)
                    .put("strikethroughprice", strikeThroughString)
                    .put("price", priceString + perNightString)
                    .format().toString() +
                    discountPercentageString
        } else {
            priceString + perNightString
        }
    }

    private fun createOptionString(): SpannableString? {
        if (optionIndex >= 0) {
            val optionString = Phrase.from(context, R.string.option_TEMPLATE).put("number", optionIndex + 1).format().toString()

            val largeTextSize = context.resources.getDimensionPixelSize(R.dimen.type_300_text_size)
            val smallTextSize = context.resources.getDimensionPixelSize(R.dimen.type_100_text_size)

            var detailString = hotelRoomResponse.roomTypeDescriptionDetail

            if (!detailString.isNullOrBlank()) {
                detailString = "  (" + detailString + ")"
                val span = SpannableString(optionString + detailString)

                val lightTextColor = ContextCompat.getColor(context, R.color.light_text_color)
                span.setSpan(AbsoluteSizeSpan(smallTextSize), optionString.length, optionString.length + detailString.length, SPAN_INCLUSIVE_INCLUSIVE)
                span.setSpan(ForegroundColorSpan(lightTextColor), optionString.length, optionString.length + detailString.length, SPAN_INCLUSIVE_INCLUSIVE)

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

    private fun createCancellationString(): String {
        if (hotelRoomResponse.hasFreeCancellation) {
            return context.resources.getString(R.string.free_cancellation)
        } else {
            return context.resources.getString(R.string.non_refundable)
        }
    }

    private fun createCancellationTimeString(): String? {
        if (hotelRoomResponse.hasFreeCancellation && hotelRoomResponse.freeCancellationWindowDate != null) {
            val dateTime = DateUtils.yyyyMMddHHmmToDateTime(hotelRoomResponse.freeCancellationWindowDate).toLocalDate()
            val cancellationDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(dateTime)
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

    private fun createDiscountPercentageString(): String? {
        val isApplyingDiscount = (hotelLoyaltyInfo?.isBurnApplied ?: false) || chargeableRateInfo.isShowAirAttached()
        if (!isPackage && chargeableRateInfo.isDiscountPercentNotZero && !isApplyingDiscount) {
            return context.resources.getString(R.string.percent_off_TEMPLATE, HotelUtils.getDiscountPercent(chargeableRateInfo))
        } else {
            return null
        }
    }

    private fun createPayLaterPriceString(): String? {
        if (isPayLater) {
            val sb = StringBuilder(moneyToShowUser.formattedMoney)

            if (!isTotalPrice) {
                sb.append(context.resources.getString(R.string.per_night))
            }
            return sb.toString()
        }
        return null
    }

    private fun createStrikeThroughString(): String? {
        if (!isPayLater && chargeableRateInfo.isStrikeThroughPriceValid) {
            return chargeableRateInfo.getDisplayMoney(true, true).formattedMoney
        } else {
            return null
        }
    }

    private fun createPriceString(): String? {
        if (isPayLater) {
            if (haveDepositTerm) {
                return null
            } else {
                val depositAmount = chargeableRateInfo.depositAmountToShowUsers?.toDouble() ?: 0.0
                val depositAmountMoney = Money(BigDecimal(depositAmount), currencyCode)
                return Phrase.from(context, R.string.room_rate_pay_later_due_now).put("amount", depositAmountMoney.formattedMoney).format().toString()
            }
        } else {
            if (isPackage && moneyToShowUser.amount >= BigDecimal.ZERO) {
                return Phrase.from(context, R.string.plus_price_TEMPLATE).put("price", moneyToShowUser.formattedMoney).format().toString()
            } else {
                return moneyToShowUser.formattedMoney
            }
        }
    }

    private fun createRoomLeftString(): String? {
        try {
            val roomLeft = hotelRoomResponse.currentAllotment.toInt()
            if (roomLeft in 1..ROOMS_LEFT_CUTOFF) {
                return context.resources.getQuantityString(R.plurals.num_rooms_left, roomLeft, roomLeft)
            }
        } catch (e: NumberFormatException) {
            return null
        }
        return null
    }

    private fun getBookButtonContentDescription(): String {
        return if (isPackage)
            Phrase.from(context, R.string.book_room_button_with_options_content_description_TEMPLATE)
                    .put("room", hotelRoomResponse.roomTypeDescription ?: "").put("option", optionString ?: "").format().toString()
        else Phrase.from(context, R.string.select_room_button_with_options_content_description_TEMPLATE)
                .put("room", hotelRoomResponse.roomTypeDescription ?: "").put("option", optionString ?: "").format().toString()
    }
}
