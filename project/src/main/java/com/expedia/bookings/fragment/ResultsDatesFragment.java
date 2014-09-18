package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;
import org.joda.time.YearMonth;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.Log;
import com.mobiata.android.time.widget.CalendarPicker;
import com.mobiata.android.util.Ui;
import com.squareup.otto.Subscribe;

/**
 * One important detail
 * <p/>
 * 1. This is designed to work in conjunction with TabletSearchFragment.  As such, it does not
 * keep track of its own internal state; I don't want to risk duplicating any data and
 * confusing the issue.
 */
public class ResultsDatesFragment extends Fragment implements
	CalendarPicker.DateSelectionChangedListener,
	CalendarPicker.YearMonthDisplayedChangedListener {

	private DatesFragmentListener mListener;

	private CalendarPicker mCalendarPicker;

	// These are only used for the initial setting; they do not represent the state most of the time
	private LocalDate mStartDate;
	private LocalDate mEndDate;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, DatesFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_results_dates, container, false);

		mCalendarPicker = Ui.findView(view, R.id.calendar_picker);
		mCalendarPicker.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM));
		mCalendarPicker.setDaysOfWeekTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM));
		mCalendarPicker.setMonthViewTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT));

		mCalendarPicker.setSelectableDateRange(LocalDate.now(), LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range)));
		mCalendarPicker.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_flight_search));
		mCalendarPicker.setSelectedDates(mStartDate, mEndDate);
		mCalendarPicker.setDateChangedListener(this);
		mCalendarPicker.setYearMonthDisplayedChangedListener(this);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	public void setDatesFromParams(SearchParams searchParams) {
		setDates(searchParams.getStartDate(), searchParams.getEndDate());
	}

	public void setDates(LocalDate startDate, LocalDate endDate){
		mStartDate = startDate;
		mEndDate = endDate;

		if (mCalendarPicker != null) {
			mCalendarPicker.setSelectedDates(mStartDate, mEndDate);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// DateSelectionChangedListener

	@Override
	public void onDateSelectionChanged(LocalDate start, LocalDate end) {
		mListener.onDatesChanged(start, end);
	}

	//////////////////////////////////////////////////////////////////////////
	// YearMonthDisplayedChangedListener

	@Override
	public void onYearMonthDisplayed(YearMonth yearMonth) {
		mListener.onYearMonthDisplayedChanged(yearMonth);
	}

	//////////////////////////////////////////////////////////////////////////
	// Otto event - GDE week clicked

	@Subscribe
	public void onGdeItemSelected(Events.GdeItemSelected event) {
		mCalendarPicker.setDisplayYearMonth(new YearMonth(event.week.getWeekStart()));
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface DatesFragmentListener {
		public void onDatesChanged(LocalDate startDate, LocalDate endDate);
		public void onYearMonthDisplayedChanged(YearMonth yearMonth);
	}

}
