package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.ResultsFlightsState;
import com.expedia.bookings.widget.CenteredCaptionedIcon;

/**
 * ResultsListSearchErrorFragment for Tablet
 * <p/>
 * This was developed with the intention of it sitting in one of the 6 grid cells of the Tablet Search/Results screen.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ResultsListSearchErrorFragment extends Fragment {

	private final static String STATE_ERROR_TEXT = "STATE_ERROR_TEXT";
	private final static String STATE_ERROR_IMAGE_RES_ID = "STATE_ERROR_IMAGE_RES_ID";

	private CenteredCaptionedIcon mErrorView;
	private CharSequence mErrorText;
	private int mErrorImageResId;

	public static ResultsListSearchErrorFragment newInstance(CharSequence errorText, int errorImageResId) {
		ResultsListSearchErrorFragment frag = new ResultsListSearchErrorFragment();
		frag.setErrorText(errorText);
		frag.setErrorImage(errorImageResId);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mErrorView = (CenteredCaptionedIcon) inflater.inflate(R.layout.fragment_results_list_search_error, null);

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

		return mErrorView;
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
		switch (state) {
		case NO_FLIGHTS_POS:
			setErrorText(getString(R.string.invalid_flights_pos));
			break;
		case NO_FLIGHTS_DROPDOWN_POS:
			String posURL = PointOfSale.getPointOfSale().getWebsiteUrl();
			setErrorText(Html.fromHtml(getString(R.string.tablet_drop_down_flight_pos_unavailable, posURL)), posURL);
			break;
		case MISSING_STARTDATE:
			setErrorText(getString(R.string.missing_flight_trip_date_message));
			break;
		case MISSING_ORIGIN:
			setErrorText(getString(R.string.missing_flight_info_message, Html.fromHtml(Sp.getParams().getDestination().getDisplayName()).toString()));
			break;
		case SEARCH_ERROR:
			setErrorText(getString(R.string.tablet_search_results_flights_unavailable));
			break;
		}
	}
}
