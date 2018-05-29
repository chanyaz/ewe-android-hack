package com.expedia.bookings.itin.lx.moreHelp

import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeContentDescription
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.itin.common.IMoreHelpViewModel
import com.expedia.bookings.itin.utils.ActionModeCallbackUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setOnClickForSelectableTextView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.mobiata.android.SocialUtils

class LxItinMoreHelpWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val helpText: TextView by bindView(R.id.itin_more_help_text)
    val confirmationNumber: TextView by bindView(R.id.confirmation_number)
    val confirmationTitle: LinearLayout by bindView(R.id.confirmation_title)
    val phoneNumberButton: TextView by bindView(R.id.itin_more_help_phone_number)

    var viewModel: IMoreHelpViewModel by notNullAndObservable {
        it.helpTextSubject.subscribeText(helpText)
        it.confirmationNumberSubject.subscribeTextAndVisibility(confirmationNumber)
        it.confirmationTitleVisibilitySubject.subscribeVisibility(confirmationTitle)
        it.confirmationNumberContentDescriptionSubject.subscribeContentDescription(confirmationNumber)
        it.callButtonContentDescriptionSubject.subscribeContentDescription(phoneNumberButton)
        it.phoneNumberSubject.subscribe { number ->
            phoneNumberButton.text = number
            phoneNumberButton.setOnClickForSelectableTextView { callSupplier(number) }
        }
    }

    init {
        View.inflate(context, R.layout.widget_itin_more_help, this)
        this.orientation = LinearLayout.VERTICAL
        confirmationNumber.customSelectionActionModeCallback = ActionModeCallbackUtil.getActionModeCallBackWithoutPhoneNumberMenuItem()
        phoneNumberButton.customSelectionActionModeCallback = ActionModeCallbackUtil.getActionModeCallbackWithPhoneNumberClickAction({
            viewModel.phoneNumberClickSubject.onNext(Unit)
        })
    }

    private fun callSupplier(number: String) {
        val pm = context.packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            SocialUtils.call(context, number)
            viewModel.phoneNumberClickSubject.onNext(Unit)
        }
    }
}
