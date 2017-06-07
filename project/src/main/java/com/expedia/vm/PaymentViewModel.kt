package com.expedia.vm

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.annotation.VisibleForTesting
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.extensions.LineOfBusinessExtensions
import com.expedia.bookings.data.extensions.isUniversalCheckout
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils
import com.expedia.bookings.utils.BookingInfoUtils
import com.expedia.bookings.utils.CreditCardUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.squareup.phrase.Phrase
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

open class PaymentViewModel(val context: Context) {
    val resources = context.resources

    // inputs
    val splitsType = BehaviorSubject.create<PaymentSplitsType>(PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD)
    val isRedeemable = BehaviorSubject.create<Boolean>(false)
    val billingInfoAndStatusUpdate = BehaviorSubject.create<Pair<BillingInfo?, ContactDetailsCompletenessStatus>>()
    val emptyBillingInfo = PublishSubject.create<Unit>()
    val storedCardRemoved = PublishSubject.create<StoredCreditCard?>()
    val showingPaymentForm = PublishSubject.create<Boolean>()
    val newCheckoutIsEnabled = BehaviorSubject.create<Boolean>(false)
    val enableMenuItem = PublishSubject.create<Boolean>()
    val menuVisibility = PublishSubject.create<Boolean>()
    val updateBackgroundColor = PublishSubject.create<Boolean>()

    val cardTypeSubject = PublishSubject.create<PaymentType?>()
    val cardBIN = BehaviorSubject.create<String>("")
    val resetCardFees = PublishSubject.create<Unit>()
    val moveFocusToPostalCodeSubject = PublishSubject.create<Unit>()
    val userLogin = PublishSubject.create<Boolean>()
    val isCreditCardRequired = BehaviorSubject.create<Boolean>(false)
    val isZipValidationRequired = BehaviorSubject.create<Boolean>(false)
    val lineOfBusiness = BehaviorSubject.create<LineOfBusiness>(LineOfBusiness.HOTELS)
    val expandObserver = PublishSubject.create<Boolean>()
    val showDebitCardsNotAcceptedSubject = BehaviorSubject.create<Boolean>(false)
    val selectCorrectCardObservable = PublishSubject.create<Boolean>()
    val clearTemporaryCardObservable = PublishSubject.create<Unit>()

    //ouputs
    val iconStatus = PublishSubject.create<ContactDetailsCompletenessStatus>()
    val paymentType = PublishSubject.create<Drawable>()
    val cardTitle = PublishSubject.create<String>()
    val cardSubtitle = PublishSubject.create<String>()
    val subtitleColorObservable = BehaviorSubject.create<Int>()

    val pwpSmallIcon = PublishSubject.create<Boolean>()
    val tempCard = PublishSubject.create<Pair<String, Drawable>>()
    val paymentTypeWarningHandledByCkoView = BehaviorSubject.create<Boolean>(false)
    val invalidPaymentTypeWarning = PublishSubject.create<String>()
    val showCardFeeInfoLabel = PublishSubject.create<Boolean>()
    val showInvalidPaymentWarning = PublishSubject.create<Boolean>()
    val userHasAtleastOneStoredCard = PublishSubject.create<Boolean>()
    val onStoredCardChosen = PublishSubject.create<Unit>()
    val onTemporarySavedCreditCardChosen = PublishSubject.create<Unit>()
    val ccFeeDisclaimer = PublishSubject.create<String>()
    val isFeatureEnabledForPaymentInfoTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidCheckoutPaymentTravelerInfo)

    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    init {
        Observable.combineLatest(billingInfoAndStatusUpdate, isRedeemable, splitsType) {
            infoAndStatus, isRedeemable, splitsType ->
            object {
                val info = infoAndStatus.first
                val status = infoAndStatus.second
                val isRedeemable = isRedeemable
                val splitsType = splitsType
            }
        }.subscribe {
            subtitleColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_default_card_text_color))

            if (it.isRedeemable && it.splitsType == PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT) {
                setPaymentTileInfo(PaymentType.POINTS_REWARDS,
                        resources.getString(R.string.checkout_paying_with_points_only_line1),
                        resources.getString(R.string.checkout_tap_to_edit), it.splitsType, ContactDetailsCompletenessStatus.COMPLETE)
            } else if (it.info == null) {
                val titleSubtitlePair = getTitleAndSubtitleNoInfo(it.isRedeemable)
                tempCard.onNext(Pair("", getCardIcon(null)))
                setPaymentTileInfo(null, titleSubtitlePair.first, titleSubtitlePair.second, it.splitsType, it.status)
            } else if (it.info.isTempCard && it.info.saveCardToExpediaAccount) {
                val title = getCardTypeAndLast4Digits(it.info.paymentType, it.info.number)
                tempCard.onNext(Pair("", getCardIcon(it.info.paymentType)))
                setPaymentTileInfo(it.info.paymentType, title, resources.getString(R.string.checkout_tap_to_edit), it.splitsType, it.status)
                Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(it.info)
            } else if (it.info.hasStoredCard()) {
                val card = it.info.storedCard
                val title = card.description
                tempCard.onNext(Pair("", getCardIcon(card.type)))
                setPaymentTileInfo(card.type, title, resources.getString(R.string.checkout_tap_to_edit), it.splitsType, it.status)
            } else {
                val card = it.info
                val cardNumber = card.number
                val title = getCardTypeAndLast4Digits(card.paymentType, cardNumber)
                if (card.isTempCard && !card.saveCardToExpediaAccount) {
                    tempCard.onNext(Pair(title, getCardIcon(card.paymentType)))
                }
                setPaymentTileInfo(card.paymentType, title, resources.getString(R.string.checkout_tap_to_edit), it.splitsType, it.status)
                Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(it.info)
            }
            Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
        }

        storedCardRemoved.subscribe { card ->
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_hotel_credit_card).mutate()
            icon.setColorFilter(ContextCompat.getColor(context, R.color.hotels_primary_color), PorterDuff.Mode.SRC_IN)
            billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.DEFAULT))
            emptyBillingInfo.onNext(Unit)
            BookingInfoUtils.resetPreviousCreditCardSelectState(userStateManager, card)
            Db.getWorkingBillingInfoManager().workingBillingInfo.storedCard = null
            Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB()
            resetCardFees.onNext(Unit)
        }

        userLogin.subscribe { isLoggedIn ->
            if (!isLoggedIn) {
                storedCardRemoved.onNext(null)
            }
        }

        cardTypeSubject
                .debounce(1, TimeUnit.SECONDS, getScheduler())
                .subscribe { cardType ->
                    val tripItem = Db.getTripBucket().getItem(lineOfBusiness.value)
                    val showingPaymentFeeWarning = tripItem?.hasPaymentFee(cardType) ?: false
                    var invalidPaymentWarningMsg = ""
                    if (tripItem != null && cardType != null && !tripItem.isPaymentTypeSupported(cardType)) {
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
                LineOfBusiness.CARS -> PointOfSale.getPointOfSale().requiresCarsPostalCode()
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
            Db.clearTemporaryCard()
        }
    }

    fun getTitleAndSubtitleNoInfo(isRedeemable: Boolean): Pair<String, String> {
        val isUniversalCheckout = LineOfBusinessExtensions.isUniversalCheckout(lineOfBusiness.value, context)
        var title: String
        var subTitle: String
        if (isUniversalCheckout && isFeatureEnabledForPaymentInfoTest) {
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
        paymentType.onNext(getCardIcon(type))
        cardTitle.onNext(paymentTitle)
        cardSubtitle.onNext(subTitle)
        pwpSmallIcon.onNext(getPwPSmallIconVisibility(type, splitsType))
    }

    private fun getPwPSmallIconVisibility(paymentType: PaymentType?, splitsType: PaymentSplitsType): Boolean {
        return paymentType != null && splitsType == PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD
    }

    private fun getCardIcon(type: PaymentType?): Drawable {
        if (type == null) {
            return ContextCompat.getDrawable(context, R.drawable.cars_checkout_cc_default_icon)
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
}
