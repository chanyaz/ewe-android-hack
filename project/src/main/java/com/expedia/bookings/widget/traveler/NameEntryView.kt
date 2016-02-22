package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.traveler.NameEntryViewModel

class NameEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val firstName: TravelerEditText by bindView(R.id.first_name_input)
    val middleInitial: TravelerEditText by bindView(R.id.middle_initial_input)
    val lastName: TravelerEditText by bindView(R.id.last_name_input)

    var viewModel: NameEntryViewModel by notNullAndObservable { vm ->
        vm.firstNameSubject.subscribeText(firstName)
        vm.middleNameSubject.subscribeText(middleInitial)
        vm.lastNameSubject.subscribeText(lastName)

        vm.firstNameErrorSubject.subscribe { error ->
            firstName.setError()
        }

        vm.middleNameErrorSubject.subscribe { error ->
            middleInitial.setError()
        }

        vm.lastNameErrorSubject.subscribe { error ->
            lastName.setError()
        }


        firstName.addTextChangedListener(TravelerEditTextWatcher(vm.firstNameObserver, firstName))
        middleInitial.addTextChangedListener(TravelerEditTextWatcher(vm.middleNameObserver, middleInitial))
        lastName.addTextChangedListener(TravelerEditTextWatcher(vm.lastNameObserver, lastName))
    }

    init {
        View.inflate(context, R.layout.name_entry_view, this)
        orientation = HORIZONTAL
    }
}