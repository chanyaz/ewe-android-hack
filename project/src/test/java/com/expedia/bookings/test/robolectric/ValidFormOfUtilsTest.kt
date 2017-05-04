package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.ValidPayment
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidFormOfUtilsTest {

    @Test
    fun testInvalidFeeTypeReturnsEmptyString() {
        val vfop = getValidFormOfPayment("Kostenlos")
        val testFee = ValidFormOfPaymentUtils.getPaymentFee(vfop.fee)

        assertEquals("", testFee)
    }

    @Test
    fun testValidFeeTypeReturnsAmountAsString() {
        val vfop = getValidFormOfPayment()
        val testFee = ValidFormOfPaymentUtils.getPaymentFee(vfop.fee)
        
        assertEquals("3.50", testFee)
    }

    @Test
    fun testValidPaymentCreatedWithInvalidFee() {
        val newVfop = getValidFormOfPayment(null)
        val oldVfop = ValidFormOfPaymentUtils.createFromValidFormOfPayment(newVfop)

        assertTrue(oldVfop is ValidPayment)
    }

    fun getValidFormOfPayment(fee: String? = "3.50") : ValidFormOfPayment{
        val vfop = ValidFormOfPayment()
        vfop.fee = fee
        vfop.feeCurrencyCode = "$"
        vfop.formattedFee = "$3.50"
        vfop.name = "Visa"
        return vfop
    }
}