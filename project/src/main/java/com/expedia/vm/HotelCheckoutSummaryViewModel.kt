package com.expedia.vm

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.util.Optional
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.LocalDate
import java.math.BigDecimal
import java.text.NumberFormat


class HotelCheckoutSummaryViewModel(val context: Context, val paymentModel: PaymentModel<HotelCreateTripResponse>) {
    // output
    val newDataObservable = BehaviorSubject.create<Unit>()
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
    val hasFreeCancellation = BehaviorSubject.createDefault<Boolean>(false)
    val freeCancellationText = PublishSubject.create<String>()
    val valueAddsListObservable = BehaviorSubject.create<List<HotelOffersResponse.ValueAdds>>()
    val currencyCode = BehaviorSubject.create<String>()
    val nightlyRatesPerRoom = BehaviorSubject.create<List<HotelRate.NightlyRatesPerRoom>>()
    val nightlyRateTotal = BehaviorSubject.create<String>()
    val dueNowAmount = BehaviorSubject.create<String>()
    val tripTotalPrice = BehaviorSubject.create<String>()
    val priceAdjustments = BehaviorSubject.create<Money>()
    val surchargeTotalForEntireStay = BehaviorSubject.create<Money>()
    val propertyServiceSurcharge = BehaviorSubject.create<Optional<Money>>()
    val taxStatusType = BehaviorSubject.create<String>()
    val extraGuestFees = BehaviorSubject.create<Money>()
    val isBestPriceGuarantee = BehaviorSubject.createDefault<Boolean>(false)
    val priceChangeMessage = BehaviorSubject.create<String>()
    val isPriceChange = BehaviorSubject.createDefault<Boolean>(false)
    val priceChangeIconResourceId = BehaviorSubject.create<Int>()
    val isResortCase = BehaviorSubject.createDefault<Boolean>(false)
    val isPayLater = BehaviorSubject.createDefault<Boolean>(false)
    val isPayLaterOrResortCase = BehaviorSubject.createDefault<Boolean>(false)
    val isDepositV2 = BehaviorSubject.createDefault<Boolean>(false)
    val feesPaidAtHotel = BehaviorSubject.create<String>()
    val showFeesPaidAtHotel = BehaviorSubject.createDefault<Boolean>(false)
    val roomHeaderImage = BehaviorSubject.create<String?>()
    val burnAmountShownOnHotelCostBreakdown = BehaviorSubject.create<String>()
    val burnPointsShownOnHotelCostBreakdown = BehaviorSubject.create<String>()
    val isShoppingWithPoints = BehaviorSubject.create<Boolean>()
    val costSummaryContentDescription = BehaviorSubject.create<String>()
    val amountDueTodayLabelObservable = BehaviorSubject.create<String>()
    val createTripConsumed = BehaviorSubject.create<Unit>()
    val newPriceSetObservable = BehaviorSubject.create<Unit>()
    val createTripResponseObservable = PublishSubject.create<HotelCreateTripResponse>()
    val checkinDateFormattedByEEEMMDD = PublishSubject.create<String>()
    val checkoutDateFormattedByEEEMMDD = PublishSubject.create<String>()

    init {

        //TO-DO remove these observables when rewriting HotelBreakDownViewModel to be more reactive
        ObservableOld.combineLatest(newPriceSetObservable, createTripConsumed, { _, _ ->
            newDataObservable.onNext(Unit)
        }).subscribe()

        createTripResponseObservable.subscribe { it ->

            val room = it.newHotelProductResponse.hotelRoomResponse
            val rate = room.rateInfo.chargeableRateInfo

            priceAdjustments.onNext(rate.getPriceAdjustments())
            hotelName.onNext(it.newHotelProductResponse.getHotelName())
            checkInDate.onNext(it.newHotelProductResponse.checkInDate)
            if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline)) {
                checkinDateFormattedByEEEMMDD.onNext(DateFormatUtils.formatLocalDateToEEEMMMdBasedOnLocale(LocalDate.parse(it.newHotelProductResponse.checkInDate)))
                checkoutDateFormattedByEEEMMDD.onNext(DateFormatUtils.formatLocalDateToEEEMMMdBasedOnLocale(LocalDate.parse(it.newHotelProductResponse.checkOutDate)))
            } else {
                checkInOutDatesFormatted.onNext(DateFormatUtils.formatHotelsV2DateRange(context, it.newHotelProductResponse.checkInDate, it.newHotelProductResponse.checkOutDate))
            }
            address.onNext(it.newHotelProductResponse.hotelAddress)
            city.onNext(context.resources.getString(R.string.single_line_street_address_TEMPLATE, it.newHotelProductResponse.hotelCity, it.newHotelProductResponse.hotelStateProvince))
            roomDescriptions.onNext(room.roomTypeDescription)
            numNights.onNext(context.resources.getQuantityString(R.plurals.number_of_nights, it.newHotelProductResponse.numberOfNights.toInt(), it.newHotelProductResponse.numberOfNights.toInt()))
            bedDescriptions.onNext(room.formattedBedNames)
            hasFreeCancellation.onNext(room.hasFreeCancellation)
            freeCancellationText.onNext(HotelUtils.getFreeCancellationText(context, it.newHotelProductResponse.hotelRoomResponse.freeCancellationWindowDate))
            valueAddsListObservable.onNext(room.valueAdds)
            currencyCode.onNext(rate.currencyCode)
            nightlyRatesPerRoom.onNext(rate.nightlyRatesPerRoom)
            nightlyRateTotal.onNext(rate.nightlyRateTotal.toString())

            if (PointOfSale.getPointOfSale().supportPropertyFee()) {
                val propertyServiceFee = rate.propertyServiceFees
                val surchargeTotalPrice = if (propertyServiceFee == null) rate.surchargeTotalForEntireStay.toString() else rate.surchargesWithoutPropertyFeeForEntireStay.toString()
                surchargeTotalForEntireStay.onNext(Money(BigDecimal(surchargeTotalPrice), rate.currencyCode))
                propertyServiceSurcharge.onNext(Optional(propertyServiceFee))
            } else {
                surchargeTotalForEntireStay.onNext(Money(BigDecimal(rate.surchargeTotalForEntireStay.toString()), rate.currencyCode))
                propertyServiceSurcharge.onNext(Optional(null))
            }
            taxStatusType.onNext(rate.taxStatusType)
            extraGuestFees.onNext(rate.extraGuestFees)

            feesPaidAtHotel.onNext(Money(BigDecimal(rate.totalMandatoryFees.toString()), currencyCode.value).formattedMoney)
            isBestPriceGuarantee.onNext(PointOfSale.getPointOfSale().displayBestPriceGuarantee() && room.isMerchant)

            roomHeaderImage.onNext(it.newHotelProductResponse.largeThumbnailUrl)
            createTripConsumed.onNext(Unit)
        }

        paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse.map {
            object {
                val originalRoomResponse = it.tripResponse.originalHotelProductResponse.hotelRoomResponse
                val newHotelProductResponse = it.tripResponse.newHotelProductResponse
                val room = newHotelProductResponse.hotelRoomResponse
                val rate = newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo
                val isExpediaRewardsRedeemable = it.tripResponse.isRewardsRedeemable()
                val payingWithPoints = it.paymentSplits.payingWithPoints
                val payingWithCard = it.paymentSplits.payingWithCards
                val paymentSplitsType = it.paymentSplits.paymentSplitsType()
                val tripTotalPayableIncludingFee = it.tripTotalPayableIncludingFee
            }
        }.subscribe {

            val hasPriceChange = it.originalRoomResponse != null
            isPriceChange.onNext(hasPriceChange)

            if (hasPriceChange) {
                // potential price change
                val currencyCode = it.originalRoomResponse.rateInfo.chargeableRateInfo.currencyCode
                val originalPrice = it.originalRoomResponse.rateInfo.chargeableRateInfo.totalPriceWithMandatoryFees.toDouble()
                val newPrice = it.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.totalPriceWithMandatoryFees.toDouble()
                val priceChange = (originalPrice - newPrice)

                if (newPrice > originalPrice) {
                    priceChangeIconResourceId.onNext(R.drawable.warning_triangle_icon)
                    priceChangeMessage.onNext(context.getString(R.string.price_changed_from_TEMPLATE, Money(BigDecimal(originalPrice), currencyCode).formattedMoney))
                } else if (newPrice < originalPrice) {
                    priceChangeMessage.onNext(context.getString(R.string.price_dropped_from_TEMPLATE, Money(BigDecimal(originalPrice), currencyCode).formattedMoney))
                    priceChangeIconResourceId.onNext(R.drawable.price_change_decrease)
                } else {
                    // API could return price change error with no difference in price (see: hotel_price_change_checkout.json)
                    priceChangeIconResourceId.onNext(R.drawable.price_change_decrease)
                    priceChangeMessage.onNext(context.getString(R.string.price_changed_from_TEMPLATE, Money(BigDecimal(originalPrice), currencyCode).formattedMoney))
                }

                HotelTracking.trackPriceChange(priceChange.toString())
            }

            //TODO remove dependency on .value(BehaviorSubjects here)
            isPayLater.onNext(it.room.isPayLater)
            isResortCase.onNext(it.rate.totalMandatoryFees != 0f && Strings.equals(it.rate.checkoutPriceType, "totalPriceWithMandatoryFees"))
            isPayLaterOrResortCase.onNext(isPayLater.value || isResortCase.value)
            isDepositV2.onNext(it.room.depositRequired)
            showFeesPaidAtHotel.onNext(isResortCase.value)

            if (it.isExpediaRewardsRedeemable && !it.paymentSplitsType.equals(PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD)) {
                dueNowAmount.onNext(it.payingWithCard.amount.formattedMoneyFromAmountAndCurrencyCode)
                burnPointsShownOnHotelCostBreakdown.onNext(Phrase.from(context, R.string.hotel_cost_breakdown_burn_points_TEMPLATE)
                        .putOptional("points", NumberFormat.getInstance().format(it.payingWithPoints.points)).format().toString())
                burnAmountShownOnHotelCostBreakdown.onNext(it.payingWithPoints.amount.formattedMoneyFromAmountAndCurrencyCode)
                tripTotalPrice.onNext(it.tripTotalPayableIncludingFee.formattedMoneyFromAmountAndCurrencyCode)
                isShoppingWithPoints.onNext(true)
            } else {
                tripTotalPrice.onNext(it.rate.displayTotalPrice.getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL))
                dueNowAmount.onNext(it.newHotelProductResponse.dueNowAmount.formattedMoney)
                isShoppingWithPoints.onNext(false)
            }
            val amountDueTodayText: String
            val accessibilityCostSummaryContentDescription = StringBuilder()
            accessibilityCostSummaryContentDescription.append(context.getString(R.string.total_with_tax)).append(" ").append(tripTotalPrice.value).append(" ")

            if (isDepositV2.value) {
                amountDueTodayText = Phrase.from(context, R.string.due_to_brand_today_today_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
                appendFeesPaidCostSummaryContDesc(accessibilityCostSummaryContentDescription)
                accessibilityCostSummaryContentDescription.append(Phrase.from(context, R.string.due_to_brand_today_today_TEMPLATE).put("brand", BuildConfig.brand).format().toString()).append(" ").append(dueNowAmount.value).append(" ")

            } else if (isPayLaterOrResortCase.value) {
                amountDueTodayText = Phrase.from(context, R.string.due_to_brand_today_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
                appendFeesPaidCostSummaryContDesc(accessibilityCostSummaryContentDescription)
                accessibilityCostSummaryContentDescription.append(Phrase.from(context, R.string.due_to_brand_today_TEMPLATE).put("brand", BuildConfig.brand).format().toString()).append(" ").append(dueNowAmount.value).append(" ")
            } else {
                amountDueTodayText = context.getString(R.string.total_with_tax)
            }

            amountDueTodayLabelObservable.onNext(amountDueTodayText)
            costSummaryContentDescription.onNext(accessibilityCostSummaryContentDescription.toString())
            newPriceSetObservable.onNext(Unit)
        }

        guestCountObserver.subscribe {
            numGuests.onNext(StrUtils.formatGuestString(context, it))
        }
    }

    private fun appendFeesPaidCostSummaryContDesc(accessibilityCostSummaryContentDescription: StringBuilder) {
        if (showFeesPaidAtHotel.value) {
            accessibilityCostSummaryContentDescription.append(context.getString(R.string.fees_paid_at_hotel)).append(" ").append(feesPaidAtHotel.value).append(" ")
        }
    }
}