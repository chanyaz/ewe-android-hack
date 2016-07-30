package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject
import java.util.ArrayList

class RailFareRulesViewModel(val context: Context) {
    val offerObservable = PublishSubject.create<RailOffer>()

    //outputs
    val fareInfoObservable = PublishSubject.create<String>()
    val fareRulesObservable = PublishSubject.create<List<String>>()
    val noFareRulesObservable = PublishSubject.create<Boolean>()

    init {
        offerObservable.subscribe { offer ->
            fareInfoObservable.onNext(formatFareInfo(offer.railProductList?.firstOrNull()))
            val fareRules = getFareRulesList(offer.railProductList?.firstOrNull())
            fareRulesObservable.onNext(fareRules)
            noFareRulesObservable.onNext(fareRules.isEmpty())
        }
    }

    private fun getFareRulesList(product: RailProduct?): List<String> {
        val fareRules = ArrayList<String>()
        fareRules.addAll(product?.fareNotes.orEmpty())
        fareRules.addAll(product?.refundableRules.orEmpty())

        return fareRules
    }

    private fun formatFareInfo(product: RailProduct?): String {
        val formattedFareInfo = Phrase.from(context, R.string.rail_fare_info_TEMPLATE)
                .put("serviceclass", product?.aggregatedCarrierServiceClassDisplayName ?: "")
                .put("fareclass", product?.aggregatedCarrierFareClassDisplayName ?: "")
                .format().toString()
        return formattedFareInfo
    }


}