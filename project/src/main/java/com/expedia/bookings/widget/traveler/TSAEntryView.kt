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
import com.expedia.bookings.section.GenderSpinnerAdapter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.accessibility.AccessibleSpinner
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEditText
import com.expedia.vm.traveler.TravelerTSAViewModel
import org.joda.time.LocalDate

class TSAEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs),
        DatePickerDialogFragment.DateChosenListener {

    val fragmentActivity: FragmentActivity
    private val TAG_DATE_PICKER = "TAG_DATE_PICKER"

    val dateOfBirth: TravelerEditText by bindView(R.id.edit_birth_date_text_btn)
    val genderSpinner: AccessibleSpinner by bindView(R.id.edit_gender_spinner)

    var viewModel: TravelerTSAViewModel by notNullAndObservable { vm ->
        vm.formattedDateSubject.subscribeEditText(dateOfBirth)
        dateOfBirth.subscribeToError(vm.dateOfBirthErrorSubject)
        genderSpinner.onItemSelectedListener = GenderItemSelectedListener()
        vm.genderSubject.subscribe { gender ->
            val adapter = genderSpinner.adapter as GenderSpinnerAdapter
            genderSpinner.onItemSelectedListener = null
            genderSpinner.setSelection(adapter.getGenderPosition(gender))
            genderSpinner.onItemSelectedListener = GenderItemSelectedListener()
        }
        vm.birthErrorTextSubject.subscribe { text ->
            showBirthdateErrorDialog(text)
        }
        vm.genderErrorSubject.subscribe { hasError ->
            (genderSpinner.adapter as GenderSpinnerAdapter).setErrorVisible(hasError)
            genderSpinner.valid = !hasError
        }
    }

    init {
        View.inflate(context, R.layout.tsa_entry_view, this)
        orientation = HORIZONTAL
        gravity = Gravity.BOTTOM

        fragmentActivity = context as FragmentActivity

        val genderAdapter = GenderSpinnerAdapter(context, R.layout.material_spinner_item, R.layout.spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter
        dateOfBirth.setOnClickListener(DateOfBirthClickListener(this))
        genderSpinner.isFocusable = true
        genderSpinner.isFocusableInTouchMode = true
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
        viewModel.dateOfBirthObserver.onNext(LocalDate(year, month, day))
    }

    private inner class GenderItemSelectedListener() : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            //do nothing
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val adapter = genderSpinner.adapter as GenderSpinnerAdapter
            viewModel.genderObserver.onNext(adapter.getGender(position))
            val gender = adapter.getGender(position)
            if (gender != Traveler.Gender.GENDER) {
                (genderSpinner.adapter as GenderSpinnerAdapter).setErrorVisible(false)
            }
        }
    }

    private inner class DateOfBirthClickListener(val dateSetListener: DatePickerDialogFragment.DateChosenListener) : OnClickListener {
        override fun onClick(v: View?) {
            var date = viewModel.birthDateSubject.value
            if (date == null) {
                date = viewModel.defaultDateSubject.value
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