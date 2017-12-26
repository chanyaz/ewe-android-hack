package com.expedia.bookings.utils

import com.expedia.bookings.services.IClientLogServices
import com.expedia.bookings.tracking.OmnitureTracking
import okhttp3.HttpUrl
import java.util.HashMap
import java.util.HashSet
import java.util.Locale

object DeepLinkUtils {

    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    // Deep Link Tracking
    //
    // Documentation:
    // https://confluence/display/Omniture/Download+-+Retargeting+-+Deeplink+Campaign+Tracking

    // TODO candidate for ExpediaPointOfSale JSON?

    private val KNOWN_DEEP_LINK_ARGS = object : HashSet<String>() {
        init {
            add("emlcid")
            add("semcid")
            add("olacid")
            add("affcid")
            add("brandcid")
            add("seocid")
            add("kword")
            add("mdpcid")
            add("mdpdtl")
            add("oladtl")
            add("afflid")
            add("icmcid")
            add("icmdtl")
            add("gclid")
            add("semdtl")
        }
    }

    @JvmStatic fun parseAndTrackDeepLink(clientLogServices: IClientLogServices, url: HttpUrl?) {
        if (url == null) {
            return
        }
        val clientLogQueryParams = HashMap<String, String>()

        url.queryParameterNames().forEach { key ->
            val lowerCaseKey = key.toLowerCase(Locale.US)
            val queryParam = url.queryParameter(key)
            if (KNOWN_DEEP_LINK_ARGS.contains(lowerCaseKey) && queryParam != null) {
                OmnitureTracking.setDeepLinkTrackingParams(lowerCaseKey, queryParam)
                clientLogQueryParams.put(lowerCaseKey, queryParam)
            }
        }

        if (clientLogQueryParams.isNotEmpty()) {
            clientLogServices.deepLinkMarketingIdLog(clientLogQueryParams)
        }
    }
}
