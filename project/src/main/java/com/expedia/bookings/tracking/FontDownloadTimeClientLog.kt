package com.expedia.bookings.tracking

import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.services.IClientLogServices

object FontDownloadTimeClientLog {

    @JvmStatic
    fun trackDownloadTimeLogger(timeLogger: DownloadableFontsTimeLogger, clientLogServices: IClientLogServices) {
        if (timeLogger.isComplete()) {
            val clientLogBuilder = ClientLog.FontDownloadTimeBuilder()
            clientLogBuilder.responseTime(timeLogger.calculateTotalTime())
            clientLogBuilder.pageName(timeLogger.pageName)
            clientLogBuilder.fontName(timeLogger.fontName)
            clientLogServices.log(clientLogBuilder.build())
        }
        timeLogger.clear()
    }
}
