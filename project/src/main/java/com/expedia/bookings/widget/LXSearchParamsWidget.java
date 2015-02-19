package com.expedia.bookings.widget;

import org.joda.time.LocalDate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchParamsBuilder;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.time.widget.CalendarPicker;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LXSearchParamsWidget extends LinearLayout
	implements TextWatcher, CalendarPicker.DateSelectionChangedListener {

	@InjectView(R.id.search_params_close)
	ImageButton cancel;

	@InjectView(R.id.search_params_done)
	ImageButton done;

	@InjectView(R.id.search_calendar)
	CalendarPicker calendarPicker;

	@InjectView(R.id.search_location)
	EditText location;

	@InjectView(R.id.select_dates)
	Button selectDates;

	LXSearchParams searchParams;

	private LXSearchParamsBuilder searchParamsBuilder = new LXSearchParamsBuilder();

	public LXSearchParamsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		calendarPicker.setVisibility(View.INVISIBLE);
		calendarPicker.setSelectableDateRange(LocalDate.now(),
			LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range)));
		calendarPicker.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_lx_search));
		calendarPicker.setDateChangedListener(this);
		location.addTextChangedListener(this);
	}

	// Click events

	@OnClick(R.id.search_params_done)
	public void performSearch() {
		searchParams = searchParamsBuilder.build();
		// Validate input
		if (validateSearchInput()) {
			Events.post(new Events.LXNewSearchParamsAvailable(searchParams));
			Ui.hideKeyboard(this);
		}
	}

	@OnClick(R.id.select_dates)
	public void showCalendar() {
		calendarPicker.setVisibility(View.VISIBLE);
	}

	private boolean validateSearchInput() {
		// TODO Api defaults dates if not provided. Check what is required for mobile !
		if (Strings.isEmpty(searchParams.location)) {
			showAlertMessage(R.string.lx_error_missing_location, R.string.ok);
			return false;
		}
		return true;
	}

	public void showAlertMessage(int messageResourceId, int confirmButtonResourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(messageResourceId)
			.setNeutralButton(confirmButtonResourceId, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();
	}

	@OnClick(R.id.search_params_close)
	public void closeSearchWidget() {
		Events.post(new Events.LXCloseSearchWidget());
	}

	// Edit textwatcher
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// ignore
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// ignore
	}

	@Override
	public void afterTextChanged(Editable s) {
		searchParamsBuilder.location = s.toString();
	}

	// Calendar

	@Override
	public void onDateSelectionChanged(LocalDate start, LocalDate end) {
		searchParamsBuilder.startDate(start);
		searchParamsBuilder.endDate(end);
		searchParamsChanged();
	}

	private void searchParamsChanged() {
		if (searchParamsBuilder.startDate != null) {
			String dateRangeText = DateFormatUtils
				.formatLXSearchDateRange(getContext(), searchParamsBuilder.startDate, searchParamsBuilder.endDate,
					DateFormatUtils.FLAGS_DATE_ABBREV_MONTH);
			selectDates.setText(dateRangeText);
		}
	}

	public LXSearchParams getCurrentParams() {
		return searchParams;
	}
}
