package com.expedia.vm

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelCheckoutV2Params
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import rx.Observable
import com.mobiata.android.util.AndroidUtils
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal

open public class HotelCheckoutViewModel(val hotelServices: HotelServices, val paymentModel: PaymentModel<HotelCreateTripResponse>) {

    // inputs
    val checkoutParams = PublishSubject.create<HotelCheckoutV2Params>()

    // outputs
    val errorObservable = PublishSubject.create<ApiError>()
    val noResponseObservable = PublishSubject.create<Throwable>()
    val checkoutResponseObservable = BehaviorSubject.create<HotelCheckoutResponse>()
    val priceChangeResponseObservable = BehaviorSubject.create<HotelCreateTripResponse>()

    init {
        checkoutParams.subscribe { params ->
            hotelServices.checkout(params, getCheckoutResponseObserver())
        }
    }

    open fun getCheckoutResponseObserver(): Observer<HotelCheckoutResponse> {
        return object : Observer<HotelCheckoutResponse> {
            override fun onNext(checkout: HotelCheckoutResponse) {
                if (checkout.hasErrors()) {
                    when (checkout.firstError.errorCode) {
                        ApiError.Code.PRICE_CHANGE -> {
                            val hotelCreateTripResponse = Db.getTripBucket().hotelV2.updateHotelProducts(checkout.checkoutResponse.jsonPriceChangeResponse)
                            priceChangeResponseObservable.onNext(hotelCreateTripResponse)
                            paymentModel.priceChangeDuringCheckoutSubject.onNext(hotelCreateTripResponse)
                        }
                        ApiError.Code.INVALID_INPUT -> {
                            val field = checkout.firstError.errorInfo.field
                            if (field == "mainMobileTraveler.lastName" ||
                                    field == "mainMobileTraveler.firstName" ||
                                    field == "phone") {
                                val apiError = ApiError(ApiError.Code.HOTEL_CHECKOUT_TRAVELLER_DETAILS)
                                apiError.errorInfo = ApiError.ErrorInfo()
                                apiError.errorInfo.field = field
                                errorObservable.onNext(apiError)
                            } else {
                                val apiError = ApiError(ApiError.Code.HOTEL_CHECKOUT_CARD_DETAILS)
                                apiError.errorInfo = ApiError.ErrorInfo()
                                apiError.errorInfo.field = field
                                errorObservable.onNext(apiError)
                            }
                        }
                        ApiError.Code.TRIP_ALREADY_BOOKED -> {
                            errorObservable.onNext(checkout.firstError)
                        }
                        ApiError.Code.SESSION_TIMEOUT -> {
                            errorObservable.onNext(checkout.firstError)
                        }
                        ApiError.Code.PAYMENT_FAILED -> {
                            errorObservable.onNext(checkout.firstError)
                        }
                        ApiError.Code.HOTEL_ROOM_UNAVAILABLE-> {
                            errorObservable.onNext(checkout.firstError)
                        }
                        else -> {
                            errorObservable.onNext(ApiError(ApiError.Code.HOTEL_CHECKOUT_UNKNOWN))
                        }
                    }
                } else {
                    checkoutResponseObservable.onNext(checkout)
                }
            }

            override fun onError(e: Throwable) {
                noResponseObservable.onNext(e)
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }
}

open class HotelCreateTripViewModel(val hotelServices: HotelServices, val paymentModel: PaymentModel<HotelCreateTripResponse>) {

    // input
    val tripParams = PublishSubject.create<HotelCreateTripParams>()

    // output
    val errorObservable = PublishSubject.create<ApiError>()
    val noResponseObservable = PublishSubject.create<Unit>()
    val tripResponseObservable = BehaviorSubject.create<HotelCreateTripResponse>()

    init {
        tripParams.subscribe { params ->
            hotelServices.createTrip(params, getCreateTripResponseObserver())
        }
    }

    open fun getCreateTripResponseObserver(): Observer<HotelCreateTripResponse> {
        return object: Observer<HotelCreateTripResponse> {
            override fun onNext(t: HotelCreateTripResponse) {
                if (t.hasErrors()) {
                    if (t.firstError.errorInfo.field == "productKey") {
                        errorObservable.onNext(ApiError(ApiError.Code.HOTEL_PRODUCT_KEY_EXPIRY))
                } else if (t.firstError.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE) {
                    errorObservable.onNext(ApiError(ApiError.Code.HOTEL_ROOM_UNAVAILABLE))
                } else {
                        errorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                    }
                } else {
                    // TODO: Move away from using DB. observers should react on fresh createTrip response
                    Db.getTripBucket().clearHotelV2()
                    Db.getTripBucket().add(TripBucketItemHotelV2(t))
                    // TODO: populate hotelCreateTripResponseData with response data
                    tripResponseObservable.onNext(t)
                    paymentModel.createTripSubject.onNext(t)
                }
            }

            override fun onError(e: Throwable) {
                if (RetrofitUtils.isNetworkError(e)) {
                    noResponseObservable.onNext(Unit)
                }
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }
}

class HotelCheckoutOverviewViewModel(val context: Context, val paymentModel: PaymentModel<HotelCreateTripResponse>) {
    // input
    val newRateObserver = BehaviorSubject.create<HotelCreateTripResponse.HotelProductResponse>()
    // output
    val legalTextInformation = BehaviorSubject.create<SpannableStringBuilder>()
    val disclaimerText = BehaviorSubject.create<Spanned>()
    val depositPolicyText = BehaviorSubject.create<Spanned>()
    val slideToText = BehaviorSubject.create<String>()
    val resetMenuButton = BehaviorSubject.create<Unit>()
    val totalPriceCharged: Observable<String> =
            paymentModel.paymentSplits.withLatestFrom(paymentModel.tripResponses, { paymentSplits, tripResponse ->
                object {
                    val payingWithPoints = paymentSplits.payingWithPoints
                    val payingWithCards = paymentSplits.payingWithCards
                    val paymentSplitsType = paymentSplits.paymentSplitsType()
                    val isExpediaRewardsRedeemable = tripResponse.isExpediaRewardsRedeemable()
                    val hotelProductResponse = tripResponse.newHotelProductResponse
                }
            }).map {
                when (it.isExpediaRewardsRedeemable) {
                    true ->
                        when (it.paymentSplitsType) {
                            PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT ->
                                Phrase.from(context, R.string.you_are_using_expedia_points_TEMPLATE)
                                        .put("amount", it.payingWithPoints.amount.formattedMoneyFromAmountAndCurrencyCode)
                                        .put("points", it.payingWithPoints.points.toString())
                                        .format().toString()

                            PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD ->
                                Phrase.from(context, R.string.your_card_will_be_charged_template)
                                        .put("dueamount", it.payingWithCards.amount.formattedMoneyFromAmountAndCurrencyCode)
                                        .format().toString()

                            PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD ->
                                Phrase.from(context, R.string.payment_through_card_and_pwp_points)
                                        .put("amount", it.payingWithPoints.amount.formattedMoneyFromAmountAndCurrencyCode)
                                        .put("points", it.payingWithPoints.points.toString())
                                        .put("dueamount", it.payingWithCards.amount.formattedMoneyFromAmountAndCurrencyCode)
                                        .format().toString()
                        }
                    false -> Phrase.from(context, R.string.your_card_will_be_charged_template)
                            .put("dueamount", getDueNowAmount(it.hotelProductResponse)).format().toString()
                }
            }

    init {
        newRateObserver.subscribe {
            disclaimerText.onNext(Html.fromHtml(""))

            val room = it.hotelRoomResponse
            if (room.isPayLater) {
                slideToText.onNext(context.getString(R.string.hotelsv2_slide_reserve))
            } else {
                slideToText.onNext(context.getString(R.string.hotelsv2_slide_purchase))
            }

            val currencyCode = room.rateInfo.chargeableRateInfo.currencyCode
            val tripTotal = room.rateInfo.chargeableRateInfo.displayTotalPrice.formattedMoney
            if (room.rateInfo.chargeableRateInfo.showResortFeeMessage) {
                val resortFees = Money(BigDecimal(room.rateInfo.chargeableRateInfo.totalMandatoryFees.toDouble()), currencyCode).formattedMoney
                val text = Html.fromHtml(context.getString(R.string.resort_fee_disclaimer_TEMPLATE, resortFees, tripTotal));
                disclaimerText.onNext(text)
            } else if (room.isPayLater) {
                if (room.rateInfo.chargeableRateInfo.depositAmount != null) {
                    val depositText = Html.fromHtml(room.depositPolicyAtIndex(0) + " " + room.depositPolicyAtIndex(1))
                    depositPolicyText.onNext(depositText)
                }
                val text = Html.fromHtml(context.getString(R.string.pay_later_disclaimer_TEMPLATE, tripTotal))
                disclaimerText.onNext(text)
            }

            legalTextInformation.onNext(StrUtils.generateHotelsBookingStatement(context, PointOfSale.getPointOfSale().hotelBookingStatement.toString(), false))
            resetMenuButton.onNext(Unit)
        }
    }
}

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
            isResortCase.onNext(rate.totalMandatoryFees != 0f)
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
            dueNowAmount.onNext(getDueNowAmount(it))
            tripTotalPrice.onNext(rate.displayTotalPrice.formattedMoney)

            showFeesPaidAtHotel.onNext(isResortCase.value && (rate.totalMandatoryFees != 0f))
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

public fun getDueNowAmount(response: HotelCreateTripResponse.HotelProductResponse): String {
    val room = response.hotelRoomResponse
    val rate = room.rateInfo.chargeableRateInfo
    val isPayLater = room.isPayLater

    if (isPayLater) {
        val depositAmount = rate.depositAmount ?: "0" // yup. For some reason API doesn't return $0 for deposit amounts
        return Money(BigDecimal(depositAmount), rate.currencyCode).formattedMoney
    } else {
        return Money(BigDecimal(rate.total.toDouble()), rate.currencyCode).formattedMoney
    }
}

data class Breakdown(val title: String, val cost: String, val isDate: Boolean, val isDiscount: Boolean)

class HotelBreakDownViewModel(val context: Context, val hotelCheckoutSummaryViewModel: HotelCheckoutSummaryViewModel) {
    val addRows = BehaviorSubject.create<List<Breakdown>>()
    val dtf = DateTimeFormat.forPattern("M/dd/yyyy")

    init {
        hotelCheckoutSummaryViewModel.newDataObservable.subscribe {
            var breakdowns = arrayListOf<Breakdown>()
            val nightlyRate = Money(BigDecimal(it.nightlyRateTotal.value), it.currencyCode.value)
            breakdowns.add(Breakdown(it.numNights.value, nightlyRate.formattedMoney, false, false))

            var count = 0;
            val checkIn = DateUtils.yyyyMMddToLocalDate(it.checkInDate.value)
            for (rate in it.nightlyRatesPerRoom.value) {
                val date = dtf.print(checkIn.plusDays(count))
                val amount = Money(BigDecimal(rate.rate), it.currencyCode.value)
                val amountStr = if ((amount.isZero))
                    context.getString(R.string.free)
                else
                    amount.formattedMoney

                breakdowns.add(Breakdown(date, amountStr, true, false))
                count++
            }

            // Discount
            val couponRate = it.priceAdjustments.value
            if (couponRate != null && !couponRate.isZero) {
                breakdowns.add(Breakdown(context.getString(R.string.discount), couponRate.formattedMoney, false, true))
            }

            // Taxes & Fees
            val surchargeTotal = it.surchargeTotalForEntireStay.value
            val taxStatusType = it.taxStatusType.value
            if (taxStatusType != null && taxStatusType.equals("UNKNOWN")) {
                breakdowns.add(Breakdown(context.getString(R.string.taxes_and_fees), context.getString(R.string.unknown), false, false))
            } else if (taxStatusType != null && taxStatusType.equals("INCLUDED")) {
                breakdowns.add(Breakdown(context.getString(R.string.taxes_and_fees), context.getString(R.string.included), false, false))
            } else if (!surchargeTotal.isZero) {
                breakdowns.add(Breakdown(context.getString(R.string.taxes_and_fees), surchargeTotal.formattedMoney, false, false))
            }

            // Extra guest fees
            val extraGuestFees = it.extraGuestFees.value
            if (extraGuestFees != null && !extraGuestFees.isZero) {
                breakdowns.add(Breakdown(context.getString(R.string.extra_guest_charge), extraGuestFees.formattedMoney, false, false))
            }

            // Show amount to be paid today in resort or ETP cases
            if (it.isResortCase.value || it.isPayLater.value) {
                var dueTodayText: String
                if (it.isDepositV2.value) {
                    dueTodayText = Phrase.from(context, R.string.due_to_brand_today_today_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
                } else {
                    dueTodayText = Phrase.from(context, R.string.due_to_brand_today_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
                }
                breakdowns.add(Breakdown(dueTodayText, it.dueNowAmount.value, false, false))
            }

            if (it.showFeesPaidAtHotel.value) {
                breakdowns.add(Breakdown(context.getString(R.string.fees_paid_at_hotel), it.feesPaidAtHotel.value, false, false))
            }

            // Total
            breakdowns.add(Breakdown(context.getString(R.string.total_price_label), it.tripTotalPrice.value, false, false))
            addRows.onNext(breakdowns)
        }
    }
}
