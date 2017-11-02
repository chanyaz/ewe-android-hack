package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class WebCheckoutViewViewModel(context: Context) : WebViewViewModel(), UserAccountRefresher.IUserAccountRefreshListener  {
    // var so that we can mock it for unit testing
    var userAccountRefresher: UserAccountRefresher = UserAccountRefresher(context, LineOfBusiness.PROFILE, this)

    val bookedTripIDObservable = BehaviorSubject.create<String>()
    val fetchItinObservable = PublishSubject.create<String>()
    val closeView = PublishSubject.create<Unit>()
    val showLoadingObservable = PublishSubject.create<Unit>()

    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    abstract fun doCreateTrip()

    override fun onUserAccountRefreshed() {
        val user = userStateManager.userSource.user
        userStateManager.addUserToAccountManager(user)

        val bookTripId = bookedTripIDObservable.value
        if (Strings.isNotEmpty(bookTripId)) {
            fetchItinObservable.onNext(bookTripId)
        } else {
            closeView.onNext(Unit)
        }
    }

}