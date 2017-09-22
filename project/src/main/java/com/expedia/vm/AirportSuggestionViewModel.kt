package com.expedia.vm

import android.content.Context
import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.SuggestionV4Utils
import rx.Observable

class AirportSuggestionViewModel(context: Context, suggestionsService: SuggestionV4Services, val isDest: Boolean, locationObservable: Observable<Location>?) : SuggestionAdapterViewModel(context, suggestionsService, locationObservable, false, false) {

    val showSuggestionLabel: Boolean = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
            AbacusUtils.EBAndroidAppFlightSearchSuggestionLabel,
            R.string.preference_flight_enable_search_suggestion_label)

    override fun getSuggestionService(query: String) {
        suggestionsService.getAirports(query, PointOfSale.getPointOfSale().siteId, ServicesUtil.generateClientId(context), isDest, generateSuggestionServiceCallback(), PointOfSale.getSuggestLocaleIdentifier(), Db.getAbacusGuid())
    }

    override fun getSuggestionHistoryFile(): String {
        return SuggestionV4Utils.RECENT_AIRPORT_SUGGESTIONS_FILE
    }

    override fun getLineOfBusinessForGaia(): String {
        return "flights"
    }

    override fun getNearbySortTypeForGaia(): String {
        return "popularity"
    }

    override fun getCurrentLocationLabel(): String {
        return context.getString(R.string.flight_search_suggestion_label_airport_near)
    }

    override fun getRecentSuggestionLabel(): String {
        return context.getString(R.string.flight_search_suggestion_label_recent_search)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.FLIGHTS_V2
    }

    override fun showSuggestionsAndLabel(): Boolean {
        return (getLineOfBusiness() == LineOfBusiness.FLIGHTS_V2 && showSuggestionLabel)
    }

    override fun isSuggestionOnOneCharEnabled(): Boolean {
        return AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightSuggestionOnOneCharacter)
    }

}