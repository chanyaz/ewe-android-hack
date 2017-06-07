package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.tracking.RailTracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.StrUtils
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

abstract class BaseTravelerPickerViewModel(var context: Context) {
    val adultTextObservable = BehaviorSubject.create<String>()
    val childTextObservable = BehaviorSubject.create<String>()
    val travelerParamsObservable = BehaviorSubject.create(TravelerParams(1, emptyList(), emptyList(), emptyList()))
    val guestsTextObservable = BehaviorSubject.create<CharSequence>()
    val isInfantInLapObservable = BehaviorSubject.create<Boolean>(false)
    val infantInSeatObservable = PublishSubject.create<Unit>()
    var showSeatingPreference = false
    var lob = LineOfBusiness.HOTELS
        set(value) {
            field = value
            val travelers = travelerParamsObservable.value
            makeTravelerText(travelers)
        }
    val adultPlusObservable = BehaviorSubject.create<Boolean>()
    val adultMinusObservable = BehaviorSubject.create<Boolean>()
    val childPlusObservable = BehaviorSubject.create<Boolean>()
    val childMinusObservable = BehaviorSubject.create<Boolean>()
    val adultTravelerCountChangeObservable = BehaviorSubject.create<Unit>()
    val childTravelerCountChangeObservable = BehaviorSubject.create<Unit>()
    val travelerSelectedObservable = PublishSubject.create<Unit>()

    init {
        travelerParamsObservable.subscribe { travelers ->
            makeTravelerText(travelers)
            adultTextObservable.onNext(
                    context.resources.getQuantityString(R.plurals.number_of_adults,
                            travelers.numberOfAdults, travelers.numberOfAdults)
            )

            childTextObservable.onNext(context.resources.getQuantityString(R.plurals.number_of_children,
                    travelers.childrenAges.size, travelers.childrenAges.size))

        }
    }

    open fun makeTravelerText(travelers: TravelerParams) {
        val total = travelers.numberOfAdults + travelers.childrenAges.size
        guestsTextObservable.onNext(
                if (lob == LineOfBusiness.PACKAGES || lob == LineOfBusiness.FLIGHTS_V2) {
                    StrUtils.formatTravelerString(context, total)
                } else {
                    StrUtils.formatGuestString(context, total)
                }
        )
    }

    open fun trackTravelerPickerClick(actionLabel: String) {
        when (lob) {
            LineOfBusiness.PACKAGES -> {
                PackagesTracking().trackSearchTravelerPickerChooserClick(actionLabel)
            }

            LineOfBusiness.HOTELS -> {
                HotelTracking.trackTravelerPickerClick(actionLabel)
            }

            LineOfBusiness.FLIGHTS_V2 -> {
                FlightsV2Tracking.trackTravelerPickerClick(actionLabel)
            }

            LineOfBusiness.RAILS -> {
                RailTracking().trackRailSearchTravelerPickerChooser(actionLabel)
            }

            else -> { // required to satisfy kotlin codestyle check
                // do nothing
            }
        }
    }
}
