package com.expedia.bookings.shared.util

import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.data.GaiaSuggestionRequest
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.utils.SuggestionV4Utils
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class GaiaNearbyManager(private val suggestionsService: ISuggestionV4Services) {
    val suggestionsSubject = PublishSubject.create<List<SuggestionV4>>()
    val errorSubject = PublishSubject.create<Unit>()

    fun nearBySuggestions(request: GaiaSuggestionRequest) {
        val location = request.location
        suggestionsService.suggestNearbyGaia(location.latitude, location.longitude,
                request.sortType, request.lob, PointOfSale.getSuggestLocaleIdentifier(),
                PointOfSale.getPointOfSale().siteId, request.misForRealWorldEnabled).subscribe(nearByObserver)
    }

    private val nearByObserver = object: DisposableObserver<List<GaiaSuggestion>>() {
        override fun onError(e: Throwable) {
            errorSubject.onNext(Unit)
        }

        override fun onComplete() {}

        override fun onNext(gaiaSuggestions: List<GaiaSuggestion>) {
            if (gaiaSuggestions == null || gaiaSuggestions.isEmpty()) {
                errorSubject.onNext(Unit)
            } else {
                suggestionsSubject.onNext(SuggestionV4Utils.convertToSuggestionV4(gaiaSuggestions))
            }
        }
    }
}
