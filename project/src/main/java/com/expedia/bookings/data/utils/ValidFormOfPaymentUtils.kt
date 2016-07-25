package com.expedia.bookings.data.utils

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.ValidPayment
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.server.ParserUtils
import com.expedia.bookings.utils.CurrencyUtils

fun ValidFormOfPayment.getNewTotalPriceWithFee(originalTotalPrice: Money): Money {
    val newTotalPrice = originalTotalPrice.copy()
    newTotalPrice.add(this.getFee())
    return newTotalPrice
}

fun ValidFormOfPayment.getFee(): Money {
    return ParserUtils.createMoney(this.fee, this.feeCurrencyCode)
}

fun ValidFormOfPayment.getPaymentType(): PaymentType {
    return CurrencyUtils.parsePaymentType(this.name)
}

object ValidFormOfPaymentUtils {

    @JvmStatic fun createFromValidFormOfPayment(newPayment: ValidFormOfPayment): ValidPayment {
        val oldPayment = ValidPayment()
        oldPayment.name = newPayment.name
        oldPayment.paymentType = CurrencyUtils.parsePaymentType(newPayment.name)
        oldPayment.fee = ParserUtils.createMoney(newPayment.fee, newPayment.feeCurrencyCode)
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
}
