package com.expedia.bookings.test

import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.services.IClientLogServices

class MockMultipleClientLogsService : IClientLogServices {

    val clientLogList: ArrayList<ClientLog> = ArrayList()

    override fun log(clientLog: ClientLog) {
        clientLogList.add(clientLog)
    }

    var lastSeenDeepLinkQueryParams: Map<String, String>? = null
    override fun deepLinkMarketingIdLog(queryParams: Map<String, String>) {
        lastSeenDeepLinkQueryParams = queryParams
    }
}
