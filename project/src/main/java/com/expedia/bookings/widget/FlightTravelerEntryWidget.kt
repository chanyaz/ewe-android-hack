package com.expedia.bookings.widget

import android.animation.Animator
import android.app.AlertDialog
import android.content.Context
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.section.CountrySpinnerAdapter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isMaterialFormsEnabled
import com.expedia.bookings.widget.accessibility.AccessibleSpinner
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.bookings.widget.traveler.TSAEntryView
import com.expedia.util.subscribeMaterialFormsError
import com.expedia.util.subscribeVisibility
import com.expedia.vm.traveler.AbstractUniversalCKOTravelerEntryWidgetViewModel
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel

class FlightTravelerEntryWidget(context: Context, attrs: AttributeSet?) : AbstractTravelerEntryWidget(context, attrs) {

    val DEFAULT_EMPTY_PASSPORT = 0
    val materialFormTestEnabled = isMaterialFormsEnabled()
    val tsaEntryView: TSAEntryView by bindView(R.id.tsa_entry_widget)
    val passportCountrySpinner: AccessibleSpinner by bindView(R.id.passport_country_spinner)
    val passportCountryInputLayout: TextInputLayout by bindView(R.id.passport_country_layout_btn)
    val passportCountryEditBox: EditText by bindView(R.id.passport_country_btn)
    val advancedOptionsWidget: FlightTravelerAdvancedOptionsWidget by bindView(R.id.traveler_advanced_options_widget)
    var advancedButton: LinearLayout? = null
    var advancedOptionsIcon: ImageView? = null

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
                if (countryCode.isNullOrBlank()) {
                    passportCountryEditBox.setText(countryCode)
                } else {
                    val adapter = CountrySpinnerAdapter(context, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME,
                            R.layout.material_item)
                    val countryName = adapter.getItem(adapter.getPositionByCountryThreeLetterCode(countryCode))
                    passportCountryEditBox.setText(countryName)
                }
            } else {
                selectPassport(countryCode)
            }
        }
        if (materialFormTestEnabled) {
            vm.showPassportCountryObservable.subscribeVisibility(passportCountryInputLayout)
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
        setOnFocusChangeListenerForView(tsaEntryView.dateOfBirth)
        if (materialFormTestEnabled) {
            setOnFocusChangeListenerForView(passportCountryEditBox)
            setOnFocusChangeListenerForView(advancedOptionsWidget.seatPreferenceEditBox)
            setOnFocusChangeListenerForView(advancedOptionsWidget.assistancePreferenceEditBox)
        } else {
            val adapter = CountrySpinnerAdapter(context, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME,
                    R.layout.material_spinner_item, R.layout.spinner_dropdown_item, true)
            adapter.setPrefix(context.getString(R.string.passport_country_colon))
            adapter.setColoredPrefix(false)

            passportCountrySpinner.adapter = adapter
            passportCountrySpinner.onItemSelectedListener = CountryItemSelectedListener()
            setOnFocusChangeListenerForView(passportCountrySpinner)
        }
        advancedOptionsWidget.redressNumber.addOnFocusChangeListener(this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (materialFormTestEnabled) {
            passportCountryEditBox.setOnClickListener {
                showCountryAlertDialog()
            }
            setOnFocusChangeListenerForView(tsaEntryView.genderEditText!!)
        } else {
            tsaEntryView.genderSpinner?.addOnFocusChangeListener(this)
            advancedOptionsIcon = findViewById(R.id.traveler_advanced_options_icon) as ImageView
            advancedButton = findViewById(R.id.traveler_advanced_options_button) as LinearLayout
            advancedButton?.setOnClickListener {
                if (advancedOptionsWidget.visibility == Presenter.GONE) {
                    showAdvancedOptions()
                } else {
                    hideAdvancedOptions()
                }
            }
        }
    }

    private fun showCountryAlertDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.passport_country))
        val adapter = CountrySpinnerAdapter(context, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME,
                R.layout.material_item)
        adapter.showPosAsFirstCountry()

        builder.setAdapter(adapter) { dialog, position ->
            if ((viewModel as FlightTravelerEntryWidgetViewModel).showPassportCountryObservable.value) {
                (viewModel as FlightTravelerEntryWidgetViewModel).passportCountrySubject.onNext(adapter.getItemValue(position, CountrySpinnerAdapter.CountryDisplayType.THREE_LETTER))
            }
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
            validPassport = (passportCountryInputLayout.visibility == View.VISIBLE && Strings.isNotEmpty(passportCountryEditBox.text.toString()))

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
        if (isMaterialFormsEnabled()) {
            View.inflate(context, R.layout.material_flight_traveler_entry_widget, this)
        } else {
            View.inflate(context, R.layout.flight_traveler_entry_widget, this)
        }
    }

    private fun setOnFocusChangeListenerForView(view: View) {
        view.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                Ui.hideKeyboard(this)
                view.performClick()
            }
            onFocusChange(view, hasFocus)
        }
    }

    override fun resetErrorState() {
        super.resetErrorState()
        tsaEntryView.dateOfBirth.viewModel.errorSubject.onNext(false)
        val requiresPassport = (viewModel as FlightTravelerEntryWidgetViewModel).showPassportCountryObservable.value ?: false
        if (requiresPassport) {
            (viewModel as FlightTravelerEntryWidgetViewModel).passportValidSubject.onNext(true)
        }
        tsaEntryView.genderEditText?.viewModel?.errorSubject?.onNext(false)
    }

}