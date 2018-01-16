package com.expedia.vm

import android.content.Context
import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.SuggestionV4Utils
import rx.Observable

class HotelSuggestionAdapterViewModel(context: Context, suggestionsService: ISuggestionV4Services, locationObservable: Observable<Location>?, shouldShowCurrentLocation: Boolean, rawQueryEnabled: Boolean) :
        SuggestionAdapterViewModel(context, suggestionsService, locationObservable, shouldShowCurrentLocation, rawQueryEnabled) {
    private var selectedSuggestion: SuggestionV4? = null

    init {
        suggestionSelectedSubject.subscribe { searchSuggestion ->
            selectedSuggestion = searchSuggestion.suggestionV4
        }
    }

    fun getLastSelectedSuggestion(): SuggestionV4? {
        return selectedSuggestion
    }

    override fun getSuggestionService(query: String) {
        val sameAsWeb = AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.HotelAutoSuggestSameAsWeb)
        val guid: String? = if (sameAsWeb) Db.sharedInstance.abacusGuid else null

        suggestionsService.getHotelSuggestionsV4(query, generateSuggestionServiceCallback(), sameAsWeb, guid)
    }

    override fun getSuggestionHistoryFile(): String = SuggestionV4Utils.RECENT_HOTEL_SUGGESTIONS_FILE

    override fun getLineOfBusinessForGaia(): String = "hotels"

    override fun getNearbySortTypeForGaia(): String = "distance"

    override fun isSearchHistorySupported(): Boolean =
            FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_user_search_history)

}
