package com.expedia.vm.rail

import android.content.Context
import android.location.Location
import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.vm.SuggestionAdapterViewModel
import rx.Observable

class RailSuggestionAdapterViewModel(context: Context, suggestionsService: SuggestionV4Services, val isDest: Boolean, locationObservable: Observable<Location>?) : SuggestionAdapterViewModel(context, suggestionsService, locationObservable, false, false) {
    override fun getSuggestionService(query: String) {
        suggestionsService.suggestRailsV4(query, PointOfSale.getPointOfSale().siteId, ServicesUtil.generateClient(context), isDest, generateSuggestionServiceCallback(), PointOfSale.getSuggestLocaleIdentifier())
    }

    override fun getSuggestionHistoryFile(): String {
        return SuggestionV4Utils.RECENT_RAIL_SUGGESTIONS_FILE
    }

    override fun getLineOfBusiness(): String {
        return "RAILS"
    }

    override fun getNearbyRegionType(): Int {
        return SuggestionResultType.MULTI_CITY
    }

    override fun getNearbySortType(): String {
        return "distance"
    }
}