package com.expedia.vm.itin

import android.content.Context
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.section.CommonSectionValidators
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.utils.Ui
import com.mobiata.android.validation.ValidationError
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import javax.inject.Inject

class AddGuestItinViewModel(val context: Context) {

    val ITIN_NUMBER_MIN_LENGTH = 11

    val showSearchDialogObservable = PublishSubject.create<Boolean>()
    val performGuestTripSearch = PublishSubject.create<Pair<String, String>>()
    val emailValidateObservable = PublishSubject.create<String>()
    val hasEmailErrorObservable = BehaviorSubject.create<Boolean>()
    val itinNumberValidateObservable = PublishSubject.create<String>()
    val hasItinErrorObservable = BehaviorSubject.create<Boolean>()
    val guestItinFetchButtonEnabledObservable = PublishSubject.create<Boolean>()

    lateinit var tripServices: ItinTripServices
        @Inject set

    init {
        Ui.getApplication(context).tripComponent().inject(this)

        performGuestTripSearch.subscribe { guestEmailItinNumPair ->
            showSearchDialogObservable.onNext(true)
            tripServices.getGuestTrip(guestEmailItinNumPair.first, guestEmailItinNumPair.second, makeGuestTripResponseObserver())
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
        }).subscribe()
    }

    fun makeGuestTripResponseObserver(): Observer<AbstractItinDetailsResponse> {
        return object : Observer<AbstractItinDetailsResponse> {
            override fun onCompleted() {
                showSearchDialogObservable.onNext(false)
            }

            override fun onError(e: Throwable?) {
                e?.printStackTrace()
                showSearchDialogObservable.onNext(false)
            }

            override fun onNext(t: AbstractItinDetailsResponse?) {
            }
        }
    }
}