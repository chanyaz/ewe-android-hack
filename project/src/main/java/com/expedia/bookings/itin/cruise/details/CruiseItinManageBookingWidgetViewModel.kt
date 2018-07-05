package com.expedia.bookings.itin.cruise.details

import com.expedia.bookings.itin.common.ItinAdditionalInfoCardViewModel
import com.expedia.bookings.itin.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.itin.common.ItinManageBookingWidgetViewModel
import com.expedia.bookings.itin.common.ItinPriceSummaryCardViewModel
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.scopes.WebViewCardScope

class CruiseItinManageBookingWidgetViewModel<S>(val scope: S) : ItinManageBookingWidgetViewModel() where S : HasStringProvider, S : HasItinRepo, S : HasWebViewLauncher, S : HasTripsTracking {
    override val moreHelpViewModel: ItinBookingInfoCardViewModel
    override val priceSummaryViewModel: ItinBookingInfoCardViewModel
    override val additionalInfoViewModel: ItinBookingInfoCardViewModel

    init {
        val webViewCardScope = WebViewCardScope(scope.strings, scope.webViewLauncher,
                scope.itinRepo, TripProducts.CRUISE.name, scope.tripsTracking, "moreHelp")
        moreHelpViewModel = CruiseItinMoreHelpCardViewModel(webViewCardScope)
        priceSummaryViewModel = ItinPriceSummaryCardViewModel(WebViewCardScope(scope.strings, scope.webViewLauncher,
                scope.itinRepo, TripProducts.CRUISE.name, scope.tripsTracking, "priceSummary"))
        additionalInfoViewModel = ItinAdditionalInfoCardViewModel(webViewCardScope)
    }
}
