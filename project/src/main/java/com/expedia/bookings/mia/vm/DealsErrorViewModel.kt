package com.expedia.bookings.mia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.vm.BaseErrorViewModel
import io.reactivex.subjects.PublishSubject

class DealsErrorViewModel(context: Context) : BaseErrorViewModel(context) {

    val showLaunchScreen = PublishSubject.create<Unit>()

    init {
        imageObservable.onNext(R.drawable.error_default)
        buttonOneTextObservable.onNext(context.getString(R.string.deal_error_go_back))
        errorMessageObservable.onNext(context.getString(R.string.deal_error_no_result_message))
        subscribeActionToButtonPress(showLaunchScreen)
    }
}
