package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject

class TravelerPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val travelerEntryEntryWidget: FlightTravelerEntryWidget by bindView(R.id.traveler_entry_widget)
    val travelersCompleteSubject = BehaviorSubject.create<Traveler>()

    init {
        View.inflate(context, R.layout.traveler_presenter, this)

        travelerEntryEntryWidget.travelerCompleteSubject.subscribe (endlessObserver<Traveler> { traveler ->
            if (allTravelersComplete()) {
                travelersCompleteSubject.onNext(traveler)
            }
        })
    }

    fun allTravelersComplete(): Boolean {
        // TODO this should eventually validate that all travelers are complete.
        return true
    }
}