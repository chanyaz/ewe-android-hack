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
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.TripBucketItemCar
import com.expedia.bookings.data.payment.PaymentSplitsType
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
    val completeBillingInfo = BehaviorSubject.create<BillingInfo>()
    val incompleteBillingInfo = PublishSubject.create<Unit>()
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
    val tempTitle = PublishSubject.create<String>()

    val paymentTypeStoredCard = PublishSubject.create<Drawable>()
    val cardTitleStoredCard = PublishSubject.create<String>()
    val invalidPayment = PublishSubject.create<String?>()

    val enableMenu = PublishSubject.create<Boolean>()
    val enableMenuDone = PublishSubject.create<Boolean>()
    val toolbarTitle = PublishSubject.create<String>()
    val doneClicked = PublishSubject.create<Unit>()
    val editText = PublishSubject.create<EditText>()

    init {
        Observable.combineLatest(completeBillingInfo, isRedeemable, splitsType) {
            info, isRedeemable, splitsType ->

            if (isRedeemable && splitsType == PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT) {
                setPaymentTileInfo(PaymentType.POINTS_EXPEDIA_REWARDS,
                        resources.getString(R.string.checkout_paying_with_points_only_line1),
                        resources.getString(R.string.checkout_tap_to_edit), splitsType, ContactDetailsCompletenessStatus.COMPLETE)
4            } else if (info == null) {
                return@combineLatest
            } else if (info.isTempCard && info.saveCardToExpediaAccount) {
                var title = temporarilySavedCardLabel(info.paymentType, info.number)
                tempTitle.onNext("")
                cardTitleStoredCard.onNext(title)
                paymentTypeStoredCard.onNext(getCardIcon(info.paymentType))
                setPaymentTileInfo(info.paymentType, title, resources.getString(R.string.checkout_tap_to_edit), splitsType, ContactDetailsCompletenessStatus.COMPLETE)
            } else if (info.hasStoredCard()) {
                val card = info.storedCard
                var title = card.description
                tempTitle.onNext("")
                cardTitleStoredCard.onNext(title)
                paymentTypeStoredCard.onNext(getCardIcon(card.type))
                setPaymentTileInfo(card.type, title, resources.getString(R.string.checkout_tap_to_edit), splitsType, ContactDetailsCompletenessStatus.COMPLETE)
            } else {
                val cardNumber = info.number
                var title = Phrase.from(context, R.string.checkout_selected_card)
                        .put("cardtype", CreditCardUtils.getHumanReadableCardTypeName(context, info.paymentType))
                        .put("cardno", cardNumber.drop(cardNumber.length - 4))
                        .format().toString()
                if (info.isTempCard && !info.saveCardToExpediaAccount) {
                    tempTitle.onNext(title)
                }
                setPaymentTileInfo(info.paymentType, title, resources.getString(R.string.checkout_tap_to_edit), splitsType, ContactDetailsCompletenessStatus.COMPLETE)
                Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(info)
            }
            Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
        }.subscribe()

        storedCardRemoved.subscribe { card ->
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_hotel_credit_card).mutate()
            icon.setColorFilter(ContextCompat.getColor(context, R.color.hotels_primary_color), PorterDuff.Mode.SRC_IN)
            paymentTypeStoredCard.onNext(icon)
            cardTitleStoredCard.onNext(context.getString(R.string.select_saved_cards))
            emptyBillingInfo.onNext(Unit)
            BookingInfoUtils.resetPreviousCreditCardSelectState(context, card)
            Db.getWorkingBillingInfoManager().workingBillingInfo.storedCard = null
            Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB()
        }

        incompleteBillingInfo.subscribe {
            val title = resources.getString(R.string.checkout_hotelsv2_enter_payment_details_line1)
            val subTitle = resources.getString(
                    if (isRedeemable.value) R.string.checkout_payment_options else R.string.checkout_hotelsv2_enter_payment_details_line2)
            tempTitle.onNext("")
            setPaymentTileInfo(null, title, subTitle, splitsType.value, ContactDetailsCompletenessStatus.INCOMPLETE)
        }

        emptyBillingInfo.subscribe {
            val title = resources.getString(R.string.checkout_hotelsv2_enter_payment_details_line1)
            val subTitle = resources.getString(
                    if (isRedeemable.value) R.string.checkout_payment_options else R.string.checkout_hotelsv2_enter_payment_details_line2)
            tempTitle.onNext("")
            setPaymentTileInfo(null, title, subTitle, splitsType.value, ContactDetailsCompletenessStatus.DEFAULT)
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
                } else if (lineOfBusiness.value == LineOfBusiness.LX) {
                    message = resources.getString(R.string.lx_does_not_accept_cardtype_TEMPLATE, cardName)
                } else if (lineOfBusiness.value == LineOfBusiness.HOTELSV2) {
                    message = resources.getString(R.string.hotel_does_not_accept_cardtype_TEMPLATE, cardName)
                }
            }
            invalidPayment.onNext(message)
        }

        lineOfBusiness.subscribe { lob ->
            isCreditCardRequired.onNext(lob == LineOfBusiness.PACKAGES || lob == LineOfBusiness.HOTELSV2)
            isZipValidationRequired.onNext(lob == LineOfBusiness.PACKAGES || lob == LineOfBusiness.HOTELSV2)
        }
    }

    fun setPaymentTileInfo(type: PaymentType?, title: String, subTitle: String, splitsType: PaymentSplitsType, completeStatus: ContactDetailsCompletenessStatus) {
        var paymentTitle = title
        if (isRedeemable.value && splitsType == PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD) {
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

    private fun temporarilySavedCardLabel(paymentType: PaymentType?, cardNumber: String): String {
        return Phrase.from(context, R.string.temporarily_saved_card_TEMPLATE)
                .put("cardtype", CreditCardUtils.getHumanReadableCardTypeName(context, paymentType))
                .put("cardno", cardNumber.drop(cardNumber.length - 4))
                .format().toString()
    }
}