package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelCheckoutInfo
import com.expedia.bookings.data.hotels.HotelCheckoutV2Params
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.payment.CardDetails
import com.expedia.bookings.data.payment.MiscellaneousParams
import com.expedia.bookings.data.payment.PaymentInfo
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.RewardDetails
import com.expedia.bookings.data.payment.Traveler
import com.expedia.bookings.data.payment.TripDetails
import com.expedia.bookings.enums.MerchandiseSpam
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.FreeCancellationWidget
import com.expedia.bookings.withLatestFrom
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelCheckoutViewModel
import io.reactivex.subjects.PublishSubject
import org.joda.time.format.ISODateTimeFormat
import java.math.BigDecimal
import java.util.ArrayList
import java.util.Locale
import kotlin.properties.Delegates
import com.expedia.bookings.utils.BookingSuppressionUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isHotelMaterialForms
import com.expedia.bookings.widget.HotelTravelerEntryWidget
import com.expedia.vm.traveler.HotelTravelersViewModel

class HotelCheckoutPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {

    val isFreeCancellationTooltipEnabled by lazy {
        AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFreeCancellationTooltip)
    }

    val isMaterialHotelEnabled by lazy {
        isHotelMaterialForms(context)
    }

    private val hotelCheckoutWidgetHeight by lazy {
        hotelCheckoutWidget.height
    }

    val freeCancellationWidget: FreeCancellationWidget by bindView(R.id.free_cancellation_view)
    var hotelCheckoutViewModel: HotelCheckoutViewModel by notNullAndObservable { vm ->
        bookedWithCVVSubject.withLatestFrom(vm.paymentModel.paymentSplits,{cvv, paymentSplits->
            object{
                val cvv = cvv
                val paymentSplits = paymentSplits
            }
        }).subscribe{
            onBookV2(it.cvv, it.paymentSplits)
        }

        bookedWithoutCVVSubject.withLatestFrom(vm.paymentModel.paymentSplits, { unit, paymentSplits -> paymentSplits }).subscribe {
            onBookV2(null, it)
        }

        hotelCheckoutWidget.slideAllTheWayObservable.withLatestFrom(vm.paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse) { unit, paymentSplitsAndLatestTripResponse ->
            paymentSplitsAndLatestTripResponse.isCardRequired()
        }.subscribe(checkoutSliderSlidObserver)

        if (isFreeCancellationTooltipEnabled) {
            vm.paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse.map {
                HtmlCompat.fromHtml(it.tripResponse.newHotelProductResponse.hotelRoomResponse.cancellationPolicy)
            }.subscribe(freeCancellationWidget.viewModel.freeCancellationTextObservable)
        }
    }

    val hotelCheckoutWidget: HotelCheckoutMainViewPresenter by bindView(R.id.checkout)
    val cvv: CVVEntryWidget by bindView(R.id.cvv)

    var hotelSearchParams: HotelSearchParams by Delegates.notNull()

    private val bookedWithCVVSubject = PublishSubject.create<String>()
    private val bookedWithoutCVVSubject = PublishSubject.create<Unit>()

    init {
        View.inflate(getContext(), R.layout.widget_hotel_checkout, this)
    }

    fun setSearchParams(params: HotelSearchParams) {
        hotelSearchParams = params
    }

    override fun onFinishInflate() {
        addTransition(checkoutToCvv)
        addDefaultTransition(defaultCheckoutTransition)
        hotelCheckoutWidget.hotelCheckoutMainViewModel.emailOptInStatus.subscribe { status ->
            if (isMaterialHotelEnabled) {
                (hotelCheckoutWidget.travelersPresenter.viewModel as HotelTravelersViewModel).createTripOptInStatus.onNext(status)
            } else {
                hotelCheckoutWidget.mainContactInfoCardView.setUPEMailOptCheckBox(status)
            }
        }
        cvv.setCVVEntryListener(this)

        if (isFreeCancellationTooltipEnabled) {
            addTransition(checkoutToFreeCancellation)

            freeCancellationWidget.viewModel.closeFreeCancellationObservable.subscribe {
                back()
            }
            hotelCheckoutWidget.hotelCheckoutSummaryWidget.freeCancellationTooltipView.setOnClickListener({
                show(freeCancellationWidget)
            })
        }

    }

    fun showCheckout(offer: HotelOffersResponse.HotelRoomResponse) {
        show(hotelCheckoutWidget)
        hotelCheckoutWidget.showCheckout(offer)
    }

    private val defaultCheckoutTransition = object : Presenter.DefaultTransition(HotelCheckoutMainViewPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            hotelCheckoutWidget.visibility = View.VISIBLE
            cvv.visibility = View.GONE
        }
    }

    private val checkoutToFreeCancellation = object : Transition(HotelCheckoutMainViewPresenter::class.java, FreeCancellationWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            hotelCheckoutWidget.visibility = View.VISIBLE
            freeCancellationWidget.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val pos = if (forward) (hotelCheckoutWidgetHeight - (f * hotelCheckoutWidgetHeight)) else (f * hotelCheckoutWidgetHeight)
            freeCancellationWidget.translationY = pos
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                hotelCheckoutWidget.visibility = View.GONE
            } else {
                freeCancellationWidget.visibility = View.GONE
            }
        }
    }

    private val checkoutToCvv = object : VisibilityTransition(this, HotelCheckoutMainViewPresenter::class.java, CVVEntryWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                hotelCheckoutWidget.slideWidget.resetSlider()
                hotelCheckoutWidget.checkoutFormWasUpdated()
            }
        }
    }

    val checkoutSliderSlidObserver = endlessObserver<Boolean> {
        val billingInfo = if (Db.sharedInstance.temporarilySavedCard != null && Db.sharedInstance.temporarilySavedCard.saveCardToExpediaAccount)
            Db.sharedInstance.temporarilySavedCard
        else hotelCheckoutWidget.paymentInfoCardView.sectionBillingInfo.billingInfo

        if (!it) {
            bookedWithoutCVVSubject.onNext(Unit)
        } else if (billingInfo.storedCard != null && billingInfo.storedCard.isGoogleWallet) {
            onBook(billingInfo.securityCode)
        } else {
            cvv.bind(billingInfo)
            show(cvv)
            HotelTracking.trackHotelCheckoutPaymentCid()
        }
    }

    override fun onBook(cvv: String) {
        bookedWithCVVSubject.onNext(cvv)
    }

    fun onBookV2(cvv: String?, paymentSplits: PaymentSplits) {
        val dtf = ISODateTimeFormat.date()

        val hotelCheckoutInfo = HotelCheckoutInfo(dtf.print(hotelSearchParams.checkIn), dtf.print(hotelSearchParams.checkOut))
        val primaryTraveler = if (isMaterialHotelEnabled) {
            (hotelCheckoutWidget.travelersPresenter.viewModel as HotelTravelersViewModel).getTravelers()[0]
        } else {
            hotelCheckoutWidget.mainContactInfoCardView.sectionTravelerInfo.traveler
        }
        val hotelCreateTripResponse = Db.getTripBucket().hotelV2.mHotelTripResponse
        val isEmailOptedIn = if (isMaterialHotelEnabled) {
            getMaterialEmailOptInStatus(hotelCreateTripResponse.guestUserPromoEmailOptInStatus)
        } else {
            hotelCheckoutWidget.mainContactInfoCardView.emailOptIn ?: false
        }
        val traveler = Traveler(primaryTraveler.firstName,
                primaryTraveler.lastName,
                primaryTraveler.phoneCountryCode,
                primaryTraveler.phoneNumber,
                primaryTraveler.email,
                isEmailOptedIn)
        val hotelRate = hotelCreateTripResponse.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo
        val expectedTotalFare = java.lang.String.format(Locale.ENGLISH, "%.2f", hotelRate.total)
        val expectedFareCurrencyCode = hotelRate.currencyCode
        val tripId = hotelCreateTripResponse.tripId
        val tripDetails = TripDetails(tripId, expectedTotalFare, expectedFareCurrencyCode, true)

        val tealeafTransactionId = hotelCreateTripResponse.tealeafTransactionId
        val miscParams = MiscellaneousParams(BookingSuppressionUtils.shouldSuppressFinalBooking(context, R.string.preference_suppress_hotel_bookings), tealeafTransactionId, ServicesUtil.generateClientId(context))

        val cardsSelectedForPayment = ArrayList<CardDetails>()
        val rewardsSelectedForPayment = ArrayList<RewardDetails>()

        val payingWithCardsSplit = paymentSplits.payingWithCards
        val payingWithPointsSplit = paymentSplits.payingWithPoints

        val rewardsPointsDetails = hotelCreateTripResponse.getPointDetails()

        val billingInfo = if (Db.sharedInstance.temporarilySavedCard != null && Db.sharedInstance.temporarilySavedCard.saveCardToExpediaAccount)
                            Db.sharedInstance.temporarilySavedCard
                            else hotelCheckoutWidget.paymentInfoCardView.sectionBillingInfo.billingInfo

        // Pay with card if CVV is entered. Pay later can have 0 amount also.
        if (!cvv.isNullOrBlank()) {
            val amountOnCard = if (payingWithCardsSplit.amount.amount.equals(BigDecimal.ZERO)) null else payingWithCardsSplit.amount.amount.toString()
            if (billingInfo.storedCard == null || billingInfo.storedCard.isGoogleWallet) {
                val creditCardNumber = billingInfo.number
                val expirationDateYear = JodaUtils.format(billingInfo.expirationDate, "yyyy")
                val expirationDateMonth = JodaUtils.format(billingInfo.expirationDate, "MM")
                val nameOnCard = billingInfo.nameOnCard
                val postalCode = if (billingInfo.location.postalCode.isEmpty()) null else billingInfo.location.postalCode
                val storeCreditCardInUserProfile = billingInfo.saveCardToExpediaAccount

                val creditCardDetails = CardDetails(
                        creditCardNumber = creditCardNumber, expirationDateYear = expirationDateYear,
                        expirationDateMonth = expirationDateMonth, nameOnCard = nameOnCard,
                        postalCode = postalCode, storeCreditCardInUserProfile = storeCreditCardInUserProfile,
                        cvv = cvv, amountOnCard = amountOnCard)
                cardsSelectedForPayment.add(creditCardDetails)
            } else {
                val storedCreditCardId = billingInfo.storedCard.id
                val nameOnCard = billingInfo.storedCard.nameOnCard

                val storedCreditCardDetails = CardDetails(storedCreditCardId = storedCreditCardId, nameOnCard = nameOnCard, amountOnCard = amountOnCard, cvv = cvv)
                cardsSelectedForPayment.add(storedCreditCardDetails)
            }
        }

        if (!payingWithPointsSplit.amount.isZero) {
            val userStateManager = Ui.getApplication(context).appComponent().userStateManager()
            val user = userStateManager.userSource.user

            val pointsCard = user?.getStoredPointsCard(PaymentType.POINTS_REWARDS)

            // If the user has already used points before, points card will be returned in the Sign in response.
            if (pointsCard != null) {
                val rewardsSelectedDetails = RewardDetails(paymentInstrumentId = pointsCard.paymentsInstrumentId, programName = hotelCreateTripResponse.getProgramName()!!,
                        amountToChargeInRealCurrency = payingWithPointsSplit.amount.amount.toFloat(), amountToChargeInVirtualCurrency = payingWithPointsSplit.points, rateId = rewardsPointsDetails!!.rateID, currencyCode = payingWithPointsSplit.amount.currencyCode)
                rewardsSelectedForPayment.add(rewardsSelectedDetails)
            }
            else {
                val rewardsSelectedDetails = RewardDetails(membershipId = user?.rewardsMembershipId, programName = hotelCreateTripResponse.getProgramName()!!,
                        amountToChargeInRealCurrency = payingWithPointsSplit.amount.amount.toFloat(), amountToChargeInVirtualCurrency = payingWithPointsSplit.points, rateId = rewardsPointsDetails!!.rateID, currencyCode = payingWithPointsSplit.amount.currencyCode)
                rewardsSelectedForPayment.add(rewardsSelectedDetails)
            }
        }

        val paymentInfo = PaymentInfo(cardsSelectedForPayment, rewardsSelectedForPayment)

        val hotelCheckoutParams = HotelCheckoutV2Params.Builder()
                                    .checkoutInfo(hotelCheckoutInfo).traveler(traveler).tripDetails(tripDetails)
                                    .misc(miscParams).paymentInfo(paymentInfo).build()

        hotelCheckoutViewModel.checkoutParams.onNext(hotelCheckoutParams)
    }

    private fun getMaterialEmailOptInStatus(createTripOptInStatus: String?): Boolean {
        createTripOptInStatus?.let { status ->
            val isOptInChecked = (hotelCheckoutWidget.travelersPresenter.travelerEntryWidget as HotelTravelerEntryWidget)
                    .merchandiseOptCheckBox.isChecked
            if (MerchandiseSpam.valueOf(status) === MerchandiseSpam.CONSENT_TO_OPT_OUT) {
                return !isOptInChecked
            } else {
                return isOptInChecked
            }
        }
        return false
    }
}
