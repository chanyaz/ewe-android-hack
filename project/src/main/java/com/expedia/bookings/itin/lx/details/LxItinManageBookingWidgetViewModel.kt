package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.itin.lx.moreHelp.LxItinMoreHelpActivity
import com.expedia.bookings.itin.common.ItinManageBookingWidgetViewModel
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.scopes.PriceSummaryCardScope
import com.expedia.bookings.itin.scopes.StringsActivityScope

class LxItinManageBookingWidgetViewModel<S>(scope: S) : ItinManageBookingWidgetViewModel() where S : HasStringProvider, S : HasWebViewLauncher, S : HasActivityLauncher, S : HasLxRepo, S : HasTripsTracking {

    override val moreHelpViewModel: ItinBookingInfoCardViewModel
    override val priceSummaryViewModel: ItinBookingInfoCardViewModel
    override val additionalInfoViewModel: ItinBookingInfoCardViewModel

    init {
        val stringsWebViewScope = PriceSummaryCardScope(scope.strings, scope.webViewLauncher, scope.itinLxRepo)
        val stringsActivityScope = StringsActivityScope(scope.strings, scope.activityLauncher, scope.itinLxRepo, scope.tripsTracking)
        moreHelpViewModel = ItinMoreHelpCardViewModel(stringsActivityScope)
        priceSummaryViewModel = ItinLxPriceSummaryCardViewModel(stringsWebViewScope)
        additionalInfoViewModel = ItinLxAdditionalInfoCardViewModel(stringsWebViewScope)
    }

    class ItinMoreHelpCardViewModel<S>(val scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider, S : HasActivityLauncher, S : HasLxRepo, S : HasTripsTracking {
        override val iconImage: Int = R.drawable.ic_itin_manage_booking_icon
        override val headingText: String = scope.strings.fetch(R.string.itin_lx_more_info_heading)
        override val subheadingText: String? = scope.strings.fetch(R.string.itin_lx_more_info_subheading)
        override val cardClickListener: () -> Unit = {
            val itin = scope.itinLxRepo.liveDataItin.value!!
            itin.tripId?.let { tripId ->
                scope.activityLauncher.launchActivity(LxItinMoreHelpActivity, tripId)
                scope.tripsTracking.trackItinLxMoreHelpClicked()
            }
        }
    }

    class ItinLxPriceSummaryCardViewModel<S>(scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider, S : HasWebViewLauncher, S : HasLxRepo {
        override val iconImage: Int = R.drawable.ic_itin_credit_card_icon
        override val headingText: String = scope.strings.fetch(R.string.itin_hotel_details_price_summary_heading)
        override val subheadingText: String? = null
        override val cardClickListener = {
            val itin = scope.itinLxRepo.liveDataItin.value!!
            val isGuest = itin.isGuest
            if (!itin.webDetailsURL.isNullOrEmpty() && !itin.tripId.isNullOrEmpty()) {
                itin.tripId?.let { tripId ->
                    scope.webViewLauncher.launchWebViewActivity(R.string.itin_hotel_details_price_summary_heading, itin.webDetailsURL!!, "price", tripId, isGuest = isGuest)
                }
            }
        }
    }

    class ItinLxAdditionalInfoCardViewModel<S>(scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider, S : HasWebViewLauncher, S : HasLxRepo {
        override val iconImage: Int = R.drawable.ic_itin_additional_info_icon
        override val headingText: String = scope.strings.fetch(R.string.itin_hotel_details_additional_info_heading)
        override val subheadingText: String? = null
        override val cardClickListener = {
            val itin = scope.itinLxRepo.liveDataItin.value!!
            val isGuest = itin.isGuest
            if (!itin.webDetailsURL.isNullOrEmpty() && !itin.tripId.isNullOrEmpty()) {
                itin.tripId?.let { tripId ->
                    scope.webViewLauncher.launchWebViewActivity(R.string.itin_hotel_details_additional_info_heading, itin.webDetailsURL!!, null, tripId, isGuest = isGuest)
                }
            }
        }
    }
}
