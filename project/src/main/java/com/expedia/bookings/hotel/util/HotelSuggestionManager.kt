package com.expedia.bookings.hotel.util

import android.content.Context
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.SuggestionV4Services
import com.mobiata.android.Log
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

open class HotelSuggestionManager(private val service: SuggestionV4Services) {

    val suggestionReturnSubject = PublishSubject.create<SuggestionV4>()
    val errorSubject = PublishSubject.create<Unit>()

    open fun fetchHotelSuggestions(context: Context, regionName: String) {
        service.getHotelSuggestionsV4(regionName, getSuggestionServiceCallback())
    }

    fun getSuggestionServiceCallback(): Observer<List<SuggestionV4>> {
        return object : DisposableObserver<List<SuggestionV4>>() {
            override fun onNext(essSuggestions: List<SuggestionV4>) {
                suggestionReturnSubject.onNext(essSuggestions.first())
            }

            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
                errorSubject.onNext(Unit)
                Log.e("Hotel Suggestions Error", e)
            }
        }
    }
}
