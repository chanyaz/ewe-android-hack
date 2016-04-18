package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.mobiata.android.util.AndroidUtils
import rx.subjects.BehaviorSubject
import java.math.BigDecimal

class HotelCheckoutSummaryViewModel(val context: Context) {
    // input
    val tripResponseObserver = BehaviorSubject.create<HotelCreateTripResponse>()
    val newRateObserver = BehaviorSubject.create<HotelCreateTripResponse.HotelProductResponse>()
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

    init {
        tripResponseObserver.subscribe { tripResponse ->
            // detect price change between old and new offers
            val originalRoomResponse = tripResponse.originalHotelProductResponse.hotelRoomResponse
            val hasPriceChange = originalRoomResponse != null
            isPriceChange.onNext(hasPriceChange)

            if (hasPriceChange) {
                // potential price change
                val currencyCode = originalRoomResponse.rateInfo.chargeableRateInfo.currencyCode
                val originalPrice = originalRoomResponse.rateInfo.chargeableRateInfo.totalPriceWithMandatoryFees.toDouble()
                val newPrice = tripResponse.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.totalPriceWithMandatoryFees.toDouble()
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


                HotelV2Tracking().trackPriceChange(priceChange.toString())
            }
            newRateObserver.onNext(tripResponse.newHotelProductResponse)
        }

        newRateObserver.subscribe {
            val room = it.hotelRoomResponse
            val rate = room.rateInfo.chargeableRateInfo

            isPayLater.onNext(room.isPayLater && !AndroidUtils.isTablet(context))
            isResortCase.onNext(rate.totalMandatoryFees != 0f && Strings.equals(rate.checkoutPriceType, "totalPriceWithMandatoryFees"))
            isPayLaterOrResortCase.onNext(isPayLater.value || isResortCase.value)
            isDepositV2.onNext(room.depositRequired)
            priceAdjustments.onNext(rate.getPriceAdjustments())
            hotelName.onNext(it.getHotelName())
            checkInDate.onNext(it.checkInDate)
            checkInOutDatesFormatted.onNext(DateFormatUtils.formatHotelsV2DateRange(context, it.checkInDate, it.checkOutDate))
            address.onNext(it.hotelAddress)
            city.onNext(context.resources.getString(R.string.single_line_street_address_TEMPLATE, it.hotelCity, it.hotelStateProvince))
            roomDescriptions.onNext(room.roomTypeDescription)
            numNights.onNext(context.resources.getQuantityString(R.plurals.number_of_nights, it.numberOfNights.toInt(), it.numberOfNights.toInt()))
            bedDescriptions.onNext(room.formattedBedNames)
            numGuests.onNext(StrUtils.formatGuestString(context, it.adultCount.toInt()))
            hasFreeCancellation.onNext(room.hasFreeCancellation)
            currencyCode.onNext(rate.currencyCode)
            nightlyRatesPerRoom.onNext(rate.nightlyRatesPerRoom)
            nightlyRateTotal.onNext(rate.nightlyRateTotal.toString())
            surchargeTotalForEntireStay.onNext(Money(BigDecimal(rate.surchargeTotalForEntireStay.toString()), rate.currencyCode))
            taxStatusType.onNext(rate.taxStatusType)
            extraGuestFees.onNext(rate.extraGuestFees)

            // calculate trip total price
            dueNowAmount.onNext(it.dueNowAmount.formattedMoney)
            tripTotalPrice.onNext(rate.displayTotalPrice.getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL))

            showFeesPaidAtHotel.onNext(isResortCase.value)
            feesPaidAtHotel.onNext(Money(BigDecimal(rate.totalMandatoryFees.toString()), currencyCode.value).formattedMoney)
            isBestPriceGuarantee.onNext(room.isMerchant)
            newDataObservable.onNext(this)

            roomHeaderImage.onNext(it.largeThumbnailUrl)
        }
        guestCountObserver.subscribe {
            numGuests.onNext(StrUtils.formatGuestString(context, it))
        }
    }
}