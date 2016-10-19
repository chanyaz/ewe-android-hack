package com.expedia.bookings.widget

import android.app.AlertDialog
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.Ui
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeChecked
import com.expedia.util.subscribeOnCheckChanged
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.expedia.vm.InsuranceViewModel

class InsuranceWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
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
        vm.programmaticToggleObservable.subscribeChecked(toggleSwitch)
        vm.termsObservable.subscribeText(termsTextView)
        vm.titleColorObservable.subscribeTextColor(titleTextView)
        vm.titleObservable.subscribeText(titleTextView)
        vm.widgetVisibilityObservable.subscribeVisibility(this)
        toggleSwitch.subscribeOnCheckChanged(vm.userInitiatedToggleObservable)

    }

    init {
        View.inflate(context, R.layout.insurance_widget, this)

        background = ContextCompat.getDrawable(context, R.drawable.card_background)
        orientation = VERTICAL

        val icon = ContextCompat.getDrawable(context, R.drawable.ic_checkout_info).mutate()
        descriptionTextView.compoundDrawablePadding = (5 * resources.displayMetrics.density + 0.5f).toInt()
        descriptionTextView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        descriptionTextView.setOnClickListener {
            benefitsDialog.show()
            FlightsV2Tracking.trackInsuranceBenefitsClick()
        }

        termsTextView.movementMethod = LinkMovementMethod.getInstance()
        termsTextView.setOnClickListener { FlightsV2Tracking.trackInsuranceTermsClick() }
        termsTextView.setTextColor(Ui.obtainThemeColor(context, R.attr.primary_color))
    }
}
