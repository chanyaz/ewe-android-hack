package com.expedia.bookings.presenter

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.SearchInputCardView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.SuggestionAdapterViewModel

abstract class BaseTwoLocationSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenterV2(context, attrs) {

    val originCardView by bindView<SearchInputCardView>(R.id.origin_card)

    protected var originSuggestionViewModel: SuggestionAdapterViewModel by notNullAndObservable { vm ->
        val suggestionSelectedObserver = suggestionSelectedObserver(getSearchViewModel().originLocationObserver, suggestionInputView = originCardView)
        vm.suggestionSelectedSubject.subscribe(suggestionSelectedObserver)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        originCardView.setOnClickListener(locationClickListener(isCustomerSelectingOrigin = true))
    }
}