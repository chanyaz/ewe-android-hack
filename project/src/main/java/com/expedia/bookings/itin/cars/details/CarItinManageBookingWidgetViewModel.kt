package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.itin.common.ItinAdditionalInfoCardViewModel
import com.expedia.bookings.itin.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.itin.common.ItinManageBookingWidgetViewModel
import com.expedia.bookings.itin.common.ItinPriceSummaryCardViewModel
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.scopes.WebViewCardScope

class CarItinManageBookingWidgetViewModel<S>(scope: S) : ItinManageBookingWidgetViewModel() where S : HasStringProvider, S : HasWebViewLauncher, S : HasActivityLauncher, S : HasCarRepo, S : HasTripsTracking {
    override val moreHelpViewModel: ItinBookingInfoCardViewModel
    override val priceSummaryViewModel: ItinBookingInfoCardViewModel
    override val additionalInfoViewModel: ItinBookingInfoCardViewModel

    init {
        val webViewScope = WebViewCardScope(scope.strings, scope.webViewLauncher, scope.itinCarRepo.liveDataItin.value!!)
        moreHelpViewModel = CarItinMoreHelpCardViewModel(scope)
        priceSummaryViewModel = ItinPriceSummaryCardViewModel(webViewScope)
        additionalInfoViewModel = ItinAdditionalInfoCardViewModel(webViewScope)
    }
}
