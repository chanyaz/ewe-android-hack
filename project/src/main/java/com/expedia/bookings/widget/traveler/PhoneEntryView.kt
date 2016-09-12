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
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEditText
import com.expedia.vm.traveler.TravelerPhoneViewModel

class PhoneEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val phoneSpinner: TelephoneSpinner by bindView(R.id.edit_phone_number_country_code_spinner)
    val phoneNumber: TravelerEditText by bindView(R.id.edit_phone_number)

    var viewModel: TravelerPhoneViewModel by notNullAndObservable { vm ->
        vm.phoneNumberSubject.subscribeEditText(phoneNumber)
        phoneNumber.subscribeToError(vm.phoneErrorSubject)
        phoneNumber.addTextChangedSubscriber(vm.phoneNumberObserver)

        vm.phoneCountyCodeSubject.subscribe { countryCode ->
            if (!TextUtils.isEmpty(countryCode)) {
                phoneSpinner.update(countryCode, "")
            }
        }
        phoneSpinner.onItemSelectedListener = PhoneSpinnerItemSelected()
        spinnerUpdated()
    }

    init {
        View.inflate(context, R.layout.phone_entry_view, this)
        orientation = HORIZONTAL
        gravity = Gravity.BOTTOM
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