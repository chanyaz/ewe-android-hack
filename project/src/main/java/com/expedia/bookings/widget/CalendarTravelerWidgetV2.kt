package com.expedia.bookings.widget

import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeText
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.HotelTravelerPickerViewModel
import com.mobiata.android.time.util.JodaUtils
import com.mobiata.android.time.widget.CalendarPicker
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject

public class CalendarTravelerWidgetV2(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    val travelerText: TextView by bindView(R.id.traveler_label)
    val calendarText: TextView by bindView(R.id.calendar_label)
    val hotelSearchViewModelSubject = BehaviorSubject.create<HotelSearchViewModel>()

    val travelerDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        val li = LayoutInflater.from(context);
        val myView = li.inflate(R.layout.widget_hotel_traveler_search, null)
        builder.setView(myView)

        val traveler = myView.findViewById(R.id.traveler_view) as HotelTravelerPickerView
        traveler.viewmodel = HotelTravelerPickerViewModel(context, false)
        traveler.viewmodel.travelerParamsObservable.subscribe(hotelSearchViewModelSubject.value.travelersObserver)
        traveler.viewmodel.guestsTextObservable.subscribeText(travelerText)

        builder.setTitle(R.string.select_traveler_title)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which ->
            dialog.dismiss()

        })
        builder.setNegativeButton(context.getString(R.string.cancel), { dialog, which -> dialog.dismiss() })
        builder.create()
    }

    val calendarDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        val li = LayoutInflater.from(context);
        val myView = li.inflate(R.layout.widget_hotel_calendar_search, null)
        builder.setView(myView)

        val calendar = myView.findViewById(R.id.calendar) as CalendarPicker
        val maxDate = LocalDate.now().plusDays(resources.getInteger(R.integer.calendar_max_selectable_date_range))
        calendar.setSelectableDateRange(LocalDate.now(), maxDate)
        calendar.setMaxSelectableDateRange(resources.getInteger(R.integer.calendar_max_selectable_date_range))
        calendar.setDateChangedListener { start, end ->
            if (JodaUtils.isEqual(start, end)) {
                if (!JodaUtils.isEqual(end, maxDate)) {
                    calendar.setSelectedDates(start, end.plusDays(1))
                } else {
                    // Do not select an end date beyond the allowed range
                    calendar.setSelectedDates(start, null)
                }
            } else {
                hotelSearchViewModelSubject.value.datesObserver.onNext(Pair(start, end))
            }
        }
        calendar.setYearMonthDisplayedChangedListener {
            calendar.hideToolTip()
        }

        hotelSearchViewModelSubject.value.calendarTooltipTextObservable.subscribe(endlessObserver { p ->
            val (top, bottom) = p
            calendar.setToolTipText(top, bottom, true)
        })

        hotelSearchViewModelSubject.value.searchParamsObservable.subscribe {
            calendar.hideToolTip()
        }
        hotelSearchViewModelSubject.value.dateTextObservable.subscribeText(calendarText)
        calendar.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))

        builder.setTitle(R.string.select_dates)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which ->
            dialog.dismiss()
            calendar.hideToolTip()
        })
        builder.setNegativeButton(context.getString(R.string.cancel), { dialog, which ->
            dialog.dismiss()
            calendar.hideToolTip()
        })
        builder.create()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    init {
        View.inflate(context, R.layout.widget_traveler_calendar_search_v2, this)
        val travelerLeftDrawable = travelerText.compoundDrawables[0].mutate()
        val calendarLeftDrawable = calendarText.compoundDrawables[0].mutate()
        val color = ContextCompat.getColor(context, R.color.hotels_primary_color)
        travelerLeftDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        calendarLeftDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        travelerText.setCompoundDrawablesWithIntrinsicBounds(travelerLeftDrawable, null, null, null)
        calendarText.setCompoundDrawablesWithIntrinsicBounds(calendarLeftDrawable, null, null, null)

        calendarText.setOnClickListener {
            calendarDialog.show()
        }

        travelerText.setOnClickListener {
            travelerDialog.show()
        }
    }
}