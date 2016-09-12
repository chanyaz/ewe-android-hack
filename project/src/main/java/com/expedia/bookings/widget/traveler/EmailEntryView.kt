package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEditText
import com.expedia.vm.traveler.TravelerEmailViewModel

class EmailEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val emailAddress: TravelerEditText by bindView(R.id.edit_email_address)

    var viewModel: TravelerEmailViewModel by notNullAndObservable { vm ->
        viewModel.emailAddressSubject.subscribeEditText(emailAddress)
        emailAddress.subscribeToError(viewModel.emailErrorSubject)
        emailAddress.addTextChangedSubscriber(viewModel.emailAddressObserver)
    }

    init {
        View.inflate(context, R.layout.email_entry_view, this)
        orientation = HORIZONTAL
        gravity = Gravity.BOTTOM
    }
}