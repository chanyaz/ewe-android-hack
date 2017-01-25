package com.expedia.bookings.utils

import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.services.ClientLogServices

import android.net.Uri
import com.expedia.bookings.tracking.OmnitureTracking
import java.util.*

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
        }
    }

    @JvmStatic fun parseAndTrackDeepLink(clientLogServices: ClientLogServices, uri: Uri, queryParameterNames: Set<String>) {
        val clientLogQueryParams = HashMap<String, String>()

        queryParameterNames.forEach { key ->
            val lowerCaseKey = key.toLowerCase(Locale.US)
            val queryParam = uri.getQueryParameter(key)
            if (KNOWN_DEEP_LINK_ARGS.contains(lowerCaseKey)) {
                OmnitureTracking.setDeepLinkTrackingParams(lowerCaseKey, queryParam)
                clientLogQueryParams.put(lowerCaseKey, queryParam)
            }
        }

        if (clientLogQueryParams.isNotEmpty()) {
            clientLogServices.deepLinkMarketingIdLog(clientLogQueryParams)
        }
    }
}
