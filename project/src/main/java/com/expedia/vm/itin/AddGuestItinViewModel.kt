package com.expedia.vm.itin

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.ItinDetailsResponse
import com.expedia.bookings.section.CommonSectionValidators
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.google.gson.Gson
import com.mobiata.android.validation.ValidationError
import com.squareup.phrase.Phrase
import retrofit2.adapter.rxjava.HttpException
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
    val showErrorObservable = PublishSubject.create<Boolean>()
    val showErrorMessageObservable = PublishSubject.create<String>()

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
            showErrorObservable.onNext(false)
        }).subscribe()
    }

    fun makeGuestTripResponseObserver(): Observer<AbstractItinDetailsResponse> {
        return object : Observer<AbstractItinDetailsResponse> {
            override fun onCompleted() {
                showSearchDialogObservable.onNext(false)
            }

            override fun onError(errorThrowable: Throwable?) {
                showSearchDialogObservable.onNext(false)
                if (errorThrowable is HttpException) {
                    var errorString = errorThrowable.response().errorBody().string()
                    val gson = Gson()
                    val response = gson.fromJson(errorString, ItinDetailsResponse::class.java)
                    if (response?.errors?.size!! > 0) {
                        OmnitureTracking.trackItinError()
                        showErrorObservable.onNext(true)
                        showErrorMessageObservable.onNext(
                                if (response.errors[0].isNotAuthenticatedError)
                                    Phrase.from(context.getString(R.string.unable_to_find_registered_user_itinerary_template))
                                            .put("brand", BuildConfig.brand).format().toString()
                                else
                                    context.getString(R.string.unable_to_find_guest_itinerary))
                    }
                }
                else {
                    errorThrowable?.printStackTrace()
                }
            }

            override fun onNext(response: AbstractItinDetailsResponse?) {
            }
        }
    }
}