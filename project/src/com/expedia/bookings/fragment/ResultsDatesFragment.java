package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.time.widget.CalendarPicker;
import com.mobiata.android.util.Ui;

/**
 * One important detail
 * <p/>
 * 1. This is designed to work in conjunction with TabletSearchFragment.  As such, it does not
 * keep track of its own internal state; I don't want to risk duplicating any data and
 * confusing the issue.
 */
public class ResultsDatesFragment extends Fragment implements CalendarPicker.DateSelectionChangedListener {

	private DatesFragment.DatesFragmentListener mListener;

	private TextView mStatusTextView;
	private CalendarPicker mCalendarPicker;

	// These are only used for the initial setting; they do not represent the state most of the time
	private LocalDate mStartDate;
	private LocalDate mEndDate;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, DatesFragment.DatesFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_results_dates, container, false);

		mStatusTextView = Ui.findView(view, R.id.status_text_view);
		mCalendarPicker = Ui.findView(view, R.id.calendar_picker);

		mCalendarPicker.setSelectableDateRange(LocalDate.now(), LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range)));
		mCalendarPicker.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_flight_search));
		mCalendarPicker.setSelectedDates(mStartDate, mEndDate);
		mCalendarPicker.setDateChangedListener(this);

		updateStatusText();

		return view;
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

	private void updateStatusText() {
		LocalDate start = mCalendarPicker.getStartDate();
		LocalDate end = mCalendarPicker.getEndDate();

		if (start != null && end != null) {
			int daysBetween = JodaUtils.daysBetween(start, end);
			mStatusTextView.setText(getString(R.string.dates_status_multi_TEMPLATE, daysBetween));
		}
		else if (start != null) {
			mStatusTextView.setText(R.string.dates_status_one);
		}
		else {
			mStatusTextView.setText(R.string.dates_status_none);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// DateSelectionChangedListener

	@Override
	public void onDateSelectionChanged(LocalDate start, LocalDate end) {
		mListener.onDatesChanged(start, end);

		updateStatusText();
	}

}
