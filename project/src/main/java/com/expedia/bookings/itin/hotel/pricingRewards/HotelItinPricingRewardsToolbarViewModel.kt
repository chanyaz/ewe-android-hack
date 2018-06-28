package com.expedia.bookings.itin.hotel.pricingRewards

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinToolbarViewModel
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstHotel

class HotelItinPricingRewardsToolbarViewModel<S>(val scope: S) : ItinToolbarViewModel() where S : HasLifecycleOwner, S : HasStringProvider, S : HasItinRepo {

    override fun updateWidget(toolbarParams: ToolbarParams) {}

    val observer: LiveDataObserver<Itin> = LiveDataObserver { itin ->
        val stringProvider = scope.strings
        val title = stringProvider.fetch(R.string.itin_hotel_price_reward_title)
        toolbarTitleSubject.onNext(title)
        val subTitle = itin?.firstHotel()?.hotelPropertyInfo?.name
        if (!subTitle.isNullOrEmpty())
            toolbarSubTitleSubject.onNext(subTitle!!)
    }

    init {
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, observer)
    }
}
