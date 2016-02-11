package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TravelerTextInput
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.traveler.NameEntryViewModel

public class NameEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val firstName: TravelerTextInput by bindView(R.id.first_name_input)
    val middleInitial: TravelerTextInput by bindView(R.id.middle_initial_input)
    val lastName: TravelerTextInput by bindView(R.id.last_name_input)

    var viewModel: NameEntryViewModel by notNullAndObservable { vm ->
        vm.firstNameSubject.subscribeText(firstName.editText)
        vm.middleNameSubject.subscribeText(middleInitial.editText)
        vm.lastNameSubject.subscribeText(lastName.editText)

        vm.firstNameErrorSubject.subscribe { error ->
            firstName.setError()
        }

        vm.middleNameErrorSubject.subscribe { error ->
            middleInitial.setError()
        }

        vm.lastNameErrorSubject.subscribe { error ->
            lastName.setError()
        }


        firstName.editText?.addTextChangedListener(TextInputTextWatcher(vm.firstNameObserver, firstName))
        middleInitial.editText?.addTextChangedListener(TextInputTextWatcher(vm.middleNameObserver, middleInitial))
        lastName.editText?.addTextChangedListener(TextInputTextWatcher(vm.lastNameObserver, lastName))
    }

    init {
        View.inflate(context, R.layout.name_entry_widget, this)
        orientation = HORIZONTAL
    }
}