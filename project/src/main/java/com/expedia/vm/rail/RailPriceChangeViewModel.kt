package com.expedia.vm.rail

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.vm.PriceChangeViewModel
import io.reactivex.subjects.BehaviorSubject

class RailPriceChangeViewModel(context: Context) : PriceChangeViewModel(context, LineOfBusiness.RAILS) {
    //input
    val priceChangedObserver = BehaviorSubject.create<Unit>()

    init {
        priceChangedObserver.subscribe {
            priceChangeText.onNext(context.getString(R.string.price_changed_text))
            priceChangeDrawable.onNext(ContextCompat.getDrawable(context, R.drawable.warning_triangle_icon))
            priceChangeVisibility.onNext(true)
        }
    }
}