package com.expedia.vm

import android.content.Context
import android.location.Location
import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.SuggestionV4Utils
import rx.Observable

class CarSuggestionAdapterViewModel(context: Context, suggestionsService: SuggestionV4Services, locationObservable: Observable<Location>?, shouldShowCurrentLocation: Boolean, rawQueryEnabled: Boolean) : SuggestionAdapterViewModel(context, suggestionsService, locationObservable, shouldShowCurrentLocation, rawQueryEnabled) {
    override fun getSuggestionService(query: String) {
        suggestionsService.getCarSuggestionsV4(query, generateSuggestionServiceCallback())
    }

    override fun getSuggestionHistoryFile(): String {
        return SuggestionV4Utils.RECENT_CAR_SUGGESTIONS_FILE
    }

    override fun getLineOfBusinessForGaia(): String {
        return "cars"
    }

    override fun getNearbySortTypeForGaia(): String {
        return "popularity"
    }
}