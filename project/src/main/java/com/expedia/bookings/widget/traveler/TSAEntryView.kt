package com.expedia.bookings.widget.traveler

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.extensions.subscribeMaterialFormsError
import com.expedia.bookings.section.GenderSpinnerAdapter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.traveler.TravelerTSAViewModel
import org.joda.time.LocalDate

class TSAEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs),
        DatePickerDialogFragment.DateChosenListener {

    val fragmentActivity: FragmentActivity
    private val TAG_DATE_PICKER = "TAG_DATE_PICKER"

    val dateOfBirth: TravelerEditText by bindView(R.id.edit_birth_date_text_btn)
    var genderEditText: TravelerEditText? = null

    var viewModel: TravelerTSAViewModel by notNullAndObservable { vm ->
        dateOfBirth.viewModel = vm.dateOfBirthViewModel
        genderEditText?.viewModel = vm.genderViewModel
        vm.genderViewModel.genderSubject.subscribe { gender ->
            val adapter = GenderSpinnerAdapter(context)
            val position = adapter.getGenderPosition(gender)
            if (position == 0) {
                genderEditText?.setText("")
            } else {
                genderEditText?.setText(adapter.getItem(position))
            }
        }
        vm.dateOfBirthViewModel.birthErrorTextSubject.subscribe { text ->
            showBirthdateErrorDialog(text)
        }
        vm.genderViewModel.errorSubject.subscribe {
            genderEditText?.subscribeMaterialFormsError(vm.genderViewModel.errorSubject, R.string.gender_validation_error_message,
                    R.drawable.material_dropdown)
        }

        dateOfBirth.subscribeMaterialFormsError(dateOfBirth.viewModel.errorSubject, R.string.date_of_birth_validation_error_message,
                R.drawable.material_dropdown)
    }

    init {
        View.inflate(context, R.layout.material_tsa_entry_view, this)
        genderEditText = this.findViewById<TravelerEditText>(R.id.edit_gender_btn)
        genderEditText?.setOnClickListener {
            showGenderAlertDialog()
        }
        orientation = HORIZONTAL
        fragmentActivity = context as FragmentActivity
        dateOfBirth.setOnClickListener(DateOfBirthClickListener(this))
    }

    private fun showGenderAlertDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.gender))
        val items = arrayOf(context.resources.getString(R.string.male), context.resources.getString(R.string.female))

        builder.setItems(items) { _, position ->
            viewModel.genderViewModel.genderSubject.onNext(Traveler.Gender.values()[position + 1])
            genderEditText?.viewModel?.errorSubject?.onNext(false)
        }

        val alert = builder.create()
        alert.show()
    }

    override fun handleDateChosen(year: Int, month: Int, day: Int, formattedDate: String) {
        viewModel.dateOfBirthViewModel.dateOfBirthObserver.onNext(LocalDate(year, month, day))
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
            if (!newDatePickerFragment!!.isAdded) {
                newDatePickerFragment.show(fragmentActivity.supportFragmentManager, TAG_DATE_PICKER)
            }
        }
    }

    private fun showBirthdateErrorDialog(message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.traveler_age_title)
        builder.setMessage(if (message.isNotEmpty()) message else null)
        builder.setPositiveButton(context.getString(R.string.DONE), object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                dialog.dismiss()
            }
        })
        val dialog = builder.create()
        dialog.show()
    }

    fun isValidGender(): Boolean {
        return genderEditText?.text?.isNotEmpty() ?: false
    }
}
