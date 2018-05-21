package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.rail.util.RailUtils
import com.expedia.util.endlessObserver
import com.expedia.vm.LobErrorViewModel
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject

class RailErrorViewModel(context: Context) : LobErrorViewModel(context) {
    val showSearch = PublishSubject.create<Unit>()
    val retrySearch = PublishSubject.create<Unit>()
    val showCheckoutForm = PublishSubject.create<Unit>()
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
            when (error.getErrorCode()) {
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
        return endlessObserver {
            imageObservable.onNext(R.drawable.error_default)
            errorMessageObservable.onNext(context.resources.getString(R.string.rail_unknown_error_message))
            buttonOneTextObservable.onNext(context.resources.getString(R.string.retry))
            titleObservable.onNext(context.resources.getString(R.string.rail_error_title))
            subTitleObservable.onNext("")
            subscribeActionToButtonPress(retrySearch)
            clickBack.subscribe {
                defaultErrorObservable.onNext(Unit)
            }
        }
    }

    override fun checkoutApiErrorHandler(): Observer<ApiError> {
        return endlessObserver { error ->
            imageObservable.onNext(R.drawable.error_default)
            buttonOneTextObservable.onNext(context.resources.getString(R.string.edit_button))
            titleObservable.onNext(context.resources.getString(R.string.rail_error_title))
            subTitleObservable.onNext("")
            clickBack.subscribe {
                defaultErrorObservable.onNext(Unit)
            }

            when (error.getErrorCode()) {
                ApiError.Code.INVALID_INPUT, ApiError.Code.RAIL_UNKNOWN_CKO_ERROR -> {
                    errorMessageObservable.onNext(context.resources.getString(R.string.rail_cko_retry_error_message))
                    subscribeActionToButtonPress(showCheckoutForm)
                }
                else -> {
                    errorMessageObservable.onNext(context.resources.getString(R.string.rail_unknown_error_message))
                    subscribeActionToButtonPress(showSearch)
                }
            }
        }
    }
}
