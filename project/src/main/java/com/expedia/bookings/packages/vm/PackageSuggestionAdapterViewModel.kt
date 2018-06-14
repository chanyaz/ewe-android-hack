package com.expedia.bookings.packages.vm

import android.content.Context
import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.vm.BaseSuggestionAdapterViewModel
import io.reactivex.Observable

class PackageSuggestionAdapterViewModel(context: Context, suggestionsService: ISuggestionV4Services, val isDest: Boolean, locationObservable: Observable<Location>?) : BaseSuggestionAdapterViewModel(context, suggestionsService, locationObservable, false, false) {
    override fun getSuggestionService(query: String) {
        val guid = Db.sharedInstance.getAbacusGuid()
        suggestionsService.suggestPackagesV4(query, isDest, generateSuggestionServiceCallback(), guid)
    }

    override fun getSuggestionHistoryFile(): String {
        if (isDest) {
            return SuggestionV4Utils.RECENT_PACKAGE_ARRIVAL_SUGGESTIONS_FILE
        } else {
            return SuggestionV4Utils.RECENT_PACKAGE_DEPARTURE_SUGGESTIONS_FILE
        }
    }

    override fun getLineOfBusinessForGaia(): String = "packages"

    override fun getNearbySortTypeForGaia(): String = "popularity"

    override fun isMISForRealWorldEnabled(): Boolean = true
    override fun getCurrentLocationLabel(): String = context.getString(R.string.flight_search_suggestion_label_airport_near)
    override fun getPastSuggestionsLabel(): String = context.getString(R.string.suggestion_label_recent_search)
    override fun isSuggestionOnOneCharEnabled(): Boolean = true
}
