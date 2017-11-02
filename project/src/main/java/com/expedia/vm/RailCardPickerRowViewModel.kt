package com.expedia.vm

import com.expedia.bookings.ObservableOld
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.data.rail.responses.RailCardSelected
import io.reactivex.subjects.PublishSubject

class RailCardPickerRowViewModel(val rowId: Int) {
    var cardTypesList = PublishSubject.create<List<RailCard>>()

    val cardTypeSelected = PublishSubject.create<RailCard>()
    val cardQuantitySelected = PublishSubject.create<Int>()

    val cardTypeQuantityChanged = ObservableOld.combineLatest(cardTypeSelected, cardQuantitySelected, { cardTypeSelected, cardQuantitySelected ->
        RailCardSelected(rowId, cardTypeSelected, cardQuantitySelected)
    })

    val resetRow = PublishSubject.create<Unit>()
}