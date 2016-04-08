package com.expedia.bookings.widget.traveler

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.section.GenderSpinnerAdapter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEditText
import com.expedia.vm.traveler.TravelerTSAViewModel
import org.joda.time.LocalDate

class TSAEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs),
        DatePickerDialogFragment.DateChosenListener {

    val fragmentActivity: FragmentActivity
    private val TAG_DATE_PICKER = "TAG_DATE_PICKER"

    val dateOfBirth: TravelerEditText by bindView(R.id.edit_birth_date_text_btn)
    val genderSpinner: Spinner by bindView(R.id.edit_gender_spinner)

    var viewModel: TravelerTSAViewModel by notNullAndObservable { vm ->
        vm.formattedDateSubject.subscribeEditText(dateOfBirth)
        dateOfBirth.subscribeToError(vm.dateOfBirthErrorSubject)

        genderSpinner.onItemSelectedListener = GenderItemSelectedListener()

        vm.genderSubject.subscribe { gender ->
            val adapter = genderSpinner.adapter as GenderSpinnerAdapter
            genderSpinner.setSelection(adapter.getGenderPosition(gender))
        }
    }

    init {
        View.inflate(context, R.layout.tsa_entry_view, this)
        orientation = HORIZONTAL
        setGravity(Gravity.BOTTOM)

        fragmentActivity = context as FragmentActivity

        val genderAdapter = GenderSpinnerAdapter(context, R.layout.material_spinner_item, R.layout.spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter
        dateOfBirth.setOnClickListener(DateOfBirthClickListener(this))
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
            newDatePickerFragment!!.show(fragmentActivity.getSupportFragmentManager(), TAG_DATE_PICKER)
        }
    }
}