package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.itin.common.ItinManageBookingWidgetViewModel
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasWebViewLauncher

class CarItinManageBookingWidgetViewModel<S>(scope: S) : ItinManageBookingWidgetViewModel() where S : HasStringProvider, S : HasWebViewLauncher, S : HasActivityLauncher, S : HasCarRepo {
    override val moreHelpViewModel: ItinBookingInfoCardViewModel
    override val priceSummaryViewModel: ItinBookingInfoCardViewModel
    override val additionalInfoViewModel: ItinBookingInfoCardViewModel

    init {
        moreHelpViewModel = CarItinMoreHelpCardViewModel(scope)
        priceSummaryViewModel = CarItinLxPriceSummaryCardViewModel(scope)
        additionalInfoViewModel = CarItinLxAdditionalInfoCardViewModel(scope)
    }

    class CarItinMoreHelpCardViewModel<S>(scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider {
        override val iconImage: Int = R.drawable.ic_itin_manage_booking_icon
        override val headingText: String = scope.strings.fetch(R.string.itin_lx_more_info_heading)
        override val subheadingText: String? = scope.strings.fetch(R.string.itin_lx_more_info_subheading)
        override val cardClickListener: () -> Unit = {
            TODO("not implemented") //add native itin manage booking activity here
        }
    }

    class CarItinLxPriceSummaryCardViewModel<S>(scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider, S : HasWebViewLauncher, S : HasCarRepo {
        override val iconImage: Int = R.drawable.ic_itin_credit_card_icon
        override val headingText: String = scope.strings.fetch(R.string.itin_hotel_details_price_summary_heading)
        override val subheadingText: String? = null
        override val cardClickListener = {
            val itin = scope.itinCarRepo.liveDataItin.value
            if (itin != null) {
                val isGuest = itin.isGuest
                if (!itin.webDetailsURL.isNullOrEmpty() && !itin.tripId.isNullOrEmpty()) {
                    scope.webViewLauncher.launchWebViewActivity(R.string.itin_hotel_details_price_summary_heading, itin.webDetailsURL!!, "price", itin.tripId!!, isGuest = isGuest)
                }
            }
        }
    }

    class CarItinLxAdditionalInfoCardViewModel<S>(scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider, S : HasWebViewLauncher, S : HasCarRepo {
        override val iconImage: Int = R.drawable.ic_itin_additional_info_icon
        override val headingText: String = scope.strings.fetch(R.string.itin_hotel_details_additional_info_heading)
        override val subheadingText: String? = null
        override val cardClickListener = {
            val itin = scope.itinCarRepo.liveDataItin.value
            if (itin != null) {
                val isGuest = itin.isGuest
                if (!itin.webDetailsURL.isNullOrEmpty() && !itin.tripId.isNullOrEmpty()) {
                    scope.webViewLauncher.launchWebViewActivity(R.string.itin_hotel_details_additional_info_heading, itin.webDetailsURL!!, null, itin.tripId!!, isGuest = isGuest)
                }
            }
        }
    }
}
