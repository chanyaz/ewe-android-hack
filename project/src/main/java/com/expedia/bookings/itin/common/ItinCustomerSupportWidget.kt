package com.expedia.bookings.itin.common

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
import com.expedia.bookings.itin.utils.ActionModeCallbackUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setOnClickForSelectableTextView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.mobiata.android.SocialUtils

class ItinCustomerSupportWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val customerSupportTextView: TextView by bindView(R.id.customer_support_text)
    val itineraryNumberHeader: LinearLayout by bindView(R.id.itinerary_number_header)
    val itineraryNumberTextView: TextView by bindView(R.id.itinerary_number)
    val callSupportActionButton: TextView by bindView(R.id.call_support_action_button)
    val customerSupportSiteButton: TextView by bindView(R.id.expedia_customer_support_site_button)

    var viewModel: ICustomerSupportViewModel by notNullAndObservable {
        it.customerSupportHeaderTextSubject.subscribeText(customerSupportTextView)
        it.itineraryHeaderVisibilitySubject.subscribeVisibility(itineraryNumberHeader)
        it.itineraryNumberSubject.subscribeTextAndVisibility(itineraryNumberTextView)
        it.itineraryNumberContentDescriptionSubject.subscribeContentDescription(itineraryNumberTextView)
        it.phoneNumberContentDescriptionSubject.subscribeContentDescription(callSupportActionButton)
        it.customerSupportTextSubject.subscribeTextAndVisibility(customerSupportSiteButton)
        it.customerSupportTextContentDescriptionSubject.subscribeContentDescription(customerSupportSiteButton)
        it.phoneNumberSubject.subscribe { number ->
            callSupportActionButton.visibility = View.VISIBLE
            callSupportActionButton.text = number
            callSupportActionButton.setOnClickForSelectableTextView { callSupplier(number) }
        }
    }

    init {
        View.inflate(context, R.layout.widget_itin_customer_support, this)

        // Not setting in xml for now since we will need to handle it for other LOBs
        itineraryNumberTextView.setTextIsSelectable(true)
        callSupportActionButton.setTextIsSelectable(true)

        customerSupportSiteButton.setOnClickListener {
            viewModel.customerSupportButtonClickedSubject.onNext(Unit)
        }

        itineraryNumberTextView.customSelectionActionModeCallback = ActionModeCallbackUtil.getActionModeCallBackWithoutPhoneNumberMenuItem()
        callSupportActionButton.customSelectionActionModeCallback = ActionModeCallbackUtil.getActionModeCallbackWithPhoneNumberClickAction {
            viewModel.phoneNumberClickedSubject.onNext(Unit)
        }
    }

    private fun callSupplier(number: String) {
        val pm = context.packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            viewModel.phoneNumberClickedSubject.onNext(Unit)
            SocialUtils.call(context, number)
        }
    }
}
