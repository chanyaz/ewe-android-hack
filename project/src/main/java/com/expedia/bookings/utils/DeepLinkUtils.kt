package com.expedia.bookings.utils

import com.expedia.bookings.services.IClientLogServices
import okhttp3.HttpUrl
import java.util.HashMap
import java.util.Locale

object DeepLinkUtils {

    private val KNOWN_DEEP_LINK_ARGS = setOf(
            "affcid",
            "afflid",
            "brandcid",
            "emlcid",
            "emldtl",
            "icmcid",
            "icmdtl",
            "mdpcid",
            "mdpdtl",
            "olacid",
            "oladtl",
            "semcid",
            "semdtl",
            "kword",
            "gclid",
            "seocid",
            "pushcid")

    @JvmStatic fun parseAndTrackDeepLink(clientLogServices: IClientLogServices, url: HttpUrl?, deepLinkAnalytics: DeepLinkAnalytics) {
        if (url == null) {
            return
        }
        val deepLinkParams = HashMap<String, String>()

        url.queryParameterNames().forEach { key ->
            val lowerCaseKey = key.toLowerCase(Locale.US)
            val queryParam = url.queryParameter(key)
            if (KNOWN_DEEP_LINK_ARGS.contains(lowerCaseKey) && queryParam != null) {
                deepLinkParams.put(lowerCaseKey, queryParam)
            }
        }

        if (deepLinkParams.isNotEmpty()) {
            deepLinkAnalytics.setDeepLinkTrackingParams(deepLinkParams)
            clientLogServices.deepLinkMarketingIdLog(deepLinkParams)
        }
    }
}
