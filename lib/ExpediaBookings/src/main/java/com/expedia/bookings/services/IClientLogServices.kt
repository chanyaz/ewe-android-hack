package com.expedia.bookings.services

import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.data.clientlog.ThumborClientLog

interface IClientLogServices {
    fun log(clientLog: ClientLog)
    fun deepLinkMarketingIdLog(queryParams: Map<String, String>)
    fun thumborClientLog(thumborClientLog: ThumborClientLog)
}
