package com.expedia.bookings.utils

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object NumberUtils {

    private var localeToNumberFormatMap = HashMap<Int, NumberFormat>()

    @JvmStatic
    fun parseDoubleSafe(str: String): Double? {
        if (Strings.isEmpty(str)) {
            return null
        }

        try {
            return java.lang.Double.parseDouble(str)
        } catch (e: NumberFormatException) {
            return null
        }
    }

    @JvmStatic
    fun getPercentagePaidWithPointsForOmniture(fraction: BigDecimal, total: BigDecimal): Int {
        if (total == BigDecimal.ZERO) {
            throw IllegalArgumentException("Total cannot be zero while calculating percentage")
        }
        return fraction.divide(total, 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal("100")).toInt()
    }

    @JvmStatic
    fun round(number: Float, precision: Int): Float {
        val shiftNumberByPlaces = Math.pow(10.0, precision.toDouble()).toFloat()
        return Math.round(number * shiftNumberByPlaces) / shiftNumberByPlaces
    }

    @JvmStatic
    fun localeBasedFormattedNumber(number: Number): String {
        val locale = Locale.getDefault()
        val key = locale.hashCode()
        var nf = localeToNumberFormatMap.get(key)
        if (nf == null) {
            nf = NumberFormat.getInstance(locale) as NumberFormat
            localeToNumberFormatMap.put(key, nf)
        }
        return nf.format(number)
    }
}
