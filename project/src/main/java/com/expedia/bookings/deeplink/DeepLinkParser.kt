package com.expedia.bookings.deeplink

import android.net.Uri
import com.expedia.bookings.data.ChildTraveler
import com.expedia.bookings.utils.GuestsPickerUtils
import com.mobiata.android.Log
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.net.URLDecoder
import java.util.ArrayList
import java.util.Arrays

open class DeepLinkParser {

    private val TAG = "ExpediaDeepLink"

    fun parseDeepLink(data: Uri) : DeepLink {
        val scheme = data.scheme
        if (isUniversalLink(scheme)) {
            return UniversalDeepLinkParser().parseUniversalDeepLink(data)
        }
        else {
            return CustomDeepLinkParser().parseCustomDeepLink(data)
        }
    }

    fun parseChildAges(childAgesStr: String, numAdults: Int): List<ChildTraveler>? {
        val childAgesArr = childAgesStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val maxChildren = GuestsPickerUtils.getMaxChildren(numAdults)
        val children = ArrayList<ChildTraveler>()
        try {
            var a = 0
            while (a < childAgesArr.size && children.size < maxChildren) {
                val childAge = Integer.parseInt(childAgesArr[a])

                if (childAge < GuestsPickerUtils.MIN_CHILD_AGE) {
                    Log.w(TAG, "Child age (" + childAge + ") less than that of a child, not adding: "
                            + childAge)
                } else if (childAge > GuestsPickerUtils.MAX_CHILD_AGE) {
                    Log.w(TAG, "Child age ($childAge) not an actual child, ignoring: $childAge")
                } else {
                    children.add(ChildTraveler(childAge, false))
                }
                a++
            }
            if (children.size > 0) {
                Log.d(TAG,
                        "Setting children ages: " + Arrays.toString(children.toTypedArray()))
                return children
            }
        } catch (e: NumberFormatException) {
            Log.w(TAG, "Could not parse childAges: " + childAgesStr, e)
        }
        return null
    }

    private fun isUniversalLink(scheme: String): Boolean {
        if (scheme.equals("https") || scheme.equals("http")) {
            return true
        }
        return false
    }

    protected fun getQueryParameterIfExists(data: Uri, queryParameterNames: Set<String>, parameterName: String): String? {
        if (queryParameterNames.contains(parameterName)) {
            return data.getQueryParameter(parameterName)
        }
        return null
    }

    protected fun getParsedLocalDateQueryParameterIfExists(data: Uri, queryParameterNames: Set<String>, parameterName: String, dateTimeFormatter: DateTimeFormatter): LocalDate? {
        if (queryParameterNames.contains(parameterName)) {
            try {
                return LocalDate.parse(URLDecoder.decode(data.getQueryParameter(parameterName), "UTF-8"), dateTimeFormatter)
            }
            catch (e: Exception) {
            }
        }
        return null
    }

    protected fun getParsedDateTimeQueryParameterIfExists(data: Uri, queryParameterNames: Set<String>, parameterName: String): DateTime? {
        if (queryParameterNames.contains(parameterName)) {
            try {
                return DateTime.parse(URLDecoder.decode(data.getQueryParameter(parameterName), "UTF-8"), DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss"))
            }
            catch (e: Exception) {
            }
        }
        return null
    }

    protected fun getIntegerParameterIfExists(data: Uri, queryParameterNames: Set<String>, parameterName: String): Int {
        if (queryParameterNames.contains(parameterName)) {
            try {
                return Integer.parseInt(data.getQueryParameter(parameterName))
            }
            catch (e: Exception) {
            }
        }
        return 0
    }
}