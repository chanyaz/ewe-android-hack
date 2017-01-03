package com.expedia.bookings.widget.traveler

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.section.GenderSpinnerAdapter
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeMaterialFormsError
import com.expedia.vm.traveler.TravelerTSAViewModel
import org.joda.time.LocalDate

class TSAEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs),
        DatePickerDialogFragment.DateChosenListener {

    val fragmentActivity: FragmentActivity
    private val TAG_DATE_PICKER = "TAG_DATE_PICKER"

    val dateOfBirth: TravelerEditText by bindView(R.id.edit_birth_date_text_btn)
    val genderSpinner: TravelerSpinner by bindView(R.id.edit_gender_spinner)
    val materialFormTestEnabled = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
            AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, R.string.preference_universal_checkout_material_forms)


    var viewModel: TravelerTSAViewModel by notNullAndObservable { vm ->
        dateOfBirth.viewModel = vm.dateOfBirthViewModel
        genderSpinner.viewModel = vm.genderViewModel

        genderSpinner.onItemSelectedListener = GenderItemSelectedListener()
        vm.genderViewModel.genderSubject.subscribe { gender ->
            val adapter = genderSpinner.adapter as GenderSpinnerAdapter
            genderSpinner.onItemSelectedListener = null
            genderSpinner.setSelection(adapter.getGenderPosition(gender))
            genderSpinner.onItemSelectedListener = GenderItemSelectedListener()
        }
        vm.dateOfBirthViewModel.birthErrorTextSubject.subscribe { text ->
            showBirthdateErrorDialog(text)
        }
        vm.genderViewModel.errorSubject.subscribe { hasError ->
            if (!materialFormTestEnabled) {
                (genderSpinner.adapter as GenderSpinnerAdapter).setErrorVisible(hasError)
                genderSpinner.valid = !hasError
            } else {
                val genderErrorMessage = findViewById(R.id.gender_error_message) as TextView
                genderErrorMessage.visibility = if (hasError) View.VISIBLE else View.GONE
            }
        }

        if (materialFormTestEnabled) {
            dateOfBirth.subscribeMaterialFormsError(dateOfBirth.viewModel.errorSubject, context.getString(R.string.date_of_birth_validation_error_message))
        }
    }

    init {
        if (materialFormTestEnabled) {
            View.inflate(context, R.layout.material_tsa_entry_view, this)
        } else {
            View.inflate(context, R.layout.tsa_entry_view, this)
            gravity = Gravity.BOTTOM
        }
        orientation = HORIZONTAL

        fragmentActivity = context as FragmentActivity

        val genderAdapter = GenderSpinnerAdapter(context, R.layout.material_spinner_item, R.layout.spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter
        dateOfBirth.setOnClickListener(DateOfBirthClickListener(this))
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val isExtraPaddingRequired = Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP
        if (isExtraPaddingRequired) {
            val editTextSpacing = context.getResources().getDimensionPixelSize(R.dimen.checkout_earlier_api_version_edit_text_spacing)
            dateOfBirth.setPadding(dateOfBirth.paddingLeft, dateOfBirth.paddingTop, dateOfBirth.paddingRight, editTextSpacing)
        }
    }

    override fun handleDateChosen(year: Int, month: Int, day: Int, formattedDate: String) {
        viewModel.dateOfBirthViewModel.dateOfBirthObserver.onNext(LocalDate(year, month, day))
    }

    private inner class GenderItemSelectedListener() : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            //do nothing
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val adapter = genderSpinner.adapter as GenderSpinnerAdapter
            viewModel.genderViewModel.genderSubject.onNext(adapter.getGender(position))
            val gender = adapter.getGender(position)
            if (gender != Traveler.Gender.GENDER) {
                (genderSpinner.adapter as GenderSpinnerAdapter).setErrorVisible(false)
            }
        }
    }

    private inner class DateOfBirthClickListener(val dateSetListener: DatePickerDialogFragment.DateChosenListener) : OnClickListener {
        override fun onClick(v: View?) {
            var date = viewModel.dateOfBirthViewModel.birthDateSubject.value
            if (date == null) {
                date = viewModel.dateOfBirthViewModel.defaultDateSubject.value
            }

            var newDatePickerFragment: DatePickerDialogFragment? = Ui.findSupportFragment<DatePickerDialogFragment>(fragmentActivity, TAG_DATE_PICKER)
            if (newDatePickerFragment == null) {
                newDatePickerFragment = DatePickerDialogFragment.createFragment(dateSetListener, date)
            }
            newDatePickerFragment!!.show(fragmentActivity.supportFragmentManager, TAG_DATE_PICKER)
        }
    }

    private fun showBirthdateErrorDialog(message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.traveler_age_title)
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.DONE), object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                dialog.dismiss()
            }
        })
        val dialog = builder.create()
        dialog.show()
    }

}