package com.expedia.bookings.widget

import android.animation.Animator
import android.app.AlertDialog
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.section.CountrySpinnerAdapter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.accessibility.AccessibleSpinner
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.bookings.widget.traveler.TSAEntryView
import com.expedia.util.subscribeMaterialFormsError
import com.expedia.util.subscribeVisibility
import com.expedia.vm.traveler.AbstractUniversalCKOTravelerEntryWidgetViewModel
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel

class FlightTravelerEntryWidget(context: Context, attrs: AttributeSet?) : AbstractTravelerEntryWidget(context, attrs) {

    val DEFAULT_EMPTY_PASSPORT = 0
    val materialFormTestEnabled = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
            AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, R.string.preference_universal_checkout_material_forms)

    val tsaEntryView: TSAEntryView by bindView(R.id.tsa_entry_widget)
    val passportCountrySpinner: AccessibleSpinner by bindView(R.id.passport_country_spinner)
    val passportCountryEditBox: EditText by bindView(R.id.passport_country_btn)
    val advancedOptionsWidget: FlightTravelerAdvancedOptionsWidget by bindView(R.id.traveler_advanced_options_widget)
    val advancedButton: LinearLayout by bindView(R.id.traveler_advanced_options_button)
    val advancedOptionsIcon: ImageView by bindView(R.id.traveler_advanced_options_icon)

    val resizeOpenAnimator: ResizeHeightAnimator by lazy {
        val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        advancedOptionsWidget.measure(widthMeasureSpec, heightMeasureSpec)
        resizeAnimator.addViewSpec(advancedOptionsWidget, advancedOptionsWidget.measuredHeight)
        resizeAnimator
    }

    val resizeCloseAnimator: ResizeHeightAnimator by lazy {
        val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
        resizeAnimator.addViewSpec(advancedOptionsWidget, 0)
        resizeAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                advancedOptionsWidget.visibility = Presenter.GONE
            }
        })
        resizeAnimator
    }

    override fun setUpViewModel(vm: AbstractUniversalCKOTravelerEntryWidgetViewModel) {
        vm as FlightTravelerEntryWidgetViewModel

        tsaEntryView.viewModel = vm.tsaViewModel
        advancedOptionsWidget.viewModel = vm.advancedOptionsViewModel
        vm.passportCountrySubject.subscribe { countryCode ->
            if (materialFormTestEnabled) {
                passportCountryEditBox.setText(countryCode)
            } else {
                selectPassport(countryCode)
            }
        }
        if (materialFormTestEnabled) {
            vm.showPassportCountryObservable.subscribeVisibility(passportCountryEditBox)
        } else {
            vm.showPassportCountryObservable.subscribeVisibility(passportCountrySpinner)
        }

        vm.showPassportCountryObservable.subscribe { show ->
            var view: View = if (materialFormTestEnabled) tsaEntryView.genderEditText as View else tsaEntryView.genderSpinner as View
            view.nextFocusForwardId = if (show) R.id.passport_country_spinner else R.id.first_name_input
        }

        vm.passportValidSubject.subscribe { isValid ->
            if (!materialFormTestEnabled) {
                val adapter = passportCountrySpinner.adapter as CountrySpinnerAdapter
                adapter.setErrorVisible(!isValid)
            } else {
                passportCountryEditBox.subscribeMaterialFormsError(vm.passportValidSubject.map { !it },
                        R.string.passport_validation_error_message, R.drawable.material_dropdown)
            }
        }
    }

    init {
        tsaEntryView.dateOfBirth.addOnFocusChangeListener(View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                Ui.hideKeyboard(this)
                tsaEntryView.dateOfBirth.performClick()
            }
            onFocusChange(view, hasFocus)
        })

        if (materialFormTestEnabled) {
            passportCountryEditBox.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    Ui.hideKeyboard(this)
                    passportCountryEditBox.performClick()
                }
                onFocusChange(view, hasFocus)
            }
        } else {
            val adapter = CountrySpinnerAdapter(context, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME,
                    R.layout.material_spinner_item, R.layout.spinner_dropdown_item, true)
            adapter.setPrefix(context.getString(R.string.passport_country_colon))
            adapter.setColoredPrefix(false)

            passportCountrySpinner.adapter = adapter
            passportCountrySpinner.onItemSelectedListener = CountryItemSelectedListener()

            passportCountrySpinner.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    Ui.hideKeyboard(this)
                    passportCountrySpinner.performClick()
                }
                onFocusChange(view, hasFocus)
            }
        }
        advancedOptionsWidget.redressNumber.addOnFocusChangeListener(this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        advancedButton.setOnClickListener {
            if (advancedOptionsWidget.visibility == Presenter.GONE) {
                showAdvancedOptions()
            } else {
                hideAdvancedOptions()
            }
        }

        if (materialFormTestEnabled) {
            passportCountryEditBox.setOnClickListener {
                showCountryAlertDialog()
            }
            tsaEntryView.genderEditText!!.addOnFocusChangeListener(View.OnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    Ui.hideKeyboard(this)
                    tsaEntryView.genderEditText!!.performClick()
                }
                onFocusChange(view, hasFocus)
            })
        } else {
            tsaEntryView.genderSpinner?.addOnFocusChangeListener(this)
        }

    }

    private fun showCountryAlertDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.passport_country))
        val adapter = CountrySpinnerAdapter(context, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME,
                R.layout.material_item)
        adapter.showPosAsFirstCountry()

        builder.setAdapter(adapter) { dialog, position ->
            passportCountryEditBox.setText(adapter.getItem(position))
            (viewModel as FlightTravelerEntryWidgetViewModel).passportCountryObserver.onNext(adapter.getItemValue(position, CountrySpinnerAdapter.CountryDisplayType.THREE_LETTER))
            (viewModel as FlightTravelerEntryWidgetViewModel).passportValidSubject.onNext(true)
        }

        val alert = builder.create()
        alert.listView.divider = (ContextCompat.getDrawable(context, R.drawable.divider_row_filter_refinement))
        alert.show()
    }

    private fun showAdvancedOptions() {
        advancedOptionsWidget.visibility = Presenter.VISIBLE
        resizeOpenAnimator.start()
        AnimUtils.rotate(advancedOptionsIcon)
    }

    private fun hideAdvancedOptions() {
        AnimUtils.reverseRotate(advancedOptionsIcon)
        resizeCloseAnimator.start()
    }

    private inner class CountryItemSelectedListener() : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            //do nothing
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val adapter = passportCountrySpinner.adapter as CountrySpinnerAdapter
            (viewModel as FlightTravelerEntryWidgetViewModel).passportCountryObserver.onNext(adapter.getItemValue(position, CountrySpinnerAdapter.CountryDisplayType.THREE_LETTER))
            if (!materialFormTestEnabled) {
                if (position == 0) {
                    adapter.setErrorVisible(true)
                }
            }
        }
    }

    //to be removed for new checkout
    override fun isCompletelyFilled(): Boolean {
        return super.isCompletelyFilled() &&
                tsaEntryView.dateOfBirth.text.isNotEmpty() && tsaEntryView.isValidGender() && isValidPassport()
    }

    private fun isValidPassport(): Boolean {
        var view: View
        var validPassport: Boolean
        if (materialFormTestEnabled) {
            view = passportCountryEditBox
            validPassport = (passportCountryEditBox.visibility == View.VISIBLE && Strings.isNotEmpty(passportCountryEditBox.text.toString()))

        } else {
            view = passportCountrySpinner
            validPassport = (passportCountrySpinner.visibility == View.VISIBLE && passportCountrySpinner.selectedItemPosition != 0)
        }
        return (validPassport) || view.visibility == View.GONE
    }

    private fun selectPassport(countryCode: String?) {
        passportCountrySpinner.onItemSelectedListener = null
        val adapter = passportCountrySpinner.adapter as CountrySpinnerAdapter
        val position = if (countryCode?.isNullOrEmpty() ?: true) DEFAULT_EMPTY_PASSPORT else adapter.getPositionByCountryThreeLetterCode(countryCode)
        if (position == 0) {
            adapter.setErrorVisible(false);
        }
        passportCountrySpinner.setSelection(position, false)
        passportCountrySpinner.onItemSelectedListener = CountryItemSelectedListener()
    }

    override fun inflateWidget() {
        View.inflate(context, R.layout.flight_traveler_entry_widget, this)
    }
}