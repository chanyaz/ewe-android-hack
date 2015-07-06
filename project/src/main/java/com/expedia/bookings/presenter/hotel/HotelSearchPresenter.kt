package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Build
import android.support.v7.widget.Toolbar
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ToggleButton
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.widget.AlwaysFilterAutoCompleteTextView
import com.expedia.bookings.widget.HotelSuggestionAdapter
import com.expedia.bookings.widget.TravelerPicker
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.DaysOfWeekView
import com.mobiata.android.time.widget.MonthView
import org.joda.time.LocalDate
import org.joda.time.YearMonth
import rx.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.properties.Delegates

public class HotelSearchPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    private val searchLocation: AlwaysFilterAutoCompleteTextView by bindView(R.id.hotel_location)
    private val selectDate: ToggleButton by bindView(R.id.select_date)
    private val selectTraveler: ToggleButton by bindView(R.id.select_traveler)
    private val calendar: CalendarPicker by bindView(R.id.calendar)
    private val monthView: MonthView by bindView(R.id.month)
    private val traveler: TravelerPicker by bindView(R.id.traveler_view)
    private val dayOfWeek: DaysOfWeekView by bindView(R.id.days_of_week)

    var hotelSearchParamsBuilder: HotelSearchParams.Builder = HotelSearchParams.Builder()
    private val hotelSuggestionAdapter by Delegates.lazy {
        val service = Ui.getApplication(getContext()).hotelComponent().suggestionsService()
        HotelSuggestionAdapter(service)
    }

    val paramsSubject = PublishSubject.create<HotelSearchParams>()

    init {
        View.inflate(context, R.layout.widget_hotel_search_params, this)
    }

    override fun onFinishInflate() {
        super<Presenter>.onFinishInflate()
        var toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar

        toolbar.inflateMenu(R.menu.cars_search_menu)

        val menuItem: MenuItem = toolbar.getMenu().findItem(R.id.menu_check)
        val customButton: Button = setupToolBarCheckmark(menuItem)
        customButton.setTextColor(getResources().getColor(android.R.color.white))

        traveler.onUpdate { text ->
            selectTraveler.setTextOn(text)
            selectTraveler.setTextOff(text)
            selectTraveler.setText(text)

            hotelSearchParamsBuilder.children(traveler.getChildAges())
            hotelSearchParamsBuilder.adults(traveler.numAdults)
        }

        calendar.setSelectableDateRange(LocalDate.now(), LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range)))
        calendar.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_hotel_stay))
        calendar.setDateChangedListener { start, end ->
            val displayText: String =
                    if (start == null && end == null) {
                        getResources().getString(R.string.select_dates_proper_case)
                    } else if (end == null) {
                        DateUtils.localDateToMMMd(start)
                    } else {
                        getResources().getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))
                    }

            selectDate.setText(displayText)
            selectDate.setTextOff(displayText)
            selectDate.setTextOn(displayText)

            val textResource =
                    if (end == null) R.string.calendar_tooltip_bottom_select_return_date
                    else R.string.calendar_tooltip_bottom_drag_to_modify
            calendar.setToolTipText(displayText, getResources().getString(textResource))
            hotelSearchParamsBuilder.checkIn(start)
            hotelSearchParamsBuilder.checkOut(end)
        }

        monthView.setTextEqualDatesColor(Color.WHITE)
        monthView.setMaxTextSize(getResources().getDimension(R.dimen.car_calendar_month_view_max_text_size))

        dayOfWeek.setDayOfWeekRenderer { dayOfWeek: LocalDate.Property ->
            if (Build.VERSION.SDK_INT >= 18) {
                val sdf = SimpleDateFormat("EEEEE", Locale.getDefault())
                sdf.format(dayOfWeek.getLocalDate().toDate())
            } else if (Locale.getDefault().getLanguage() == "en") {
                dayOfWeek.getAsShortText().toUpperCase(Locale.getDefault()).substring(0, 1)
            } else {
                DaysOfWeekView.DayOfWeekRenderer.DEFAULT.renderDayOfWeek(dayOfWeek)
            }
        }

        selectDate.setOnClickListener {
            calendar.setVisibility(View.VISIBLE)
            traveler.setVisibility(View.GONE)
        }

        selectTraveler.setOnClickListener {
            calendar.setVisibility(View.GONE)
            traveler.setVisibility(View.VISIBLE)
            calendar.hideToolTip()
        }

        selectDate.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        selectTraveler.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        calendar.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        dayOfWeek.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        monthView.setDaysTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT))
        monthView.setTodayTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM))

        searchLocation.setAdapter(hotelSuggestionAdapter)
        searchLocation.setOnItemClickListener {
            adapterView, view, position, l ->
            var suggestion: SuggestionV4 = hotelSuggestionAdapter.getItem(position)
            searchLocation.setText(Html.fromHtml(StrUtils.formatCityName(hotelSuggestionAdapter.getItem(position).regionNames.displayName)).toString(), false)
            hotelSearchParamsBuilder.city(suggestion)
            com.mobiata.android.util.Ui.hideKeyboard(this)
        }
    }

    fun setupToolBarCheckmark(menuItem: MenuItem): Button {
        val tv: Button = LayoutInflater.from(getContext()).inflate(R.layout.toolbar_checkmark_item, null) as Button
        val navIcon: Drawable = getResources().getDrawable(R.drawable.ic_check_white_24dp).mutate()
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        tv.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null)
        tv.setOnClickListener { view ->
            if (hotelSearchParamsBuilder.areRequiredParamsFilled()) {
                calendar.hideToolTip()
                paramsSubject.onNext(hotelSearchParamsBuilder.build())
            } else {
                if (!hotelSearchParamsBuilder.hasOrigin()) {
                    AnimUtils.doTheHarlemShake(searchLocation)
                } else if (!hotelSearchParamsBuilder.hasStartAndEndDates()) {
                    AnimUtils.doTheHarlemShake(calendar)
                }
            }
        }
        menuItem.setActionView(tv)
        return tv
    }
}
