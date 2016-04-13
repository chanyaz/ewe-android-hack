package com.expedia.vm

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.User
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.TripBucketItemCar
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.BookingInfoUtils
import com.expedia.bookings.utils.CreditCardUtils
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class PaymentViewModel(val context: Context) {
    val resources = context.resources

    // inputs
    val splitsType = BehaviorSubject.create<PaymentSplitsType>(PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD)
    val isRedeemable = BehaviorSubject.create<Boolean>(false)
    val billingInfoAndStatusUpdate = BehaviorSubject.create<Pair<BillingInfo?,ContactDetailsCompletenessStatus>>()
    val emptyBillingInfo = PublishSubject.create<Unit>()
    val storedCardRemoved = PublishSubject.create<StoredCreditCard?>()

    val cardType = PublishSubject.create<PaymentType?>()
    val userLogin = PublishSubject.create<Boolean>()
    val isCreditCardRequired = BehaviorSubject.create<Boolean>(false)
    val isZipValidationRequired = BehaviorSubject.create<Boolean>(false)
    val lineOfBusiness = BehaviorSubject.create<LineOfBusiness>(LineOfBusiness.HOTELSV2)

    var expandObserver = PublishSubject.create<Boolean>()

    //ouputs
    val iconStatus = PublishSubject.create<ContactDetailsCompletenessStatus>()
    val paymentType = PublishSubject.create<Drawable>()
    val cardTitle = PublishSubject.create<String>()
    val cardSubtitle = PublishSubject.create<String>()
    val pwpSmallIcon = PublishSubject.create<Boolean>()
    val tempCard = PublishSubject.create<Pair<String, Drawable>>()
    val invalidPayment = PublishSubject.create<String?>()

    val userHasAtleastOneStoredCard = PublishSubject.create<Boolean>()
    val onStoredCardChosen = PublishSubject.create<Unit>()

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
            if (it.isRedeemable && it.splitsType == PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT) {
                setPaymentTileInfo(PaymentType.POINTS_EXPEDIA_REWARDS,
                        resources.getString(R.string.checkout_paying_with_points_only_line1),
                        resources.getString(R.string.checkout_tap_to_edit), it.splitsType, ContactDetailsCompletenessStatus.COMPLETE)
            } else if (it.info == null) {
                val title = resources.getString(R.string.checkout_enter_payment_details)
                val subTitle = resources.getString(
                        if (it.isRedeemable) R.string.checkout_payment_options else R.string.checkout_hotelsv2_enter_payment_details_line2)
                tempCard.onNext(Pair("", getCardIcon(null)))
                setPaymentTileInfo(null, title, subTitle, it.splitsType, it.status)
            } else if (it.info.isTempCard && it.info.saveCardToExpediaAccount) {
                var title = manuallyEnteredCard(it.info.paymentType, it.info.number)
                tempCard.onNext(Pair("", getCardIcon(it.info.paymentType)))
                setPaymentTileInfo(it.info.paymentType, title, resources.getString(R.string.checkout_tap_to_edit), it.splitsType, it.status)
                Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(it.info)
            } else if (it.info.hasStoredCard()) {
                val card = it.info.storedCard
                var title = card.description
                tempCard.onNext(Pair("", getCardIcon(card.type)))
                setPaymentTileInfo(card.type, title, resources.getString(R.string.checkout_tap_to_edit), it.splitsType, it.status)
            } else {
                val cardNumber = it.info.number
                var title = manuallyEnteredCard(it.info.paymentType, cardNumber)
                if (it.info.isTempCard && !it.info.saveCardToExpediaAccount) {
                    tempCard.onNext(Pair(title, getCardIcon(it.info.paymentType)))
                }
                setPaymentTileInfo(it.info.paymentType, title, resources.getString(R.string.checkout_tap_to_edit), it.splitsType, it.status)
                Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(it.info)
            }
            Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
        }

        storedCardRemoved.subscribe { card ->
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_hotel_credit_card).mutate()
            icon.setColorFilter(ContextCompat.getColor(context, R.color.hotels_primary_color), PorterDuff.Mode.SRC_IN)
            billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.DEFAULT))
            emptyBillingInfo.onNext(Unit)
            BookingInfoUtils.resetPreviousCreditCardSelectState(context, card)
            Db.getWorkingBillingInfoManager().workingBillingInfo.storedCard = null
            Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB()
        }

        userLogin.subscribe { isLoggedIn ->
            if (!isLoggedIn) {
                storedCardRemoved.onNext(null)
            }
        }

        cardType.subscribe { cardType ->
            val tripItem = Db.getTripBucket().getItem(lineOfBusiness.value)
            var message: String? = null
            if (tripItem != null && cardType != null && !tripItem.isPaymentTypeSupported(cardType)) {
                val cardName = CreditCardUtils.getHumanReadableName(context, cardType)
                if (lineOfBusiness.value == LineOfBusiness.CARS) {
                    message = resources.getString(R.string.car_does_not_accept_cardtype_TEMPLATE,
                            (tripItem as TripBucketItemCar).mCarTripResponse.carProduct.vendor.name, cardName)
                } else if (lineOfBusiness.value == LineOfBusiness.LX || lineOfBusiness.value == LineOfBusiness.TRANSPORT) {
                    message = resources.getString(R.string.lx_does_not_accept_cardtype_TEMPLATE, cardName)
                } else if (lineOfBusiness.value == LineOfBusiness.HOTELSV2) {
                    message = resources.getString(R.string.hotel_does_not_accept_cardtype_TEMPLATE, cardName)
                }
            }
            invalidPayment.onNext(message)
        }

        lineOfBusiness.subscribe { lob ->
            isCreditCardRequired.onNext(lob == LineOfBusiness.PACKAGES || lob == LineOfBusiness.HOTELSV2 || lob == LineOfBusiness.FLIGHTS)
            isZipValidationRequired.onNext(lob == LineOfBusiness.PACKAGES || lob == LineOfBusiness.HOTELSV2 || lob == LineOfBusiness.FLIGHTS)
        }
    }

    fun setPaymentTileInfo(type: PaymentType?, title: String, subTitle: String, splitsType: PaymentSplitsType, completeStatus: ContactDetailsCompletenessStatus) {
        var paymentTitle = title
        if (type != null && isRedeemable.value && splitsType == PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD) {
            paymentTitle = Phrase.from(context, R.string.checkout_paying_with_points_and_card_line1)
                    .put("carddescription", title)
                    .format().toString()
        }
        iconStatus.onNext(completeStatus)
        paymentType.onNext(getCardIcon(type))
        cardTitle.onNext(paymentTitle)
        cardSubtitle.onNext(subTitle)
        pwpSmallIcon.onNext(getPwPSmallIconVisibility(type, splitsType))
    }

    fun getPwPSmallIconVisibility(paymentType: PaymentType?, splitsType: PaymentSplitsType): Boolean {
        return paymentType != null && splitsType == PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD
    }

    fun getCardIcon(type: PaymentType?): Drawable {
        if (type == null) {
            return ContextCompat.getDrawable(context, R.drawable.cars_checkout_cc_default_icon)
        } else {
            return ContextCompat.getDrawable(context, BookingInfoUtils.getTabletCardIcon(type))
        }
    }

    private fun manuallyEnteredCard(paymentType: PaymentType?, cardNumber: String): String {
        return Phrase.from(context, R.string.checkout_selected_card)
                .put("cardtype", CreditCardUtils.getHumanReadableCardTypeName(context, paymentType))
                .put("cardno", cardNumber.drop(cardNumber.length - 4))
                .format().toString()
    }
}