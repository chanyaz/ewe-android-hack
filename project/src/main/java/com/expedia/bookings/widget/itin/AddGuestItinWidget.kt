package com.expedia.bookings.presenter.trips

import android.content.Context
import android.support.design.widget.TextInputEditText
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEnabled
import com.expedia.util.subscribeMaterialFormsError
import com.expedia.vm.itin.AddGuestItinViewModel

class AddGuestItinWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val findItinButton: Button by bindView(R.id.find_itinerary_button)
    val itinNumberEditText: TextInputEditText by bindView(R.id.itin_number_edit_text)
    val guestEmailEditText: EditText by bindView(R.id.email_edit_text)
    val loadingContainer: LinearLayout by bindView(R.id.loading_container)
    val addGuestFormFieldContainer: LinearLayout by bindView(R.id.outer_container)

    var viewModel: AddGuestItinViewModel by notNullAndObservable { vm ->
        vm.showSearchDialogObservable.subscribe { show ->
            if (show) {
                loadingContainer.visibility = View.VISIBLE
                addGuestFormFieldContainer.visibility = View.GONE
            } else {
                loadingContainer.visibility = View.GONE
                addGuestFormFieldContainer.visibility = View.VISIBLE
            }
        }
        vm.guestItinFetchButtonEnabledObservable.subscribeEnabled(findItinButton)
    }

    init {
        View.inflate(context, R.layout.add_guest_itin_widget, this)
        viewModel = AddGuestItinViewModel(context)
        findItinButton.setOnClickListener {
            viewModel.performGuestTripSearch.onNext(Pair(guestEmailEditText.text.toString(), itinNumberEditText.text.toString()))
        }

        itinNumberEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                (view as EditText).hint = context.getString(R.string.itinerary_number_hint)
            } else {
                (view as EditText).hint = ""
                viewModel.itinNumberValidateObservable.onNext(itinNumberEditText.text.toString())
            }
        }

        guestEmailEditText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                viewModel.emailValidateObservable.onNext(guestEmailEditText.text.toString())
            }
        }

        guestEmailEditText.subscribeMaterialFormsError(viewModel.hasEmailErrorObservable, R.string.email_validation_error_message)
        itinNumberEditText.subscribeMaterialFormsError(viewModel.hasItinErrorObservable, R.string.itinerary_number_error_message)
    }
}