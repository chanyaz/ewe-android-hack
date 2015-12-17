package com.expedia.bookings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.mobiata.android.util.Ui;

public class FlightNoFlightsFragment extends Fragment implements OnClickListener {

	public static final String TAG = FlightNoFlightsFragment.class.getName();

	private static final String ARG_ERROR_MESSAGE = "ARG_ERROR_MESSAGE";

	private NoFlightsFragmentListener mListener;

	public static FlightNoFlightsFragment newInstance(CharSequence errMsg) {
		FlightNoFlightsFragment fragment = new FlightNoFlightsFragment();
		Bundle args = new Bundle();
		args.putCharSequence(ARG_ERROR_MESSAGE, errMsg);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		mListener = Ui.findFragmentListener(this, NoFlightsFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_no_flights, container, false);

		TextView noFlightsTextView = Ui.findView(view, R.id.no_flights_text_view);
		noFlightsTextView.setTypeface(FontCache.getTypeface(Font.ROBOTO_LIGHT));

		CharSequence errMsg = getArguments().getCharSequence(ARG_ERROR_MESSAGE);
		if (!TextUtils.isEmpty(errMsg)) {
			TextView errorTextView = Ui.findView(view, R.id.error_text_view);
			errorTextView.setTypeface(FontCache.getTypeface(Font.ROBOTO_LIGHT));
			errorTextView.setText(errMsg);
			errorTextView.setVisibility(View.VISIBLE);
		}

		Ui.setOnClickListener(view, R.id.edit_search_button, this);

		return view;
	}

	//////////////////////////////////////////////////////////////////////////
	// OnClickListener

	@Override
	public void onClick(View v) {
		mListener.onClickEditSearch();
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface NoFlightsFragmentListener {
		void onClickEditSearch();
	}
}
