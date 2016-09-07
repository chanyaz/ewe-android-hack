package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.tracking.HotelTracking
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.mobiata.android.util.AndroidUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import java.math.BigDecimal
import java.text.NumberFormat

class HotelCheckoutSummaryViewModel(val context: Context, val paymentModel: PaymentModel<HotelCreateTripResponse>) {
    // output
    val newDataObservable = BehaviorSubject.create<HotelCheckoutSummaryViewModel>()
    val hotelName = BehaviorSubject.create<String>()
    val checkInDate = BehaviorSubject.create<String>()
    val checkInOutDatesFormatted = BehaviorSubject.create<String>()
    val address = BehaviorSubject.create<String>()
    val city = BehaviorSubject.create<String>()
    val roomDescriptions = BehaviorSubject.create<String>()
    val bedDescriptions = BehaviorSubject.create<String>()
    val numNights = BehaviorSubject.create<String>()
    val guestCountObserver = BehaviorSubject.create<Int>()
    val numGuests = BehaviorSubject.create<String>()
    val hasFreeCancellation = BehaviorSubject.create<Boolean>(false)
    val currencyCode = BehaviorSubject.create<String>()
    val nightlyRatesPerRoom = BehaviorSubject.create<List<HotelRate.NightlyRatesPerRoom>>()
    val nightlyRateTotal = BehaviorSubject.create<String>()
    val dueNowAmount = BehaviorSubject.create<String>()
    val tripTotalPrice = BehaviorSubject.create<String>()
    val priceAdjustments = BehaviorSubject.create<Money>()
    val surchargeTotalForEntireStay = BehaviorSubject.create<Money>()
    val taxStatusType = BehaviorSubject.create<String>()
    val extraGuestFees = BehaviorSubject.create<Money>()
    val isBestPriceGuarantee = BehaviorSubject.create<Boolean>(false)
    val priceChangeMessage = BehaviorSubject.create<String>()
    val isPriceChange = BehaviorSubject.create<Boolean>(false)
    val priceChangeIconResourceId = BehaviorSubject.create<Int>()
    val isResortCase = BehaviorSubject.create<Boolean>(false)
    val isPayLater = BehaviorSubject.create<Boolean>(false)
    val isPayLaterOrResortCase = BehaviorSubject.create<Boolean>(false)
    val isDepositV2 = BehaviorSubject.create<Boolean>(false)
    val feesPaidAtHotel = BehaviorSubject.create<String>()
    val showFeesPaidAtHotel = BehaviorSubject.create<Boolean>(false)
    val roomHeaderImage = BehaviorSubject.create<String?>()
    val burnAmountShownOnHotelCostBreakdown = BehaviorSubject.create<String>()
    val burnPointsShownOnHotelCostBreakdown = BehaviorSubject.create<String>()
    val isShoppingWithPoints = BehaviorSubject.create<Boolean>()

    init {
        paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse.map {
            object {
                val country = it.tripResponse.newHotelProductResponse.hotelCountry
                val originalRoomResponse = it.tripResponse.originalHotelProductResponse.hotelRoomResponse
                val newHotelProductResponse = it.tripResponse.newHotelProductResponse
                val isExpediaRewardsRedeemable = it.tripResponse.isRewardsRedeemable()
                val payingWithPoints = it.paymentSplits.payingWithPoints
                val payingWithCard = it.paymentSplits.payingWithCards
                val paymentSplitsType = it.paymentSplits.paymentSplitsType()
                val tripTotalPayableIncludingFee = it.tripTotalPayableIncludingFee
            }
        }.subscribe {
            // detect price change between old and new offers
            val hasPriceChange = it.originalRoomResponse != null
            isPriceChange.onNext(hasPriceChange)

            if (hasPriceChange) {
                // potential price change
                val currencyCode = it.originalRoomResponse.rateInfo.chargeableRateInfo.currencyCode
                val originalPrice = it.originalRoomResponse.rateInfo.chargeableRateInfo.totalPriceWithMandatoryFees.toDouble()
                val newPrice = it.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.totalPriceWithMandatoryFees.toDouble()
                val priceChange = (originalPrice - newPrice)

                if (newPrice > originalPrice) {
                    priceChangeIconResourceId.onNext(R.drawable.price_change_increase)
                    priceChangeMessage.onNext(context.getString(R.string.price_changed_from_TEMPLATE, Money(BigDecimal(originalPrice), currencyCode).formattedMoney))
                } else if (newPrice < originalPrice) {
                    priceChangeMessage.onNext(context.getString(R.string.price_dropped_from_TEMPLATE, Money(BigDecimal(originalPrice), currencyCode).formattedMoney))
                    priceChangeIconResourceId.onNext(R.drawable.price_change_decrease)
                } else {
                    // API could return price change error with no difference in price (see: hotel_price_change_checkout.json)
                    priceChangeIconResourceId.onNext(R.drawable.price_change_decrease)
                    priceChangeMessage.onNext(context.getString(R.string.price_changed_from_TEMPLATE, Money(BigDecimal(originalPrice), currencyCode).formattedMoney))
                }

                HotelTracking().trackPriceChange(priceChange.toString())
            }
            val room = it.newHotelProductResponse.hotelRoomResponse
            val rate = room.rateInfo.chargeableRateInfo

            isPayLater.onNext(room.isPayLater && !AndroidUtils.isTablet(context))
            isResortCase.onNext(rate.totalMandatoryFees != 0f && Strings.equals(rate.checkoutPriceType, "totalPriceWithMandatoryFees"))
            isPayLaterOrResortCase.onNext(isPayLater.value || isResortCase.value)
            isDepositV2.onNext(room.depositRequired)
            priceAdjustments.onNext(rate.getPriceAdjustments())
            hotelName.onNext(it.newHotelProductResponse.getHotelName())
            checkInDate.onNext(it.newHotelProductResponse.checkInDate)
            checkInOutDatesFormatted.onNext(DateFormatUtils.formatHotelsV2DateRange(context, it.newHotelProductResponse.checkInDate, it.newHotelProductResponse.checkOutDate))
            address.onNext(it.newHotelProductResponse.hotelAddress)
            city.onNext(context.resources.getString(R.string.single_line_street_address_TEMPLATE, it.newHotelProductResponse.hotelCity, it.newHotelProductResponse.hotelStateProvince))
            roomDescriptions.onNext(room.roomTypeDescription)
            numNights.onNext(context.resources.getQuantityString(R.plurals.number_of_nights, it.newHotelProductResponse.numberOfNights.toInt(), it.newHotelProductResponse.numberOfNights.toInt()))
            bedDescriptions.onNext(room.formattedBedNames)
            hasFreeCancellation.onNext(room.hasFreeCancellation)
            currencyCode.onNext(rate.currencyCode)
            nightlyRatesPerRoom.onNext(rate.nightlyRatesPerRoom)
            nightlyRateTotal.onNext(rate.nightlyRateTotal.toString())
            surchargeTotalForEntireStay.onNext(Money(BigDecimal(rate.surchargeTotalForEntireStay.toString()), rate.currencyCode))
            taxStatusType.onNext(rate.taxStatusType)
            extraGuestFees.onNext(rate.extraGuestFees)

            showFeesPaidAtHotel.onNext(isResortCase.value)
            feesPaidAtHotel.onNext(Money(BigDecimal(rate.totalMandatoryFees.toString()), currencyCode.value).formattedMoney)
            isBestPriceGuarantee.onNext(PointOfSale.getPointOfSale().displayBestPriceGuarantee() && room.isMerchant)
            if (it.isExpediaRewardsRedeemable && !it.paymentSplitsType.equals(PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD)) {
                dueNowAmount.onNext(it.payingWithCard.amount.formattedMoneyFromAmountAndCurrencyCode)
                burnPointsShownOnHotelCostBreakdown.onNext(Phrase.from(context, R.string.hotel_cost_breakdown_burn_points_TEMPLATE)
                        .putOptional("points", NumberFormat.getInstance().format(it.payingWithPoints.points)).format().toString())
                burnAmountShownOnHotelCostBreakdown.onNext(it.payingWithPoints.amount.formattedMoneyFromAmountAndCurrencyCode)
                tripTotalPrice.onNext(it.tripTotalPayableIncludingFee.formattedMoneyFromAmountAndCurrencyCode)
                isShoppingWithPoints.onNext(true)
            } else {
                tripTotalPrice.onNext(rate.getDisplayTotalPrice().getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL))
                dueNowAmount.onNext(it.newHotelProductResponse.dueNowAmount.formattedMoney)

                isShoppingWithPoints.onNext(false)
            }

            newDataObservable.onNext(this)

            roomHeaderImage.onNext(it.newHotelProductResponse.largeThumbnailUrl)
        }
        guestCountObserver.subscribe {
            numGuests.onNext(StrUtils.formatGuestString(context, it))
        }
    }
}