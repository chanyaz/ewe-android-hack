package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.AvailabilitySummaryWidget;
import com.expedia.bookings.widget.AvailabilitySummaryWidget.AvailabilitySummaryListener;
import com.expedia.bookings.widget.HotelCollage;
import com.expedia.bookings.widget.HotelCollage.OnCollageImageClickedListener;

public class MiniDetailsFragment extends Fragment implements AvailabilitySummaryListener {

	public static MiniDetailsFragment newInstance() {
		return new MiniDetailsFragment();
	}

	private TextView mNameTextView;
	private TextView mLocationTextView;
	private RatingBar mRatingBar;

	private HotelCollage mCollageHandler;

	private MiniDetailsFragmentListener mListener;

	private AvailabilitySummaryWidget mAvailabilitySummary;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof MiniDetailsFragmentListener)) {
			throw new RuntimeException("MiniDetailsFragment Activity must implement MiniDetailsFragmentListener!");
		}
		else if (!(activity instanceof OnCollageImageClickedListener)) {
			throw new RuntimeException("MiniDetailsFragment Activity must implement OnCollageImageClickedListener!");
		}

		mListener = (MiniDetailsFragmentListener) activity;

		mAvailabilitySummary = new AvailabilitySummaryWidget(activity);
		mAvailabilitySummary.setListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mini_details, container, false);

		// #11181: We make the root view clickable so that clicks don't bleed through to the underlying MapView
		view.setClickable(true);

		mNameTextView = (TextView) view.findViewById(R.id.name_text_view);
		mLocationTextView = (TextView) view.findViewById(R.id.location_text_view);
		mRatingBar = (RatingBar) view.findViewById(R.id.hotel_rating_bar);
		mCollageHandler = new HotelCollage(view, (OnCollageImageClickedListener) getActivity());

		mAvailabilitySummary.init(view);

		updateViews(Db.getSelectedProperty(), view);

		return view;
	}

	//////////////////////////////////////////////////////////////////////////
	// Views

	private void updateViews(Property property) {
		updateViews(property, getView());
	}

	private void updateViews(final Property property, final View view) {
		// don't update views if there is no
		// view attached.
		if (view != null && property != null) {
			mNameTextView.setText(property.getName());
			mLocationTextView.setText(StrUtils.formatAddress(property.getLocation()).replace("\n", ", "));
			mRatingBar.setRating((float) property.getHotelRating());
			mCollageHandler.updateCollage(property);

			mAvailabilitySummary.updateProperty(property);

			AvailabilityResponse availabilityResponse = Db.getSelectedAvailabilityResponse();

			if (availabilityResponse != null) {
				mAvailabilitySummary.showRates(availabilityResponse);
			}
		}

	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment control

	public void notifyPropertySelected() {
		updateViews(Db.getSelectedProperty());
	}

	public void notifyAvailabilityQueryStarted() {
		if (mAvailabilitySummary != null) {
			mAvailabilitySummary.showProgressBar();
		}
	}

	public void notifyAvailabilityQueryError(String errMsg) {
		if (mAvailabilitySummary != null) {
			mAvailabilitySummary.showError(errMsg);
		}
	}

	public void notifyAvailabilityQueryComplete() {
		AvailabilityResponse response = Db.getSelectedAvailabilityResponse();
		if (!response.canRequestMoreData()) {
			mAvailabilitySummary.showRates(response);
		}
		else {
			updateViews(Db.getSelectedProperty());
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// AvailabilitySummaryListener

	@Override
	public void onRateClicked(Rate rate) {
		mListener.onMiniDetailsRateClicked(rate);
	}

	@Override
	public void onButtonClicked() {
		mListener.onMoreDetailsClicked();
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface MiniDetailsFragmentListener {
		public void onMiniDetailsRateClicked(Rate rate);

		public void onMoreDetailsClicked();
	}
}
