package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.presenter.BaseErrorPresenter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.packages.FlightErrorViewModel

class FlightErrorPresenter(context: Context, attr: AttributeSet?) : BaseErrorPresenter(context, attr) {

    var viewmodel: FlightErrorViewModel by notNullAndObservable { vm ->
        vm.imageObservable.subscribe { errorImage.setImageResource(it) }
        vm.buttonTextObservable.subscribeText(errorButton)
        vm.errorMessageObservable.subscribeText(errorText)
        vm.titleObservable.subscribe { standardToolbar.title = it }
        vm.subTitleObservable.subscribe { standardToolbar.subtitle = it }
        errorButton.setOnClickListener { vm.errorDismissedObservable.onNext(Unit) }
    }

    init {
        standardToolbar.setNavigationOnClickListener {
            viewmodel.errorDismissedObservable.onNext(Unit)
        }
    }

    override fun back(): Boolean {
        viewmodel.errorDismissedObservable.onNext(Unit)
        return true
    }
}