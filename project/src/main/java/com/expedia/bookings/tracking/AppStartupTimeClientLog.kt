package com.expedia.bookings.tracking

import android.os.Build
import com.expedia.bookings.services.ClientLogServices
import com.expedia.bookings.utils.ClientLogConstants
import com.expedia.bookings.data.clientlog.ClientLog

object AppStartupTimeClientLog {

    @JvmStatic
    fun trackAppStartupTime(appStartupTimeLogger: AppStartupTimeLogger, clientLogServices: ClientLogServices) {
        if (appStartupTimeLogger.isComplete()) {
            val clientLogBuilder: ClientLog.AppStartupTimeBuilder = ClientLog.AppStartupTimeBuilder()
            clientLogBuilder.requestToUser(appStartupTimeLogger.calculateAppStartupTime())
            clientLogBuilder.pageName(ClientLogConstants.APP_LAUNCH_PAGENAME)
            clientLogServices.log(clientLogBuilder.build())
            appStartupTimeLogger.clear()
        }
    }

}