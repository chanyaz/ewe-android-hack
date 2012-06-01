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
import com.mobiata.android.util.Ui;

public class FlightListFragment extends ListFragment {

	private FlightListFragmentListener mListener;

	private ImageView mHeaderImage;

	private Drawable mHeaderDrawable;

	private ProgressBar mProgressBar;
	private TextView mProgressTextView;
	private TextView mErrorTextView;

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
