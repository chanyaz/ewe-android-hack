package com.expedia.bookings.widget

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.section.CountrySpinnerAdapter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.bookings.widget.traveler.NameEntryView
import com.expedia.bookings.widget.traveler.PhoneEntryView
import com.expedia.bookings.widget.traveler.TSAEntryView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeVisibility
import com.expedia.vm.traveler.TravelerViewModel
import com.jakewharton.rxbinding.widget.RxTextView
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription

class FlightTravelerEntryWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
        TravelerButton.ITravelerButtonListener, View.OnFocusChangeListener {

    val ANIMATION_DURATION = 500L
    val travelerButton: TravelerButton by bindView(R.id.traveler_button)
    val nameEntryView: NameEntryView by bindView(R.id.name_entry_widget)
    val phoneEntryView: PhoneEntryView by bindView(R.id.phone_entry_widget)
    val tsaEntryView: TSAEntryView by bindView(R.id.tsa_entry_widget)
    val passportCountrySpinner: Spinner by bindView(R.id.passport_country_spinner)
    val advancedOptionsWidget: FlightTravelerAdvancedOptionsWidget by bindView(R.id.traveler_advanced_options_widget)
    val advancedButton: LinearLayout by bindView(R.id.traveler_advanced_options_button)
    val advancedOptionsIcon: ImageView by bindView(R.id.traveler_advanced_options_icon)

    val travelerCompleteSubject = PublishSubject.create<Traveler>()
    val focusedView = PublishSubject.create<EditText>()
    val filledIn = PublishSubject.create<Boolean>()
    val doneClicked = PublishSubject.create<Unit>()

    val resizeOpenAnimator: ResizeHeightAnimator by lazy {
        val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        advancedOptionsWidget.measure(widthMeasureSpec, heightMeasureSpec);
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
    }

    var compositeSubscription: CompositeSubscription? = null
    val formFilledSubscriber = endlessObserver<TextViewAfterTextChangeEvent>() {
        filledIn.onNext(isCompletelyFilled())
    }

    init {
        View.inflate(context, R.layout.flight_traveler_entry_widget, this)
        travelerButton.visibility == View.GONE
        val adapter = CountrySpinnerAdapter(context, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME,
                R.layout.material_spinner_item, R.layout.spinner_dropdown_item, false)
        adapter.setPrefix(context.getString(R.string.passport_country_colon))
        adapter.setColoredPrefix(false)

        passportCountrySpinner.adapter = adapter
        passportCountrySpinner.onItemSelectedListener = CountryItemSelectedListener()
        travelerButton.setTravelButtonListener(this)
        nameEntryView.firstName.onFocusChangeListener = this
        nameEntryView.middleName.onFocusChangeListener = this
        nameEntryView.lastName.onFocusChangeListener = this
        phoneEntryView.phoneNumber.onFocusChangeListener = this
        advancedOptionsWidget.redressNumber.onFocusChangeListener = this

        doneClicked.subscribe {
            if (isValid()) {
                travelerCompleteSubject.onNext(viewModel.getTraveler())
            }
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            compositeSubscription = CompositeSubscription()
            compositeSubscription?.add(RxTextView.afterTextChangeEvents(nameEntryView.firstName).distinctUntilChanged().subscribe(formFilledSubscriber))
            compositeSubscription?.add(RxTextView.afterTextChangeEvents(nameEntryView.lastName).distinctUntilChanged().subscribe(formFilledSubscriber))
            compositeSubscription?.add(RxTextView.afterTextChangeEvents(phoneEntryView.phoneNumber).distinctUntilChanged().subscribe(formFilledSubscriber))
        } else {
            compositeSubscription?.unsubscribe()
        }
    }

    override fun onTravelerChosen(traveler: Traveler) {
        viewModel.updateTraveler(traveler)
        selectPassport(traveler.primaryPassportCountry)
    }

    override fun onAddNewTravelerSelected() {
        val newTraveler = Traveler()
        val passengerCategory = viewModel.getTraveler().getPassengerCategory(Db.getPackageParams())
        newTraveler.setPassengerCategory(passengerCategory)
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

    private fun isValid(): Boolean {
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
        }
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            focusedView.onNext(v as EditText)
        }
    }

    fun isCompletelyFilled(): Boolean {
        return nameEntryView.firstName.text.isNotEmpty() &&
                nameEntryView.lastName.text.isNotEmpty() &&
                phoneEntryView.phoneNumber.text.isNotEmpty()
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
        val adapter = passportCountrySpinner.adapter as CountrySpinnerAdapter
        val position = if (countryCode?.isNullOrEmpty() ?: true) adapter.defaultLocalePosition else adapter.getPositionByCountryThreeLetterCode(countryCode)
        passportCountrySpinner.setSelection(position)
    }
}