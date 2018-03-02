package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.hotel.HotelBookingData
import com.expedia.bookings.data.hotels.HotelCheckoutInfo
import com.expedia.bookings.data.hotels.HotelCheckoutV2Params
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.CardDetails
import com.expedia.bookings.data.payment.MiscellaneousParams
import com.expedia.bookings.data.payment.PaymentInfo
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.RewardDetails
import com.expedia.bookings.data.payment.TripDetails
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.BookingSuppressionUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.Ui
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.format.ISODateTimeFormat
import java.math.BigDecimal
import java.util.Date
import java.util.Locale

open class HotelCheckoutViewModel(val context: Context, val hotelServices: HotelServices, val paymentModel: PaymentModel<HotelCreateTripResponse>) {

    // inputs
    val checkoutParams = PublishSubject.create<HotelCheckoutV2Params>()
    val bookedWithCVVSubject = PublishSubject.create<String>()
    val bookedWithoutCVVSubject = PublishSubject.create<Unit>()

    // outputs
    val errorObservable = PublishSubject.create<ApiError>()
    val noResponseObservable = PublishSubject.create<Throwable>()
    val checkoutResponseObservable = BehaviorSubject.create<HotelCheckoutResponse>()
    val priceChangeResponseObservable = BehaviorSubject.create<HotelCreateTripResponse>()
    val checkoutRequestStartTimeObservable = BehaviorSubject.create<Long>()
    val hotelBookingDataObservable = PublishSubject.create<HotelBookingData>()

    init {
        checkoutParams.subscribe { params ->
            checkoutRequestStartTimeObservable.onNext(Date().time)
            hotelServices.checkout(params, getCheckoutResponseObserver())
        }

        hotelBookingDataObservable.subscribe {
            val dtf = ISODateTimeFormat.date()
            val hotelCheckoutInfo = HotelCheckoutInfo(dtf.print(it.checkIn), dtf.print(it.checkOut))

            val primaryTraveler = it.primaryTraveler

            val hotelCreateTripResponse = Db.getTripBucket().hotelV2.mHotelTripResponse
            val traveler = com.expedia.bookings.data.payment.Traveler(primaryTraveler.firstName,
                    primaryTraveler.lastName,
                    primaryTraveler.phoneCountryCode,
                    primaryTraveler.phoneNumber,
                    primaryTraveler.email,
                    it.isEmailOptedIn)
            val hotelRate = hotelCreateTripResponse.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo
            val expectedTotalFare = java.lang.String.format(Locale.ENGLISH, "%.2f", hotelRate.total)
            val expectedFareCurrencyCode = hotelRate.currencyCode
            val tripId = hotelCreateTripResponse.tripId
            val tripDetails = TripDetails(tripId, expectedTotalFare, expectedFareCurrencyCode, true)

            val tealeafTransactionId = hotelCreateTripResponse.tealeafTransactionId
            val miscParams = MiscellaneousParams(BookingSuppressionUtils
                    .shouldSuppressFinalBooking(context, R.string.preference_suppress_hotel_bookings), tealeafTransactionId, ServicesUtil.generateClientId(context))

            val cardsSelectedForPayment = ArrayList<CardDetails>()
            val rewardsSelectedForPayment = ArrayList<RewardDetails>()

            val payingWithCardsSplit = it.paymentSplits.payingWithCards
            val payingWithPointsSplit = it.paymentSplits.payingWithPoints

            val rewardsPointsDetails = hotelCreateTripResponse.getPointDetails()

            // Pay with card if CVV is entered. Pay later can have 0 amount also.
            if (!it.cvv.isNullOrBlank()) {
                val billingInfo = it.billingInfo!!
                val amountOnCard = if (payingWithCardsSplit.amount.amount.equals(BigDecimal.ZERO)) null else payingWithCardsSplit.amount.amount.toString()
                if (billingInfo.storedCard == null || billingInfo.storedCard.isGoogleWallet) {
                    val creditCardNumber = billingInfo.number
                    val expirationDateYear = JodaUtils.format(billingInfo.expirationDate, "yyyy")
                    val expirationDateMonth = JodaUtils.format(billingInfo.expirationDate, "MM")
                    val nameOnCard = billingInfo.nameOnCard
                    val postalCode = if (billingInfo.location.postalCode.isNullOrEmpty()) null else billingInfo.location.postalCode
                    val storeCreditCardInUserProfile = billingInfo.saveCardToExpediaAccount

                    val creditCardDetails = CardDetails(
                            creditCardNumber = creditCardNumber,
                            expirationDateYear = expirationDateYear,
                            expirationDateMonth = expirationDateMonth,
                            nameOnCard = nameOnCard,
                            postalCode = postalCode,
                            storeCreditCardInUserProfile = storeCreditCardInUserProfile,
                            cvv = it.cvv,
                            amountOnCard = amountOnCard)
                    cardsSelectedForPayment.add(creditCardDetails)
                } else {
                    val storedCreditCardId = billingInfo.storedCard.id
                    val nameOnCard = billingInfo.storedCard.nameOnCard

                    val storedCreditCardDetails = CardDetails(storedCreditCardId = storedCreditCardId,
                            nameOnCard = nameOnCard,
                            amountOnCard = amountOnCard,
                            cvv = it.cvv)
                    cardsSelectedForPayment.add(storedCreditCardDetails)
                }
            }

            if (!payingWithPointsSplit.amount.isZero) {
                val userStateManager = Ui.getApplication(context).appComponent().userStateManager()
                val user = userStateManager.userSource.user

                val pointsCard = user?.getStoredPointsCard(PaymentType.POINTS_REWARDS)

                // If the user has already used points before, points card will be returned in the Sign in response.
                if (pointsCard != null) {
                    val rewardsSelectedDetails = RewardDetails(paymentInstrumentId = pointsCard.paymentsInstrumentId,
                            programName = hotelCreateTripResponse.getProgramName()!!,
                            amountToChargeInRealCurrency = payingWithPointsSplit.amount.amount.toFloat(),
                            amountToChargeInVirtualCurrency = payingWithPointsSplit.points,
                            rateId = rewardsPointsDetails!!.rateID,
                            currencyCode = payingWithPointsSplit.amount.currencyCode)
                    rewardsSelectedForPayment.add(rewardsSelectedDetails)
                } else {
                    val rewardsSelectedDetails = RewardDetails(membershipId = user?.rewardsMembershipId,
                            programName = hotelCreateTripResponse.getProgramName()!!,
                            amountToChargeInRealCurrency = payingWithPointsSplit.amount.amount.toFloat(),
                            amountToChargeInVirtualCurrency = payingWithPointsSplit.points,
                            rateId = rewardsPointsDetails!!.rateID,
                            currencyCode = payingWithPointsSplit.amount.currencyCode)
                    rewardsSelectedForPayment.add(rewardsSelectedDetails)
                }
            }

            val paymentInfo = PaymentInfo(cardsSelectedForPayment, rewardsSelectedForPayment)

            val hotelCheckoutParams = HotelCheckoutV2Params.Builder()
                    .checkoutInfo(hotelCheckoutInfo).traveler(traveler).tripDetails(tripDetails)
                    .misc(miscParams).paymentInfo(paymentInfo).build()

            checkoutParams.onNext(hotelCheckoutParams)
        }
    }

    open fun getCheckoutResponseObserver(): Observer<HotelCheckoutResponse> {
        return object : DisposableObserver<HotelCheckoutResponse>() {
            override fun onNext(checkout: HotelCheckoutResponse) {
                if (checkout.hasErrors()) {
                    when (checkout.firstError.errorCode) {
                        ApiError.Code.PRICE_CHANGE -> {
                            val hotelCreateTripResponse = Db.getTripBucket().hotelV2.updateAfterCheckoutPriceChange(checkout)
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
                        ApiError.Code.HOTEL_ROOM_UNAVAILABLE -> {
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

            override fun onComplete() {
                // ignore
            }
        }
    }
}
