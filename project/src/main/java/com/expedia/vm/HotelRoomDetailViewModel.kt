package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.HotelUtils
import com.expedia.util.LoyaltyUtil
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import java.math.BigDecimal
import java.util.ArrayList

class HotelRoomDetailViewModel(val context: Context, val hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, val hotelId: String, val rowIndex: Int, val optionIndex: Int, val hasETP: Boolean) {

    val isPackage: Boolean get() = hotelRoomResponse.isPackage

    val optionString: String?
        get() = if (optionIndex >= 0) {
            Phrase.from(context, R.string.option_TEMPLATE).put("number", optionIndex + 1).format().toString()
        } else { null }

    val cancellationString: String
        get() = if (hotelRoomResponse.hasFreeCancellation) {
            context.resources.getString(R.string.free_cancellation)
        } else {
            context.resources.getString(R.string.non_refundable)
        }

    val cancellationTimeString: String? get() = createCancellationTimeString()

    val amenityToShow: List<Pair<String, Int>> get() = createAmenityToShow()

    val earnMessageString: String? get() = createEarnMessageString()

    val mandatoryFeeString: String? get() = createMandatoryFeeString()

    val discountPercentageString: String?
        get() = if (!isPackage && chargeableRateInfo.isDiscountPercentNotZero
                && !(hotelLoyaltyInfo?.isBurnApplied ?: false) && !chargeableRateInfo.isShowAirAttached()) {
            context.resources.getString(R.string.percent_off_TEMPLATE, HotelUtils.getDiscountPercent(chargeableRateInfo))
        } else { null }

    val payLaterPriceString: String? get() = createPayLaterPriceString()

    val showDepositTerm: Boolean get() = isPayLater && haveDepositTerm

    val strikeThroughString: String?
        get() = if (!isPayLater && chargeableRateInfo.priceToShowUsers < chargeableRateInfo.strikethroughPriceToShowUsers) {
            Money(BigDecimal(chargeableRateInfo.strikethroughPriceToShowUsers.toDouble()), currencyCode).formattedMoney
        } else { null }

    val pricePerNightString: String? get() = createPricePerNightString()

    val showPerNight: Boolean get() = !isPayLater && !isTotalPrice

    val roomLeftString: String? get() = createRoomLeftString()

    val roomSoldOut = BehaviorSubject.create<Boolean>(false)

    private val chargeableRateInfo = hotelRoomResponse.rateInfo.chargeableRateInfo
    private val hotelLoyaltyInfo: LoyaltyInformation? = chargeableRateInfo.loyaltyInfo
    private val currencyCode = chargeableRateInfo.currencyCode
    private val isPayLater = hotelRoomResponse.isPayLater
    private val priceToShowUser = Money(BigDecimal(chargeableRateInfo.priceToShowUsers.toDouble()), currencyCode).formattedMoney
    private val isTotalPrice = chargeableRateInfo.getUserPriceType() == HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES
    private val haveDepositTerm = hotelRoomResponse.depositPolicy != null && !hotelRoomResponse.depositPolicy.isEmpty()

    private fun createCancellationTimeString(): String? {
        if (hotelRoomResponse.hasFreeCancellation && hotelRoomResponse.freeCancellationWindowDate != null) {
            val dateTime = DateUtils.yyyyMMddHHmmToDateTime(hotelRoomResponse.freeCancellationWindowDate).toLocalDate()
            val cancellationDate = DateUtils.localDateToEEEMMMd(dateTime)
            return Phrase.from(context, R.string.before_TEMPLATE).put("date", cancellationDate).format().toString()
        }
        return null
    }

    private fun createAmenityToShow(): List<Pair<String, Int>> {
        val amenities = ArrayList<Pair<String, Int>>()
        if (hotelRoomResponse.valueAdds != null && hotelRoomResponse.valueAdds.isNotEmpty()) {
            hotelRoomResponse.valueAdds.forEach { valueAdd ->
                // TODO: create function to get value add icon and string from id
                // https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/189
                val amenityAndIcon = Pair<String, Int>(valueAdd.description, R.drawable.checkmark)
                amenities.add(amenityAndIcon)
            }
        }

        return amenities
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
        val roomLeft = hotelRoomResponse.currentAllotment.toInt()
        if (roomLeft != null && roomLeft > 0 && roomLeft <= ROOMS_LEFT_CUTOFF) {
            return context.resources.getQuantityString(R.plurals.num_rooms_left, roomLeft, roomLeft)
        }
        return null
    }
}
