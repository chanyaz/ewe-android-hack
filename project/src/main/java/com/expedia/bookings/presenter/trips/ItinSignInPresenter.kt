package com.expedia.bookings.presenter.trips

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView

class ItinSignInPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr) {
    val signInWidget: ItinSignInWidget by bindView(R.id.sign_in_widget)
    val addGuestItinWidget: AddGuestItinWidget by bindView(R.id.add_guest_itin_widget)

    private val defaultTransition = object : Presenter.DefaultTransition(ItinSignInWidget::class.java.name) {
        override fun endTransition(forward: Boolean) {
            signInWidget.visibility = View.VISIBLE
        }
    }

    private val signInToAddGuestTransition = object : Transition(ItinSignInWidget::class.java, AddGuestItinWidget::class.java, DecelerateInterpolator(), 400) {
        override fun endTransition(forward: Boolean) {
            signInWidget.visibility = if (forward) View.GONE else View.VISIBLE
            addGuestItinWidget.visibility = if (forward) View.VISIBLE else View.GONE
        }
    }

    init {
        View.inflate(context, R.layout.itin_sign_in_presenter, this)
        addDefaultTransition(defaultTransition)
        addTransition(signInToAddGuestTransition)
        show(signInWidget)
        signInWidget.itinSignInViewModel.addGuestItinClickSubject.subscribe {
            show(signInWidget)
            show(addGuestItinWidget)
        }
    }
}