package com.expedia.bookings.test

import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.services.IClientLogServices

class MockClientLogServices : IClientLogServices {
    var lastSeenClientLog: ClientLog? = null
    override fun log(clientLog: ClientLog) {
        lastSeenClientLog = clientLog
    }

    override fun deepLinkMarketingIdLog(queryParams: Map<String, String>) {
        TODO("not implemented: deepLinkMarketingIdLog")
    }
}