package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelSuggestionAdapter
import com.expedia.util.endlessObserver
import com.mobiata.android.Log
import rx.Observer
import rx.Subscription
import rx.subjects.PublishSubject
import java.util.ArrayList
import kotlin.properties.Delegates

class HotelSuggestionAdapterViewModel(val suggestionsService: SuggestionV4Services) {

    val updateSuggestionsV4Observable = PublishSubject.create<List<SuggestionV4>>()

    val hotelSearchTextObserver = endlessObserver<CharSequence>  { input ->
        val query = input?.toString() ?: ""
        if (query.isNotBlank() && query.length() >= 3) {
            suggestionsService.getHotelSuggestionsV4(query.toString(), object : Observer<List<SuggestionV4>> {
                override fun onNext(suggestions: List<SuggestionV4>?) {
                    updateSuggestionsV4Observable.onNext(suggestions)
                }

                override fun onCompleted() {
                    Log.d("Hotel Suggestions Completed")
                }

                override fun onError(e: Throwable?) {
                    Log.d("Hotel Suggestions Error", e)
                }
            })
        }
    }

}