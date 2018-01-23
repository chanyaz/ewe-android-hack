package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailProduct
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

class RailFareRulesViewModel(val context: Context) {
    val railProductObservable = PublishSubject.create<RailProduct>()

    //outputs
    val fareInfoObservable = PublishSubject.create<String>()
    val fareRulesObservable = PublishSubject.create<List<String>>()
    val noFareRulesObservable = PublishSubject.create<Boolean>()

    init {
        railProductObservable.subscribe { railProduct ->
            fareInfoObservable.onNext(formatFareInfo(railProduct))
            val fareRules = getFareRulesList(railProduct)
            fareRulesObservable.onNext(fareRules)
            noFareRulesObservable.onNext(fareRules.isEmpty())
        }
    }

    private fun getFareRulesList(product: RailProduct?): List<String> {
        val fareRules = ArrayList<String>()
        val description = product?.aggregatedFareDescription
        if (description != null) fareRules.add(description)
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
