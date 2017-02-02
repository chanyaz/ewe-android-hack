package com.expedia.bookings.presenter.trips

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeContentDescription
import com.expedia.util.subscribeImageDrawable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.expedia.vm.itin.ItinSignInViewModel

class ItinSignInWidget(context: Context, attr: AttributeSet?) : RelativeLayout(context, attr) {
    val signInContainer: CardView by bindView(R.id.account_sign_in_container)
    val signInText: TextView by bindView(R.id.login_text_view)
    val addGuestItinTextButton: TextView by bindView(R.id.add_guest_itin_text_button)
    val statusText: TextView by bindView(R.id.status_text)
    val statusImage: ImageView by bindView(R.id.status_image)

    val viewModel = ItinSignInViewModel(context)

    init {
        View.inflate(context, R.layout.itin_sign_in_widget, this)
    }


    override fun onFinishInflate() {
        super.onFinishInflate()


        signInText.text = viewModel.getSignInText()
        signInContainer.subscribeOnClick(viewModel.signInClickSubject)
        addGuestItinTextButton.subscribeOnClick(viewModel.addGuestItinClickSubject)

        signInContainer.subscribeOnClick(viewModel.signInClickSubject)
        viewModel.statusTextColorSubject.subscribeTextColor(statusText)
        viewModel.statusImageVisibilitySubject.subscribeVisibility(statusImage)
        viewModel.statusTextSubject.subscribeText(statusText)
        viewModel.statusImageSubject.subscribeImageDrawable(statusImage)
        viewModel.updateButtonTextSubject.subscribeText(signInText)
        viewModel.updateButtonContentDescriptionSubject.subscribeContentDescription(signInText)
    }
}