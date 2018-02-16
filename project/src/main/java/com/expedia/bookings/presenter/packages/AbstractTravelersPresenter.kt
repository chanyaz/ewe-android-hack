package com.expedia.bookings.presenter.packages

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.AbstractTravelerEntryWidget
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.bookings.widget.traveler.TravelerPickerWidget
import com.expedia.util.getMainTravelerToolbarTitle
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeVisibility
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel
import com.expedia.vm.traveler.FlightTravelersViewModel
import com.expedia.vm.traveler.TravelersViewModel
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject

abstract class AbstractTravelersPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val travelerPickerWidget: TravelerPickerWidget by bindView(R.id.traveler_picker_widget)
    val travelerEntryWidget: AbstractTravelerEntryWidget by bindView(R.id.traveler_entry_widget)
    val dropShadow: View by bindView(R.id.drop_shadow)

    val toolbarTitleSubject = PublishSubject.create<String>()
    val menuVisibility = PublishSubject.create<Boolean>()
    val closeSubject = PublishSubject.create<Unit>()
    val toolbarNavIcon = PublishSubject.create<ArrowXDrawableUtil.ArrowDrawableType>()
    val toolbarNavIconContDescSubject = PublishSubject.create<String>()

    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    abstract fun setUpTravelersViewModel(vm: TravelersViewModel)

    abstract fun inflateTravelersView()

    var viewModel: TravelersViewModel by notNullAndObservable { vm ->
        vm.invalidTravelersSubject.subscribe {
            show(travelerPickerWidget, Presenter.FLAG_CLEAR_TOP)
        }
        vm.emptyTravelersSubject.subscribe {
            show(travelerPickerWidget, Presenter.FLAG_CLEAR_TOP)
        }
        vm.showMainTravelerMinAgeMessaging.subscribeVisibility(travelerPickerWidget.mainTravelerMinAgeTextView)
        vm.refreshSelectedTravelerStatus.subscribe {
            travelerPickerWidget.viewModel.selectedTravelerSubject.value?.refreshStatusObservable?.onNext(Unit)
        }
        travelerPickerWidget.viewModel.currentlySelectedTravelerStatusObservable.subscribe { if (it == TravelerCheckoutStatus.DIRTY) vm.isDirtyObservable.onNext(true) }
        setUpTravelersViewModel(vm)
    }

    init {
        inflateTravelersView()
        travelerPickerWidget.viewModel.selectedTravelerSubject.subscribe { travelerSelectItemViewModel ->
            travelerEntryWidget.viewModel = viewModel.createNewTravelerEntryWidgetModel(context, travelerSelectItemViewModel.index, viewModel.passportRequired, travelerSelectItemViewModel.currentStatusObservable.value)
            show(travelerEntryWidget)
            toolbarTitleSubject.onNext(travelerSelectItemViewModel.emptyText)
            if (viewModel.isTravelerEmpty(viewModel.getTraveler(travelerSelectItemViewModel.index))) {
                travelerSelectItemViewModel.currentStatusObservable.onNext(TravelerCheckoutStatus.CLEAN)
                travelerEntryWidget.resetErrorState()
            } else {
                travelerSelectItemViewModel.currentStatusObservable.onNext(TravelerCheckoutStatus.DIRTY)
            }
            if (shouldShowFrequentFlyerWidget()) {
                updateFlightTravelerEntryWidgetViewModel()
            }
        }
    }

    fun onDoneClicked() {
        travelerPickerWidget.viewModel.selectedTravelerSubject.value?.refreshStatusObservable?.onNext(Unit)
        val numberOfInvalidFields = travelerEntryWidget.getNumberOfInvalidFields()
        if (numberOfInvalidFields == 0) {
            viewModel.updateCompletionStatus()
            if (shouldShowTravelerDialog()) {
                showTravelerDialog()
            } else {
                showPickerWidgetOrCloseEntryWidget()
            }
        } else {
            Ui.hideKeyboard(this@AbstractTravelersPresenter)
            travelerEntryWidget.requestFocus()
            val announcementString = StringBuilder()
            announcementString.append(Phrase.from(context.resources.getQuantityString(R.plurals.number_of_errors_TEMPLATE, numberOfInvalidFields))
                    .put("number", numberOfInvalidFields)
                    .format()
                    .toString())
                    .append(" ")
                    .append(context.getString(R.string.accessibility_announcement_please_review_and_resubmit))
            announceForAccessibility(announcementString)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        show(travelerPickerWidget, Presenter.FLAG_CLEAR_BACKSTACK)
    }
    private val defaultTransition = object : Presenter.DefaultTransition(TravelerPickerWidget::class.java.name) {
        override fun endTransition(forward: Boolean) {
            menuVisibility.onNext(false)
            travelerPickerWidget.show()
            travelerEntryWidget.visibility = View.GONE
            dropShadow.visibility = View.VISIBLE
            if (currentState != null) {
                toolbarTitleSubject.onNext(resources.getString(R.string.traveler_details_text))
                viewModel.focusToolbarBackSubject.onNext(Unit)
            }
        }
    }

    open inner class SelectToEntryTransition(className: Class<*>) : Presenter.Transition(TravelerPickerWidget::class.java, className) {
        override fun startTransition(forward: Boolean) {
            travelerEntryWidget.visibility = if (forward) View.VISIBLE else View.GONE
            travelerEntryWidget.travelerButton.visibility = if (userStateManager.isUserAuthenticated() && forward) View.VISIBLE else View.GONE
            if (!forward) travelerPickerWidget.show() else travelerPickerWidget.visibility = View.GONE
            menuVisibility.onNext(forward)
            dropShadow.visibility = View.VISIBLE
            if (!forward) {
                toolbarTitleSubject.onNext(resources.getString(R.string.traveler_details_text))
                travelerEntryWidget.travelerButton.dismissPopup()
                Ui.hideKeyboard(this@AbstractTravelersPresenter)
            } else {
                toolbarTitleSubject.onNext(getMainTravelerToolbarTitle(resources))
                viewModel.doneClickedMethod.onNext { onDoneClicked() }
            }
            viewModel.focusToolbarBackSubject.onNext(Unit)
        }

        override fun endTransition(forward: Boolean) {
            if (forward) {
                travelerEntryWidget.resetStoredTravelerSelection()
                if (viewModel.lob == LineOfBusiness.PACKAGES) {
                    PackagesTracking().trackCheckoutEditTraveler()
                } else if (viewModel.lob == LineOfBusiness.FLIGHTS_V2) {
                    FlightsV2Tracking.trackCheckoutEditTraveler()
                }
            } else {
                travelerPickerWidget.viewModel.selectedTravelerSubject.value?.refreshStatusObservable?.onNext(Unit)
                viewModel.refresh()
            }
        }
    }

    fun showSelectOrEntryState() {
        if (viewModel.requiresMultipleTravelers()) {
            showPickerWidget()
        } else {
            showEntryWidget()
        }
    }

    private fun showEntryWidget() {
        if (currentState == null) show(travelerPickerWidget, FLAG_CLEAR_BACKSTACK)
        travelerEntryWidget.viewModel = viewModel.createNewTravelerEntryWidgetModel(context, 0, viewModel.passportRequired, TravelerCheckoutStatus.CLEAN)
        show(travelerEntryWidget, FLAG_CLEAR_BACKSTACK)

        if (viewModel.travelersCompletenessStatus.value == TravelerCheckoutStatus.DIRTY) {
            travelerEntryWidget.viewModel.validate()
        } else if (viewModel.travelersCompletenessStatus.value == TravelerCheckoutStatus.CLEAN) {
            travelerEntryWidget.resetErrorState()
        }
        if (shouldShowFrequentFlyerWidget()) {
            updateFlightTravelerEntryWidgetViewModel()
        }
    }

    private fun showPickerWidget() {
        toolbarTitleSubject.onNext(resources.getString(R.string.traveler_details_text))
        show(travelerPickerWidget, FLAG_CLEAR_TOP)
    }

    fun resetTravelers() {
        viewModel.isDirtyObservable.onNext(false)
        if (viewModel.requiresMultipleTravelers()) {
            travelerPickerWidget.refresh(viewModel.getTravelers())
        }
    }

    fun updateAllTravelerStatuses() {
        travelerPickerWidget.viewModel.refreshStatusObservable.onNext(Unit)
    }

    fun onLogin(isLoggedIn: Boolean) {
        resetTravelers()
        travelerEntryWidget.emailEntryView.visibility = if (isLoggedIn) GONE else VISIBLE
        viewModel.refresh()
    }

    override fun back(): Boolean {
        menuVisibility.onNext(false)
        if (shouldShowTravelerDialog()) {
            showTravelerDialog()
            return true
        }
        return super.back()
    }

    fun showTravelerDialog() {
        var title = context.getString(R.string.update_traveler_dialog_title)
        var positiveButtonText = context.getString(R.string.update)
        val negativeButtonText = context.getString(R.string.no_thanks)
        var message = Phrase.from(context.getString(R.string.update_traveler_dialog_message_TEMPLATE))
                .put("brand", BuildConfig.brand)
                .format().toString()
        val newTraveler = travelerEntryWidget.viewModel.getTraveler()
        if (!newTraveler.hasTuid()) {
            title = context.getString(R.string.save_traveler)
            positiveButtonText = context.getString(R.string.save)
            message = Phrase.from(context.getString(R.string.save_traveler_dialog_message_TEMPLATE))
                    .put("brand", BuildConfig.brand)
                    .format().toString()
        }

        val alert = AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText) { dialog, _ ->
                    Db.getWorkingTravelerManager().commitTravelerToAccount(context, newTraveler, true)
                    OmnitureTracking.trackUserChoosesToSaveTraveler()
                    closeDialogAndForm(dialog)
                }
                .setNegativeButton(negativeButtonText) { dialog, _ ->
                    OmnitureTracking.trackUserChoosesNotToSaveTraveler()
                    closeDialogAndForm(dialog)
                }

        alert.show()
    }

    private fun closeDialogAndForm(dialog: DialogInterface) {
        dialog.dismiss()
        showPickerWidgetOrCloseEntryWidget()
    }

    fun showPickerWidgetOrCloseEntryWidget() {
        viewModel.refreshSelectedTravelerStatus.onNext(Unit)
        if (!viewModel.allTravelersValid() && viewModel.requiresMultipleTravelers()) {
            showPickerWidget()
        } else {
            closeSubject.onNext(Unit)
        }
    }

    fun shouldShowTravelerDialog(): Boolean {
        return currentState == FlightTravelerEntryWidget::class.java.name &&
                userStateManager.isUserAuthenticated() &&
                (travelerEntryWidget.viewModel.getTraveler().compareTo(Db.getWorkingTravelerManager().workingTraveler)) != 0 &&
                travelerEntryWidget.getNumberOfInvalidFields() == 0
    }

    private fun updateFlightTravelerEntryWidgetViewModel() {
        val flightTravelerEntryWidgetViewModel = travelerEntryWidget.viewModel as FlightTravelerEntryWidgetViewModel
        val flightTravelersViewModel = viewModel as FlightTravelersViewModel
        flightTravelersViewModel.flightLegs?.let { legs ->
            flightTravelerEntryWidgetViewModel.flightLegsObservable.onNext(legs)
        }
        flightTravelersViewModel.frequentFlyerPlans?.let { plans ->
            flightTravelerEntryWidgetViewModel.frequentFlyerPlans.onNext(plans)
        }
    }

    private fun shouldShowFrequentFlyerWidget(): Boolean {
        return viewModel.lob == LineOfBusiness.FLIGHTS_V2
    }
}
