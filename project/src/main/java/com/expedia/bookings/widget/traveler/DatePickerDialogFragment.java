package com.expedia.bookings.widget.traveler;

import java.util.Calendar;

import org.joda.time.LocalDate;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils;

public class DatePickerDialogFragment extends DialogFragment implements DatePicker.OnDateChangedListener {
	private static final String EXTRA_DEFAULT_DATE = "extraDefaultDate";

	private DateChosenListener dateChosenListener;

	private DatePicker datePicker;
	private LocalDate date;

	public interface DateChosenListener {
		void handleDateChosen(int year, int month, int day, String formattedDate);
	}

	public static DatePickerDialogFragment createFragment(DateChosenListener dateChosenListener, LocalDate cal) {
		DatePickerDialogFragment fragment = new DatePickerDialogFragment();
		fragment.setDateChosenListener(dateChosenListener);
		Bundle extras = new Bundle();
		extras.putSerializable(EXTRA_DEFAULT_DATE, cal);
		fragment.setArguments(extras);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getArguments();
		if (extras != null) {
			date = (LocalDate) extras.getSerializable(EXTRA_DEFAULT_DATE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		View view = inflater.inflate(R.layout.spinner_date_picker, container);
		datePicker = (DatePicker) view.findViewById(R.id.datePicker);
		datePicker.setMaxDate(Calendar.getInstance().getTimeInMillis());
		View doneButton = view.findViewById(R.id.datePickerDoneButton);
		doneButton.setOnClickListener(new DateChosenClickListener());

		if (date != null) {
			datePicker.init(date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth(), this);
		}
		return view;
	}

	public void setDateChosenListener(DateChosenListener dateChosenListener) {
		this.dateChosenListener = dateChosenListener;
	}

	@Override
	public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		datePicker.init(year, monthOfYear, dayOfMonth, this);
	}

	private class DateChosenClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			datePicker.clearFocus();
			if (dateChosenListener != null) {
				int year = datePicker.getYear();
				int month = datePicker.getMonth();
				int day = datePicker.getDayOfMonth();
				dateChosenListener.handleDateChosen(year, month + 1, day,
					LocaleBasedDateFormatUtils.formatBirthDate(year, month + 1, day));
			}
			dismiss();
		}
	}
}
