package com.expedia.bookings.widget

import android.app.AlertDialog
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.SwitchCompat
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.extensions.subscribeEnabled
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextColor
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.Ui
import com.expedia.util.notNullAndObservable
import com.expedia.vm.InsuranceViewModel

class InsuranceWidget(context: Context, attrs: AttributeSet) : CardView(context, attrs) {
    val descriptionTextView: TextView by bindView(R.id.insurance_description)
    val termsTextView: TextView by bindView(R.id.insurance_terms)
    val titleTextView: TextView by bindView(R.id.insurance_title)
    val toggleSwitch: SwitchCompat by bindView(R.id.insurance_switch)

    val benefitsDialog: AlertDialog by lazy {
        val benefitsTextView = View.inflate(context, R.layout.insurance_benefits_dialog_body, null) as TextView
        viewModel.benefitsObservable.subscribeText(benefitsTextView)

        AlertDialog.Builder(context).setPositiveButton(R.string.button_done, null)
                .setTitle(context.resources.getString(R.string.insurance_description))
                .setView(benefitsTextView)
                .create()
    }

    var viewModel: InsuranceViewModel by notNullAndObservable { vm ->
        vm.programmaticToggleObservable.subscribe { isChecked ->
            if (toggleSwitch.isChecked != isChecked) {
                suppressNextToggleEvent = true
                toggleSwitch.isChecked = isChecked
            }
        }
        vm.titleColorObservable.subscribeTextColor(titleTextView)
        vm.titleObservable.subscribeText(titleTextView)
        vm.toggleSwitchEnabledObservable.subscribeEnabled(toggleSwitch)
        vm.widgetVisibilityObservable.subscribeVisibility(this)
    }

    private var suppressNextToggleEvent: Boolean = false

    init {
        View.inflate(context, R.layout.insurance_widget, this)

        val icon = ContextCompat.getDrawable(context, R.drawable.ic_checkout_info)?.mutate()
        descriptionTextView.compoundDrawablePadding = (5 * resources.displayMetrics.density + 0.5f).toInt()
        descriptionTextView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        descriptionTextView.setOnClickListener {
            benefitsDialog.show()
            FlightsV2Tracking.trackInsuranceBenefitsClick()
        }

        termsTextView.setTextColor(Ui.obtainThemeColor(context, R.attr.primary_color))
        termsTextView.setOnClickListener {
            FlightsV2Tracking.trackInsuranceTermsClick()
            context.startActivity(WebViewActivity.IntentBuilder(context)
                    .setTitle(termsTextView.text.toString())
                    .setUrl(viewModel.termsUrl)
                    .intent)
        }

        toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!suppressNextToggleEvent) {
                viewModel.userInitiatedToggleObservable.onNext(isChecked)
            }
            suppressNextToggleEvent = false
        }
        setupAccessibilityContentDescription()
    }

    fun setupAccessibilityContentDescription() {
        AccessibilityUtil.appendRoleContDesc(descriptionTextView, descriptionTextView.text.toString(), R.string.accessibility_cont_desc_role_button)
        AccessibilityUtil.appendRoleContDesc(termsTextView, termsTextView.text.toString(), R.string.accessibility_cont_desc_role_button)
    }
}
