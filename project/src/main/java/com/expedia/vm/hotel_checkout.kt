package com.expedia.vm

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.hotels.HotelCheckoutParams
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Strings
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

    init {
        checkoutParams.subscribe { params ->
            hotelServices.checkout(params, object : Observer<HotelCheckoutResponse> {
                override fun onNext(t: HotelCheckoutResponse) {
                    checkoutResponseObservable.onNext(t)
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
    val hotelName = BehaviorSubject.create<String>()
    val checkinDates = BehaviorSubject.create<String>()
    val address = BehaviorSubject.create<String>()
    val city = BehaviorSubject.create<String>()
    val roomDescriptions = BehaviorSubject.create<String>()
    val bedDescriptions = BehaviorSubject.create<String>()
    val numNights = BehaviorSubject.create<String>()
    val numGuests = BehaviorSubject.create<String>()
    val hasFreeCancellation = BehaviorSubject.create<Boolean>()
    val totalMandatoryPrices = BehaviorSubject.create<String>()
    val totalPrices = BehaviorSubject.create<String>()
    val feePrices = BehaviorSubject.create<String>()
    val isBestPriceGuarantee = BehaviorSubject.create<Boolean>()
    val discounts = BehaviorSubject.create<Float?>()
    val priceChange = BehaviorSubject.create<String>()
    val isPriceChange = BehaviorSubject.create<Boolean>()
    init {
        newRateObserver.subscribe {
            val room = it.hotelRoomResponse

            hotelName.onNext(it.localizedHotelName)
            checkinDates.onNext(context.getResources().getString(R.string.calendar_instructions_date_range_TEMPLATE, it.checkInDate, it.checkOutDate))
            address.onNext(it.hotelAddress)
            city.onNext(context.getResources().getString(R.string.single_line_street_address_TEMPLATE, it.hotelCity, it.hotelStateProvince))

            val discountPercent = room.rateInfo.chargeableRateInfo.discountPercent
            discounts.onNext(if (discountPercent > 1) discountPercent else null)

            val currencyCode = room.rateInfo.chargeableRateInfo.currencyCode

            roomDescriptions.onNext(room.roomTypeDescription)
            bedDescriptions.onNext(room.getFormattedBedNames())

            numNights.onNext(context.getResources().getQuantityString(R.plurals.number_of_nights, it.numberOfNights.toInt(), it.numberOfNights.toInt()))
            numGuests.onNext(context.getResources().getQuantityString(R.plurals.number_of_guests, it.adultCount.toInt(), it.adultCount.toInt()))

            hasFreeCancellation.onNext(room.hasFreeCancellation)

            val mandatoryPrice = Money(BigDecimal(room.rateInfo.chargeableRateInfo.totalPriceWithMandatoryFees.toDouble()), currencyCode)
            totalMandatoryPrices.onNext(mandatoryPrice.getFormattedMoney())

            val totalPrice = Money(BigDecimal(room.rateInfo.chargeableRateInfo.total.toDouble()), currencyCode)
            totalPrices.onNext(totalPrice.getFormattedMoney())

            val fees = Money(BigDecimal(room.rateInfo.chargeableRateInfo.surchargeTotalForEntireStay.toDouble()), currencyCode)
            feePrices.onNext(fees.getFormattedMoney())

            isBestPriceGuarantee.onNext(room.isMerchant())
        }

        originalRateObserver.subscribe {
            val room = it.hotelRoomResponse
            val hasPriceChange = room != null
            if (hasPriceChange) {
                val currencyCode = room.rateInfo.chargeableRateInfo.currencyCode
                val totalPrice = Money(BigDecimal(room.rateInfo.chargeableRateInfo.total.toDouble()), currencyCode)
                priceChange.onNext(context.getString(R.string.price_changed_from_TEMPLATE,
                        totalPrice.getFormattedMoney()))

            }
            isPriceChange.onNext(hasPriceChange)
        }
    }
}

data class Breakdown(val title: String, val cost: String, val isDate: Boolean)

class HotelBreakDownViewModel(val context: Context) {
    val tripObserver = BehaviorSubject.create<HotelCreateTripResponse>()

    val addRows = BehaviorSubject.create<List<Breakdown>>()

    init {
        tripObserver.subscribe { hotel ->
            var breakdowns = arrayListOf<Breakdown>()
            val originalRate = if (hotel.originalHotelProductResponse.hotelRoomResponse == null) hotel.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo else hotel.originalHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo

            val nights = context.getResources().getQuantityString(R.plurals.number_of_nights, hotel.newHotelProductResponse.numberOfNights.toInt(), hotel.newHotelProductResponse.numberOfNights.toInt())
            val nightlyRate = Money(BigDecimal(originalRate.nightlyRateTotal.toString()), originalRate.currencyCode)
            breakdowns.add(Breakdown(nights, nightlyRate.getFormattedMoney(), false))

            var count = 0;
            val checkIn = DateUtils.yyyyMMddToLocalDate(hotel.newHotelProductResponse.checkInDate)
            for (rate in originalRate.nightlyRatesPerRoom) {
                var dtf = DateTimeFormat.forPattern("M/dd/yyyy");
                val date = dtf.print(checkIn.plusDays(count))
                val amount = Money(BigDecimal(rate.rate), originalRate.currencyCode)
                val amountStr = if ((amount.isZero()))
                    context.getString(R.string.free)
                else
                    amount.getFormattedMoney()

                breakdowns.add(Breakdown(date, amountStr, true))
                count++
            }

            // Discount
            val couponRate = if (hotel.originalHotelProductResponse.hotelRoomResponse == null) null else hotel.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo
            if (couponRate != null && !couponRate.getPriceAdjustments().isZero()) {
                breakdowns.add(Breakdown(context.getString(R.string.discount), couponRate.getPriceAdjustments().getFormattedMoney(), false))
            }

            // Taxes & Fees
            val surchargeTotal = Money(BigDecimal(originalRate.surchargeTotalForEntireStay.toString()), originalRate.currencyCode)
            if (!surchargeTotal.isZero()) {
                val surcharge: String
                if (originalRate.taxStatusType != null && originalRate.taxStatusType.equals("UNKNOWN")) {
                    surcharge = context.getString(R.string.unknown)
                } else {
                    surcharge = if ((surchargeTotal.isZero()))
                        context.getString(R.string.included)
                    else
                        surchargeTotal.getFormattedMoney()
                }
                breakdowns.add(Breakdown(context.getString(R.string.taxes_and_fees), surcharge, false))
            }

            // Extra guest fees
            if (originalRate.getExtraGuestFees() != null && !originalRate.getExtraGuestFees().isZero()) {
                breakdowns.add(Breakdown(context.getString(R.string.extra_guest_charge), originalRate.getExtraGuestFees().getFormattedMoney(), false))
            }

            val total: Money
            val rateWeCareAbout = couponRate ?: originalRate
            val resortCase = Strings.equals(rateWeCareAbout.checkoutPriceType, "totalPriceWithMandatoryFees")

            // Show amount to be paid today in resort or ETP cases
            if (resortCase ) {
                val dueToday = Money(BigDecimal(rateWeCareAbout.total.toString()), originalRate.currencyCode)
                val dueTodayText = Phrase.from(context, R.string.due_to_brand_today_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
                breakdowns.add(Breakdown(dueTodayText, dueToday.getFormattedMoney(), false))
            }

            if (resortCase) {
                total = Money(BigDecimal(rateWeCareAbout.totalPriceWithMandatoryFees.toString()), originalRate.currencyCode)
                var rate = Money(BigDecimal(rateWeCareAbout.totalMandatoryFees.toString()), originalRate.currencyCode)
                breakdowns.add(Breakdown(context.getString(R.string.fees_paid_at_hotel), rate.getFormattedMoney(), false))
            } else {
                total = rateWeCareAbout.getDisplayTotalPrice()
            }

            // Total
            breakdowns.add(Breakdown(context.getString(R.string.total_price_label), total.getFormattedMoney(), false))
            addRows.onNext(breakdowns)
        }
    }
}