package com.expedia.bookings.fragment;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.widget.FlightAdapter;
import com.expedia.bookings.widget.FlightAdapter.FlightAdapterListener;
import com.mobiata.android.util.Ui;

public class FlightListFragment extends ListFragment {

	public static final String TAG = FlightListFragment.class.getName();

	private static final String ARG_LEG_POSITION = "ARG_LEG_POSITION";

	private FlightAdapter mAdapter;

	private FlightAdapterListener mListener;

	private ImageView mHeaderImage;

	private Drawable mHeaderDrawable;

	private ProgressBar mProgressBar;
	private TextView mProgressTextView;
	private TextView mErrorTextView;

	public static FlightListFragment newInstance(int legPosition) {
		FlightListFragment fragment = new FlightListFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_LEG_POSITION, legPosition);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof FlightAdapterListener)) {
			throw new RuntimeException("FlightListFragment Activity must implement FlightAdapterListener!");
		}

		mListener = (FlightAdapterListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		// Configure the header
		ListView lv = Ui.findView(v, android.R.id.list);
		mHeaderImage = (ImageView) inflater.inflate(R.layout.snippet_flight_header, lv, false);
		lv.addHeaderView(mHeaderImage);
		lv.setHeaderDividersEnabled(false);
		displayHeaderDrawable();

		// Configure the progress/error stuff
		mProgressBar = Ui.findView(v, R.id.progress_bar);
		mProgressTextView = Ui.findView(v, R.id.progress_text_view);
		mErrorTextView = Ui.findView(v, R.id.error_text_view);

		// Add the adapter
		mAdapter = new FlightAdapter(getActivity(), savedInstanceState);
		setListAdapter(mAdapter);
		mAdapter.setListener(mListener);

		// Set initial data
		int legPosition = getArguments().getInt(ARG_LEG_POSITION);
		mAdapter.setLegPosition(legPosition);
		mAdapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(legPosition));

		// Need to set this since we have buttons inside of the expandable rows
		lv.setItemsCanFocus(true);

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mAdapter.saveInstanceState(outState);
	}

	//////////////////////////////////////////////////////////////////////////
	// Header control

	public void setHeaderDrawable(Drawable drawable) {
		mHeaderDrawable = drawable;
		displayHeaderDrawable();
	}

	private void displayHeaderDrawable() {
		if (mHeaderImage != null) {
			if (mHeaderDrawable == null) {
				mHeaderImage.setVisibility(View.GONE);
			}
			else {
				mHeaderImage.setVisibility(View.VISIBLE);
				mHeaderImage.setImageDrawable(mHeaderDrawable);
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Progress control

	public void showProgress() {
		mProgressBar.setVisibility(View.VISIBLE);
		mProgressTextView.setVisibility(View.VISIBLE);
		mErrorTextView.setVisibility(View.GONE);
	}

	public void showError(CharSequence errorText) {
		mProgressBar.setVisibility(View.GONE);
		mProgressTextView.setVisibility(View.GONE);
		mErrorTextView.setVisibility(View.VISIBLE);

		mErrorTextView.setText(errorText);
	}
}
