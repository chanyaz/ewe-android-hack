package com.expedia.vm.itin

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.section.CommonSectionValidators
import com.mobiata.android.validation.ValidationError
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

open class AddGuestItinViewModel(val context: Context) {

    val ITIN_NUMBER_MIN_LENGTH = 9

    val showSearchDialogObservable = PublishSubject.create<Boolean>()
    val performGuestTripSearch = PublishSubject.create<Pair<String, String>>()
    val emailValidateObservable = PublishSubject.create<String>()
    val hasEmailErrorObservable = BehaviorSubject.create<Boolean>()
    val itinNumberValidateObservable = PublishSubject.create<String>()
    val hasItinErrorObservable = BehaviorSubject.create<Boolean>()
    val guestItinFetchButtonEnabledObservable = PublishSubject.create<Boolean>()
    val showErrorObservable = PublishSubject.create<Boolean>()
    val showErrorMessageObservable = PublishSubject.create<String>()
    val toolBarVisibilityObservable = PublishSubject.create<Boolean>()
    val emailFieldFocusObservable = PublishSubject.create<Unit>()
    val showItinFetchProgressObservable = PublishSubject.create<Unit>()

    init {
        performGuestTripSearch.subscribe { guestEmailItinNumPair ->
            println("Supreeth AddGuestItinViewModel showItinFetchProgressObservable")
            showItinFetchProgressObservable.onNext(Unit)
            getItinManager().addGuestTrip(guestEmailItinNumPair.first, guestEmailItinNumPair.second)
        }

        emailValidateObservable.subscribe { email ->
            var isValid = CommonSectionValidators.EMAIL_STRING_VALIDATIOR_STRICT.validate(email) == ValidationError.NO_ERROR
            hasEmailErrorObservable.onNext(!isValid)
        }

        itinNumberValidateObservable.subscribe { itinNumber ->
            var isValid = itinNumber != null && itinNumber.length >= ITIN_NUMBER_MIN_LENGTH
            hasItinErrorObservable.onNext(!isValid)
        }

        Observable.combineLatest(hasEmailErrorObservable, hasItinErrorObservable, { hasEmailError, hasItinError ->
            guestItinFetchButtonEnabledObservable.onNext(!hasEmailError && !hasItinError)
            showErrorObservable.onNext(false)
        }).subscribe()
    }


    inner class createSyncAdapter() : ItineraryManager.ItinerarySyncAdapter() {
        override fun onTripFailedFetchingGuestItinerary() {
            println("Supreeth AddGuestItinViewModel onTripFailedFetchingGuestItinerary")
            showErrorObservable.onNext(true)
            showErrorMessageObservable.onNext(context.getString(R.string.unable_to_find_guest_itinerary))
        }

        override fun onTripFailedFetchingRegisteredUserItinerary() {
            println("Supreeth AddGuestItinViewModel onTripFailedFetchingRegisteredUserItinerary")
            showErrorObservable.onNext(true)
            showErrorMessageObservable.onNext(Phrase.from(context.getString(R.string.unable_to_find_registered_user_itinerary_template))
                    .put("brand", BuildConfig.brand).format().toString())
        }
    }

    fun addItinSyncListener() {
        getItinManager().addSyncListener(createSyncAdapter())
    }

    open fun getItinManager(): ItineraryManager {
       return ItineraryManager.getInstance()
    }
}