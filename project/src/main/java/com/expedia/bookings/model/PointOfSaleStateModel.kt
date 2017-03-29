package com.expedia.bookings.model

import com.expedia.bookings.data.pos.PointOfSale
import rx.subjects.BehaviorSubject

class PointOfSaleStateModel {
    val pointOfSaleChangedSubject: BehaviorSubject<PointOfSale> = BehaviorSubject.create<PointOfSale>()
}
