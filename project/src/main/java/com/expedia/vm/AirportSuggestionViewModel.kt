package com.expedia.vm

import android.content.Context
import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.SuggestionV4Utils
import rx.Observable

class AirportSuggestionViewModel(context: Context, suggestionsService: SuggestionV4Services, private val isDest: Boolean, locationObservable: Observable<Location>?) : SuggestionAdapterViewModel(context, suggestionsService, locationObservable, false, false) {

    private val showSuggestionLabel: Boolean = AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppFlightSearchSuggestionLabel)

    override fun getSuggestionService(query: String) {
        suggestionsService.getAirports(query, isDest, generateSuggestionServiceCallback(), Db.sharedInstance.abacusGuid)
    }

    override fun getSuggestionHistoryFile(): String = SuggestionV4Utils.RECENT_AIRPORT_SUGGESTIONS_FILE

    override fun getLineOfBusinessForGaia(): String = "flights"

    override fun getNearbySortTypeForGaia(): String = "popularity"

    override fun getCurrentLocationLabel(): String =
            context.getString(R.string.flight_search_suggestion_label_airport_near)

    override fun getPastSuggestionsLabel(): String =
            context.getString(R.string.flight_search_suggestion_label_recent_search)

    override fun getLineOfBusiness(): LineOfBusiness = LineOfBusiness.FLIGHTS_V2

    override fun showSuggestionsAndLabel(): Boolean =
            (getLineOfBusiness() == LineOfBusiness.FLIGHTS_V2 && showSuggestionLabel)

    override fun isSuggestionOnOneCharEnabled(): Boolean =
            AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightSuggestionOnOneCharacter)

}
