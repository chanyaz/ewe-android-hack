package com.expedia.bookings.lx.vm

import android.content.Context
import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.islXEssRegionTypeCallEnabled
import com.expedia.vm.BaseSuggestionAdapterViewModel
import io.reactivex.Observable

class LXSuggestionAdapterViewModel(context: Context, suggestionsService: ISuggestionV4Services, locationObservable: Observable<Location>?, shouldShowCurrentLocation: Boolean, rawQueryEnabled: Boolean) : BaseSuggestionAdapterViewModel(context, suggestionsService, locationObservable, shouldShowCurrentLocation, rawQueryEnabled) {
    override fun getSuggestionService(query: String) {
        suggestionsService.getLxSuggestionsV4(query, generateSuggestionServiceCallback(),
                AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppLXDisablePOISearch), islXEssRegionTypeCallEnabled())
    }

    override fun getSuggestionHistoryFile(): String = SuggestionV4Utils.RECENT_LX_SUGGESTIONS_FILE

    override fun getLineOfBusinessForGaia(): String = "lx"

    override fun getNearbySortTypeForGaia(): String = "distance"
    override fun getCurrentLocationLabel(): String = context.getString(R.string.nearby_locations)
    override fun getPastSuggestionsLabel(): String = context.getString(R.string.suggestion_label_recent_search)
}
