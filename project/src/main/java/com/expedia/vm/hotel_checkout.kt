package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.hotels.HotelCheckoutParams
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.services.HotelServices
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
    val rateObserver = BehaviorSubject.create<HotelCreateTripResponse.HotelProductResponse>()

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

    init {
        rateObserver.subscribe {
            val room = it.hotelRoomResponse

            hotelName.onNext(it.localizedHotelName)
            checkinDates.onNext(context.getResources().getString(R.string.calendar_instructions_date_range_TEMPLATE, it.checkInDate, it.checkOutDate))
            address.onNext(it.hotelAddress)
            city.onNext(context.getResources().getString(R.string.single_line_street_address_TEMPLATE, it.hotelCity, it.hotelStateProvince))

            val discountPercent = room.rateInfo.chargeableRateInfo.discountPercent
            discounts.onNext(if (discountPercent > 1) discountPercent else null)

            val currencyCode = room.rateInfo.chargeableRateInfo.currencyCode

            roomDescriptions.onNext(room.roomTypeDescription)
            bedDescriptions.onNext(room.rateDescription)

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
    }
}