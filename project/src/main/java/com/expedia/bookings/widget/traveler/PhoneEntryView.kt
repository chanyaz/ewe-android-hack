package com.expedia.bookings.widget.traveler

import android.app.AlertDialog
import android.content.Context
import android.telephony.PhoneNumberUtils
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TelephoneSpinnerAdapter
import com.expedia.bookings.widget.accessibility.AccessibleEditTextForSpinner
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeMaterialFormsError
import com.expedia.vm.traveler.TravelerPhoneViewModel

class PhoneEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    var phoneEditBox: AccessibleEditTextForSpinner? = null
    val phoneNumber: TravelerEditText by bindView(R.id.edit_phone_number)
    val phoneAdapter: TelephoneSpinnerAdapter by lazy {
        val adapter = TelephoneSpinnerAdapter(context, R.layout.material_item)
        adapter.currentPosition = adapter.getPositionFromName(viewModel.phoneCountryNameSubject.value)
        adapter
    }

    val countryDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.country))

        builder.setSingleChoiceItems(phoneAdapter, phoneAdapter.currentPosition, { dialogInterface, position ->
            val countryCode = phoneAdapter.getCountryCode(position)
            viewModel.countryCodeObserver.onNext(countryCode)
            viewModel.countryNameObserver.onNext(phoneAdapter.getCountryName(position))
            viewModel.phoneCountryCodeSubject.onNext(countryCode.toString())
            dialogInterface.dismiss()
        })

        builder.create()
    }

    var viewModel: TravelerPhoneViewModel by notNullAndObservable { vm ->
        phoneNumber.viewModel = vm.phoneViewModel
        vm.phoneCountryCodeSubject.subscribe { countryCode ->
            phoneEditBox?.setText("+$countryCode")
        }

        phoneNumber.subscribeMaterialFormsError(phoneNumber.viewModel.errorSubject, R.string.phone_validation_error_message)
        phoneEditBox?.subscribeMaterialFormsError(viewModel.phoneCountryCodeErrorSubject, R.string.error_select_a_valid_country_code, R.drawable.material_dropdown)
        phoneEditBox?.setOnClickListener {
            countryDialog.show()
        }
        vm.phoneCountryNameSubject.subscribe { name ->
            phoneAdapter.currentPosition = phoneAdapter.getPositionFromName(name)
        }
        if (PointOfSale.getPointOfSale().shouldFormatTravelerPhoneNumber()) {
            phoneNumber.viewModel.textSubject.subscribe { number ->
                phoneNumber.setText(PhoneNumberUtils.formatNumber(number))
                val selection = phoneNumber.text?.length ?: 0
                phoneNumber.setSelection(selection)
            }
        }
    }

    init {
        View.inflate(context, R.layout.material_phone_entry_view, this)
        phoneEditBox = findViewById<AccessibleEditTextForSpinner>(R.id.material_edit_phone_number_country_code)
        orientation = HORIZONTAL
    }
}
