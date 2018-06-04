package com.expedia.vm

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.annotation.VisibleForTesting
import android.support.v4.content.ContextCompat
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.extensions.LineOfBusinessExtensions
import com.expedia.bookings.data.extensions.isUniversalCheckout
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils
import com.expedia.bookings.utils.BookingInfoUtils
import com.expedia.bookings.utils.CreditCardUtils
import com.expedia.bookings.utils.TravelerUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.util.Optional
import com.squareup.phrase.Phrase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

open class PaymentViewModel(val context: Context) {
    val resources = context.resources

    // inputs
    val splitsType = BehaviorSubject.createDefault<PaymentSplitsType>(PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD)
    val isRedeemable = BehaviorSubject.createDefault<Boolean>(false)
    val statusUpdate = PublishSubject.create<ContactDetailsCompletenessStatus>()
    val billingInfoAndStatusUpdate = BehaviorSubject.create<Pair<BillingInfo?, ContactDetailsCompletenessStatus>>()
    val emptyBillingInfo = PublishSubject.create<Unit>()
    val storedCardRemoved = PublishSubject.create<Optional<StoredCreditCard>>()
    val showingPaymentForm = PublishSubject.create<Boolean>()
    val newCheckoutIsEnabled = BehaviorSubject.createDefault<Boolean>(false)
    val enableMenuItem = PublishSubject.create<Boolean>()
    val menuVisibility = PublishSubject.create<Boolean>()
    val updateBackgroundColor = PublishSubject.create<Boolean>()
    val shouldShowPayLaterMessaging = BehaviorSubject.createDefault<Boolean>(false)

    val cardTypeSubject = PublishSubject.create<Optional<PaymentType>>()
    val cardBIN = BehaviorSubject.createDefault<String>("")
    val resetCardFees = PublishSubject.create<Unit>()
    val moveFocusToPostalCodeSubject = PublishSubject.create<Unit>()
    val userAuthenticationState = PublishSubject.create<Boolean>()
    val isCreditCardRequired = BehaviorSubject.createDefault<Boolean>(false)
    val isZipValidationRequired = BehaviorSubject.createDefault<Boolean>(false)
    val lineOfBusiness = BehaviorSubject.createDefault<LineOfBusiness>(LineOfBusiness.HOTELS)
    val expandObserver = PublishSubject.create<Boolean>()
    val showDebitCardsNotAcceptedSubject = BehaviorSubject.createDefault<Boolean>(false)
    val selectCorrectCardObservable = PublishSubject.create<Boolean>()
    val clearTemporaryCardObservable = PublishSubject.create<Unit>()
    val travelerFirstName = BehaviorSubject.create<AccessibleEditText>()
    val travelerLastName = BehaviorSubject.create<AccessibleEditText>()
    val populateCardholderNameObservable = BehaviorSubject.createDefault<String>("")
    val createFakeAddressObservable = PublishSubject.create<Unit>()
    val resetCardList = PublishSubject.create<Unit>()

    //ouputs
    val iconStatus = PublishSubject.create<ContactDetailsCompletenessStatus>()
    val paymentType = PublishSubject.create<Drawable>()
    val cardTitle = PublishSubject.create<String>()
    val cardSubtitle = PublishSubject.create<String>()
    val subtitleColorObservable = BehaviorSubject.create<Int>()

    val pwpSmallIcon = PublishSubject.create<Boolean>()
    val tempCard = PublishSubject.create<Pair<String, Drawable>>()
    val paymentTypeWarningHandledByCkoView = BehaviorSubject.createDefault<Boolean>(false)
    val invalidPaymentTypeWarning = PublishSubject.create<String>()
    val showCardFeeInfoLabel = PublishSubject.create<Boolean>()
    val showInvalidPaymentWarning = PublishSubject.create<Boolean>()
    val userHasAtleastOneStoredCard = PublishSubject.create<Boolean>()
    val onStoredCardChosen = PublishSubject.create<Unit>()
    val onTemporarySavedCreditCardChosen = PublishSubject.create<Unit>()
    val ccFeeDisclaimer = PublishSubject.create<String>()
    val updateBillingCountryFields = PublishSubject.create<String>()
    val removeBillingAddressForApac = PublishSubject.create<Boolean>()
    val populateFakeBillingAddress = PublishSubject.create<Location>()
    val clearHiddenBillingAddress = PublishSubject.create<Unit>()
    val showValidCards = PublishSubject.create<List<ValidFormOfPayment>>()
    val doneClickedMethod = PublishSubject.create<() -> Unit>()
    val toolbarNavIconFocusObservable = PublishSubject.create<Unit>()

    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    private class PaymentTileInfo(val paymentType: PaymentType?, val title: String, var subTitle: String,
                                  val splitType: PaymentSplitsType, val completionStatus: ContactDetailsCompletenessStatus)

    init {
        statusUpdate.subscribe { it ->
            billingInfoAndStatusUpdate.onNext(Pair(billingInfoAndStatusUpdate.value?.first, it))
        }

        ObservableOld.combineLatest(billingInfoAndStatusUpdate, isRedeemable, splitsType, shouldShowPayLaterMessaging) {
            infoAndStatus, isRedeemable, splitsType, shouldShowPayLaterMessaging ->
            object {
                val info = infoAndStatus.first
                val status = infoAndStatus.second
                val isRedeemable = isRedeemable
                val splitsType = splitsType
                val shouldShowPayLaterMessaging = shouldShowPayLaterMessaging
            }
        }.subscribe {
            val paymentTile: PaymentTileInfo
            subtitleColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_default_card_text_color))

            if (it.isRedeemable && it.splitsType == PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT) {
                paymentTile = PaymentTileInfo(PaymentType.POINTS_REWARDS,
                        resources.getString(R.string.checkout_paying_with_points_only_line1),
                        resources.getString(R.string.checkout_tap_to_edit), it.splitsType, ContactDetailsCompletenessStatus.COMPLETE)
            } else if (it.info == null) {
                val titleSubtitlePair = getTitleAndSubtitleNoInfo(it.isRedeemable)
                tempCard.onNext(Pair("", getCardIcon(null)!!))
                paymentTile = PaymentTileInfo(null, titleSubtitlePair.first, titleSubtitlePair.second, it.splitsType, it.status)
            } else if (it.info.isTempCard && it.info.saveCardToExpediaAccount) {
                val title = getCardTypeAndLast4Digits(it.info.getPaymentType(context), it.info.number)
                tempCard.onNext(Pair("", getCardIcon(it.info.getPaymentType(context))!!))
                paymentTile = PaymentTileInfo(it.info.getPaymentType(context), title, resources.getString(R.string.checkout_tap_to_edit), it.splitsType, it.status)
                Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(it.info)
            } else if (it.info.hasStoredCard()) {
                val card = it.info.storedCard
                val title = card.description
                tempCard.onNext(Pair("", getCardIcon(card.type)!!))
                paymentTile = PaymentTileInfo(card.type, title, resources.getString(R.string.checkout_tap_to_edit), it.splitsType, it.status)
            } else {
                val card = it.info
                val cardNumber = card.number
                val title = getCardTypeAndLast4Digits(card.getPaymentType(context), cardNumber)
                if (card.isTempCard && !card.saveCardToExpediaAccount) {
                    tempCard.onNext(Pair(title, getCardIcon(card.getPaymentType(context))!!))
                }
                paymentTile = PaymentTileInfo(card.getPaymentType(context), title, resources.getString(R.string.checkout_tap_to_edit), it.splitsType, it.status)
                Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(it.info)
            }
            if (it.shouldShowPayLaterMessaging) {
                paymentTile.subTitle = resources.getString(R.string.checkout_pay_later_only_to_confirm)
            }
            setPaymentTileInfo(paymentTile.paymentType, paymentTile.title, paymentTile.subTitle, paymentTile.splitType, paymentTile.completionStatus)
            Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB()
        }

        ObservableOld.combineLatest(travelerFirstName, travelerLastName, { firstName, lastName ->
            if (firstName.valid && lastName.valid) {
                if (firstName.text.isEmpty() && lastName.text.isEmpty()) {
                    populateCardholderNameObservable.onNext("")
                } else if (firstName.text.isNotEmpty() && lastName.text.isNotEmpty()) {
                    populateCardholderNameObservable.onNext(TravelerUtils.getFullName(firstName, lastName))
                }
            }
        }).subscribe()

        storedCardRemoved.subscribe { card ->
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_hotel_credit_card)?.mutate()
            icon?.setColorFilter(ContextCompat.getColor(context, R.color.hotels_primary_color), PorterDuff.Mode.SRC_IN)
            billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.DEFAULT))
            emptyBillingInfo.onNext(Unit)
            BookingInfoUtils.resetPreviousCreditCardSelectState(userStateManager, card.value)
            Db.getWorkingBillingInfoManager().workingBillingInfo.storedCard = null
            Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB()
            resetCardFees.onNext(Unit)
        }

        userAuthenticationState.subscribe { isUserAuthenticated ->
            if (!isUserAuthenticated) {
                storedCardRemoved.onNext(Optional(null))
            }
        }

        cardTypeSubject
                .debounce(1, TimeUnit.SECONDS, getScheduler())
                .subscribe { cardTypeOptional ->
                    val cardType = cardTypeOptional.value
                    val tripItem = Db.getTripBucket().getItem(lineOfBusiness.value)
                    val showingPaymentFeeWarning = tripItem?.hasPaymentFee(cardType) ?: false
                    var invalidPaymentWarningMsg = ""
                    if (tripItem != null && cardType != null && !tripItem.isPaymentTypeSupported(cardType, context)) {
                        invalidPaymentWarningMsg = ValidFormOfPaymentUtils.getInvalidFormOfPaymentMessage(context, cardType, lineOfBusiness.value)
                    }
                    val showLabel = !paymentTypeWarningHandledByCkoView.value || !showingPaymentFeeWarning && invalidPaymentWarningMsg.isBlank()
                    invalidPaymentTypeWarning.onNext(invalidPaymentWarningMsg)
                    showCardFeeInfoLabel.onNext(showLabel)
                }

        invalidPaymentTypeWarning.map { !paymentTypeWarningHandledByCkoView.value && it.isNotBlank() }
                .subscribe(showInvalidPaymentWarning)

        lineOfBusiness.subscribe { lob ->
            isCreditCardRequired.onNext(lobRequiresCreditCard(lob))
            val isPostalCodeRequired = when (lob) {
                LineOfBusiness.HOTELS -> PointOfSale.getPointOfSale().requiresHotelPostalCode()
                LineOfBusiness.TRANSPORT -> PointOfSale.getPointOfSale().requiresLXPostalCode()
                LineOfBusiness.LX -> PointOfSale.getPointOfSale().requiresLXPostalCode()
                else -> true
            }
            isZipValidationRequired.onNext(isPostalCodeRequired)
            ccFeeDisclaimer.onNext(getCCFeeDisclaimerText(lob))
            newCheckoutIsEnabled.onNext(lob.isUniversalCheckout(context))
        }

        resetCardFees.subscribe { cardBIN.onNext("") }

        onStoredCardChosen.map { !newCheckoutIsEnabled.value }.subscribe { isEnabled ->
            enableMenuItem.onNext(isEnabled)
            menuVisibility.onNext(isEnabled)
        }

        clearTemporaryCardObservable.subscribe {
            Db.sharedInstance.clearTemporaryCard()
        }
        createFakeAddressObservable.subscribe {
            val location = Location()
            location.city = resources.getString(R.string.dummy_city)
            location.countryCode = resources.getString(R.string.dummy_country)
            location.addStreetAddressLine(resources.getString(R.string.dummy_address_line_one))
            location.addStreetAddressLine(resources.getString(R.string.dummy_address_line_two))
            location.postalCode = resources.getString(R.string.dummy_postal_code)
            location.stateCode = resources.getString(R.string.dummy_state)
            populateFakeBillingAddress.onNext(location)
        }
    }

    fun getTitleAndSubtitleNoInfo(isRedeemable: Boolean): Pair<String, String> {
        val doesNotRequireSubtitle = LineOfBusinessExtensions.isUniversalCheckout(lineOfBusiness.value, context)
                || lineOfBusiness.value == LineOfBusiness.HOTELS
        val title: String
        val subTitle: String
        if (doesNotRequireSubtitle) {
            title = resources.getString(R.string.enter_payment_information)
            if (billingInfoAndStatusUpdate.value.second == ContactDetailsCompletenessStatus.DEFAULT) {
                subTitle = ""
            } else {
                subTitle = resources.getString(R.string.enter_missing_payment_details)
                subtitleColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_incomplete_text_color))
            }
        } else {
            title = resources.getString(R.string.checkout_enter_payment_details)
            subTitle = resources.getString(
                    if (isRedeemable) R.string.checkout_payment_options else R.string.checkout_hotelsv2_enter_payment_details_line2)
        }
        return Pair(title, subTitle)
    }

    @VisibleForTesting
    protected open fun getScheduler() = AndroidSchedulers.mainThread()!!

    private fun getCCFeeDisclaimerText(lob: LineOfBusiness?): String {
        return if (lob == LineOfBusiness.RAILS) context.getString(R.string.rail_card_fee_disclaimer) else ""
    }

    private fun lobRequiresCreditCard(lob: LineOfBusiness): Boolean {
        return lob == LineOfBusiness.PACKAGES || lob == LineOfBusiness.HOTELS
                || lob == LineOfBusiness.FLIGHTS || lob == LineOfBusiness.FLIGHTS_V2
                || lob == LineOfBusiness.RAILS || lob == LineOfBusiness.LX
    }

    private fun setPaymentTileInfo(type: PaymentType?, title: String, subTitle: String, splitsType: PaymentSplitsType, completeStatus: ContactDetailsCompletenessStatus) {
        var paymentTitle = title
        if (type != null && isRedeemable.value && splitsType == PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD) {
            paymentTitle = Phrase.from(context, R.string.checkout_paying_with_points_and_card_line1_TEMPLATE)
                    .put("carddescription", title)
                    .format().toString()
        }
        iconStatus.onNext(completeStatus)
        paymentType.onNext(getCardIcon(type)!!)
        cardTitle.onNext(paymentTitle)
        cardSubtitle.onNext(subTitle)
        pwpSmallIcon.onNext(getPwPSmallIconVisibility(type, splitsType))
    }

    private fun getPwPSmallIconVisibility(paymentType: PaymentType?, splitsType: PaymentSplitsType): Boolean {
        return paymentType != null && splitsType == PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD
    }

    private fun getCardIcon(type: PaymentType?): Drawable? {
        if (type == null) {
            return ContextCompat.getDrawable(context, R.drawable.ic_checkout_default_creditcard)
        } else {
            return ContextCompat.getDrawable(context, BookingInfoUtils.getColorfulCardIcon(type))
        }
    }

    private fun getCardTypeAndLast4Digits(paymentType: PaymentType?, cardNumber: String): String {
        return Phrase.from(context, R.string.checkout_selected_card)
                .put("cardtype", CreditCardUtils.getHumanReadableCardTypeName(context, paymentType))
                .put("cardno", cardNumber.drop(cardNumber.length - 4))
                .format().toString()
    }

    fun hasCard(): Boolean {
        if (isCreditCardRequired.value) {
            return hasStoredCard() || Db.sharedInstance.temporarilySavedCard != null || !cardBIN.value.isNullOrEmpty()
        } else {
            return true
        }
    }

    fun hasStoredCard(): Boolean {
        billingInfoAndStatusUpdate.value?.first?.let { info ->
            return info.hasStoredCard()
        }
        return false
    }
}
