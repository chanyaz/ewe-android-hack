package com.expedia.bookings.presenter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityEvent
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.shared.SearchInputTextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.SuggestionAdapterViewModel
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

abstract class BaseTwoLocationSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenter(context, attrs) {
    open val originCardView by bindView<SearchInputTextView>(R.id.origin_card)
    open val delayBeforeShowingDestinationSuggestions = 325L
    open val waitForOtherSuggestionListeners = 350L

    protected open var originSuggestionViewModel: SuggestionAdapterViewModel by notNullAndObservable { vm ->
        val suggestionSelectedObserver = suggestionSelectedObserver(getSearchViewModel().originLocationObserver)

        vm.suggestionSelectedSubject
                .doOnNext(suggestionSelectedObserver)
                .debounce(waitForOtherSuggestionListeners + delayBeforeShowingDestinationSuggestions, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter { destinationCardView.text.equals(getDestinationSearchBoxPlaceholderText()) }
                .subscribe {
                    showSuggestionState(selectOrigin = false)
                }
    }

    override fun performLocationClick(isCustomerSelectingOrigin: Boolean) {
        searchLocationEditText?.queryHint = if (isCustomerSelectingOrigin) getOriginSearchBoxPlaceholderText() else getDestinationSearchBoxPlaceholderText()
        super.performLocationClick(isCustomerSelectingOrigin)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        originCardView.setOnClickListener({ showSuggestionState(selectOrigin = true) })
    }

    override fun requestA11yFocus(isOrigin: Boolean) {
        var a11yFocusView: View = if(isOrigin) originCardView else destinationCardView
        a11yFocusView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
    }
}
