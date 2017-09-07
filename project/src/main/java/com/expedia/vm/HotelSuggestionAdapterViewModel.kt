package com.expedia.vm

import android.content.Context
import android.location.Location
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.SuggestionV4Utils
import rx.Observable

class HotelSuggestionAdapterViewModel(context: Context, suggestionsService: SuggestionV4Services, locationObservable: Observable<Location>?, shouldShowCurrentLocation: Boolean, rawQueryEnabled: Boolean) : SuggestionAdapterViewModel(context, suggestionsService, locationObservable, shouldShowCurrentLocation, rawQueryEnabled) {
   private var selectedSuggestion: SuggestionV4? = null

    init {
        suggestionSelectedSubject.subscribe { searchSuggestion ->
            selectedSuggestion = searchSuggestion.suggestionV4
        }
    }

    fun getLastSelectedSuggestion() : SuggestionV4? {
        return selectedSuggestion
    }

    override fun getSuggestionService(query: String) {
        val sameAsWeb = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelAutoSuggestSameAsWeb)
        val guid: String? = if (sameAsWeb) Db.getAbacusGuid() else null
        suggestionsService.getHotelSuggestionsV4(query, ServicesUtil.generateClient(context), generateSuggestionServiceCallback(), PointOfSale.getSuggestLocaleIdentifier(), sameAsWeb, guid)
    }

    override fun getSuggestionHistoryFile(): String {
        return SuggestionV4Utils.RECENT_HOTEL_SUGGESTIONS_FILE
    }

    override fun getLineOfBusinessForGaia(): String {
        return "hotels"
    }

    override fun getNearbySortTypeForGaia(): String {
        return "distance"
    }
}