package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.RailUtils
import com.expedia.util.endlessObserver
import com.expedia.vm.AbstractErrorViewModel
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.PublishSubject

class RailErrorViewModel(context: Context): AbstractErrorViewModel(context) {
    val showSearch = PublishSubject.create<Unit>()
    val retrySearch = PublishSubject.create<Unit>()
    val paramsSubject = PublishSubject.create<RailSearchRequest>()

    init {
        paramsSubject.subscribe { params ->
            val title = RailUtils.getToolbarTitleFromSearchRequest(params)
            titleObservable.onNext(title)

            val subtitle = RailUtils.getToolbarSubtitleFromSearchRequest(context, params)
            subTitleObservable.onNext(subtitle)
        }
    }

    override fun searchErrorHandler(): Observer<ApiError> {
        return endlessObserver { error ->
            when (error.errorCode) {
                ApiError.Code.RAIL_SEARCH_NO_RESULTS -> {
                    imageObservable.onNext(R.drawable.error_search)
                    errorMessageObservable.onNext(context.getString(R.string.error_no_result_message))
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_search))
                    subscribeActionToButtonPress(showSearch)
                    clickBack.subscribe(errorButtonClickedObservable)
                }
                else -> {
                    makeDefaultError()
                    subscribeActionToButtonPress(retrySearch)
                    clickBack.subscribe {
                        defaultErrorObservable.onNext(Unit)
                    }
                }
            }
        }
    }

    override fun createTripErrorHandler(): Observer<ApiError> {
        return endlessObserver {  }
    }

    override fun checkoutApiErrorHandler(): Observer<ApiError> {
        return endlessObserver {  }
    }
}