package com.expedia.bookings.hotel.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.shared.CalendarListener
import com.expedia.bookings.shared.CalendarRules
import com.expedia.bookings.hotel.widget.HotelChangeDateCalendarPicker
import org.joda.time.LocalDate
import android.view.ViewGroup
import android.view.Window
import com.expedia.bookings.hotel.util.HotelCalendarInstructions
import com.expedia.bookings.widget.TextView

class ChangeDatesDialogFragment() : DialogFragment(), CalendarListener {

    private var rules: CalendarRules? = null

    private var pickerView: HotelChangeDateCalendarPicker? = null
    private var doneButton: TextView? = null

    private var initialDates = Pair<LocalDate?, LocalDate?>(null, null)
    private var newDates = Pair<LocalDate?, LocalDate?>(null, null)

    private var userTappedDone = false

    companion object {
        fun createFragment(rules: CalendarRules): ChangeDatesDialogFragment {
            val fragment = ChangeDatesDialogFragment(rules)
            return fragment
        }
    }

    private constructor(rules: CalendarRules) : this() {
        this.rules = rules
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val container = inflater?.inflate(R.layout.change_dates_calendar_picker, container, false)

        pickerView = container?.findViewById(R.id.change_dates_picker)
        doneButton = container?.findViewById(R.id.change_dates_done_button)

        return container
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rules?.let { rules ->
            pickerView?.bind(rules, this, HotelCalendarInstructions(context))
            pickerView?.setDates(initialDates.first, initialDates.second)
            doneButton?.setOnClickListener {
                userTappedDone = true
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (rules == null) {
            dialog.dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        if (userTappedDone && newDates.first != null && newDates.second != null) {
            //todo tell someone about new dates
        } else {
            //todo use old dates
        }
        userTappedDone = false
    }

    override fun datesUpdated(startDate: LocalDate?, endDate: LocalDate?) {
        newDates = Pair(startDate, endDate)
    }

    fun presetDates(startDate: LocalDate?, endDate: LocalDate?) {
        initialDates = Pair(startDate, endDate)
    }
}