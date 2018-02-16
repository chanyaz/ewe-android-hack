package com.expedia.vm.itin

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.section.CommonSectionValidators
import com.expedia.bookings.tracking.OmnitureTracking
import com.mobiata.android.validation.ValidationError
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

open class AddGuestItinViewModel(val context: Context) {

    val ITIN_NUMBER_MIN_LENGTH = 9

    val showSearchDialogObservable = PublishSubject.create<Boolean>()
    val performGuestTripSearch = PublishSubject.create<Pair<String, String>>()
    val emailValidateObservable = PublishSubject.create<String>()
    val hasEmailErrorObservable = BehaviorSubject.create<Boolean>()
    val itinNumberValidateObservable = PublishSubject.create<String>()
    val hasItinErrorObservable = BehaviorSubject.create<Boolean>()
    val showErrorObservable = PublishSubject.create<Boolean>()
    val showErrorMessageObservable = PublishSubject.create<String>()
    val toolBarVisibilityObservable = PublishSubject.create<Boolean>()
    val emailFieldFocusObservable = PublishSubject.create<Unit>()
    val showItinFetchProgressObservable = PublishSubject.create<Unit>()

    var guestEmail: String = ""
    var itineraryNumber: String = ""

    init {
        performGuestTripSearch.subscribe { guestEmailItinNumPair ->
            showItinFetchProgressObservable.onNext(Unit)
            getItinManager().addGuestTrip(guestEmailItinNumPair.first, guestEmailItinNumPair.second)
        }

        emailValidateObservable.subscribe { email ->
            guestEmail = email
            val isValid = isEmailValid(email)
            hasEmailErrorObservable.onNext(!isValid)
        }

        itinNumberValidateObservable.subscribe { itinNumber ->
            itineraryNumber = itinNumber
            val isValid = isItinNumberValid(itinNumber)
            hasItinErrorObservable.onNext(!isValid)
        }

        ObservableOld.combineLatest(hasEmailErrorObservable, hasItinErrorObservable, { _, _ ->
            showErrorObservable.onNext(false)
        }).subscribe()
    }

    fun isItinNumberValid(itinNumber: String?): Boolean {
        return itinNumber?.length ?: 0 >= ITIN_NUMBER_MIN_LENGTH
    }

    fun isEmailValid(email: String?): Boolean {
        return CommonSectionValidators.EMAIL_STRING_VALIDATIOR_STRICT.validate(email) == ValidationError.NO_ERROR
    }

    inner class createSyncAdapter : ItineraryManager.ItinerarySyncAdapter() {
        override fun onTripFailedFetchingGuestItinerary() {
            showErrorObservable.onNext(true)
            showErrorMessageObservable.onNext(context.getString(R.string.unable_to_find_guest_itinerary))
            OmnitureTracking.trackItinError()
        }

        override fun onTripFailedFetchingRegisteredUserItinerary() {
            showErrorObservable.onNext(true)
            showErrorMessageObservable.onNext(Phrase.from(context.getString(R.string.unable_to_find_registered_user_itinerary_template))
                    .put("brand", BuildConfig.brand).format().toString())
            OmnitureTracking.trackItinError()
        }
    }

    fun addItinSyncListener() {
        getItinManager().addSyncListener(createSyncAdapter())
    }

    open fun getItinManager(): ItineraryManager {
        return ItineraryManager.getInstance()
    }
}
