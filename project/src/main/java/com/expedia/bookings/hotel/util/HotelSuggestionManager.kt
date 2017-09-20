package com.expedia.bookings.hotel.util

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.ServicesUtil
import com.mobiata.android.Log
import rx.Observer
import rx.subjects.PublishSubject

open class HotelSuggestionManager(private val service: SuggestionV4Services) {

    val suggestionReturnSubject = PublishSubject.create<SuggestionV4>()
    val errorSubject = PublishSubject.create<Unit>()

    open fun fetchHotelSuggestions(context: Context, regionName: String) {
        val sameAsWeb = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelAutoSuggestSameAsWeb)
        val guid: String? = if (sameAsWeb) Db.getAbacusGuid() else null

        service.getHotelSuggestionsV4(regionName, getSuggestionServiceCallback(), sameAsWeb, guid)
    }

    fun getSuggestionServiceCallback() : Observer<List<SuggestionV4>> {
        return object : Observer<List<SuggestionV4>> {
            override fun onNext(essSuggestions: List<SuggestionV4>) {
                suggestionReturnSubject.onNext(essSuggestions.first())
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                errorSubject.onNext(Unit)
                Log.e("Hotel Suggestions Error", e)
            }
        }
    }
}