package com.expedia.bookings.hotel.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.hotel.widget.HotelChangeDateCalendarPicker
import org.joda.time.LocalDate
import android.view.ViewGroup
import android.view.Window
import com.expedia.bookings.hotel.util.HotelCalendarDirections
import com.expedia.bookings.hotel.util.HotelCalendarRules
import com.expedia.bookings.model.HotelStayDates
import com.expedia.bookings.widget.TextView
import rx.Subscription
import rx.subjects.PublishSubject

class ChangeDatesDialogFragment : DialogFragment() {
    val datesChangedSubject = PublishSubject.create<HotelStayDates>()

    private lateinit var rules: HotelCalendarRules

    @VisibleForTesting
    internal lateinit var pickerView: HotelChangeDateCalendarPicker
    private lateinit var doneButton: TextView

    private var initialDates: HotelStayDates? = null
    private var newDates: HotelStayDates? = null

    private var userTappedDone = false
    private var dateSubscription: Subscription? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        rules = HotelCalendarRules(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.change_dates_calendar_picker, container, false)

        pickerView = view.findViewById(R.id.change_dates_picker)
        doneButton = view.findViewById(R.id.change_dates_done_button)

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pickerView.bind(rules, HotelCalendarDirections(context))

        dateSubscription = pickerView.datesUpdatedSubject.subscribe { dates ->
            newDates = dates
        }
        pickerView.setDates(initialDates)
        doneButton.setOnClickListener {
            userTappedDone = true
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dateSubscription?.unsubscribe()
        dateSubscription = null
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        val dates = newDates
        if (userTappedDone && dates != null && !dates.sameHotelStayDates(initialDates)) {
            if (dates.getStartDate() != null && dates.getEndDate() != null) {
                datesChangedSubject.onNext(newDates)
            }
        }
        userTappedDone = false
        pickerView.hideToolTip()
    }

    fun presetDates(hotelStayDates: HotelStayDates?) {
        initialDates = hotelStayDates
    }
}
