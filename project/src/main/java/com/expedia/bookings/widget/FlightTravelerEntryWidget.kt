package com.expedia.bookings.widget

import android.animation.Animator
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.section.CountrySpinnerAdapter
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isFrequentFlyerNumberForFlightsEnabled
import com.expedia.bookings.utils.isMaterialFormsEnabled
import com.expedia.bookings.widget.accessibility.AccessibleSpinner
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.bookings.widget.traveler.FrequentFlyerAdapter
import com.expedia.bookings.widget.traveler.TSAEntryView
import com.expedia.util.subscribeMaterialFormsError
import com.expedia.util.subscribeVisibility
import com.expedia.util.updateVisibility
import com.expedia.vm.traveler.AbstractUniversalCKOTravelerEntryWidgetViewModel
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel

class FlightTravelerEntryWidget(context: Context, attrs: AttributeSet?) : AbstractTravelerEntryWidget(context, attrs) {

    val DEFAULT_EMPTY_PASSPORT = 0
    val materialFormTestEnabled = isMaterialFormsEnabled()
    val frequentflyerTestEnabled = isFrequentFlyerNumberForFlightsEnabled(context)
    val tsaEntryView: TSAEntryView by bindView(R.id.tsa_entry_widget)
    val passportCountrySpinner: AccessibleSpinner by bindView(R.id.passport_country_spinner)
    val passportCountryInputLayout: TextInputLayout by bindView(R.id.passport_country_layout_btn)
    val passportCountryEditBox: EditText by bindView(R.id.passport_country_btn)
    val advancedOptionsWidget: FlightTravelerAdvancedOptionsWidget by bindView(R.id.traveler_advanced_options_widget)
    val advancedOptionsIcon: ImageView by bindView(R.id.traveler_advanced_options_icon)
    val advancedButton: LinearLayout by bindView(R.id.traveler_advanced_options_button)
    val advancedOptionsText: TextView by bindView(R.id.advanced_options_text)
    var frequentFlyerButton: LinearLayout? = null
    var frequentFlyerRecycler: RecyclerView? = null
    var frequentFlyerIcon: ImageView? = null
    var frequentFlyerText: TextView? = null

    val resizeOpenAnimator: ResizeHeightAnimator by lazy {
        val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        advancedOptionsWidget.measure(widthMeasureSpec, heightMeasureSpec)
        resizeAnimator.addViewSpec(advancedOptionsWidget, advancedOptionsWidget.measuredHeight)
        resizeAnimator
    }

    val resizeOpenFrequentFlyerAnimator: ResizeHeightAnimator by lazy {
        val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        frequentFlyerRecycler?.measure(widthMeasureSpec, heightMeasureSpec)
        resizeAnimator.addViewSpec(frequentFlyerRecycler as View, frequentFlyerRecycler?.measuredHeight as Int)
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

    val resizeCloseFrequentFlyerAnimator: ResizeHeightAnimator by lazy {
        val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
        resizeAnimator.addViewSpec(frequentFlyerRecycler as View, 0)
        resizeAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                frequentFlyerRecycler?.visibility = Presenter.GONE
            }
        })
        resizeAnimator
    }

    override fun setUpViewModel(vm: AbstractUniversalCKOTravelerEntryWidgetViewModel) {
        vm as FlightTravelerEntryWidgetViewModel

        tsaEntryView.viewModel = vm.tsaViewModel
        advancedOptionsWidget.viewModel = vm.advancedOptionsViewModel
        if (frequentflyerTestEnabled) {
            vm.frequentFlyerAdapterViewModel?.let { viewModel ->
                frequentFlyerRecycler?.adapter = FrequentFlyerAdapter(viewModel)
            }
        }
        vm.passportCountrySubject.subscribe { countryCode ->
            if (materialFormTestEnabled) {
                val adapter = CountrySpinnerAdapter(context, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME,
                        R.layout.material_item)
                if (countryCode.isNullOrBlank()) {
                    passportCountryEditBox.setText(countryCode)
                } else if (adapter.getPositionByCountryThreeLetterCode(countryCode) == -1) {
                    passportCountryEditBox.text = null
                } else {
                    val countryName = adapter.getItem(adapter.getPositionByCountryThreeLetterCode(countryCode))
                    passportCountryEditBox.setText(countryName)
                    vm.passportValidSubject.onNext(true)
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
                passportCountrySpinner.valid = isValid
            } else {
                passportCountryEditBox.subscribeMaterialFormsError(vm.passportValidSubject.map { !it },
                        R.string.passport_validation_error_message, R.drawable.material_dropdown)
            }
        }

    }

    init {
        if (!AccessibilityUtil.isTalkBackEnabled(context)) {
            setOnFocusChangeListenerForView(tsaEntryView.dateOfBirth)
        }
        if (materialFormTestEnabled) {
            setOnFocusChangeListenerForView(passportCountryEditBox)
            setOnFocusChangeListenerForView(advancedOptionsWidget.seatPreferenceEditBox)
            setOnFocusChangeListenerForView(advancedOptionsWidget.assistancePreferenceEditBox)
            if (frequentflyerTestEnabled) {
                frequentFlyerButton = findViewById<LinearLayout>(R.id.traveler_frequent_flyer_button)
                frequentFlyerRecycler = findViewById<RecyclerView>(R.id.frequent_flyer_recycler_view)
                frequentFlyerIcon = findViewById<ImageView>(R.id.traveler_frequent_flyer_program_icon)
                frequentFlyerText = findViewById<TextView>(R.id.frequent_flyer_program_text)
                frequentFlyerButton!!.updateVisibility(frequentflyerTestEnabled)
                setUpFrequentFlyerRecyclerView(context)
            }
        } else {
            val adapter = CountrySpinnerAdapter(context, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME,
                    R.layout.material_spinner_item, R.layout.spinner_dropdown_item, true)
            adapter.setPrefix(context.getString(R.string.passport_country_colon))
            adapter.setColoredPrefix(false)

            passportCountrySpinner.adapter = adapter
            passportCountrySpinner.onItemSelectedListener = CountryItemSelectedListener()
            setOnFocusChangeListenerForView(passportCountrySpinner)
            passportCountrySpinner.errorMessage = context.getString(R.string.passport_validation_error_message)
        }
        advancedOptionsWidget.redressNumber.addOnFocusChangeListener(this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (materialFormTestEnabled) {
            passportCountryEditBox.setOnClickListener {
                showCountryAlertDialog()
            }
            if (frequentflyerTestEnabled) {
                frequentFlyerButton!!.setOnClickListener {
                    if (frequentFlyerRecycler?.visibility == Presenter.GONE) {
                        showFrequentFlyerProgram()
                    } else {
                        hideFrequentFlyerProgram()
                    }
                }
            }
            setOnFocusChangeListenerForView(tsaEntryView.genderEditText!!)
        } else {
            tsaEntryView.genderSpinner?.addOnFocusChangeListener(this)
        }

        advancedButton.setOnClickListener {
            if (advancedOptionsWidget.visibility == Presenter.GONE) {
                showAdvancedOptions()
            } else {
                hideAdvancedOptions()
            }
        }

        val isExtraPaddingRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP
        if (isExtraPaddingRequired) {
            phoneEntryView.phoneNumber.updatePaddingForOldApi()
            phoneEntryView.phoneEditBox?.updatePaddingForOldApi()
            tsaEntryView.dateOfBirth.updatePaddingForOldApi()
            tsaEntryView.genderEditText?.updatePaddingForOldApi()
            if (materialFormTestEnabled) {
                nameEntryView.firstName.updatePaddingForOldApi()
                nameEntryView.middleName?.updatePaddingForOldApi()
                nameEntryView.lastName.updatePaddingForOldApi()
                emailEntryView.emailAddress.updatePaddingForOldApi()
                advancedOptionsWidget.travelerNumber.updatePaddingForOldApi()
                advancedOptionsWidget.redressNumber.updatePaddingForOldApi()
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
        }

        val alert = builder.create()
        alert.listView.divider = (ContextCompat.getDrawable(context, R.drawable.divider_row_filter_refinement))
        alert.show()
    }

    private fun showAdvancedOptions() {
        advancedOptionsWidget.visibility = Presenter.VISIBLE
        resizeOpenAnimator.start()
        AnimUtils.rotate(advancedOptionsIcon)
        advancedOptionsText.contentDescription = context.getString(R.string.collapse_advanced_button_cont_desc)
    }

    private fun hideAdvancedOptions() {
        AnimUtils.reverseRotate(advancedOptionsIcon)
        resizeCloseAnimator.start()
        advancedOptionsText.contentDescription = context.getString(R.string.expand_advanced_button_cont_desc)
    }

    private fun showFrequentFlyerProgram() {
        frequentFlyerRecycler?.visibility = Presenter.VISIBLE
        resizeOpenFrequentFlyerAnimator.start()
        AnimUtils.rotate(frequentFlyerIcon)
        frequentFlyerText?.contentDescription = context.getString(R.string.collapse_frequent_flyer_button_cont_desc)
    }

    private fun hideFrequentFlyerProgram() {
        AnimUtils.reverseRotate(frequentFlyerIcon)
        resizeCloseFrequentFlyerAnimator.start()
        frequentFlyerText?.contentDescription = context.getString(R.string.expand_frequent_flyer_button_cont_desc)
    }

    private inner class CountryItemSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            //do nothing
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val adapter = passportCountrySpinner.adapter as CountrySpinnerAdapter
            (viewModel as FlightTravelerEntryWidgetViewModel).passportCountryObserver.onNext(adapter.getItemValue(position, CountrySpinnerAdapter.CountryDisplayType.THREE_LETTER))
            if (!materialFormTestEnabled) {
                if (position == 0) {
                    adapter.setErrorVisible(true)
                } else {
                    passportCountrySpinner.valid = true
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
            adapter.setErrorVisible(false)
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

    fun setUpFrequentFlyerRecyclerView(context: Context) {
        val linearLayoutManager = LinearLayoutManager(context)
        frequentFlyerRecycler?.layoutManager = linearLayoutManager
    }
}
