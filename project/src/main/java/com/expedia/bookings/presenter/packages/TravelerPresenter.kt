package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.User
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.traveler.TravelerPickerWidget
import com.expedia.util.getMainTravelerToolbarTitle
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeVisibility
import com.expedia.vm.traveler.CheckoutTravelerViewModel
import com.expedia.vm.traveler.FlightTravelerViewModel
import rx.subjects.PublishSubject

class TravelerPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val travelerPickerWidget: TravelerPickerWidget by bindView(R.id.traveler_picker_widget)
    val travelerEntryWidget: FlightTravelerEntryWidget by bindView(R.id.traveler_entry_widget)
    val boardingWarning: TextView by bindView(R.id.boarding_warning)
    val dropShadow: View by bindView(R.id.drop_shadow)

    val toolbarTitleSubject = PublishSubject.create<String>()
    val menuVisibility = PublishSubject.create<Boolean>()
    val doneClicked = PublishSubject.create<Unit>()
    val closeSubject = PublishSubject.create<Unit>()
    val toolbarNavIcon = PublishSubject.create<ArrowXDrawableUtil.ArrowDrawableType>()
    val toolbarNavIconContDescSubject = PublishSubject.create<String>()

    var viewModel: CheckoutTravelerViewModel by notNullAndObservable { vm ->
        vm.invalidTravelersSubject.subscribe {
            show(travelerPickerWidget, Presenter.FLAG_CLEAR_TOP)
        }
        vm.emptyTravelersSubject.subscribe {
            show(travelerPickerWidget, Presenter.FLAG_CLEAR_TOP)
        }
        vm.showMainTravelerMinAgeMessaging.subscribeVisibility(travelerPickerWidget.mainTravelerMinAgeTextView)
        vm.singleTravelerCompletenessStatus.subscribe {
            val selectedTraveler = travelerPickerWidget.travelerIndexSelectedSubject.value
            if (selectedTraveler != null) {
                val selectedTravelerPickerTravelerViewModel = travelerPickerWidget.travelerViewModels[selectedTraveler.first]
                selectedTravelerPickerTravelerViewModel.updateStatus(it)
            }
        }
        vm.passportRequired.subscribe(travelerPickerWidget.passportRequired)
        vm.passportRequired.subscribe { isPassportRequired ->
            travelerPickerWidget.travelerViewModels.forEach { vm ->
                vm.isPassportRequired = isPassportRequired
            }
        }
    }

    init {
        View.inflate(context, R.layout.traveler_presenter, this)

        travelerPickerWidget.travelerIndexSelectedSubject.subscribe { selectedTraveler ->
            show(travelerEntryWidget)
            toolbarTitleSubject.onNext(selectedTraveler.second)
            travelerEntryWidget.viewModel = FlightTravelerViewModel(context, selectedTraveler.first, viewModel.passportRequired.value)
            travelerEntryWidget.viewModel.showPassportCountryObservable.subscribe(travelerPickerWidget.passportRequired)
            val selectedTravelerPickerTravelerViewModel = travelerPickerWidget.travelerViewModels[selectedTraveler.first]
            if (selectedTravelerPickerTravelerViewModel.status == TravelerCheckoutStatus.DIRTY) {
                travelerEntryWidget.viewModel.validate()
            }
        }

        travelerEntryWidget.nameEntryViewFocused.subscribeVisibility(boardingWarning)

        doneClicked.subscribe {
            if (travelerEntryWidget.isValid()) {
                viewModel.updateCompletionStatus()
                if (viewModel.allTravelersValid()) {
                    closeSubject.onNext(Unit)
                }
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(selectToEntry)
        show(travelerPickerWidget, Presenter.FLAG_CLEAR_BACKSTACK)
    }

    private val defaultTransition = object : Presenter.DefaultTransition(TravelerPickerWidget::class.java.name) {
        override fun endTransition(forward: Boolean) {
            menuVisibility.onNext(false)
            travelerPickerWidget.show()
            travelerEntryWidget.visibility = View.GONE
            boardingWarning.visibility = View.GONE
            dropShadow.visibility = View.VISIBLE
            if (currentState != null) {
                toolbarTitleSubject.onNext(resources.getString(R.string.traveler_details_text))
            }
        }
    }

    private val selectToEntry = object : Presenter.Transition(TravelerPickerWidget::class.java,
            FlightTravelerEntryWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            travelerEntryWidget.visibility = if (forward) View.VISIBLE else View.GONE
            travelerEntryWidget.travelerButton.visibility = if (User.isLoggedIn(context) && forward) View.VISIBLE else View.GONE
            if (!forward) travelerPickerWidget.show() else travelerPickerWidget.visibility = View.GONE
            menuVisibility.onNext(forward)
            dropShadow.visibility = View.VISIBLE
            boardingWarning.visibility =  if (forward) View.VISIBLE else View.GONE
            if (!forward) {
                toolbarTitleSubject.onNext(resources.getString(R.string.traveler_details_text))
                travelerEntryWidget.travelerButton.dismissPopup()
                Ui.hideKeyboard(this@TravelerPresenter)
            }
        }

        override fun endTransition(forward: Boolean) {
            if (forward) {
                travelerEntryWidget.resetStoredTravelerSelection()
                travelerEntryWidget.nameEntryView.firstName.requestFocus()
                travelerEntryWidget.onFocusChange(travelerEntryWidget.nameEntryView.firstName, true)
                Ui.showKeyboard(travelerEntryWidget.nameEntryView.firstName, null)
                if (viewModel.lob == LineOfBusiness.PACKAGES) {
                    PackagesTracking().trackCheckoutEditTraveler()
                } else if (viewModel.lob == LineOfBusiness.FLIGHTS_V2) {
                    FlightsV2Tracking.trackCheckoutEditTraveler()
                }
            } else {
                viewModel.updateCompletionStatusForTraveler(travelerEntryWidget.viewModel.travelerIndex)
                viewModel.refresh()
            }
        }
    }

    fun showSelectOrEntryState() {
        if (viewModel.getTravelers().size > 1) {
            toolbarTitleSubject.onNext(resources.getString(R.string.traveler_details_text))
            updateAllTravelerStatuses()
            show(travelerPickerWidget, FLAG_CLEAR_TOP)
        } else {
            if (currentState == null) show(travelerPickerWidget, FLAG_CLEAR_BACKSTACK)
            show(travelerEntryWidget, FLAG_CLEAR_BACKSTACK)

            val travelerViewModel = FlightTravelerViewModel(context, 0, viewModel.passportRequired.value)
            travelerEntryWidget.viewModel = travelerViewModel
            toolbarTitleSubject.onNext(getMainTravelerToolbarTitle(resources))
            if (viewModel.travelerCompletenessStatus.value == TravelerCheckoutStatus.DIRTY) {
                travelerEntryWidget.viewModel.validate()
            }
        }
    }

    fun resetTravelers() {
        travelerPickerWidget.refresh(viewModel.getTravelers())
        viewModel.resetCompleteness()
    }

    fun updateAllTravelerStatuses() {
        viewModel.getTravelers().forEachIndexed { i, traveler ->
            if (viewModel.isValidForBooking(traveler, i)) {
                travelerPickerWidget.travelerViewModels[i].updateStatus(TravelerCheckoutStatus.COMPLETE)
            } else if (viewModel.isTravelerEmpty(traveler)) {
                travelerPickerWidget.travelerViewModels[i].updateStatus(TravelerCheckoutStatus.CLEAN)
            } else {
                travelerPickerWidget.travelerViewModels[i].updateStatus(TravelerCheckoutStatus.DIRTY)
            }
        }
    }

    fun onLogin(isLoggedIn: Boolean) {
        resetTravelers()
        travelerEntryWidget.emailEntryView.visibility = if (isLoggedIn) GONE else VISIBLE
        viewModel.refresh()
    }

    override fun back(): Boolean {
        menuVisibility.onNext(false)
        return super.back()
    }
}