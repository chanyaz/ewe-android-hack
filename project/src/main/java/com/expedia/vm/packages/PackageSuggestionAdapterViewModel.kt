package com.expedia.vm.packages

import android.content.Context
import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.isPackagesMISRealWorldGeoEnabled
import com.expedia.vm.BaseSuggestionAdapterViewModel
import io.reactivex.Observable

class PackageSuggestionAdapterViewModel(context: Context, suggestionsService: ISuggestionV4Services, val isDest: Boolean, locationObservable: Observable<Location>?) : BaseSuggestionAdapterViewModel(context, suggestionsService, locationObservable, false, false) {
    override fun getSuggestionService(query: String) {
        val guid = Db.sharedInstance.getAbacusGuid()
        suggestionsService.suggestPackagesV4(query, isDest, isPackagesMISRealWorldGeoEnabled(context), generateSuggestionServiceCallback(), guid)
    }

    override fun getSuggestionHistoryFile(): String {
        val isRWGEnabled = isPackagesMISRealWorldGeoEnabled(context)
        if (isDest) {
            if (isRWGEnabled) return SuggestionV4Utils.RECENT_PACKAGE_ARRIVAL_SUGGESTIONS_FILE_V2
            else return SuggestionV4Utils.RECENT_PACKAGE_ARRIVAL_SUGGESTIONS_FILE
        } else {
            if (isRWGEnabled) return SuggestionV4Utils.RECENT_PACKAGE_DEPARTURE_SUGGESTIONS_FILE_V2
            else return SuggestionV4Utils.RECENT_PACKAGE_DEPARTURE_SUGGESTIONS_FILE
        }
    }

    override fun getLineOfBusinessForGaia(): String = "packages"

    override fun getNearbySortTypeForGaia(): String = "popularity"

    override fun isMISForRealWorldEnabled(): Boolean = isPackagesMISRealWorldGeoEnabled(context)
    override fun getCurrentLocationLabel(): String = context.getString(R.string.flight_search_suggestion_label_airport_near)
    override fun getPastSuggestionsLabel(): String = context.getString(R.string.suggestion_label_recent_search)
}
