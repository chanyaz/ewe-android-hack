package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
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
import io.reactivex.subjects.PublishSubject
import io.reactivex.disposables.CompositeDisposable

class RailTravelerEntryWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs), TravelerButton.ITravelerButtonListener {

    val travelerButton: TravelerButton by bindView(R.id.rail_traveler_button)
    val nameEntryView: NameEntryView by bindView(R.id.name_entry_widget)
    val emailEntryView: EmailEntryView by bindView(R.id.email_entry_widget)
    val phoneEntryView: PhoneEntryView by bindView(R.id.phone_entry_widget)

    val travelerCompleteSubject = PublishSubject.create<Unit>()

    val formFilledSubscriber = endlessObserver<String> {
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

    var CompositeDisposable: CompositeDisposable? = null

    private val toolbar: EntryFormToolbar by bindView(R.id.rail_traveler_toolbar)
    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    init {
        View.inflate(context, R.layout.rail_traveler_entry_widget, this)
        travelerButton.visibility = View.GONE
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
        CompositeDisposable?.dispose()
        if (changedView is RailTravelerEntryWidget) {
            if (visibility == View.VISIBLE) {
                CompositeDisposable = CompositeDisposable()
                CompositeDisposable?.add(nameEntryView.firstName.subscribeTextChange(formFilledSubscriber))
                CompositeDisposable?.add(nameEntryView.lastName.subscribeTextChange(formFilledSubscriber))
                CompositeDisposable?.add(phoneEntryView.phoneNumber.subscribeTextChange(formFilledSubscriber))
                CompositeDisposable?.add(emailEntryView.emailAddress.subscribeTextChange(formFilledSubscriber))
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
                && hasFilledEmailIfNecessary()
    }

    private fun hasFilledEmailIfNecessary(): Boolean {
        return (emailEntryView.visibility == View.VISIBLE && emailEntryView.emailAddress.text.isNotEmpty())
                || userStateManager.isUserAuthenticated()
                || emailEntryView.visibility == View.GONE
    }
}