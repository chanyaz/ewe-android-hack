package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeMaterialFormsError
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.traveler.TravelerEmailViewModel

class EmailEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val emailAddress: TravelerEditText by bindView(R.id.edit_email_address)
    var viewModel: TravelerEmailViewModel by notNullAndObservable { vm ->
        emailAddress.viewModel = vm
        emailAddress.subscribeMaterialFormsError(emailAddress.viewModel.errorSubject, R.string.email_validation_error_message)
    }

    init {
        View.inflate(context, R.layout.material_email_entry_view, this)
        orientation = HORIZONTAL
        gravity = Gravity.BOTTOM
    }
}
