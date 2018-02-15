package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.subscribeTextChange
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.TravelerUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.traveler.EmailEntryView
import com.expedia.bookings.widget.traveler.NameEntryView
import com.expedia.bookings.widget.traveler.PhoneEntryView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.traveler.AbstractUniversalCKOTravelerEntryWidgetViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

abstract class AbstractTravelerEntryWidget(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs),
        TravelerButton.ITravelerButtonListener, View.OnFocusChangeListener {

    val ANIMATION_DURATION = 500L

    val travelerButton: TravelerButton by bindView(R.id.traveler_button)
    val nameEntryView: NameEntryView by bindView(R.id.name_entry_widget)
    val emailEntryView: EmailEntryView by bindView(R.id.email_entry_widget)
    val phoneEntryView: PhoneEntryView by bindView(R.id.phone_entry_widget)
    val rootContainer: ViewGroup by bindView(R.id.root_container)

    val nameEntryViewFocused = PublishSubject.create<Boolean>()
    val focusedView = PublishSubject.create<View>()
    val filledIn = PublishSubject.create<Boolean>()

    var viewModel: AbstractUniversalCKOTravelerEntryWidgetViewModel by notNullAndObservable { vm ->
        nameEntryView.viewModel = vm.nameViewModel
        emailEntryView.viewModel = vm.emailViewModel
        phoneEntryView.viewModel = vm.phoneViewModel
        vm.showEmailObservable.subscribeVisibility(emailEntryView)
        vm.showPhoneNumberObservable.subscribeVisibility(phoneEntryView)
        ObservableOld.combineLatest(vm.showEmailObservable, vm.showPhoneNumberObservable, { showEmail, showPhoneNumber ->
            val pointOfSale = PointOfSale.getPointOfSale()
            val nameView = if (pointOfSale.hideMiddleName() || pointOfSale.showLastNameFirst()) {
                nameEntryView.firstName
            } else {
                nameEntryView.lastName
            }
            if (showEmail && showPhoneNumber) {
                nameView.nextFocusForwardId = R.id.edit_email_address
                emailEntryView.emailAddress.nextFocusForwardId = R.id.edit_phone_number
            } else if (showEmail && !showPhoneNumber) {
                nameView.nextFocusForwardId = R.id.edit_email_address
            } else if (!showEmail && showPhoneNumber) {
                nameView.nextFocusForwardId = R.id.edit_phone_number
            } else {
                nameView.nextFocusForwardId = R.id.edit_birth_date_text_btn
            }
        }).subscribe()

        focusedView.subscribe {
            nameEntryViewFocused.onNext(nameEntryView.firstName.hasFocus() || (nameEntryView.middleName?.hasFocus() ?: false) || nameEntryView.lastName.hasFocus())
        }

        setUpViewModel(vm)
    }

    abstract fun setUpViewModel(vm: AbstractUniversalCKOTravelerEntryWidgetViewModel)

    var CompositeDisposable: CompositeDisposable? = null
    val formFilledSubscriber = endlessObserver<String> {
        filledIn.onNext(isCompletelyFilled())
    }

    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    init {
        inflateWidget()
        travelerButton.setTravelButtonListener(this)
        nameEntryView.firstName.addOnFocusChangeListener(this)
        nameEntryView.middleName?.let { it.addOnFocusChangeListener(this) }
        nameEntryView.lastName.addOnFocusChangeListener(this)
        emailEntryView.emailAddress.addOnFocusChangeListener(this)
        phoneEntryView.phoneNumber.addOnFocusChangeListener(this)
        phoneEntryView.phoneEditBox?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                Ui.hideKeyboard(this)
                phoneEntryView.phoneEditBox?.performClick()
            }
            onFocusChange(view, hasFocus)
        }
    }

    abstract fun inflateWidget()

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        CompositeDisposable?.dispose()
        if (changedView is AbstractTravelerEntryWidget) {
            if (visibility == View.VISIBLE) {
                CompositeDisposable = CompositeDisposable()
                CompositeDisposable?.add(nameEntryView.firstName.subscribeTextChange(formFilledSubscriber))
                CompositeDisposable?.add(nameEntryView.lastName.subscribeTextChange(formFilledSubscriber))
                CompositeDisposable?.add(phoneEntryView.phoneNumber.subscribeTextChange(formFilledSubscriber))
                CompositeDisposable?.add(emailEntryView.emailAddress.subscribeTextChange(formFilledSubscriber))
            }
        }
    }

    override fun onTravelerChosen(traveler: Traveler) {
        val passengerCategory = viewModel.getTraveler().passengerCategory
        traveler.passengerCategory = passengerCategory
        viewModel.updateTraveler(traveler)
    }

    override fun onAddNewTravelerSelected() {
        val newTraveler = Traveler()
        val passengerCategory = viewModel.getTraveler().passengerCategory
        newTraveler.passengerCategory = passengerCategory
        newTraveler.phoneCountryCode = viewModel.getTraveler().phoneCountryCode
        viewModel.updateTraveler(newTraveler)
        resetErrorState()
    }

    fun getNumberOfInvalidFields(): Int {
        viewModel.validate()
        return viewModel.numberOfInvalidFields.value
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            focusedView.onNext(v)
        }
    }

    //to be removed for new checkout
    open fun isCompletelyFilled(): Boolean {
        return nameEntryView.firstName.text.isNotEmpty()
                && nameEntryView.lastName.text.isNotEmpty()
                && hasFilledPhoneNumberIfNecessary()
                && hasFilledEmailIfNecessary()
    }

    private fun hasFilledPhoneNumberIfNecessary(): Boolean {
        return !TravelerUtils.isMainTraveler(viewModel.travelerIndex)
                || phoneEntryView.phoneNumber.text.isNotEmpty()
    }

    private fun hasFilledEmailIfNecessary(): Boolean {
        return (emailEntryView.visibility == View.VISIBLE && emailEntryView.emailAddress.text.isNotEmpty())
                || userStateManager.isUserAuthenticated()
                || emailEntryView.visibility == View.GONE
    }

    fun resetStoredTravelerSelection() {
        val traveler = viewModel.getTraveler()
        if (traveler.isStoredTraveler || traveler.hasTuid()) {
            val travelerFullName = traveler.fullNameBasedOnPos
            travelerButton.updateSelectTravelerText(travelerFullName)
        } else {
            travelerButton.updateSelectTravelerText(resources.getString(R.string.traveler_saved_contacts_text))
        }
    }

    open fun resetErrorState() {
        nameEntryView.viewModel.firstNameViewModel.errorSubject.onNext(false)
        nameEntryView.viewModel.lastNameViewModel.errorSubject.onNext(false)
        emailEntryView.viewModel.errorSubject.onNext(false)
        phoneEntryView.phoneNumber.viewModel.errorSubject.onNext(false)
    }
}
