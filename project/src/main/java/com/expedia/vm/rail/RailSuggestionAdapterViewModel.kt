package com.expedia.vm.rail

import android.content.Context
import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.vm.BaseSuggestionAdapterViewModel
import io.reactivex.Observable

class RailSuggestionAdapterViewModel(context: Context, suggestionsService: ISuggestionV4Services, val isDest: Boolean, locationObservable: Observable<Location>?) : BaseSuggestionAdapterViewModel(context, suggestionsService, locationObservable, false, false) {
    override fun getSuggestionService(query: String) {
        suggestionsService.suggestRailsV4(query, isDest, generateSuggestionServiceCallback())
    }

    override fun getSuggestionHistoryFile(): String = SuggestionV4Utils.RECENT_RAIL_SUGGESTIONS_FILE

    override fun getLineOfBusinessForGaia(): String = "rails"

    override fun getNearbySortTypeForGaia(): String = "distance"
    override fun getPastSuggestionsLabel(): String = context.getString(R.string.suggestion_label_recent_search)
}
