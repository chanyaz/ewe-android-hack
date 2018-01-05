package com.expedia.vm.packages

import android.content.Context
import android.location.Location
import com.expedia.bookings.data.Db
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.isPackagesMISRealWorldGeoEnabled
import com.expedia.vm.SuggestionAdapterViewModel
import rx.Observable

class PackageSuggestionAdapterViewModel(context: Context, suggestionsService: ISuggestionV4Services, val isDest: Boolean, locationObservable: Observable<Location>?) : SuggestionAdapterViewModel(context, suggestionsService, locationObservable, false, false) {
    override fun getSuggestionService(query: String) {
        val guid = Db.getAbacusGuid()
        suggestionsService.suggestPackagesV4(query, isDest, isPackagesMISRealWorldGeoEnabled(context), generateSuggestionServiceCallback(), guid)
    }

    override fun getSuggestionHistoryFile(): String = SuggestionV4Utils.RECENT_PACKAGE_SUGGESTIONS_FILE

    override fun shouldShowOnlyAirportNearbySuggestions(): Boolean = true

    override fun getLineOfBusinessForGaia(): String = "packages"

    override fun getNearbySortTypeForGaia(): String = "popularity"

    override fun isMISForRealWorldEnabled(): Boolean = isPackagesMISRealWorldGeoEnabled(context)
}
