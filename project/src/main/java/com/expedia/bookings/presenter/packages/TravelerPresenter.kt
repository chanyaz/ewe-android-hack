package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.bookings.widget.traveler.TravelerDefaultState
import com.expedia.bookings.widget.traveler.TravelerSelectState
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.traveler.CheckoutTravelerViewModel
import com.expedia.vm.traveler.TravelerSummaryViewModel
import com.expedia.vm.traveler.TravelerViewModel
import rx.subjects.BehaviorSubject

class TravelerPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val travelerDefaultState: TravelerDefaultState by bindView(R.id.traveler_default_state)
    val travelerSelectState: TravelerSelectState by bindView(R.id.traveler_select_state)
    val travelerEntryWidget: FlightTravelerEntryWidget by bindView(R.id.traveler_entry_widget)

    val expandedSubject = BehaviorSubject.create<Boolean>()
    val travelersCompleteSubject = BehaviorSubject.create<Traveler>()

    var viewModel: CheckoutTravelerViewModel  by notNullAndObservable { vm ->
        travelerSelectState.viewModel = vm
    }

    init {
        View.inflate(context, R.layout.traveler_presenter, this)

        travelerDefaultState.viewModel = TravelerSummaryViewModel(context)

        travelerEntryWidget.travelerCompleteSubject.subscribe(endlessObserver<Traveler> { traveler ->
            if (viewModel.validateTravelersComplete()) {
                travelersCompleteSubject.onNext(traveler)
                expandedSubject.onNext(false)
                travelerDefaultState.viewModel.travelerStatusObserver.onNext(TravelerSummaryViewModel.Status.COMPLETE)
                show(travelerDefaultState, Presenter.FLAG_CLEAR_BACKSTACK)
            } else {
                show(travelerSelectState, Presenter.FLAG_CLEAR_TOP)
            }
        })

        travelerDefaultState.setOnClickListener {
            if (viewModel.getTravelers().size > 1) {
                travelerSelectState.refresh()
                show(travelerSelectState)
            } else {
                val travelerViewModel = TravelerViewModel(context, viewModel.getTraveler(0), 1)
                showTravelerEntryWidget(travelerViewModel)
            }
        }

        travelerSelectState.travelerSelectedSubject.subscribe { travelerViewModel ->
            showTravelerEntryWidget(travelerViewModel)
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
        travelerDefaultState.viewModel.travelerStatusObserver.onNext(TravelerSummaryViewModel.Status.EMPTY)
        visibility = View.VISIBLE
    }

    fun validateAndBindTravelerSummary() {
        if (viewModel.getTravelers().isEmpty()) {
            travelerDefaultState.viewModel.travelerStatusObserver.onNext(TravelerSummaryViewModel.Status.EMPTY)
        } else if (viewModel.validateTravelersComplete()) {
            travelerDefaultState.viewModel.travelerStatusObserver.onNext(TravelerSummaryViewModel.Status.INCOMPLETE)
        } else {
            travelerDefaultState.viewModel.travelerStatusObserver.onNext(TravelerSummaryViewModel.Status.COMPLETE)
        }
    }

    private fun showTravelerEntryWidget(travelerViewModel: TravelerViewModel) {
        travelerEntryWidget.viewModel = travelerViewModel
        show(travelerEntryWidget)
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

    private val defaultTransition = object : Presenter.DefaultTransition(TravelerDefaultState::class.java.name) {
        override fun endTransition(forward: Boolean) {
            travelerDefaultState.visibility = if (forward) View.VISIBLE else View.GONE
            travelerSelectState.visibility = if (!forward) View.VISIBLE else View.GONE
            travelerEntryWidget.visibility = if (!forward) View.VISIBLE else View.GONE
        }
    }

    private val defaultToSelect = object : Presenter.Transition(TravelerDefaultState::class.java,
            TravelerSelectState::class.java) {
        override fun startTransition(forward: Boolean) {
            expandedSubject.onNext(forward)
        }

        override fun endTransition(forward: Boolean) {
            if (!forward) validateAndBindTravelerSummary()
            travelerDefaultState.visibility = if (!forward) View.VISIBLE else View.GONE
            travelerSelectState.visibility = if (forward) View.VISIBLE else View.GONE
        }
    }

    private val defaultToEntry = object : Presenter.Transition(TravelerDefaultState::class.java,
            FlightTravelerEntryWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            expandedSubject.onNext(forward)
        }

        override fun endTransition(forward: Boolean) {
            if (!forward) validateAndBindTravelerSummary()
            travelerDefaultState.visibility = if (!forward) View.VISIBLE else View.GONE
            travelerEntryWidget.visibility = if (forward) View.VISIBLE else View.GONE
        }
    }

    private val selectToEntry = object : Presenter.Transition(TravelerSelectState::class.java,
            FlightTravelerEntryWidget::class.java) {

        override fun endTransition(forward: Boolean) {
            travelerSelectState.visibility = if (!forward) View.VISIBLE else View.GONE
            travelerEntryWidget.visibility = if (forward) View.VISIBLE else View.GONE
            if (!forward) {
                travelerEntryWidget.viewModel.validate()
            }
        }
    }
}