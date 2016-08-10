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
import com.expedia.bookings.widget.traveler.TravelerSelectState
import com.expedia.util.getMainTravelerToolbarTitle
import com.expedia.util.notNullAndObservable
import com.expedia.vm.traveler.CheckoutTravelerViewModel
import com.expedia.vm.traveler.TravelerViewModel
import rx.subjects.PublishSubject

class TravelerPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val travelerSelectState: TravelerSelectState by bindView(R.id.traveler_select_state)
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
            show(travelerSelectState, Presenter.FLAG_CLEAR_BACKSTACK)
        }
        vm.emptyTravelersSubject.subscribe {
            show(travelerSelectState, Presenter.FLAG_CLEAR_BACKSTACK)
        }
    }

    init {
        View.inflate(context, R.layout.traveler_presenter, this)
        travelerSelectState.travelerIndexSelectedSubject.subscribe { selectedTraveler ->
            toolbarTitleSubject.onNext(selectedTraveler.second)
            travelerEntryWidget.viewModel = TravelerViewModel(context, selectedTraveler.first)
            travelerEntryWidget.viewModel.showPassportCountryObservable.onNext(viewModel.passportRequired.value)
            show(travelerEntryWidget)
        }
        doneClicked.subscribe {
            if (travelerEntryWidget.isValid()) {
                viewModel.updateCompletionStatus()
                if (viewModel.validateTravelersComplete()) {
                    closeSubject.onNext(Unit)
                }
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(selectToEntry)
        show(travelerSelectState, Presenter.FLAG_CLEAR_BACKSTACK)
    }

    private val defaultTransition = object : Presenter.DefaultTransition(TravelerSelectState::class.java.name) {
        override fun endTransition(forward: Boolean) {
            menuVisibility.onNext(false)
            travelerSelectState.show()
            travelerEntryWidget.visibility = View.GONE
            boardingWarning.visibility = View.GONE
            dropShadow.visibility = View.VISIBLE
            toolbarTitleSubject.onNext(resources.getString(R.string.traveler_details_text))
            toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.BACK)
        }
    }

    private val selectToEntry = object : Presenter.Transition(TravelerSelectState::class.java,
            FlightTravelerEntryWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            menuVisibility.onNext(forward)
            dropShadow.visibility = View.VISIBLE
            boardingWarning.visibility =  if (forward) View.VISIBLE else View.GONE
            if (!forward) travelerSelectState.show() else travelerSelectState.visibility = View.GONE
            travelerEntryWidget.visibility = if (forward) View.VISIBLE else View.GONE
            travelerEntryWidget.travelerButton.visibility = if (User.isLoggedIn(context) && forward) View.VISIBLE else View.GONE
            if (!forward) {
                toolbarTitleSubject.onNext(resources.getString(R.string.traveler_details_text))
                travelerEntryWidget.travelerButton.dismissPopup()
                Ui.hideKeyboard(this@TravelerPresenter)
            }
        }

        override fun endTransition(forward: Boolean) {
            setToolbarNavIcon(forward)
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
                travelerEntryWidget.viewModel.validate()
            }
        }
    }

    private fun setToolbarNavIcon(forward : Boolean) {
        if(!forward) {
            toolbarNavIconContDescSubject.onNext(resources.getString(R.string.toolbar_nav_icon_cont_desc))
            toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.BACK)
        }
        else {
            toolbarNavIconContDescSubject.onNext(resources.getString(R.string.toolbar_nav_icon_close_cont_desc))
            toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        }
    }

    fun showSelectOrEntryState(status : TravelerCheckoutStatus) {
        if (viewModel.getTravelers().size > 1) {
            toolbarTitleSubject.onNext(resources.getString(R.string.traveler_details_text))
            travelerSelectState.refresh(status, viewModel.getTravelers())
            show(travelerSelectState)
        } else {
            val travelerViewModel = TravelerViewModel(context, 0)
            currentState ?: show(travelerSelectState, FLAG_CLEAR_BACKSTACK)
            travelerEntryWidget.viewModel = travelerViewModel
            travelerEntryWidget.viewModel.showPassportCountryObservable.onNext(viewModel.passportRequired.value)
            toolbarTitleSubject.onNext(getMainTravelerToolbarTitle(resources))
            if (viewModel.travelerCompletenessStatus.value == TravelerCheckoutStatus.DIRTY) {
                travelerEntryWidget.viewModel.validate()
            }
            show(travelerEntryWidget, FLAG_CLEAR_BACKSTACK)
        }
    }

    override fun back(): Boolean {
        menuVisibility.onNext(false)
        return super.back()
    }
}