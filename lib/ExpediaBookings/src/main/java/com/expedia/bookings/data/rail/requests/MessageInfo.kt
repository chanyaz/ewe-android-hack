package com.expedia.bookings.data.rail.requests

import java.util.UUID

class MessageInfo(val userGUID: String) {
    val messageGUID: String
    val transactionGUID: String
    val debugTraceEnabled = false

    init {
        messageGUID = UUID.randomUUID().toString().replace("-".toRegex(), "")
        transactionGUID = UUID.randomUUID().toString().replace("-".toRegex(), "")
    }
}
