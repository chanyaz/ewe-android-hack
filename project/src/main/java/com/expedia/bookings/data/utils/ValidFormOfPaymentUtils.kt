package com.expedia.bookings.data.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.ValidPayment
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.utils.CreditCardUtils
import com.expedia.bookings.utils.CurrencyUtils
import com.squareup.phrase.Phrase

fun ValidFormOfPayment.getPaymentType(): PaymentType {
    return CurrencyUtils.parsePaymentType(this.name)
}

object ValidFormOfPaymentUtils {

    @JvmStatic fun createFromValidFormOfPayment(newPayment: ValidFormOfPayment): ValidPayment {
        val oldPayment = ValidPayment()
        oldPayment.name = newPayment.name
        oldPayment.paymentType = CurrencyUtils.parsePaymentType(newPayment.name)
        return oldPayment
    }

    @JvmStatic fun addValidPayment(payments: MutableList<ValidFormOfPayment>?, payment: ValidFormOfPayment?) {
        if (payments == null) {
            throw IllegalArgumentException("payments can not be null")
        }
        if (payment == null) {
            throw IllegalArgumentException("payment can not be null")
        }
        payments.add(payment)
    }

    fun isPaymentTypeSupported(validPaymentTypes: List<ValidFormOfPayment>, paymentType: PaymentType): Boolean {
        for (payment in validPaymentTypes) {
            if (payment.getPaymentType() == paymentType) {
                return true
            }
        }
        return false
    }

    @JvmStatic fun getInvalidFormOfPaymentMessage(context: Context, paymentType: PaymentType, lob: LineOfBusiness) : String {
        val cardName = CreditCardUtils.getHumanReadableName(context, paymentType)
        val invalidPaymentWarningMsg = when (lob) {
            LineOfBusiness.CARS -> {
                Phrase.from(context, R.string.car_does_not_accept_cardtype_TEMPLATE)
                        .put("card_type", cardName)
                        .format().toString()
            }

            LineOfBusiness.LX, LineOfBusiness.TRANSPORT -> {
                Phrase.from(context, R.string.lx_does_not_accept_cardtype_TEMPLATE)
                        .put("card_type", cardName)
                        .format().toString()
            }

            LineOfBusiness.HOTELS -> {
                Phrase.from(context, R.string.hotel_does_not_accept_cardtype_TEMPLATE)
                        .put("card_type", cardName)
                        .format().toString()
            }

            LineOfBusiness.FLIGHTS_V2, LineOfBusiness.FLIGHTS -> {
                Phrase.from(context, R.string.airline_does_not_accept_cardtype_TEMPLATE)
                        .put("card_type", cardName)
                        .format().toString()
            }

            LineOfBusiness.PACKAGES -> {
                Phrase.from(context.resources, R.string.package_does_not_accept_cardtype_TEMPLATE)
                        .put("card_type", cardName)
                        .format().toString()
            }

            else -> ""
        }
        return invalidPaymentWarningMsg
    }

}
