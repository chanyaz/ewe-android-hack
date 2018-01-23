package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.ViewGroup
import io.reactivex.subjects.BehaviorSubject

abstract class AccessibleCardView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    abstract fun contentDescription(): String
    abstract fun selectedCardContentDescription(): String
    abstract fun loadingContentDescription(): String
    abstract fun disabledContentDescription(): String
    abstract fun getRowInfoContainer(): ViewGroup
    var loadingStateObservable = BehaviorSubject.create<Boolean>()
    var disabledStateObservable = BehaviorSubject.create<Unit>()
    val selectedCardObservable = BehaviorSubject.create<Unit>()

    init {
        loadingStateObservable.subscribe { isLoading ->
            if (isLoading) {
                getRowInfoContainer().contentDescription = loadingContentDescription()
            } else {
                getRowInfoContainer().contentDescription = contentDescription()
            }
        }
        selectedCardObservable.subscribe {
            getRowInfoContainer().contentDescription = selectedCardContentDescription()
        }
        disabledStateObservable.subscribe {
            getRowInfoContainer().contentDescription = disabledContentDescription()
        }
    }
}
