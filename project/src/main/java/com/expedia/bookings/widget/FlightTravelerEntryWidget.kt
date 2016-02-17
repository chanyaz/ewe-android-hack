package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.traveler.NameEntryView
import com.expedia.bookings.widget.traveler.PhoneEntryView
import com.expedia.bookings.widget.traveler.TSAEntryView
import com.expedia.vm.traveler.PhoneEntryViewModel
import com.expedia.vm.traveler.NameEntryViewModel
import com.expedia.vm.traveler.TSAEntryViewModel
import rx.subjects.PublishSubject

public class FlightTravelerEntryWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    var traveler: Traveler? = null

    val nameEntryView: NameEntryView by bindView(R.id.name_entry_widget)
    val phoneEntryView: PhoneEntryView by bindView(R.id.phone_entry_widget)
    val tsaEntryView: TSAEntryView by bindView(R.id.tsa_entry_widget)

    val doneButton: TextView by bindView(R.id.new_traveler_done_button)
    val travelerCompleteSubject = PublishSubject.create<Traveler>()

    lateinit var nameViewModel: NameEntryViewModel
    lateinit var phoneViewModel: PhoneEntryViewModel
    lateinit var tsaViewModel: TSAEntryViewModel

    init {
        View.inflate(context, R.layout.flight_traveler_entry_widget, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        val isLoggedIn = User.isLoggedIn(context)
        if (isLoggedIn) { // User is logged in - default to primary
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

        doneButton.setOnClickListener {
            if (isValid()) {
                travelerCompleteSubject.onNext(traveler)
            }
        }
    }

    fun isValid(): Boolean {
        val nameValid = nameViewModel.validate()
        val phoneValid = phoneViewModel.validate()
        val tsaValid = tsaViewModel.validate()

        return nameValid && phoneValid && tsaValid
    }
}