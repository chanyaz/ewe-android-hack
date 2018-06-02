package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.itin.tripstore.data.CarLocation
import com.expedia.bookings.itin.tripstore.data.ItinCar

class CarItinPickupMapWidgetViewModel(ViewModelScope: CarItinMapWidgetViewModelScope) : CarItinMapWidgetViewModel<CarItinMapWidgetViewModelScope>(ViewModelScope) {
    override fun getLocation(itinCar: ItinCar): CarLocation? {
        return itinCar.pickupLocation
    }
}
