package com.expedia.bookings.data.utils

object CreditCardLuhnCheckUtil {

    fun cardNumberIsValid(cardNumber: String): Boolean {
        try {
            return sumOfProductsIsValid(sumOfProducts(cardNumber.toLong()))
        } catch (e: Exception) {
            return false
        }
    }

    private fun sumOfProducts(cardNumber: Long): Int {
        var currNumbers = cardNumber
        var currDigit: Int
        var currSum = 0
        while (currNumbers.compareTo(0) != 0) {
            currSum += (currNumbers % 10).toInt()
            currDigit = (((currNumbers / 10) % 10) * 2).toInt()
            if (currDigit > 9) {
                currDigit -= 9
            }
            currSum += currDigit
            currNumbers /= 100
        }
        return currSum
    }

    private fun sumOfProductsIsValid(totalSum: Int): Boolean {
        return totalSum % 10 == 0
    }
}
