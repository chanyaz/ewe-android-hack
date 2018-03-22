package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.utils.CreditCardLuhnCheckUtil
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidFormOfUtilsTest {

    @Test
    fun testValidPaymentCreatedWithInvalidFee() {
        val newVfop = getValidFormOfPayment()
        val oldVfop = ValidFormOfPaymentUtils.createValidPaymentFromValidFormOfPayment(newVfop)

        assertEquals(oldVfop.name, "Visa")
        assertEquals(oldVfop.paymentType, PaymentType.CARD_VISA)
    }

    @Test
    fun testInvalidTwoDigitLuhnCheck() {
        val invalidTwoDigit = "10"
        assertFalse(CreditCardLuhnCheckUtil.cardNumberIsValid(invalidTwoDigit))
    }

    @Test
    fun testValidTwoDigitLuhnCheck() {
        val validTwoDigit = "00"
        assertTrue(CreditCardLuhnCheckUtil.cardNumberIsValid(validTwoDigit))
    }

    @Test
    fun testInvalidThreeDigitLuhnCheck() {
        val invalidThreeDigit = "110"
        assertFalse(CreditCardLuhnCheckUtil.cardNumberIsValid(invalidThreeDigit))
    }

    @Test
    fun testValidThreeDigitLuhnCheck() {
        val validThreeDigit = "240"
        assertTrue(CreditCardLuhnCheckUtil.cardNumberIsValid(validThreeDigit))
    }

    @Test
    fun testInvalidFiveDigitLuhnCheck() {
        val invalidFiveDigit = "09999"
        assertFalse(CreditCardLuhnCheckUtil.cardNumberIsValid(invalidFiveDigit))
    }

    @Test
    fun testValidFiveDigitLuhnCheck() {
        val validFiveDigit = "73890"
        assertTrue(CreditCardLuhnCheckUtil.cardNumberIsValid(validFiveDigit))
    }

    @Test
    fun testInvalidInputLuhnCheck() {
        val invalidInput = "1s2d"
        assertFalse(CreditCardLuhnCheckUtil.cardNumberIsValid(invalidInput))
    }

    @Test
    fun testInvalidEmptryStringLuhnCheck() {
        val invalidInput = ""
        assertFalse(CreditCardLuhnCheckUtil.cardNumberIsValid(invalidInput))
    }

    @Test
    fun testInvalidLongWithDecimalsLuhnCheck() {
        val invalidInput = "73890.00"
        assertFalse(CreditCardLuhnCheckUtil.cardNumberIsValid(invalidInput))
    }

    @Test
    fun testInvalidLongWithDecimalPointLuhnCheck() {
        val invalidInput = "73890."
        assertFalse(CreditCardLuhnCheckUtil.cardNumberIsValid(invalidInput))
    }

    @Test
    fun testInvalidCardNumber() {
        val invalidCardNumber = "1111111111111111"
        assertFalse(CreditCardLuhnCheckUtil.cardNumberIsValid(invalidCardNumber))
    }

    @Test
    fun testValidCardNumber() {
        val validCardNumber = "378282246310005"
        assertTrue(CreditCardLuhnCheckUtil.cardNumberIsValid(validCardNumber))
    }

    private fun getValidFormOfPayment(): ValidFormOfPayment {
        val vfop = ValidFormOfPayment()
        vfop.name = "Visa"
        return vfop
    }
}
