package com.expedia.bookings.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.ResultsFlightsState;
import com.expedia.bookings.enums.ResultsHotelsState;
import com.expedia.bookings.enums.ResultsSearchState;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CenteredCaptionedIcon;

/**
 * ResultsListSearchErrorFragment for Tablet
 * <p/>
 * This was developed with the intention of it sitting in one of the 6 grid cells of the Tablet Search/Results screen.
 */
public class ResultsListSearchErrorFragment extends Fragment {

	private final static String STATE_ERROR_TEXT = "STATE_ERROR_TEXT";
	private final static String STATE_ERROR_IMAGE_RES_ID = "STATE_ERROR_IMAGE_RES_ID";

	private CenteredCaptionedIcon mErrorView;
	private CharSequence mErrorText;
	private int mErrorImageResId;

	private ResultsFlightsState mDefaultFlightsState;

	public static ResultsListSearchErrorFragment newInstance() {
		return new ResultsListSearchErrorFragment();
	}

	public void setDefaultState(ResultsFlightsState state) {
		mDefaultFlightsState = state;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_results_list_search_error, null);
		mErrorView = Ui.findView(v, R.id.caption_view);
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_ERROR_TEXT)) {
				setErrorText(savedInstanceState.getCharSequence(STATE_ERROR_TEXT));
				setErrorImage(savedInstanceState.getInt(STATE_ERROR_IMAGE_RES_ID));
			}
		}

		if (mErrorText != null) {
			setErrorText(mErrorText);
		}
		if (mErrorImageResId != 0) {
			setErrorImage(mErrorImageResId);
		}

		if (mDefaultFlightsState != null) {
			setState(mDefaultFlightsState);
		}

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mErrorText != null) {
			outState.putCharSequence(STATE_ERROR_TEXT, mErrorText);
		}
		if (mErrorImageResId != 0) {
			outState.putInt(STATE_ERROR_IMAGE_RES_ID, mErrorImageResId);
		}
	}

	public void setErrorText(CharSequence text) {
		setErrorText(text, null);
	}

	public void setErrorText(CharSequence text, String link) {
		mErrorText = text;
		if (mErrorView != null) {
			mErrorView.setCaption(mErrorText, link);
		}
	}

	public void setErrorImage(int resId) {
		mErrorImageResId = resId;
		if (mErrorView != null) {
			mErrorView.setSVG(resId);
		}
	}

	public void setState(ResultsFlightsState state) {
		mErrorView.clearActionButton();
		if (mErrorImageResId == 0) {
			setErrorImage(R.raw.ic_tablet_sold_out_flight);
		}
		switch (state) {
		case NO_FLIGHTS_POS:
			setErrorText(getString(R.string.invalid_flights_pos));
			break;
		case NO_FLIGHTS_DROPDOWN_POS:
			String posURL = PointOfSale.getPointOfSale().getWebsiteUrl();
			setErrorText(HtmlCompat.fromHtml(getString(R.string.tablet_drop_down_flight_pos_unavailable_TEMPLATE, posURL)), posURL);
			break;
		case MISSING_STARTDATE:
			setErrorText(getString(R.string.missing_flight_trip_date_message));
			break;
		case MISSING_ORIGIN:
			setErrorText(getString(R.string.missing_flight_info_message_TEMPLATE, StrUtils.formatCity(Sp.getParams().getDestination())));
			mErrorView.setActionButton(R.string.missing_flight_info_button_prompt, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.ShowSearchFragment(ResultsSearchState.FLIGHT_ORIGIN));
				}
			});
			break;
		case ZERO_RESULT:
			setErrorText(getString(R.string.tablet_search_results_flights_unavailable));
			break;
		case SEARCH_ERROR:
			setErrorText(getString(R.string.tablet_search_results_flights_unavailable));
			break;
		case INVALID_START_DATE:
			setErrorText(getString(R.string.flight_search_range_error_TEMPLATE,
				getActivity().getResources().getInteger(R.integer.calendar_max_days_flight_search)));
			break;
		}
	}

	public void setState(ResultsHotelsState state) {
		if (mErrorImageResId == 0) {
			setErrorImage(R.raw.ic_tablet_sold_out_hotel);
		}
		switch (state) {
		case MAX_HOTEL_STAY:
			setErrorText(getString(R.string.hotel_search_range_error_TEMPLATE, getResources().getInteger(R.integer.calendar_max_days_hotel_stay)));
			break;
		case SEARCH_ERROR:
		case ZERO_RESULT:
			if (CalendarUtils.isSearchDateTonight(Db.getHotelSearch().getSearchParams())) {
				setErrorText(getString(R.string.tablet_search_results_hotels_unavailable_tonight));
			}
			else {
				setErrorText(getString(R.string.tablet_search_results_hotels_unavailable));
			}
			break;
		}
	}
}
