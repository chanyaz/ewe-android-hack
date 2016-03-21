package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.traveler.TravelerNameViewModel

class NameEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val firstName: TravelerEditText by bindView(R.id.first_name_input)
    val middleInitial: TravelerEditText by bindView(R.id.middle_initial_input)
    val lastName: TravelerEditText by bindView(R.id.last_name_input)

    var viewModel: TravelerNameViewModel by notNullAndObservable {
        viewModel.firstNameSubject.distinctUntilChanged().subscribeText(firstName)
        firstName.subscribeToError(viewModel.firstNameErrorSubject)
        firstName.addTextChangedSubscriber(viewModel.firstNameObserver)

        viewModel.middleNameSubject.distinctUntilChanged().subscribeText(middleInitial)
        middleInitial.subscribeToError(viewModel.middleNameErrorSubject)
        middleInitial.addTextChangedSubscriber(viewModel.middleNameObserver)

        viewModel.lastNameSubject.distinctUntilChanged().subscribeText(lastName)
        lastName.subscribeToError(viewModel.lastNameErrorSubject)
        lastName.addTextChangedSubscriber(viewModel.lastNameObserver)
    }

    init {
        View.inflate(context, R.layout.name_entry_view, this)
        orientation = HORIZONTAL
    }
}