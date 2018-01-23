package com.expedia.bookings.tracking

import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.services.IClientLogServices

object AppStartupTimeClientLog {

    @JvmStatic
    fun trackTimeLogger(timeLogger: TimeLogger, clientLogServices: IClientLogServices) {
        if (timeLogger.isComplete()) {
            val clientLogBuilder: ClientLog.AppStartupTimeBuilder = ClientLog.AppStartupTimeBuilder()
            clientLogBuilder.requestToUser(timeLogger.calculateTotalTime())
            clientLogBuilder.pageName(timeLogger.pageName)
            clientLogServices.log(clientLogBuilder.build())
        }
        timeLogger.clear()
    }
}
