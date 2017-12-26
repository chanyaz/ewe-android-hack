package com.expedia.bookings.tracking

import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.services.IClientLogServices
import com.expedia.bookings.utils.ClientLogConstants

object AppStartupTimeClientLog {

    @JvmStatic
    fun trackAppStartupTime(appStartupTimeLogger: AppStartupTimeLogger, clientLogServices: IClientLogServices) {
        if (appStartupTimeLogger.isComplete()) {
            val clientLogBuilder: ClientLog.AppStartupTimeBuilder = ClientLog.AppStartupTimeBuilder()
            clientLogBuilder.requestToUser(appStartupTimeLogger.calculateAppStartupTime())
            clientLogBuilder.pageName(ClientLogConstants.APP_LAUNCH_PAGENAME)
            clientLogServices.log(clientLogBuilder.build())
            appStartupTimeLogger.clear()
        }
    }

}