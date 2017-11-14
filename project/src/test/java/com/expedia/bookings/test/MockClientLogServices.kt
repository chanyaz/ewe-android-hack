package com.expedia.bookings.test

import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.data.clientlog.ThumborClientLog
import com.expedia.bookings.services.IClientLogServices

class MockClientLogServices : IClientLogServices {
    var lastSeenClientLog: ClientLog? = null
    override fun log(clientLog: ClientLog) {
        lastSeenClientLog = clientLog
    }

    var lastSeenDeepLinkQueryParams: Map<String, String>? = null
    override fun deepLinkMarketingIdLog(queryParams: Map<String, String>) {
        lastSeenDeepLinkQueryParams = queryParams
    }

    var lastSeenThumborClientLog: ThumborClientLog? = null
    override fun thumborClientLog(thumborClientLog: ThumborClientLog) {
        lastSeenThumborClientLog = thumborClientLog
    }
}
