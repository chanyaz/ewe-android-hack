package com.expedia.bookings.extensions

import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.utils.CurrencyUtils

fun ValidFormOfPayment.getPaymentType(): PaymentType {
    return CurrencyUtils.parsePaymentType(this.name)
}
