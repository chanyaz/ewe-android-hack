package com.expedia.vm

import android.content.Context
import android.location.Location
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.SuggestionV4Utils
import rx.Observable

class LXSuggestionAdapterViewModel(context: Context, suggestionsService: SuggestionV4Services, locationObservable: Observable<Location>?, shouldShowCurrentLocation: Boolean, rawQueryEnabled: Boolean) : SuggestionAdapterViewModel(context, suggestionsService, locationObservable, shouldShowCurrentLocation, rawQueryEnabled) {
    override fun getSuggestionService(query: String) {
        suggestionsService.getLxSuggestionsV4(query, ServicesUtil.generateClient(context), generateSuggestionServiceCallback(), PointOfSale.getSuggestLocaleIdentifier(), AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppLXDisablePOISearch))
    }

    override fun getSuggestionHistoryFile(): String {
        return SuggestionV4Utils.RECENT_LX_SUGGESTIONS_FILE
    }

    override fun getLineOfBusinessForGaia(): String {
        return "lx"
    }

    override fun getNearbySortTypeForGaia(): String {
        return "distance"
    }
}