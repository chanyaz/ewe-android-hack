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
    var genderSpinner: TravelerSpinner? = null
    var genderEditText: TravelerEditText? = null

    val materialFormTestEnabled = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
            AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, R.string.preference_universal_checkout_material_forms)


    var viewModel: TravelerTSAViewModel by notNullAndObservable { vm ->
        dateOfBirth.viewModel = vm.dateOfBirthViewModel
        if (materialFormTestEnabled) {
            genderEditText?.viewModel = vm.genderViewModel
        } else {
            genderSpinner?.viewModel = vm.genderViewModel
            vm.genderViewModel.genderSubject.subscribe { gender ->
                val adapter = genderSpinner?.adapter as GenderSpinnerAdapter
                genderSpinner?.onItemSelectedListener = null
                genderSpinner?.setSelection(adapter.getGenderPosition(gender))
                genderSpinner?.onItemSelectedListener = GenderItemSelectedListener()
            }
            genderSpinner?.onItemSelectedListener = GenderItemSelectedListener()

        }

        vm.dateOfBirthViewModel.birthErrorTextSubject.subscribe { text ->
            showBirthdateErrorDialog(text)
        }
        vm.genderViewModel.errorSubject.subscribe { hasError ->
            if (!materialFormTestEnabled) {
                (genderSpinner?.adapter as GenderSpinnerAdapter).setErrorVisible(hasError)
                genderSpinner?.valid = !hasError
            } else {
                genderEditText?.subscribeMaterialFormsError(vm.genderViewModel.errorSubject, R.string.gender_validation_error_message,
                        R.drawable.material_dropdown)
            }
        }

        if (materialFormTestEnabled) {
            dateOfBirth.subscribeMaterialFormsError(dateOfBirth.viewModel.errorSubject, R.string.date_of_birth_validation_error_message,
                    R.drawable.material_dropdown)
        }
    }

    init {
        if (materialFormTestEnabled) {
            View.inflate(context, R.layout.material_tsa_entry_view, this)
            genderEditText = this.findViewById(R.id.edit_gender_btn) as TravelerEditText
            genderEditText?.setOnClickListener {
                showGenderAlertDialog()
            }
        } else {
            View.inflate(context, R.layout.tsa_entry_view, this)
            gravity = Gravity.BOTTOM
            genderSpinner = this.findViewById(R.id.edit_gender_spinner) as TravelerSpinner
            val genderAdapter = GenderSpinnerAdapter(context, R.layout.material_spinner_item, R.layout.spinner_dropdown_item)
            genderSpinner?.adapter = genderAdapter

        }
        orientation = HORIZONTAL
        fragmentActivity = context as FragmentActivity
        dateOfBirth.setOnClickListener(DateOfBirthClickListener(this))
    }

    private fun showGenderAlertDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.gender))
        val items = arrayOf(context.resources.getString(R.string.male), context.resources.getString(R.string.female))

        builder.setItems(items) { dialog, position ->
            genderEditText?.setText(items[position])
            items[position]
            viewModel.genderViewModel.genderSubject.onNext(Traveler.Gender.valueOf(items[position].toUpperCase()))
            genderEditText?.viewModel?.errorSubject?.onNext(false)
        }

        val alert = builder.create()
        alert.show()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val isExtraPaddingRequired = Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP
        if (isExtraPaddingRequired) {
            val editTextSpacing = context.resources.getDimensionPixelSize(R.dimen.checkout_earlier_api_version_edit_text_spacing)
            dateOfBirth.setPadding(dateOfBirth.paddingLeft, dateOfBirth.paddingTop, dateOfBirth.paddingRight, editTextSpacing)
            genderEditText?.setPadding(dateOfBirth.paddingLeft, dateOfBirth.paddingTop, dateOfBirth.paddingRight, editTextSpacing)
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
            val adapter = genderSpinner?.adapter as GenderSpinnerAdapter
            viewModel.genderViewModel.genderSubject.onNext(adapter.getGender(position))
            val gender = adapter.getGender(position)
            if (gender != Traveler.Gender.GENDER) {
                (genderSpinner?.adapter as GenderSpinnerAdapter).setErrorVisible(false)
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

    fun isValidGender(): Boolean {
        if (materialFormTestEnabled) {
            return genderEditText?.text?.isNotEmpty() ?: false
        } else {
            return genderSpinner?.selectedItemPosition != 0
        }
    }

}