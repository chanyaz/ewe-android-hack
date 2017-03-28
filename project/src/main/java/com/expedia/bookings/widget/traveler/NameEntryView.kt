package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isMaterialFormsEnabled
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeMaterialFormsError
import com.expedia.vm.traveler.TravelerNameViewModel

class NameEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val firstName: TravelerEditText by bindView(R.id.first_name_input)
    val middleName: TravelerEditText by bindView(R.id.middle_name_input)
    val lastName: TravelerEditText by bindView(R.id.last_name_input)
    val materialFormTestEnabled = isMaterialFormsEnabled(context)

    var viewModel: TravelerNameViewModel by notNullAndObservable { vm ->
        firstName.viewModel = vm.firstNameViewModel
        middleName.viewModel = vm.middleNameViewModel
        lastName.viewModel = vm.lastNameViewModel

        if (materialFormTestEnabled) {
            firstName.subscribeMaterialFormsError(firstName.viewModel.errorSubject, R.string.first_name_validation_error_message)
            middleName.subscribeMaterialFormsError(middleName.viewModel.errorSubject, R.string.middle_name_validation_error_message)
            lastName.subscribeMaterialFormsError(lastName.viewModel.errorSubject, R.string.last_name_validation_error_message)
        }

    }

    init {
        View.inflate(context, if (materialFormTestEnabled) R.layout.material_name_entry_view else R.layout.name_entry_view, this)
        orientation = VERTICAL
    }
}