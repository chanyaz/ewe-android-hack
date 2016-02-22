package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.traveler.NameEntryView
import com.expedia.bookings.widget.traveler.PhoneEntryView
import com.expedia.bookings.widget.traveler.TSAEntryView
import com.expedia.vm.traveler.NameEntryViewModel
import com.expedia.vm.traveler.PhoneEntryViewModel
import com.expedia.vm.traveler.TSAEntryViewModel
import com.expedia.vm.traveler.TravelerAdvancedOptionsViewModel
import rx.subjects.PublishSubject

class FlightTravelerEntryWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    var traveler: Traveler? = null

    val nameEntryView: NameEntryView by bindView(R.id.name_entry_widget)
    val phoneEntryView: PhoneEntryView by bindView(R.id.phone_entry_widget)
    val tsaEntryView: TSAEntryView by bindView(R.id.tsa_entry_widget)
    val advancedOptionsWidget: FlightTravelerAdvancedOptionsWidget by bindView(R.id.traveler_advanced_options_widget)

    val doneButton: TextView by bindView(R.id.new_traveler_done_button)
    val advancedButton: TextView by bindView(R.id.advanced_options_button)
    val advancedOptionsIcon: ImageView by bindView(R.id.traveler_advanced_options_icon)

    val travelerCompleteSubject = PublishSubject.create<Traveler>()

    lateinit var nameViewModel: NameEntryViewModel
    lateinit var phoneViewModel: PhoneEntryViewModel
    lateinit var tsaViewModel: TSAEntryViewModel
    lateinit var advancedOptionsViewModel: TravelerAdvancedOptionsViewModel

    init {
        View.inflate(context, R.layout.flight_traveler_entry_widget, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        val isLoggedIn = User.isLoggedIn(context)
        if (isLoggedIn) {
            // User is logged in - default to primary
            if (traveler == null) {
                traveler = Db.getUser().primaryTraveler
            }
            Db.getWorkingTravelerManager().shiftWorkingTraveler(traveler)
            traveler?.setEmail(Db.getUser().primaryTraveler.email)
        }
        if (traveler == null) {
            traveler = Traveler()
        }
        // TODO add logic to determine traveler category.
        traveler?.setPassengerCategory(PassengerCategory.ADULT)

        nameViewModel = NameEntryViewModel(traveler!!)
        phoneViewModel = PhoneEntryViewModel(traveler!!)
        tsaViewModel = TSAEntryViewModel(context, traveler!!)

        nameEntryView.viewModel = nameViewModel
        phoneEntryView.viewModel = phoneViewModel
        tsaEntryView.viewModel = tsaViewModel
        advancedOptionsWidget.viewModel = TravelerAdvancedOptionsViewModel(traveler!!)

        doneButton.setOnClickListener {
            if (isValid()) {
                travelerCompleteSubject.onNext(traveler)
            }
        }

        advancedButton.setOnClickListener {
            if (advancedOptionsWidget.visibility == Presenter.GONE) {
                showAdvancedOptions()
            } else {
                hideAdvancedOptions()
            }
        }
    }

    fun isValid(): Boolean {
        val nameValid = nameViewModel.validate()
        val phoneValid = phoneViewModel.validate()
        val tsaValid = tsaViewModel.validate()

        return nameValid && phoneValid && tsaValid
    }

    private fun showAdvancedOptions() {
        advancedOptionsWidget.visibility = Presenter.VISIBLE
        AnimUtils.rotate(advancedOptionsIcon)
    }

    private fun hideAdvancedOptions() {
        advancedOptionsWidget.visibility = Presenter.GONE
        AnimUtils.reverseRotate(advancedOptionsIcon)
        advancedOptionsIcon.clearAnimation()
    }
}