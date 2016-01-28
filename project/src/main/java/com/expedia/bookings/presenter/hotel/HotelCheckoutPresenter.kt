package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.hotels.HotelCheckoutInfo
import com.expedia.bookings.data.hotels.HotelCheckoutV2Params
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.payment.CardDetails
import com.expedia.bookings.data.payment.MiscellaneousParams
import com.expedia.bookings.data.payment.PaymentInfo
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.data.payment.RewardDetails
import com.expedia.bookings.data.payment.Traveler
import com.expedia.bookings.data.payment.TripDetails
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.BookingSuppressionUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelCheckoutViewModel
import org.joda.time.format.ISODateTimeFormat
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.Locale
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelCheckoutPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {

    var hotelCheckoutViewModel: HotelCheckoutViewModel by Delegates.notNull()

    val hotelCheckoutWidget: HotelCheckoutMainViewPresenter by bindView(R.id.checkout)
    val cvv: CVVEntryWidget by bindView(R.id.cvv)

    lateinit var paymentModel: PaymentModel<HotelCreateTripResponse>
        @Inject set

    private val bookedWithCVVSubject = PublishSubject.create<String>()
    private val bookedWithoutCVVSubject = PublishSubject.create<Unit>()

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        View.inflate(getContext(), R.layout.widget_hotel_checkout, this)
        bookedWithCVVSubject.withLatestFrom(paymentModel.paymentSplits,{cvv, paymentSplits->
            object{
                val cvv = cvv
                val paymentSplits = paymentSplits
            }
        }).subscribe{
            onBookV2(it.cvv, it.paymentSplits)
        }

        bookedWithoutCVVSubject.withLatestFrom(paymentModel.paymentSplits, { unit, paymentSplits -> paymentSplits }).subscribe {
            onBookV2(null, it)
        }
    }

    override fun onFinishInflate() {
        addTransition(checkoutToCvv)
        addDefaultTransition(defaultCheckoutTransition)
        hotelCheckoutWidget.slideAllTheWayObservable.withLatestFrom(paymentModel.paymentSplits) { unit, paymentSplits ->
            paymentSplits.paymentSplitsType()
        }.subscribe(checkoutSliderSlidObserver)
        hotelCheckoutWidget.emailOptInStatus.subscribe { status ->
            hotelCheckoutWidget.mainContactInfoCardView.setUPEMailOptCheckBox(status)
        }
        cvv.setCVVEntryListener(this)
    }

    fun showCheckout(offer: HotelOffersResponse.HotelRoomResponse) {
        show(hotelCheckoutWidget)
        hotelCheckoutWidget.showCheckout(offer)
    }

    private val defaultCheckoutTransition = object : Presenter.DefaultTransition(HotelCheckoutMainViewPresenter::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            hotelCheckoutWidget.setVisibility(View.VISIBLE)
            cvv.setVisibility(View.GONE)
        }
    }

    private val checkoutToCvv = object : VisibilityTransition(this, HotelCheckoutMainViewPresenter::class.java, CVVEntryWidget::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (!forward) {
                hotelCheckoutWidget.slideWidget.resetSlider()
                hotelCheckoutWidget.checkoutFormWasUpdated()
            }
        }
    }

    val checkoutSliderSlidObserver = endlessObserver<PaymentSplitsType> {
        val billingInfo = hotelCheckoutWidget.paymentInfoCardView.sectionBillingInfo.getBillingInfo()
        if (it.equals(PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT )) {
            bookedWithoutCVVSubject.onNext(Unit)
        } else if (billingInfo.getStoredCard() != null && billingInfo.getStoredCard().isGoogleWallet()) {
            onBook(billingInfo.getSecurityCode())
        } else {
            cvv.bind(billingInfo)
            show(cvv)
            HotelV2Tracking().trackHotelV2CheckoutPaymentCid()
        }
    }

    override fun onBook(cvv: String) {
        bookedWithCVVSubject.onNext(cvv)
    }

    fun onBookV2(cvv: String?, paymentSplits: PaymentSplits) {
        val dtf = ISODateTimeFormat.date()

        val hotelSearchParams = Db.getHotelSearch().searchParams
        val hotelCheckoutInfo = HotelCheckoutInfo(dtf.print(hotelSearchParams.checkInDate), dtf.print(hotelSearchParams.checkOutDate))

        val primaryTraveler = hotelCheckoutWidget.mainContactInfoCardView.sectionTravelerInfo.traveler
        val traveler = Traveler(primaryTraveler.firstName, primaryTraveler.lastName, primaryTraveler.phoneCountryCode, primaryTraveler.phoneNumber, primaryTraveler.email)

        val hotelCreateTripResponse = Db.getTripBucket().hotelV2.mHotelTripResponse
        val hotelRate = hotelCreateTripResponse.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo
        val expectedTotalFare = java.lang.String.format(Locale.ENGLISH, "%.2f", hotelRate.total)
        val expectedFareCurrencyCode = hotelRate.currencyCode
        val tripId = hotelCreateTripResponse.tripId
        val abacusUserGuid = Db.getAbacusGuid()
        val tripDetails = TripDetails(tripId, expectedTotalFare, expectedFareCurrencyCode, abacusUserGuid, true)

        val tealeafTransactionId = hotelCreateTripResponse.tealeafTransactionId
        val miscParams = MiscellaneousParams(BookingSuppressionUtils.shouldSuppressFinalBooking(context, R.string.preference_suppress_hotel_bookings), tealeafTransactionId)

        val cardsSelectedForPayment = ArrayList<CardDetails>()
        val rewardsSelectedForPayment = ArrayList<RewardDetails>()

        val payingWithCardsSplit = paymentSplits.payingWithCards
        val payingWithPointsSplit = paymentSplits.payingWithPoints

        val expediaRewardsPointsDetails = hotelCreateTripResponse.getPointDetails(ProgramName.ExpediaRewards)

        val billingInfo = hotelCheckoutWidget.paymentInfoCardView.sectionBillingInfo.billingInfo
        if (!cvv.isNullOrBlank() && !payingWithCardsSplit.amount.isZero) {
            if (billingInfo.storedCard == null || billingInfo.storedCard.isGoogleWallet) {
                val creditCardNumber = billingInfo.number
                val expirationDateYear = JodaUtils.format(billingInfo.expirationDate, "yyyy")
                val expirationDateMonth = JodaUtils.format(billingInfo.expirationDate, "MM")
                val nameOnCard = billingInfo.nameOnCard
                val postalCode = billingInfo.location.postalCode
                val storeCreditCardInUserProfile = billingInfo.saveCardToExpediaAccount

                val creditCardDetails = CardDetails(
                        creditCardNumber = creditCardNumber, expirationDateYear = expirationDateYear,
                        expirationDateMonth = expirationDateMonth, nameOnCard = nameOnCard,
                        postalCode = postalCode, storeCreditCardInUserProfile = storeCreditCardInUserProfile,
                        cvv = cvv, amountOnCard = payingWithCardsSplit.amount.amount.toString())
                cardsSelectedForPayment.add(creditCardDetails)
            } else {
                val storedCreditCardId = billingInfo.storedCard.id
                val nameOnCard = billingInfo.storedCard.nameOnCard

                val storedCreditCardDetails = CardDetails(storedCreditCardId = storedCreditCardId, nameOnCard = nameOnCard, amountOnCard = payingWithCardsSplit.amount.amount.toString(), cvv = cvv)
                cardsSelectedForPayment.add(storedCreditCardDetails)
            }
        }

        if (!payingWithPointsSplit.amount.isZero) {
            val expediaPointsCard = Db.getUser().getStoredPointsCard(PaymentType.POINTS_EXPEDIA_REWARDS)

            // If the user has already used points before, points card will be returned in the Sign in response.
            if (expediaPointsCard != null) {
                val rewardsSelectedDetails = RewardDetails(paymentInstrumentId = expediaPointsCard.paymentsInstrumentId, programName = ProgramName.ExpediaRewards,
                        amountToChargeInRealCurrency = payingWithPointsSplit.amount.amount.toFloat(), amountToChargeInVirtualCurrency = payingWithPointsSplit.points, rateId = expediaRewardsPointsDetails!!.rateID, currencyCode = payingWithPointsSplit.amount.currencyCode)
                rewardsSelectedForPayment.add(rewardsSelectedDetails)
            }
            else {
                val rewardsSelectedDetails = RewardDetails(membershipId = Db.getUser().expediaRewardsMembershipId, programName = ProgramName.ExpediaRewards,
                        amountToChargeInRealCurrency = payingWithPointsSplit.amount.amount.toFloat(), amountToChargeInVirtualCurrency = payingWithPointsSplit.points, rateId = expediaRewardsPointsDetails!!.rateID, currencyCode = payingWithPointsSplit.amount.currencyCode)
                rewardsSelectedForPayment.add(rewardsSelectedDetails)
            }
        }

        val paymentInfo = PaymentInfo(cardsSelectedForPayment, rewardsSelectedForPayment)

        val hotelCheckoutParams = HotelCheckoutV2Params.Builder()
                                    .checkoutInfo(hotelCheckoutInfo).traveler(traveler).tripDetails(tripDetails)
                                    .misc(miscParams).paymentInfo(paymentInfo).build()

        hotelCheckoutViewModel.checkoutParams.onNext(hotelCheckoutParams)
    }
}
