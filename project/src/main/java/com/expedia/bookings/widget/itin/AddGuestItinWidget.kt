package com.expedia.bookings.presenter.trips

import android.app.ProgressDialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.itin.AddGuestItinViewModel

class AddGuestItinWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val findItinButton: Button by bindView(R.id.find_itinerary_button)
    val itinNumberEditText: EditText by bindView(R.id.itin_number_edit_text)
    val guestEmailEditText: EditText by bindView(R.id.email_edit_text)

    private val guestItinSearchDialog = ProgressDialog(context)

    var viewModel: AddGuestItinViewModel by notNullAndObservable { vm ->
        vm.showSearchDialogObservable.subscribe { show ->
            if (show) {
                guestItinSearchDialog.show()
                guestItinSearchDialog.setContentView(R.layout.process_dialog_layout)
                AccessibilityUtil.delayedFocusToView(guestItinSearchDialog.findViewById(R.id.progress_dialog_container), 0)
                guestItinSearchDialog.findViewById(R.id.progress_dialog_container).contentDescription = context.getString(R.string.spinner_text_add_guest_trip)
                announceForAccessibility(context.getString(R.string.spinner_text_add_guest_trip))
            } else {
                guestItinSearchDialog.dismiss()
            }

        }
    }

    init {
        View.inflate(context, R.layout.add_guest_itin_widget, this)
        viewModel = AddGuestItinViewModel(context)
        findItinButton.setOnClickListener {
            viewModel.performGuestTripSearch.onNext(Pair(guestEmailEditText.text.toString(), itinNumberEditText.text.toString()))
        }
        setUpDialog()
    }

    private fun setUpDialog() {
        guestItinSearchDialog.setCancelable(false)
        guestItinSearchDialog.isIndeterminate = true
    }
}