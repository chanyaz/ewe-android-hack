package com.expedia.bookings.widget.rail

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.traveler.EmailEntryView
import com.expedia.bookings.widget.traveler.NameEntryView
import com.expedia.bookings.widget.traveler.PhoneEntryView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.traveler.SimpleTravelerViewModel
import rx.subjects.PublishSubject

class RailTravelerEntryWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val nameEntryView: NameEntryView by bindView(R.id.name_entry_widget)
    val emailEntryView: EmailEntryView by bindView(R.id.email_entry_widget)
    val phoneEntryView: PhoneEntryView by bindView(R.id.phone_entry_widget)

    val travelerCompleteSubject = PublishSubject.create<Unit>()

    val doneSelectedObserver = endlessObserver<Unit> {
        if (viewModel.validate()) {
            travelerCompleteSubject.onNext(Unit)
        }
    }

    var viewModel: SimpleTravelerViewModel by notNullAndObservable { vm ->
        nameEntryView.viewModel = vm.nameViewModel
        emailEntryView.viewModel = vm.emailViewModel
        phoneEntryView.viewModel = vm.phoneViewModel
    }

    init {
        View.inflate(context, R.layout.rail_traveler_entry_widget, this)
    }

    fun getToolbarTitle() : String {
        return context.getString(R.string.traveler_enter_details_text)
    }
}