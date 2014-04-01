package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

/**
 * ResultsListSearchErrorFragment for Tablet
 * <p/>
 * This was developed with the intention of it sitting in one of the 6 grid cells of the Tablet Search/Results screen.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ResultsListSearchErrorFragment extends Fragment {

	private final static String STATE_ERROR_TEXT = "STATE_ERROR_TEXT";

	private View mRootC;
	private TextView mErrorTv;
	private String mErrorText;

	public static ResultsListSearchErrorFragment newInstance(String errorText) {
		ResultsListSearchErrorFragment frag = new ResultsListSearchErrorFragment();
		frag.setErrorText(errorText);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = inflater.inflate(R.layout.fragment_results_list_search_error, null);
		mErrorTv = Ui.findView(mRootC, R.id.error_tv);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_ERROR_TEXT)) {
				setErrorText(savedInstanceState.getString(STATE_ERROR_TEXT));
			}
		}

		if (mErrorText != null) {
			setErrorText(mErrorText);
		}

		return mRootC;
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mErrorText != null) {
			outState.putString(STATE_ERROR_TEXT, mErrorText);
		}
	}

	public void setErrorText(String text) {
		mErrorText = text;
		if (mErrorTv != null) {
			mErrorTv.setText(mErrorText);
		}
	}
}
