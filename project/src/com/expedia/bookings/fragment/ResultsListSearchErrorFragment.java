package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
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
	private String mErrorText;
	private int mErrorImageResId;

	public static ResultsListSearchErrorFragment newInstance(String errorText, int errorImageResId) {
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
				setErrorText(savedInstanceState.getString(STATE_ERROR_TEXT));
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
			outState.putString(STATE_ERROR_TEXT, mErrorText);
		}
		if (mErrorImageResId != 0) {
			outState.putInt(STATE_ERROR_IMAGE_RES_ID, mErrorImageResId);
		}
	}

	public void setErrorText(String text) {
		mErrorText = text;
		if (mErrorView != null) {
			mErrorView.setCaption(mErrorText);
		}
	}

	public void setErrorImage(int resId) {
		mErrorImageResId = resId;
		if (mErrorView != null) {
			mErrorView.setSVG(resId);
		}
	}
}
