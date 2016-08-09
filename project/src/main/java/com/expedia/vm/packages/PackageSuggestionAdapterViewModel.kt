package com.expedia.vm.packages

import android.content.Context
import android.location.Location
import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.vm.SuggestionAdapterViewModel
import rx.Observable

class PackageSuggestionAdapterViewModel(context: Context, suggestionsService: SuggestionV4Services, val isDest: Boolean, locationObservable: Observable<Location>?) : SuggestionAdapterViewModel(context, suggestionsService, locationObservable, false, false) {
    override fun getSuggestionService(query: String) {
        suggestionsService.suggestPackagesV4(query, ServicesUtil.generateClient(context), isDest, generateSuggestionServiceCallback(), PointOfSale.getSuggestLocaleIdentifier())
    }

    override fun getSuggestionHistoryFile(): String {
        return SuggestionV4Utils.RECENT_PACKAGE_SUGGESTIONS_FILE
    }

    override fun shouldShowOnlyAirportNearbySuggestions(): Boolean = true

    override fun getLineOfBusiness(): String {
        return "PACKAGES"
    }

    override fun getNearbyRegionType(): Int {
        return SuggestionResultType.AIRPORT or SuggestionResultType.AIRPORT_METRO_CODE
    }

    override fun getNearbySortType(): String {
        return "distance"
    }
}