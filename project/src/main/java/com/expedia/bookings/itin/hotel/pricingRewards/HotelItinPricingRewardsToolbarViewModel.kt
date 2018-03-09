package com.expedia.bookings.itin.hotel.pricingRewards

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinToolbarViewModel
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.ItinHotel

class HotelItinPricingRewardsToolbarViewModel<S>(val scope: S) : ItinToolbarViewModel() where S : HasLifecycleOwner, S : HasStringProvider, S : HasHotelRepo {

    override fun updateWidget(toolbarParams: ToolbarParams) {}

    var observer: LiveDataObserver<ItinHotel>

    init {
        observer = LiveDataObserver<ItinHotel> { hotel ->
            val stringProvider = scope.strings
            val title = stringProvider.fetch(R.string.itin_hotel_price_reward_title)
            toolbarTitleSubject.onNext(title)
            val subTitle = hotel?.hotelPropertyInfo?.name
            if (!subTitle.isNullOrEmpty())
                toolbarSubTitleSubject.onNext(subTitle!!)
        }
        scope.itinHotelRepo.liveDataHotel.observe(scope.lifecycleOwner, observer)
    }
}
