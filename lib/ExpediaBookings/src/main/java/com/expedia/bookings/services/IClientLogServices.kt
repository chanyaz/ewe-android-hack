package com.expedia.bookings.services

import com.expedia.bookings.data.clientlog.ClientLog

interface IClientLogServices {
    fun log(clientLog: ClientLog)
    fun deepLinkMarketingIdLog(queryParams: Map<String, String>)
}