package com.expedia.bookings.hotel.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.expedia.bookings.R
import com.expedia.bookings.hotel.util.HotelCalendarDirections
import com.expedia.bookings.hotel.util.HotelCalendarRules
import com.expedia.bookings.hotel.widget.HotelChangeDateCalendarPicker
import com.expedia.bookings.model.HotelStayDates
import com.expedia.bookings.widget.TextView
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class ChangeDatesDialogFragment : DialogFragment() {
    val datesChangedSubject = PublishSubject.create<HotelStayDates>()
    var isShowInitiated = false

    private lateinit var rules: HotelCalendarRules

    @VisibleForTesting
    internal lateinit var pickerView: HotelChangeDateCalendarPicker
    @VisibleForTesting
    internal lateinit var doneButton: TextView

    private var initialDates: HotelStayDates? = null
    private var newDates: HotelStayDates? = null

    private var userTappedDone = false
    private var dateSubscription: Disposable? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        rules = HotelCalendarRules(context)
    }

    override fun show(manager: FragmentManager?, tag: String?) {
        isShowInitiated = true
        super.show(manager, tag)
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
            setButtonEnabled(true)
            newDates = dates
        }
        pickerView.setDates(initialDates)
        doneButton.setOnClickListener {
            userTappedDone = true
            dismiss()
        }
        setButtonEnabled(initialDates != null)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setOnShowListener {
            // Force the window to match parent's width. This must be done after it's shown.
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dateSubscription?.dispose()
        dateSubscription = null
        isShowInitiated = false
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        val dates = newDates
        if (userTappedDone && dates != null && !dates.sameHotelStayDates(initialDates)) {
            if (dates.getStartDate() != null && dates.getEndDate() != null) {
                datesChangedSubject.onNext(dates)
            }
        }
        userTappedDone = false
        pickerView.hideToolTip()
    }

    fun presetDates(hotelStayDates: HotelStayDates?) {
        initialDates = hotelStayDates
    }

    private fun setButtonEnabled(enabled: Boolean) {
        doneButton.isEnabled = enabled
        if (enabled) {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(R.attr.primary_color, typedValue, true)
            doneButton.setTextColor(typedValue.data)
        } else {
            doneButton.setTextColor(ContextCompat.getColor(context, R.color.disabled_text_color))
        }
    }
}
