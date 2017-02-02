package com.expedia.bookings.presenter.trips

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeOnClick
import com.expedia.vm.itin.ItinSignInViewModel

class ItinSignInWidget(context: Context, attr: AttributeSet?) : RelativeLayout(context, attr) {
    val signInContainer: CardView by bindView(R.id.account_sign_in_container)
    val signInText: TextView by bindView(R.id.login_text_view)
    val addGuestItinTextButton: TextView by bindView(R.id.add_guest_itin_text_button)

    val itinSignInViewModel= ItinSignInViewModel(context)

    init {
        View.inflate(context, R.layout.itin_sign_in_widget, this)

        signInText.text = itinSignInViewModel.getSignInText()
        signInText.contentDescription = itinSignInViewModel.getSignInContentDescription()
        signInContainer.subscribeOnClick(itinSignInViewModel.signInClickSubject)
        addGuestItinTextButton.subscribeOnClick(itinSignInViewModel.addGuestItinClickSubject)
    }
}