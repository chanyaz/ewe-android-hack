package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.Toolbar
import android.text.Html
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.ToggleButton
import android.widget.Button
import android.graphics.PorterDuff
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.CarSearch
import com.expedia.bookings.presenter.CarSearchPresenter
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.widget.AlwaysFilterAutoCompleteTextView
import com.expedia.bookings.widget.HotelSuggestionAdapter
import com.expedia.bookings.widget.TravelerPicker
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.MonthView
import com.expedia.bookings.data.cars.Suggestion
import com.expedia.bookings.data.hotels.HotelSearchParams
import org.joda.time.LocalDate
import org.joda.time.YearMonth
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import com.expedia.bookings.utils.FontCache
import kotlin.properties.Delegates
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.mobiata.android.time.widget.DaysOfWeekView
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import com.expedia.bookings.services.HotelServices
import rx.Observer
import rx.Subscription

import com.expedia.bookings.data.hotels.Hotel
import com.mobiata.android.Log


public class HotelSearchPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CalendarPicker.DateSelectionChangedListener, TravelerPicker.TravelersUpdatedListener, CalendarPicker.YearMonthDisplayedChangedListener, DaysOfWeekView.DayOfWeekRenderer {

    val searchLocation: AlwaysFilterAutoCompleteTextView by bindView(R.id.hotel_location)
    val selectDate: ToggleButton by bindView(R.id.select_date)
    val selectTraveler: ToggleButton by bindView(R.id.select_traveler)
    val calendar: CalendarPicker by bindView(R.id.calendar)
    val monthView: MonthView by bindView(R.id.month)
    val traveler: TravelerPicker by bindView(R.id.traveler_view)
    val dayOfWeek: DaysOfWeekView by bindView(R.id.days_of_week)

    var hotelServices : HotelServices? = null
    [Inject] set

    var downloadSubscription: Subscription? = null

    var hotelSearchParams : HotelSearchParams = HotelSearchParams()

    val hotelSuggestionAdapter: HotelSuggestionAdapter by Delegates.lazy {
        HotelSuggestionAdapter()
    }

    init {
        View.inflate(context, R.layout.widget_hotel_search_params, this)
    }

    override fun onFinishInflate() {
        super<Presenter>.onFinishInflate()
        Ui.getApplication(getContext()).defaultLaunchComponents()
        Ui.getApplication(getContext()).launchComponent().inject(this)
        var toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar

        toolbar.inflateMenu(R.menu.cars_search_menu)

        var menuItem: MenuItem = toolbar.getMenu().findItem(R.id.menu_check)
        var customButton: Button = setupToolBarCheckmark(menuItem)
        customButton.setTextColor(getResources().getColor(android.R.color.white))

        toolbar.setOnMenuItemClickListener {
            item -> doSearch()
        }

        traveler.setTravelerUpdatedListener(this)
        calendar.setSelectableDateRange(LocalDate.now(), LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range)))
        calendar.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_hotel_stay))
        calendar.setDateChangedListener(this)
        monthView.setTextEqualDatesColor(Color.WHITE)
        monthView.setMaxTextSize(getResources().getDimension(R.dimen.car_calendar_month_view_max_text_size))
        dayOfWeek.setDayOfWeekRenderer(this)
        selectDate.setOnClickListener {
            calendar.setVisibility(View.VISIBLE)
            traveler.setVisibility(View.GONE)
        }

        selectDate.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        selectTraveler.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))

        selectTraveler.setOnClickListener {
            calendar.setVisibility(View.GONE)
            hideToolTip()
            traveler.setVisibility(View.VISIBLE)
        }

        calendar.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        dayOfWeek.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        monthView.setDaysTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT))
        monthView.setTodayTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM))

        Ui.getApplication(getContext()).hotelComponent().inject(hotelSuggestionAdapter)
        searchLocation.setAdapter(hotelSuggestionAdapter)
        searchLocation.setOnItemClickListener {
            adapterView, view, position, l ->
            var suggestion: Suggestion = hotelSuggestionAdapter.getItem(position) as Suggestion
            searchLocation.setText(Html.fromHtml(StrUtils.formatCityName(suggestion.displayName)).toString(), false)
            hotelSearchParams.location = suggestion
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

        hotelSearchParams.checkIn = start
        hotelSearchParams.checkOut = end
    }

    override fun onTravelerUpdate(text: String) {
        selectTraveler.setTextOn(text)
        selectTraveler.setTextOff(text)
        selectTraveler.setText(text)
        hotelSearchParams.mNumAdults = traveler.numAdults
        hotelSearchParams.mChildren = traveler.getChildAges()
        System.out.println("Malcolm " + hotelSearchParams.mChildren)
    }

    override fun onYearMonthDisplayed(yearMonth: YearMonth?) {
        hideToolTip()
    }

    fun hideToolTip() {
        calendar.hideToolTip()
    }

    public fun setupToolBarCheckmark(menuItem: MenuItem): Button {
        val tv: Button = LayoutInflater.from(getContext()).inflate(R.layout.toolbar_checkmark_item, null) as Button
        val navIcon: Drawable = getResources().getDrawable(R.drawable.ic_check_white_24dp)!!.mutate() as Drawable
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        tv.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null)
        menuItem.setActionView(tv)
        return tv
    }

    override fun renderDayOfWeek(dayOfWeek: LocalDate.Property): String? {
        if (Build.VERSION.SDK_INT >= 18) {
            val sdf = SimpleDateFormat("EEEEE", Locale.getDefault())
            return sdf.format(dayOfWeek.getLocalDate().toDate())
        } else if (Locale.getDefault().getLanguage() == "en") {
            return dayOfWeek.getAsShortText().toUpperCase(Locale.getDefault()).substring(0, 1)
        }
        return DaysOfWeekView.DayOfWeekRenderer.DEFAULT.renderDayOfWeek(dayOfWeek)
    }

    fun doSearch() : Boolean {
        System.out.println("Malcolm " + hotelSearchParams?.location?.shortName)
        downloadSubscription = hotelServices?.suggestHotels(hotelSearchParams, downloadListener)
        return true
    }

    val downloadListener : Observer<MutableList<Hotel>> = object : Observer<MutableList<Hotel>> {
        override fun onNext(t: MutableList<Hotel>?) {
            Log.d("Malcolm Next")
        }

        override fun onCompleted() {
            Log.d("Malcolm Completed")
        }

        override fun onError(e: Throwable?) {
            Log.d("Malcolm Error")
        }
    }
    
}

