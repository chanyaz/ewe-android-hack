package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.Toolbar
import android.text.Html
import android.util.AttributeSet
import android.view.View
import android.widget.ToggleButton
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.widget.AlwaysFilterAutoCompleteTextView
import com.expedia.bookings.widget.HotelSuggestionAdapter
import com.expedia.bookings.widget.TravelerPicker
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.MonthView
import org.joda.time.LocalDate
import org.joda.time.YearMonth
import kotlin.properties.Delegates
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

public class HotelSearchPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CalendarPicker.DateSelectionChangedListener, TravelerPicker.TravelersUpdatedListener,CalendarPicker.YearMonthDisplayedChangedListener {

    val searchLocation: AlwaysFilterAutoCompleteTextView by bindView(R.id.hotel_location)
    val selectDate: ToggleButton by bindView(R.id.select_date)
    val selectTraveler: ToggleButton by bindView(R.id.select_traveler)
    val calendar: CalendarPicker by bindView(R.id.calendar)
    val monthView: MonthView by bindView(R.id.month)
    val traveler: TravelerPicker by bindView(R.id.traveler_view)

    val hotelSuggestionAdapter: HotelSuggestionAdapter by Delegates.lazy {
        HotelSuggestionAdapter()
    }

    init {
        View.inflate(context, R.layout.widget_hotel_search_params, this)
    }

    override fun onFinishInflate() {
        super<Presenter>.onFinishInflate()

        var toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar

        toolbar.inflateMenu(R.menu.cars_search_menu)
        traveler.setTravelerUpdatedListener(this)
        calendar.setSelectableDateRange(LocalDate.now(), LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range)))
        calendar.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_hotel_stay))
        calendar.setDateChangedListener(this)
        monthView.setTextEqualDatesColor(Color.WHITE)
        selectDate.setOnClickListener {
            calendar.setVisibility(View.VISIBLE)
            traveler.setVisibility(View.GONE)
        }

        selectTraveler.setOnClickListener {
            calendar.setVisibility(View.GONE)
            hideToolTip()
            traveler.setVisibility(View.VISIBLE)
        }

        Ui.getApplication(getContext()).hotelComponent().inject(hotelSuggestionAdapter)
        searchLocation.setAdapter(hotelSuggestionAdapter)
        searchLocation.setOnItemClickListener {
            adapterView, view, position, l ->
            searchLocation.setText(Html.fromHtml(StrUtils.formatCityName(hotelSuggestionAdapter.getItem(position).displayName)).toString(), false)
        }
    }

    override fun onDateSelectionChanged(start: LocalDate?, end: LocalDate?) {
        var displayText: String = if (start == null && end == null) getResources().getString(R.string.select_dates_proper_case)
        else if (end == null) DateUtils.localDateToMMMd(start)
        else getResources().getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))

        selectDate.setText(displayText)
        selectDate.setTextOff(displayText)
        selectDate.setTextOn(displayText)

        calendar.setToolTipText(displayText, if (end == null) getResources().getString(R.string.calendar_tooltip_bottom_select_return_date)
                                              else getResources().getString(R.string.calendar_tooltip_bottom_drag_to_modify))

    }

    override fun onTravelerUpdate(text: String) {
        selectTraveler.setTextOn(text)
        selectTraveler.setTextOff(text)
        selectTraveler.setText(text)
    }

    override fun onYearMonthDisplayed(yearMonth: YearMonth?) {
        hideToolTip()
    }

    fun hideToolTip() {
        calendar.hideToolTip()
    }
}

