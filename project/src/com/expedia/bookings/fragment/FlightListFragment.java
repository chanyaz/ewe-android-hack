package com.expedia.bookings.fragment;

import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.widget.FlightAdapter;
import com.expedia.bookings.widget.FlightAdapter.FlightAdapterListener;
import com.mobiata.android.util.Ui;

public class FlightListFragment extends ListFragment {

	private FlightAdapter mAdapter;

	private FlightAdapterListener mListener;

	private ImageView mHeaderImage;

	private Drawable mHeaderDrawable;

	private ProgressBar mProgressBar;
	private TextView mProgressTextView;
	private TextView mErrorTextView;

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
		View v = inflater.inflate(R.layout.fragment_flight_list, container, false);

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
		mAdapter = new FlightAdapter(getActivity());
		setListAdapter(mAdapter);
		mAdapter.setListener(mListener);

		// Need to set this since we have buttons inside of the expandable rows
		lv.setItemsCanFocus(true);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
				// Adjust position for headers
				position -= getListView().getHeaderViewsCount();

				if (mAdapter.getExpandedLegPosition() == position) {
					mAdapter.setExpandedLegPosition(-1);
				}
				else {
					mAdapter.setExpandedLegPosition(position);
				}

				mAdapter.notifyDataSetChanged();
			}

		});

		return v;
	}

	public void setLegPosition(int position) {
		mAdapter.setLegPosition(position);
	}

	public void setFlights(List<FlightTrip> flightTrips) {
		mAdapter.setFlights(flightTrips);
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
