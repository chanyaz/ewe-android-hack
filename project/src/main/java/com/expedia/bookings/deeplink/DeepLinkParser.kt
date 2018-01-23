package com.expedia.bookings.deeplink

import android.content.res.AssetManager
import android.net.Uri
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.net.URLDecoder

open class DeepLinkParser(assets: AssetManager) {

    protected val TAG = "ExpediaDeepLink"
    protected val ASSETS = assets

    fun parseDeepLink(data: Uri): DeepLink {
        val scheme = data.scheme
        if (isUniversalLink(scheme)) {
            return UniversalDeepLinkParser(ASSETS).parseUniversalDeepLink(data)
        } else {
            return CustomDeepLinkParser(ASSETS).parseCustomDeepLink(data)
        }
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
            } catch (e: Exception) {
            }
        }
        return null
    }

    protected fun getParsedDateTimeQueryParameterIfExists(data: Uri, queryParameterNames: Set<String>, parameterName: String): DateTime? {
        if (queryParameterNames.contains(parameterName)) {
            try {
                return DateTime.parse(URLDecoder.decode(data.getQueryParameter(parameterName), "UTF-8"), DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss"))
            } catch (e: Exception) {
            }
        }
        return null
    }

    protected fun getIntegerParameterIfExists(data: Uri, queryParameterNames: Set<String>, parameterName: String): Int {
        if (queryParameterNames.contains(parameterName)) {
            try {
                return Integer.parseInt(data.getQueryParameter(parameterName))
            } catch (e: Exception) {
            }
        }
        return 0
    }

    protected fun getNullableIntegerParameterIfExists(data: Uri, queryParameterNames: Set<String>, parameterName: String): Int? {
        if (queryParameterNames.contains(parameterName)) {
            try {
                return Integer.parseInt(data.getQueryParameter(parameterName))
            } catch (e: Exception) {
            }
        }
        return null
    }
}
