package com.expedia.vm.lx

import android.content.Context
import com.expedia.bookings.utils.Ui
import com.expedia.vm.AbstractCheckoutViewModel
import io.reactivex.subjects.PublishSubject

class LXCheckoutViewModel(context: Context) : AbstractCheckoutViewModel(context) {

    val hideOverviewSummaryObservable = PublishSubject.create<Boolean>()
    val backToDetailsObservable = PublishSubject.create<Unit>()

    override fun injectComponents() {
        Ui.getApplication(context).lxComponent().inject(this)
    }

    override fun getTripId(): String {
        //TODO
        return ""
    }
}
