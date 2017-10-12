package com.expedia.bookings.itin.widget

import android.content.Context
import android.graphics.Color
import android.support.annotation.VisibleForTesting
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinConfirmationViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText

class ItinConfirmationWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val confirmationNumbers: TextView by bindView(R.id.confirmation_code_text_view)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val confirmationStatus: TextView by bindView(R.id.confirmation_status_text_view)

    var viewModel: FlightItinConfirmationViewModel by notNullAndObservable {
        viewModel.widgetConfirmationNumbersSubject.subscribeText(confirmationNumbers)
        viewModel.widgetConfirmationStatusSubject.subscribeText(confirmationStatus)
    }

    init {
        View.inflate(context, R.layout.widget_itin_flight_confirmation, this)
        confirmationNumbers.movementMethod = LinkMovementMethod.getInstance()
        confirmationNumbers.highlightColor = Color.TRANSPARENT
    }
}