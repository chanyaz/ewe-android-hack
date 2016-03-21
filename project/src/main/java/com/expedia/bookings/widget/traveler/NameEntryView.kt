package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.vm.traveler.TravelerNameViewModel

class NameEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val firstName: TravelerEditText by bindView(R.id.first_name_input)
    val middleInitial: TravelerEditText by bindView(R.id.middle_initial_input)
    val lastName: TravelerEditText by bindView(R.id.last_name_input)

    fun setViewModel(viewModel: TravelerNameViewModel) {
        firstName.setText(viewModel.firstNameSubject.value)
        firstName.subscribeToError(viewModel.firstNameErrorSubject)
        firstName.addTextChangedSubscriber(viewModel.firstNameObserver)

        middleInitial.setText(viewModel.middleNameSubject.value)
        middleInitial.subscribeToError(viewModel.middleNameErrorSubject)
        middleInitial.addTextChangedSubscriber(viewModel.middleNameObserver)

        lastName.setText(viewModel.lastNameSubject.value)
        lastName.subscribeToError(viewModel.lastNameErrorSubject)
        lastName.addTextChangedSubscriber(viewModel.lastNameObserver)
    }

    init {
        View.inflate(context, R.layout.name_entry_view, this)
        orientation = HORIZONTAL
    }
}