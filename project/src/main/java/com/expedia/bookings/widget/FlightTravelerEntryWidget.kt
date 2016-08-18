package com.expedia.bookings.widget

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.section.CountrySpinnerAdapter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.TravelerUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.bookings.widget.traveler.NameEntryView
import com.expedia.bookings.widget.traveler.PhoneEntryView
import com.expedia.bookings.widget.traveler.TSAEntryView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeTextChange
import com.expedia.util.subscribeVisibility
import com.expedia.vm.traveler.TravelerViewModel
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription

class FlightTravelerEntryWidget(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs),
        TravelerButton.ITravelerButtonListener, View.OnFocusChangeListener {

    val ANIMATION_DURATION = 500L
    val DEFAULT_EMPTY_PASSPORT = 0

    val travelerButton: TravelerButton by bindView(R.id.traveler_button)
    val nameEntryView: NameEntryView by bindView(R.id.name_entry_widget)
    val phoneEntryView: PhoneEntryView by bindView(R.id.phone_entry_widget)
    val tsaEntryView: TSAEntryView by bindView(R.id.tsa_entry_widget)
    val passportCountrySpinner: Spinner by bindView(R.id.passport_country_spinner)
    val advancedOptionsWidget: FlightTravelerAdvancedOptionsWidget by bindView(R.id.traveler_advanced_options_widget)
    val advancedButton: LinearLayout by bindView(R.id.traveler_advanced_options_button)
    val advancedOptionsIcon: ImageView by bindView(R.id.traveler_advanced_options_icon)

    val focusedView = PublishSubject.create<View>()
    val filledIn = PublishSubject.create<Boolean>()

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

    var viewModel: TravelerViewModel by notNullAndObservable { vm ->
        nameEntryView.viewModel = vm.nameViewModel
        phoneEntryView.viewModel = vm.phoneViewModel
        tsaEntryView.viewModel = vm.tsaViewModel
        advancedOptionsWidget.viewModel = vm.advancedOptionsViewModel

        vm.passportCountrySubject.subscribe { countryCode ->
            selectPassport(countryCode)
        }
        vm.showPassportCountryObservable.subscribeVisibility(passportCountrySpinner)
        vm.showPhoneNumberObservable.subscribeVisibility(phoneEntryView)
        vm.showPhoneNumberObservable.subscribe { show ->
            nameEntryView.lastName.nextFocusForwardId = if (show) R.id.edit_phone_number else R.id.edit_birth_date_text_btn
        }
        vm.showPassportCountryObservable.subscribe {
            show ->
            tsaEntryView.genderSpinner.nextFocusForwardId = if (show) R.id.passport_country_spinner else R.id.first_name_input
        }

        vm.tsaViewModel.formattedDateSubject.subscribe {
            filledIn.onNext(isCompletelyFilled())
        }

        vm.tsaViewModel.genderSubject.subscribe {
            filledIn.onNext(isCompletelyFilled())
        }

        vm.passportValidSubject.subscribe { isValid ->
            val adapter = passportCountrySpinner.adapter as CountrySpinnerAdapter
            adapter.setErrorVisible(!isValid)
        }
    }

    var compositeSubscription: CompositeSubscription? = null
    val formFilledSubscriber = endlessObserver<String>() {
        filledIn.onNext(isCompletelyFilled())
    }

    init {
        View.inflate(context, R.layout.flight_traveler_entry_widget, this)
        travelerButton.visibility == View.GONE
        val adapter = CountrySpinnerAdapter(context, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME,
                R.layout.material_spinner_item, R.layout.spinner_dropdown_item, true)
        adapter.setPrefix(context.getString(R.string.passport_country_colon))
        adapter.setColoredPrefix(false)

        passportCountrySpinner.adapter = adapter
        passportCountrySpinner.onItemSelectedListener = CountryItemSelectedListener()
        passportCountrySpinner.isFocusable = true
        passportCountrySpinner.isFocusableInTouchMode = true
        travelerButton.setTravelButtonListener(this)
        nameEntryView.firstName.onFocusChangeListener = this
        nameEntryView.middleName.onFocusChangeListener = this
        nameEntryView.lastName.onFocusChangeListener = this
        phoneEntryView.phoneNumber.onFocusChangeListener = this
        tsaEntryView.dateOfBirth.setOnFocusChangeListener { view, hasFocus ->
            onFocusChange(view, hasFocus)
            if (hasFocus) {
                Ui.hideKeyboard(this)
                tsaEntryView.dateOfBirth.performClick()
            }
        }
        tsaEntryView.genderSpinner.setOnFocusChangeListener { view, hasFocus ->
            onFocusChange(view, hasFocus)
            if (hasFocus) {
                Ui.hideKeyboard(this)
                tsaEntryView.genderSpinner.performClick()
            }
        }
        passportCountrySpinner.setOnFocusChangeListener { view, hasFocus ->
            onFocusChange(view, hasFocus)
            if (hasFocus) {
                Ui.hideKeyboard(this)
                passportCountrySpinner.performClick()
            }
        }
        advancedOptionsWidget.redressNumber.onFocusChangeListener = this
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            compositeSubscription = CompositeSubscription()
            compositeSubscription?.add(nameEntryView.firstName.subscribeTextChange(formFilledSubscriber))
            compositeSubscription?.add(nameEntryView.lastName.subscribeTextChange(formFilledSubscriber))
            compositeSubscription?.add(phoneEntryView.phoneNumber.subscribeTextChange(formFilledSubscriber))
        } else {
            compositeSubscription?.unsubscribe()
        }
    }

    override fun onTravelerChosen(traveler: Traveler) {
        val passengerCategory = viewModel.getTraveler().passengerCategory
        traveler.passengerCategory = passengerCategory
        selectPassport(traveler.primaryPassportCountry)
        viewModel.updateTraveler(traveler)
    }

    override fun onAddNewTravelerSelected() {
        val newTraveler = Traveler()
        val passengerCategory = viewModel.getTraveler().passengerCategory
        newTraveler.passengerCategory = passengerCategory
        viewModel.updateTraveler(newTraveler)
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
    }

    fun isValid(): Boolean {
        return viewModel.validate()
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
            viewModel.passportCountryObserver.onNext(adapter.getItemValue(position, CountrySpinnerAdapter.CountryDisplayType.THREE_LETTER))
            if (position == 0) {
                adapter.setErrorVisible(true)
            }
        }
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            focusedView.onNext(v)
        }
    }

    fun isCompletelyFilled(): Boolean {
        return nameEntryView.firstName.text.isNotEmpty() &&
                nameEntryView.lastName.text.isNotEmpty() &&
                (!TravelerUtils.isMainTraveler(viewModel.travelerIndex) || phoneEntryView.phoneNumber.text.isNotEmpty()) &&
                (tsaEntryView.dateOfBirth.text.isNotEmpty() && tsaEntryView.genderSpinner.selectedItemPosition != 0)
    }

    fun resetStoredTravelerSelection() {
        val traveler = viewModel.getTraveler()
        if (traveler.isStoredTraveler) {
            travelerButton.selectTraveler.text = traveler.fullName
        } else {
            travelerButton.selectTraveler.text = resources.getString(R.string.traveler_saved_contacts_text)
        }
    }

    private fun selectPassport(countryCode: String?) {
        passportCountrySpinner.onItemSelectedListener = null
        val adapter = passportCountrySpinner.adapter as CountrySpinnerAdapter
        val position = if (countryCode?.isNullOrEmpty() ?: true) DEFAULT_EMPTY_PASSPORT else adapter.getPositionByCountryThreeLetterCode(countryCode)
        passportCountrySpinner.setSelection(position, false)
        passportCountrySpinner.onItemSelectedListener = CountryItemSelectedListener()
    }
}