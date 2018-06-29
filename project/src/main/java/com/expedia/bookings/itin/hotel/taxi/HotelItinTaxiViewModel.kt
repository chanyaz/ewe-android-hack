package com.expedia.bookings.itin.hotel.taxi

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinTaxiViewModel
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstHotel

class HotelItinTaxiViewModel<out S>(val scope: S) : ItinTaxiViewModel() where S : HasItinRepo, S : HasLifecycleOwner {
    var observer: LiveDataObserver<Itin>

    init {
        observer = LiveDataObserver { itin ->
            itin?.firstHotel()?.let { itinHotel ->
                val nonLocalizedHotelName = itinHotel.hotelPropertyInfo?.name
                val localizedHotelName = itinHotel.localizedHotelPropertyInfo?.name
                val localizedHotelAddress = itinHotel.localizedHotelPropertyInfo?.address?.fullAddress
                val nonLocalizedHotelAddress = itinHotel.hotelPropertyInfo?.address?.fullAddress
                if (localizedHotelAddress != null) {
                    localizedAddressSubject.onNext(localizedHotelAddress)
                }
                if (nonLocalizedHotelAddress != null) {
                    nonLocalizedAddressSubject.onNext(nonLocalizedHotelAddress)
                }
                if (localizedHotelName != null) {
                    localizedLocationNameSubject.onNext(localizedHotelName)
                }
                if (nonLocalizedHotelName != null) {
                    nonLocalizedLocationNameSubject.onNext(nonLocalizedHotelName)
                }
            }
        }
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, observer)
    }
}
