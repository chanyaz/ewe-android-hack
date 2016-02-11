package com.expedia.bookings.widget.traveler

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TelephoneSpinner
import com.expedia.bookings.widget.TravelerTextInput
import com.expedia.util.notNullAndObservable
import com.expedia.vm.traveler.PhoneEntryViewModel

public class PhoneEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val phoneSpinner: TelephoneSpinner by bindView(R.id.edit_phone_number_country_code_spinner)
    val phoneNumber: TravelerTextInput by bindView(R.id.edit_phone_number)

    var viewModel: PhoneEntryViewModel by notNullAndObservable { vm ->
        vm.phoneSubject.subscribe { phone ->
            if (!TextUtils.isEmpty(phone.number)) {
                phoneNumber.editText?.setText(phone.number)
            }
            if (!TextUtils.isEmpty(phone.countryCode)) {
                phoneSpinner.update(phone.countryCode, phone.countryName)
            }
        }
        vm.phoneErrorSubject.subscribe { error ->
            phoneNumber.setError()
        }

        phoneNumber.editText?.addTextChangedListener(TextInputTextWatcher(vm.phoneNumberObserver, phoneNumber))
        spinnerUpdated()
    }

    init {
        View.inflate(context, R.layout.phone_entry_widget, this)
        orientation = HORIZONTAL
        setGravity(Gravity.BOTTOM)

        phoneSpinner.onItemSelectedListener = PhoneSpinnerItemSelected()
    }

    private fun spinnerUpdated() {
        viewModel.countryCodeObserver.onNext(phoneSpinner.selectedTelephoneCountryCode)
        viewModel.countryNameObserver.onNext(phoneSpinner.selectedTelephoneCountry)
    }

    private inner class PhoneSpinnerItemSelected(): AdapterView.OnItemSelectedListener {

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // do nothing
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            spinnerUpdated()
            phoneSpinner.updateText()
        }
    }
}