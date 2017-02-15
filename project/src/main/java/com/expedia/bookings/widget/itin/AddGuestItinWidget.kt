package com.expedia.bookings.presenter.trips

import android.app.Activity
import android.content.Context
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEnabled
import com.expedia.util.subscribeMaterialFormsError
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.itin.AddGuestItinViewModel
import com.mobiata.android.util.Ui

class AddGuestItinWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val findItinButton: Button by bindView(R.id.find_itinerary_button)
    val itinNumberEditText: TextInputEditText by bindView(R.id.itin_number_edit_text)
    val guestEmailEditText: EditText by bindView(R.id.email_edit_text)
    val loadingContainer: LinearLayout by bindView(R.id.loading_container)
    val addGuestFormFieldContainer: LinearLayout by bindView(R.id.outer_container)
    val unableToFindItinErrorText: TextView by bindView(R.id.unable_to_find_itin_error_message)
    val toolbar: Toolbar by bindView(R.id.toolbar)

    var viewModel: AddGuestItinViewModel by notNullAndObservable { vm ->
        vm.showSearchDialogObservable.subscribe { show ->
            if (show) {
                loadingContainer.visibility = View.VISIBLE
                addGuestFormFieldContainer.visibility = View.GONE
            } else {
                loadingContainer.visibility = View.GONE
                addGuestFormFieldContainer.visibility = View.VISIBLE
            }
            Ui.hideKeyboard(this)
        }
        vm.guestItinFetchButtonEnabledObservable.subscribeEnabled(findItinButton)
        vm.showErrorObservable.subscribeVisibility(unableToFindItinErrorText)
        vm.showErrorMessageObservable.subscribeText(unableToFindItinErrorText)
        vm.emailFieldFocusObservable.subscribe {
            guestEmailEditText.requestFocus()
            Ui.showKeyboard(guestEmailEditText, null)
        }
    }

    init {
        View.inflate(context, R.layout.add_guest_itin_widget, this)
        orientation = VERTICAL
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

        itinNumberEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.itinNumberValidateObservable.onNext(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        guestEmailEditText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                viewModel.emailValidateObservable.onNext(guestEmailEditText.text.toString())
            }
        }

        guestEmailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.emailValidateObservable.onNext(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        guestEmailEditText.subscribeMaterialFormsError(viewModel.hasEmailErrorObservable, R.string.email_validation_error_message)
        itinNumberEditText.subscribeMaterialFormsError(viewModel.hasItinErrorObservable, R.string.itinerary_number_error_message)

        toolbar.setNavigationOnClickListener {
            viewModel.toolBarVisibilityObservable.onNext(true)
            (context as Activity).onBackPressed()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.setTitle(R.string.find_guest_itinerary_title)
    }
}