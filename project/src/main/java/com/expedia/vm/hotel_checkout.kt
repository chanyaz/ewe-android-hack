package com.expedia.vm

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Rate
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelCheckoutParams
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Strings
import com.mobiata.android.util.AndroidUtils
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal

public class HotelCheckoutViewModel(val hotelServices: HotelServices) {

    val checkoutParams = PublishSubject.create<HotelCheckoutParams>()

    val checkoutResponseObservable = BehaviorSubject.create<HotelCheckoutResponse>()
    val priceChangeResponseObservable = BehaviorSubject.create<HotelCreateTripResponse>()

    init {
        checkoutParams.subscribe { params ->
            hotelServices.checkout(params, object : Observer<HotelCheckoutResponse> {
                override fun onNext(checkout: HotelCheckoutResponse) {
                    if (checkout.hasErrors() && checkout.getFirstError().errorCode == ApiError.Code.PRICE_CHANGE) {
                        val hotelCreateTripResponse = Db.getTripBucket().getHotelV2().updateHotelProducts(checkout.checkoutResponse.jsonPriceChangeResponse)
                        priceChangeResponseObservable.onNext(hotelCreateTripResponse)
                    } else {
                        checkoutResponseObservable.onNext(checkout)
                    }
                }

                override fun onError(e: Throwable) {
                    throw OnErrorNotImplementedException(e)
                }

                override fun onCompleted() {
                    // ignore
                }
            })
        }
    }
}

public class HotelCreateTripViewModel(val hotelServices: HotelServices) {

    val tripParams = PublishSubject.create<HotelCreateTripParams>()

    val tripResponseObservable = BehaviorSubject.create<HotelCreateTripResponse>()

    init {
        tripParams.subscribe { params ->
            hotelServices.createTrip(params, object : Observer<HotelCreateTripResponse> {
                override fun onNext(t: HotelCreateTripResponse) {
                    // TODO: Move away from using DB. observers should react on fresh createTrip response
                    Db.getTripBucket().add(TripBucketItemHotelV2(t))
                    // TODO: populate hotelCreateTripResponseData with response data
                    tripResponseObservable.onNext(t)
                }

                override fun onError(e: Throwable) {
                    throw OnErrorNotImplementedException(e)
                }

                override fun onCompleted() {
                    // ignore
                }
            })
        }
    }
}

class HotelCheckoutSummaryViewModel(val context: Context) {
    // input
    val newRateObserver = BehaviorSubject.create<HotelCreateTripResponse.HotelProductResponse>()
    val originalRateObserver = BehaviorSubject.create<HotelCreateTripResponse.HotelProductResponse>()
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
    val discounts = BehaviorSubject.create<Float?>()
    val priceChange = BehaviorSubject.create<String>()
    val isPriceChange = BehaviorSubject.create<Boolean>(false)
    val isResortCase = BehaviorSubject.create<Boolean>(false)
    val isPayLater = BehaviorSubject.create<Boolean>(false)
    val isPayLaterOrResortCase = BehaviorSubject.create<Boolean>(false)
    val feesPaidAtHotel = BehaviorSubject.create<String>()
    val showFeesPaidAtHotel = BehaviorSubject.create<Boolean>(false)

    init {
        newRateObserver.subscribe {
            val room = it.hotelRoomResponse
            val rate = room.rateInfo.chargeableRateInfo

            isPayLater.onNext(room.isPayLater && !AndroidUtils.isTablet(context))
            isResortCase.onNext(Strings.equals(rate.checkoutPriceType, "totalPriceWithMandatoryFees"))
            isPayLaterOrResortCase.onNext(isPayLater.value || isResortCase.value)
            priceAdjustments.onNext(rate.getPriceAdjustments())
            hotelName.onNext(it.localizedHotelName)
            checkInDate.onNext(it.checkInDate)
            checkInOutDatesFormatted.onNext(context.resources.getString(R.string.calendar_instructions_date_range_TEMPLATE, it.checkInDate, it.checkOutDate))
            address.onNext(it.hotelAddress)
            city.onNext(context.resources.getString(R.string.single_line_street_address_TEMPLATE, it.hotelCity, it.hotelStateProvince))
            val discountPercent = room.rateInfo.chargeableRateInfo.discountPercent
            discounts.onNext(if (discountPercent > 1) discountPercent else null)
            roomDescriptions.onNext(room.roomTypeDescription)
            numNights.onNext(context.resources.getQuantityString(R.plurals.number_of_nights, it.numberOfNights.toInt(), it.numberOfNights.toInt()))
            bedDescriptions.onNext(room.formattedBedNames)
            numGuests.onNext(context.resources.getQuantityString(R.plurals.number_of_guests, it.adultCount.toInt(), it.adultCount.toInt()))
            hasFreeCancellation.onNext(room.hasFreeCancellation)
            currencyCode.onNext(rate.currencyCode)
            nightlyRatesPerRoom.onNext(rate.nightlyRatesPerRoom)
            nightlyRateTotal.onNext(rate.nightlyRateTotal.toString())
            surchargeTotalForEntireStay.onNext(Money(BigDecimal(rate.surchargeTotalForEntireStay.toString()), rate.currencyCode))
            taxStatusType.onNext(rate.taxStatusType)
            extraGuestFees.onNext(rate.extraGuestFees)

            // calculate trip total price
            if (isResortCase.value) {
                tripTotalPrice.onNext(Money(BigDecimal(room.rateInfo.chargeableRateInfo.totalPriceWithMandatoryFees.toDouble()), currencyCode.value).formattedMoney)
            } else {
                tripTotalPrice.onNext(rate.displayTotalPrice.formattedMoney)
            }

            // Calculate Due to {Brand} price
            if (isPayLater.value || isResortCase.value) {
                if (isPayLater.value) {
                    val depositAmount = rate.depositAmount ?: "0" // yup. For some reason API doesn't return $0 for deposit amounts
                    dueNowAmount.onNext(Money(BigDecimal(depositAmount), rate.currencyCode).formattedMoney)
                }
                else {
                    dueNowAmount.onNext(rate.displayTotalPrice.formattedMoney)
                }
            }
            else {
                dueNowAmount.onNext(tripTotalPrice.value)
            }
            showFeesPaidAtHotel.onNext(isResortCase.value && (rate.totalMandatoryFees != 0f))
            feesPaidAtHotel.onNext(Money(BigDecimal(rate.totalMandatoryFees.toString()), currencyCode.value).formattedMoney)
            isBestPriceGuarantee.onNext(room.isMerchant)
            newDataObservable.onNext(this)
        }

        originalRateObserver.subscribe {
            val room = it.hotelRoomResponse
            val hasPriceChange = room != null
            if (hasPriceChange) {
                val currencyCode = room.rateInfo.chargeableRateInfo.currencyCode
                val totalPrice = Money(BigDecimal(room.rateInfo.chargeableRateInfo.total.toDouble()), currencyCode)
                priceChange.onNext(context.getString(R.string.price_changed_from_TEMPLATE,
                        totalPrice.formattedMoney))

            }
            isPriceChange.onNext(hasPriceChange)
        }
        guestCountObserver.subscribe {
            numGuests.onNext(context.resources.getQuantityString(R.plurals.number_of_guests, it, it))
        }
    }
}

data class Breakdown(val title: String, val cost: String, val isDate: Boolean)

class HotelBreakDownViewModel(val context: Context, val hotelCheckoutSummaryViewModel: HotelCheckoutSummaryViewModel) {
    val addRows = BehaviorSubject.create<List<Breakdown>>()
    val dtf = DateTimeFormat.forPattern("M/dd/yyyy")

    init {
        hotelCheckoutSummaryViewModel.newDataObservable.subscribe {
            var breakdowns = arrayListOf<Breakdown>()
            val nightlyRate = Money(BigDecimal(it.nightlyRateTotal.value), it.currencyCode.value)
            breakdowns.add(Breakdown(it.numNights.value, nightlyRate.formattedMoney, false))

            var count = 0;
            val checkIn = DateUtils.yyyyMMddToLocalDate(it.checkInDate.value)
            for (rate in it.nightlyRatesPerRoom.value) {
                val date = dtf.print(checkIn.plusDays(count))
                val amount = Money(BigDecimal(rate.rate), it.currencyCode.value)
                val amountStr = if ((amount.isZero))
                    context.getString(R.string.free)
                else
                    amount.formattedMoney

                breakdowns.add(Breakdown(date, amountStr, true))
                count++
            }

            // Discount
            val couponRate = it.priceAdjustments.value
            if (couponRate != null && !couponRate.isZero) {
                breakdowns.add(Breakdown(context.getString(R.string.discount), couponRate.formattedMoney, false))
            }

            // Taxes & Fees
            val surchargeTotal = it.surchargeTotalForEntireStay.value
            if (!surchargeTotal.isZero) {
                val taxStatusType = it.taxStatusType.value
                val surcharge = if (taxStatusType != null && taxStatusType.equals("UNKNOWN")) context.getString(R.string.unknown) else if (surchargeTotal.isZero) context.getString(R.string.included) else surchargeTotal.formattedMoney
                breakdowns.add(Breakdown(context.getString(R.string.taxes_and_fees), surcharge, false))
            }

            // Extra guest fees
            val extraGuestFees = it.extraGuestFees.value
            if (extraGuestFees != null && !extraGuestFees.isZero) {
                breakdowns.add(Breakdown(context.getString(R.string.extra_guest_charge), extraGuestFees.formattedMoney, false))
            }

            // Show amount to be paid today in resort or ETP cases
            if (it.isResortCase.value || it.isPayLater.value) {
                val dueTodayText = Phrase.from(context, R.string.due_to_brand_today_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
                breakdowns.add(Breakdown(dueTodayText, it.dueNowAmount.value, false))
            }

            if (it.showFeesPaidAtHotel.value) {
                breakdowns.add(Breakdown(context.getString(R.string.fees_paid_at_hotel), it.feesPaidAtHotel.value, false))
            }

            // Total
            breakdowns.add(Breakdown(context.getString(R.string.total_price_label), it.tripTotalPrice.value, false))
            addRows.onNext(breakdowns)
        }
    }
}