package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isRichContentForPackagesEnabled
import com.expedia.vm.BaseResultsViewModel

class PackageResultsViewModel(context: Context) : BaseResultsViewModel(context) {

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }

    override fun injectComponents(context: Context) {
        Ui.getApplication(context).packageComponent().inject(this)
    }

    override fun trackRouteHappyResultCountRatio(isOutbound: Boolean, routeHappyCount: Int, totalCount: Int) {
        // TODO Implement with the tracking sub task of main story #15350
    }

    override fun trackRouteHappyEmptyResults(isOutboundFlight: Boolean) {
        // TODO Implement with the tracking sub task of main story #15350
    }

    override fun shouldShowRichContent(context: Context): Boolean {
        return isRichContentForPackagesEnabled(context)
    }
}
