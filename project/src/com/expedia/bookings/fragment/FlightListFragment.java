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

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

public class FlightListFragment extends ListFragment {

	private FlightListFragmentListener mListener;

	private ImageView mHeaderImage;

	private Drawable mHeaderDrawable;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof FlightListFragmentListener)) {
			throw new RuntimeException("FlightListFragment Activity must implement FlightListFragmentListener!");
		}

		mListener = (FlightListFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Hijack this to add a header view
		View v = super.onCreateView(inflater, container, savedInstanceState);

		ListView lv = Ui.findView(v, android.R.id.list);
		mHeaderImage = (ImageView) getActivity().getLayoutInflater().inflate(R.layout.snippet_flight_header, lv, false);
		lv.addHeaderView(mHeaderImage);
		lv.setHeaderDividersEnabled(false);
		displayHeaderDrawable();

		return v;
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
	// FlightListFragmentListener

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mListener.onFlightClick(position);
	}

	public interface FlightListFragmentListener {

		public void onFlightClick(int position);
	}
}
