package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.bookings.widget.traveler.TravelerDefaultState
import com.expedia.bookings.widget.traveler.TravelerSelectState
import com.expedia.util.endlessObserver
import com.expedia.util.getCheckoutToolbarTitle
import com.expedia.util.getMainTravelerToolbarTitle
import com.expedia.vm.traveler.CheckoutTravelerViewModel
import com.expedia.vm.traveler.TravelerSummaryViewModel
import com.expedia.vm.traveler.TravelerViewModel
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class TravelerPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val travelerDefaultState: TravelerDefaultState by bindView(R.id.traveler_default_state)
    val travelerSelectState: TravelerSelectState by bindView(R.id.traveler_select_state)
    val travelerEntryWidget: FlightTravelerEntryWidget by bindView(R.id.traveler_entry_widget)

    val expandedSubject = BehaviorSubject.create<Boolean>()
    val travelersCompleteSubject = BehaviorSubject.create<Traveler>()
    val toolbarTitleSubject = PublishSubject.create<String>()

    val menuVisibility = PublishSubject.create<Boolean>()
    val toolbarNavIcon = PublishSubject.create<ArrowXDrawableUtil.ArrowDrawableType>()
    var viewModel = CheckoutTravelerViewModel()

    init {
        View.inflate(context, R.layout.traveler_presenter, this)

        travelerDefaultState.viewModel = TravelerSummaryViewModel(context)

        travelerEntryWidget.travelerCompleteSubject.subscribe(endlessObserver<Traveler> { traveler ->
            if (viewModel.validateTravelersComplete()) {
                viewModel.getTravelers().forEach { traveler ->
                    travelersCompleteSubject.onNext(traveler)
                }
                expandedSubject.onNext(false)
                travelerDefaultState.updateStatus(TravelerCheckoutStatus.COMPLETE)
                show(travelerDefaultState, Presenter.FLAG_CLEAR_BACKSTACK)
            } else {
                show(travelerSelectState, Presenter.FLAG_CLEAR_TOP)
            }
        })

        travelerDefaultState.setOnClickListener {
            if (viewModel.getTravelers().size > 1) {
                travelerSelectState.refresh(travelerDefaultState.status, viewModel.getTravelers())
                show(travelerSelectState)
            } else {
                val travelerViewModel = TravelerViewModel(context, 0)
                showTravelerEntryWidget(travelerViewModel)
            }
        }

        travelerSelectState.travelerIndexSelectedSubject.subscribe { selectedTraveler ->
            toolbarTitleSubject.onNext(selectedTraveler.second)
            val travelerVM = TravelerViewModel(context, selectedTraveler.first)
            showTravelerEntryWidget(travelerVM)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(defaultToEntry)
        addTransition(defaultToSelect)
        addTransition(selectToEntry)
        show(travelerDefaultState, Presenter.FLAG_CLEAR_BACKSTACK)
    }

    fun refreshAndShow(packageParams: PackageSearchParams) {
        viewModel.refreshTravelerList(packageParams)
        travelerDefaultState.updateStatus(TravelerCheckoutStatus.CLEAN)
        visibility = View.VISIBLE
    }

    override fun back(): Boolean {
        val backHandled = super.back()
        val stateIsDefault = backStack.peek() is TravelerDefaultState
        if (backHandled and stateIsDefault) {
            // Lie to our parent presenter and pretend we didn't handle the back
            // TODO, be honest, move default state to be checkout responsibility
            return false
        }
        return backHandled
    }

    private fun validateAndBindTravelerSummary() {
        if (viewModel.validateTravelersComplete()) {
            travelerDefaultState.updateStatus(TravelerCheckoutStatus.COMPLETE)
        } else {
            travelerDefaultState.updateStatus(TravelerCheckoutStatus.DIRTY)
        }
    }

    private fun showTravelerEntryWidget(travelerViewModel: TravelerViewModel) {
        travelerEntryWidget.viewModel = travelerViewModel
        show(travelerEntryWidget)
    }

    private val defaultTransition = object : Presenter.DefaultTransition(TravelerDefaultState::class.java.name) {
        override fun endTransition(forward: Boolean) {
            menuVisibility.onNext(false)
            travelerDefaultState.visibility = if (forward) View.VISIBLE else View.GONE
            travelerSelectState.visibility = if (!forward) View.VISIBLE else View.GONE
            travelerEntryWidget.visibility = if (!forward) View.VISIBLE else View.GONE
            toolbarTitleSubject.onNext(getCheckoutToolbarTitle(resources, false))
        }
    }

    private val defaultToSelect = object : Presenter.Transition(TravelerDefaultState::class.java,
            TravelerSelectState::class.java) {
        override fun startTransition(forward: Boolean) {
            menuVisibility.onNext(false)
            expandedSubject.onNext(forward)
            if (forward) {
                toolbarTitleSubject.onNext(resources.getString(R.string.traveler_details_text))
            } else {
                toolbarTitleSubject.onNext(getCheckoutToolbarTitle(resources, false))
            }
        }

        override fun endTransition(forward: Boolean) {
            if (!forward) validateAndBindTravelerSummary()
            travelerDefaultState.visibility = if (!forward) View.VISIBLE else View.GONE
            if (forward) travelerSelectState.show() else travelerSelectState.visibility = View.GONE
        }
    }

    private val defaultToEntry = object : Presenter.Transition(TravelerDefaultState::class.java,
            FlightTravelerEntryWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            menuVisibility.onNext(forward)
            expandedSubject.onNext(forward)
            travelerEntryWidget.travelerButton.visibility = if (User.isLoggedIn(context) && forward) View.VISIBLE else View.GONE

            if (forward && travelerDefaultState.status == TravelerCheckoutStatus.DIRTY) {
                travelerEntryWidget.viewModel.validate()
            }

            if (forward) {
                toolbarTitleSubject.onNext(getMainTravelerToolbarTitle(resources))
            } else {
                toolbarTitleSubject.onNext(getCheckoutToolbarTitle(resources, false))
                travelerEntryWidget.travelerButton.dismissPopup()
            }
        }

        override fun endTransition(forward: Boolean) {
            if (!forward) validateAndBindTravelerSummary()
            travelerDefaultState.visibility = if (!forward) View.VISIBLE else View.GONE
            travelerEntryWidget.visibility = if (forward) View.VISIBLE else View.GONE
            if (forward) {
                travelerEntryWidget.nameEntryView.firstName.requestFocus()
                travelerEntryWidget.onFocusChange(travelerEntryWidget.nameEntryView.firstName, true)
                Ui.showKeyboard(travelerEntryWidget.nameEntryView.firstName, null)
            }
        }
    }

    private val selectToEntry = object : Presenter.Transition(TravelerSelectState::class.java,
            FlightTravelerEntryWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            menuVisibility.onNext(forward)
            toolbarNavIcon.onNext(if (!forward) ArrowXDrawableUtil.ArrowDrawableType.BACK
            else ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
            travelerEntryWidget.travelerButton.visibility = if (User.isLoggedIn(context) && forward) View.VISIBLE else View.GONE
            if (!forward) {
                toolbarTitleSubject.onNext(resources.getString(R.string.traveler_details_text))
                travelerEntryWidget.travelerButton.dismissPopup()
            }
        }

        override fun endTransition(forward: Boolean) {
            if (!forward) travelerSelectState.show() else travelerSelectState.visibility = View.GONE
            travelerEntryWidget.visibility = if (forward) View.VISIBLE else View.GONE
            if (!forward) {
                travelerEntryWidget.viewModel.validate()
            } else {
                travelerEntryWidget.nameEntryView.firstName.requestFocus()
                travelerEntryWidget.onFocusChange(travelerEntryWidget.nameEntryView.firstName, true)
                Ui.showKeyboard(travelerEntryWidget.nameEntryView.firstName, null)
            }
        }
    }
}