package com.expedia.bookings.widget;

import org.joda.time.LocalDate;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.utils.JodaUtils;

public class AbSearchInfoButton extends TextView {

	public AbSearchInfoButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public boolean bindFromDb(Context context) {
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchParams() != null) {
			String city = Db.getFlightSearch().getSearchParams().getArrivalLocation().getCity();
			String dateStr;

			int flags = DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY;
			LocalDate startDate = Db.getFlightSearch().getSearchParams().getDepartureDate();
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				LocalDate endDate = Db.getFlightSearch().getSearchParams().getReturnDate();
				dateStr = JodaUtils.formatDateRange(context, startDate, endDate, flags);
			}
			else {
				dateStr = JodaUtils.formatLocalDate(context, startDate, flags);
			}

			setText(context.getResources().getString(R.string.destination_and_date_range_TEMPLATE, city, dateStr));

			return true;
		}
		return false;
	}

}
