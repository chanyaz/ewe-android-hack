package com.expedia.bookings.itin.hotel.taxi

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinTaxiViewModel
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.tripstore.data.ItinHotel

class HotelItinTaxiViewModel<out S>(val scope: S) : ItinTaxiViewModel() where S : HasHotelRepo {
    var observer: LiveDataObserver<ItinHotel>

    init {
        observer = LiveDataObserver { hotel ->
            val nonLocalizedHotelName = hotel?.hotelPropertyInfo?.name
            val localizedHotelName = hotel?.localizedHotelPropertyInfo?.name
            val localizedHotelAddress = hotel?.localizedHotelPropertyInfo?.address?.fullAddress
            val nonLocalizedHotelAddress = hotel?.hotelPropertyInfo?.address?.fullAddress
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
}
