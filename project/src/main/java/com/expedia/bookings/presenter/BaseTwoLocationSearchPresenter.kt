package com.expedia.bookings.presenter

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.SearchInputCardView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.SuggestionAdapterViewModel
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

abstract class BaseTwoLocationSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenterV2(context, attrs) {

    open val originCardView by bindView<SearchInputCardView>(R.id.origin_card)

    protected var originSuggestionViewModel: SuggestionAdapterViewModel by notNullAndObservable { vm ->
        val suggestionSelectedObserver = suggestionSelectedObserver(getSearchViewModel().originLocationObserver, suggestionInputView = originCardView)
        val delayBeforeShowingDestinationSuggestions = 325L
        val waitForOtherSuggestionListeners = 350L
        vm.suggestionSelectedSubject
                .doOnNext(suggestionSelectedObserver)
                .debounce(waitForOtherSuggestionListeners + delayBeforeShowingDestinationSuggestions, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    showSuggestionState(selectOrigin = false)
                }
    }

    override fun performLocationClick(isCustomerSelectingOrigin: Boolean) {
        searchLocationEditText?.queryHint = context.resources.getString(if (isCustomerSelectingOrigin) R.string.fly_from_hint else R.string.fly_to_hint)
        super.performLocationClick(isCustomerSelectingOrigin)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        originCardView.setOnClickListener({ showSuggestionState(selectOrigin = true) })
    }
}
