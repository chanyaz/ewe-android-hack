package com.expedia.bookings.model

import com.expedia.bookings.data.pos.PointOfSale
import io.reactivex.subjects.BehaviorSubject

class PointOfSaleStateModel {
    val pointOfSaleChangedSubject: BehaviorSubject<PointOfSale> = BehaviorSubject.create<PointOfSale>()
}
