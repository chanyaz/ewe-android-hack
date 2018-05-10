package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.scopes.PriceSummaryCardScope
import com.expedia.bookings.itin.scopes.StringsActivityScope

class LxItinManageBookingWidgetViewModel<S>(scope: S) where S : HasStringProvider, S : HasWebViewLauncher, S : HasActivityLauncher, S : HasLxRepo {

    val moreHelpViewModel: ItinMoreHelpCardViewModel<StringsActivityScope>
    val priceSummaryViewModel: ItinLxPriceSummaryCardViewModel<PriceSummaryCardScope>
    val additionalInfoViewModel: ItinLxAdditionalInfoCardViewModel<PriceSummaryCardScope>

    init {
        val stringsWebViewScope = PriceSummaryCardScope(scope.strings, scope.webViewLauncher, scope.itinLxRepo)
        val stringsActivityScope = StringsActivityScope(scope.strings, scope.activityLauncher)
        moreHelpViewModel = ItinMoreHelpCardViewModel(stringsActivityScope)
        priceSummaryViewModel = ItinLxPriceSummaryCardViewModel(stringsWebViewScope)
        additionalInfoViewModel = ItinLxAdditionalInfoCardViewModel(stringsWebViewScope)
    }

    class ItinMoreHelpCardViewModel<S>(scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider {
        override val iconImage: Int = R.drawable.ic_itin_manage_booking_icon
        override val headingText: String = scope.strings.fetch(R.string.itin_lx_more_info_heading)
        override val subheadingText: String? = scope.strings.fetch(R.string.itin_lx_more_info_subheading)
        override val cardClickListener: () -> Unit = {
            TODO("not implemented") //add native itin manage booking activity here
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
                scope.webViewLauncher.launchWebViewActivity(R.string.itin_hotel_details_price_summary_heading, itin.webDetailsURL!!, "price", itin.tripId!!, isGuest = isGuest)
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
                scope.webViewLauncher.launchWebViewActivity(R.string.itin_hotel_details_additional_info_heading, itin.webDetailsURL!!, null, itin.tripId!!, isGuest = isGuest)
            }
        }
    }
}
