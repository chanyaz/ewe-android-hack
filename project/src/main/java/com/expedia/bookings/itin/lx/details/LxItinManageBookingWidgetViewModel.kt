package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.itin.common.ItinAdditionalInfoCardViewModel
import com.expedia.bookings.itin.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.itin.common.ItinManageBookingWidgetViewModel
import com.expedia.bookings.itin.common.ItinPriceSummaryCardViewModel
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.scopes.WebViewCardScope
import com.expedia.bookings.itin.scopes.StringsActivityScope

class LxItinManageBookingWidgetViewModel<S>(scope: S) : ItinManageBookingWidgetViewModel() where S : HasStringProvider, S : HasWebViewLauncher, S : HasActivityLauncher, S : HasItinRepo, S : HasTripsTracking {

    override val moreHelpViewModel: ItinBookingInfoCardViewModel
    override val priceSummaryViewModel: ItinBookingInfoCardViewModel
    override val additionalInfoViewModel: ItinBookingInfoCardViewModel

    init {
        val stringsWebViewScope = WebViewCardScope(scope.strings, scope.webViewLauncher, scope.itinRepo, TripProducts.ACTIVITY.name, scope.tripsTracking, "price")
        val stringsActivityScope = StringsActivityScope(scope.strings, scope.activityLauncher, scope.itinRepo, scope.tripsTracking)
        moreHelpViewModel = LxItinMoreHelpCardViewModel(stringsActivityScope)
        priceSummaryViewModel = ItinPriceSummaryCardViewModel(stringsWebViewScope)
        additionalInfoViewModel = ItinAdditionalInfoCardViewModel(stringsWebViewScope)
    }
}
