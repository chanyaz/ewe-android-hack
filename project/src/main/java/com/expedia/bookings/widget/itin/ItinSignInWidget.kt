package com.expedia.bookings.presenter.trips

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeBackgroundColor
import com.expedia.bookings.extensions.subscribeContentDescription
import com.expedia.bookings.extensions.subscribeImageDrawable
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribePorterDuffColorFilter
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextColor
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.itin.ItinPOSHeader
import com.expedia.vm.itin.ItinSignInViewModel

class ItinSignInWidget(context: Context, attr: AttributeSet?) : RelativeLayout(context, attr) {
    val signInContainer: CardView by bindView(R.id.account_sign_in_container)
    val signInText: TextView by bindView(R.id.login_text_view)
    val addGuestItinTextButton: TextView by bindView(R.id.add_guest_itin_text_button)
    val statusText: TextView by bindView(R.id.status_text)
    val statusImage: ImageView by bindView(R.id.status_image)
    val buttonImage: ImageView by bindView(R.id.exp_logo)

    val itinPOSHeader: ItinPOSHeader by bindView(R.id.sign_in_toolbar)

    val viewModel = ItinSignInViewModel(context)

    init {
        View.inflate(context, R.layout.itin_sign_in_widget, this)
        AccessibilityUtil.appendRoleContDesc(addGuestItinTextButton, addGuestItinTextButton.text.toString(), R.string.accessibility_cont_desc_role_button)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        signInText.text = viewModel.getSignInText()
        signInContainer.subscribeOnClick(viewModel.signInClickSubject)
        addGuestItinTextButton.subscribeOnClick(viewModel.addGuestItinClickSubject)
        addGuestItinTextButton.setCompoundDrawablesTint(ContextCompat.getColor(getContext(), R.color.itin_add_guest_text_button_color))

        viewModel.statusTextColorSubject.subscribeTextColor(statusText)
        viewModel.statusImageVisibilitySubject.subscribeVisibility(statusImage)
        viewModel.statusTextSubject.subscribeText(statusText)
        viewModel.statusImageSubject.subscribeImageDrawable(statusImage)
        viewModel.statusImageColorSubject.subscribePorterDuffColorFilter(statusImage)

        viewModel.updateButtonTextSubject.subscribeText(signInText)
        viewModel.updateButtonContentDescriptionSubject.subscribeContentDescription(signInText)
        viewModel.updateButtonTextColorSubject.subscribeTextColor(signInText)
        viewModel.updateButtonImageVisibilitySubject.subscribeVisibility(buttonImage)
        viewModel.updateButtonColorSubject.subscribeBackgroundColor(signInContainer)
    }
}
