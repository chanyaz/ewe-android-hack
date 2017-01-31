package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.widget.TravelerButton
import com.expedia.bookings.widget.shared.EntryFormToolbar
import com.expedia.bookings.widget.traveler.EmailEntryView
import com.expedia.bookings.widget.traveler.NameEntryView
import com.expedia.bookings.widget.traveler.PhoneEntryView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeTextChange
import com.expedia.util.subscribeVisibility
import com.expedia.vm.EntryFormToolbarViewModel
import com.expedia.vm.traveler.SimpleTravelerEntryWidgetViewModel
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription

class RailTravelerEntryWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs), TravelerButton.ITravelerButtonListener {

    val travelerButton: TravelerButton by bindView(R.id.rail_traveler_button)
    val nameEntryView: NameEntryView by bindView(R.id.name_entry_widget)
    val emailEntryView: EmailEntryView by bindView(R.id.email_entry_widget)
    val phoneEntryView: PhoneEntryView by bindView(R.id.phone_entry_widget)

    val travelerCompleteSubject = PublishSubject.create<Unit>()

    val formFilledSubscriber = endlessObserver<String>() {
        toolbarViewModel.formFilledIn.onNext(isCompletelyFilled())
    }

    val toolbarViewModel = EntryFormToolbarViewModel()

    var viewModel: SimpleTravelerEntryWidgetViewModel by notNullAndObservable { vm ->
        nameEntryView.viewModel = vm.nameViewModel
        emailEntryView.viewModel = vm.emailViewModel
        phoneEntryView.viewModel = vm.phoneViewModel
        vm.showTravelerButtonObservable.subscribeVisibility(travelerButton)
        vm.showEmailSubject.subscribeVisibility(emailEntryView)

        vm.selectedTravelerSubject.subscribe { text ->
            travelerButton.updateSelectTravelerText(text)
        }
        vm.clearPopupsSubject.subscribe {
            travelerButton.dismissPopup()
        }
    }

    var compositeSubscription: CompositeSubscription? = null

    private val toolbar: EntryFormToolbar by bindView(R.id.rail_traveler_toolbar)

    init {
        View.inflate(context, R.layout.rail_traveler_entry_widget, this)
        travelerButton.visibility == View.GONE
        travelerButton.setTravelButtonListener(this)
        toolbar.viewModel = toolbarViewModel
        toolbarViewModel.doneClicked.subscribe {
            if (viewModel.validate()) {
                travelerCompleteSubject.onNext(Unit)
            }
        }
        toolbarViewModel.nextClicked.subscribe {
            val nextFocus: View? = findFocus()?.focusSearch(FOCUS_FORWARD)
            nextFocus?.requestFocus() ?: nameEntryView.requestFocus()
        }
    }

    override fun onTravelerChosen(traveler: Traveler) {
        viewModel.updateTraveler(traveler)
    }

    override fun onAddNewTravelerSelected() {
        val newTraveler = Traveler()
        viewModel.updateTraveler(newTraveler)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        compositeSubscription?.unsubscribe()
        if (changedView is RailTravelerEntryWidget) {
            if (visibility == View.VISIBLE) {
                compositeSubscription = CompositeSubscription()
                compositeSubscription?.add(nameEntryView.firstName.subscribeTextChange(formFilledSubscriber))
                compositeSubscription?.add(nameEntryView.lastName.subscribeTextChange(formFilledSubscriber))
                compositeSubscription?.add(phoneEntryView.phoneNumber.subscribeTextChange(formFilledSubscriber))
                compositeSubscription?.add(emailEntryView.emailAddress.subscribeTextChange(formFilledSubscriber))
            }
        }
    }

    fun resetFocusToToolbarNavigationIcon() {
        AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar)
    }

    private fun isCompletelyFilled(): Boolean {
        return nameEntryView.firstName.text.isNotEmpty()
                && nameEntryView.lastName.text.isNotEmpty()
                && phoneEntryView.phoneNumber.text.isNotEmpty()
                && ((emailEntryView.visibility == View.VISIBLE && emailEntryView.emailAddress.text.isNotEmpty()) || User.isLoggedIn(context) || emailEntryView.visibility == View.GONE)
    }
}