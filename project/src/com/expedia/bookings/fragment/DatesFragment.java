package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.graphics.ArrowDrawable;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.time.widget.CalendarPicker;
import com.mobiata.android.util.Ui;

/**
 * One important detail
 * 
 * 1. This is designed to work in conjunction with TabletSearchFragment.  As such, it does not
 *    keep track of its own internal state; I don't want to risk duplicating any data and
 *    confusing the issue.
 */
public class DatesFragment extends Fragment implements CalendarPicker.DateSelectionChangedListener {

	private static final int DATE_BOX_FLAGS = DateUtils.FORMAT_SHOW_DATE;

	private DatesFragmentListener mListener;

	private TextView mStatusTextView;
	private TextView mStartTextView;
	private TextView mEndTextView;
	private View mArrowView;
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
		View view = inflater.inflate(R.layout.fragment_dates, container, false);

		mStatusTextView = Ui.findView(view, R.id.status_text_view);
		mStartTextView = Ui.findView(view, R.id.start_text_view);
		mEndTextView = Ui.findView(view, R.id.end_text_view);
		mArrowView = Ui.findView(view, R.id.arrow_view);
		mCalendarPicker = Ui.findView(view, R.id.calendar_picker);

		mCalendarPicker.setSelectableDateRange(LocalDate.now(), LocalDate.now().plusDays(330));
		mCalendarPicker.setMaxSelectableDateRange(28);
		mCalendarPicker.setSelectedDates(mStartDate, mEndDate);
		mCalendarPicker.setDateChangedListener(this);

		updateStatusText();
		updateDateBoxes();
		updateArrow();

		// Initial arrow config
		ArrowDrawable arrowDrawable = new ArrowDrawable(getResources().getColor(R.color.bg_dates_color));
		mArrowView.setBackgroundDrawable(arrowDrawable);

		return view;
	}

	public void setDatesFromParams(SearchParams searchParams) {
		mStartDate = searchParams.getStartDate();
		mEndDate = searchParams.getEndDate();

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

	private void updateDateBoxes() {
		LocalDate start = mCalendarPicker.getStartDate();
		LocalDate end = mCalendarPicker.getEndDate();

		if (start != null) {
			String date = JodaUtils.formatLocalDate(getActivity(), start, DATE_BOX_FLAGS);
			mStartTextView.setText(getString(R.string.start_date_TEMPLATE, date));
		}
		else {
			mStartTextView.setText(getString(R.string.start_date_TEMPLATE, ""));
		}

		if (end != null) {
			String date = JodaUtils.formatLocalDate(getActivity(), end, DATE_BOX_FLAGS);
			mEndTextView.setText(getString(R.string.end_date_TEMPLATE, date));
		}
		else {
			mEndTextView.setText(R.string.end_date_optional);
		}

		// Make sure only one box is selected
		View selectedView = getSelectedDateBox();
		mStartTextView.setSelected(mStartTextView == selectedView);
		mEndTextView.setSelected(mEndTextView == selectedView);
	}

	private void updateArrow() {
		if (mArrowView.getWidth() == 0) {
			// Delay until we've measured
			mArrowView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					mArrowView.getViewTreeObserver().removeOnPreDrawListener(this);
					updateArrow();
					return true;
				}
			});
		}

		View selectedView = getSelectedDateBox();

		if (selectedView == null) {
			// Hide it by lowering it below the calendar
			mArrowView.animate().translationY(mArrowView.getHeight()).start();
		}
		else {
			// Move it beneath the center of the selected view
			int left = selectedView.getLeft();
			int right = selectedView.getRight();
			mArrowView.animate().translationY(0).translationX(((right - left) / 2) + left).start();
		}
	}

	private View getSelectedDateBox() {
		if (mCalendarPicker.getStartDate() == null) {
			return mStartTextView;
		}
		else if (mCalendarPicker.getEndDate() == null) {
			return mEndTextView;
		}
		else {
			return null;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// DateSelectionChangedListener

	@Override
	public void onDateSelectionChanged(LocalDate start, LocalDate end) {
		mListener.onDatesChanged(start, end);

		updateStatusText();
		updateDateBoxes();
		updateArrow();
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface DatesFragmentListener {
		public void onDatesChanged(LocalDate startDate, LocalDate endDate);
	}

}
