package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchResultsFragmentActivity;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.AvailabilitySummaryWidget;
import com.expedia.bookings.widget.AvailabilitySummaryWidget.AvailabilitySummaryListener;
import com.expedia.bookings.widget.HotelCollage;
import com.expedia.bookings.widget.HotelCollage.OnCollageImageClickedListener;
import com.expedia.bookings.widget.SummarizedRoomRates;

public class MiniDetailsFragment extends Fragment implements AvailabilitySummaryListener {

	public static MiniDetailsFragment newInstance() {
		return new MiniDetailsFragment();
	}

	private TextView mNameTextView;
	private TextView mLocationTextView;
	private RatingBar mRatingBar;

	private HotelCollage mCollageHandler;

	private AvailabilitySummaryWidget mAvailabilitySummary;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

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
		mCollageHandler = new HotelCollage(view, mOnImageClickedListener);

		mAvailabilitySummary.init(view);

		updateViews(Db.getSelectedProperty(), view);

		return view;
	}

	//////////////////////////////////////////////////////////////////////////
	// Callbacks

	private OnCollageImageClickedListener mOnImageClickedListener = new OnCollageImageClickedListener() {
		public void onImageClicked(Media media) {
			((SearchResultsFragmentActivity) getActivity()).startHotelGalleryActivity(media);
		}

		@Override
		public void onPromotionClicked() {
			SummarizedRoomRates summarizedRoomRates = Db.getSelectedSummarizedRoomRates();

			if (summarizedRoomRates != null) {
				Rate startRate = summarizedRoomRates.getStartingRate();
				if (startRate != null) {
					((SearchResultsFragmentActivity) getActivity()).bookRoom(startRate, false);
				}
			}
		}
	};

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
		mAvailabilitySummary.showProgressBar();
	}

	public void notifyAvailabilityQueryError(String errMsg) {
		mAvailabilitySummary.showError(errMsg);
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
		((SearchResultsFragmentActivity) getActivity()).bookRoom(rate, true);
	}

	@Override
	public void onButtonClicked() {
		((SearchResultsFragmentActivity) getActivity())
				.moreDetailsForPropertySelected(SearchResultsFragmentActivity.SOURCE_MINI_DETAILS);
	}
}
