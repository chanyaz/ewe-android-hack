package com.expedia.bookings.flights.vm

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.Ui
import com.expedia.vm.BaseResultsViewModel

class FlightResultsViewModel(context: Context) : BaseResultsViewModel(context) {

    override val showLoadingStateV1 = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFLightLoadingStateV1)

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.FLIGHTS_V2
    }

    override fun injectComponents(context: Context) {
        Ui.getApplication(context).flightComponent().inject(this)
    }

    override fun trackRouteHappyResultCountRatio(isOutbound: Boolean, routeHappyCount: Int, totalCount: Int) {
        FlightsV2Tracking.trackRouteHappyResultCountRatio(isOutbound, Db.getFlightSearchParams().isRoundTrip(),
                routeHappyCount, totalCount)
    }

    override fun trackRouteHappyEmptyResults(isOutboundFlight: Boolean) {
        FlightsV2Tracking.trackRouteHappyEmptyResults(isOutboundFlight, Db.getFlightSearchParams().isRoundTrip())
    }
}
