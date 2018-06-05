package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.itin.tripstore.data.CarLocation
import com.expedia.bookings.itin.tripstore.data.ItinCar
import com.expedia.bookings.itin.tripstore.extensions.isDropOffSame
import io.reactivex.subjects.PublishSubject

class CarItinDropOffMapWidgetViewModel(ViewModelScope: CarItinMapWidgetViewModelScope) : CarItinMapWidgetViewModel<CarItinMapWidgetViewModelScope>(ViewModelScope) {
    val showVisibilitySubject: PublishSubject<Unit> = PublishSubject.create()
    override fun getLocation(itinCar: ItinCar): CarLocation? {
        return if (!itinCar.isDropOffSame()) {
            showVisibilitySubject.onNext(Unit)
            itinCar.dropOffLocation
        } else {
            null
        }
    }
}
