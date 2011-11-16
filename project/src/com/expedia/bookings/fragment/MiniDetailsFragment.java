package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchResultsFragmentActivity;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.utils.AvailabilitySummaryLayoutUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.HotelCollage;
import com.expedia.bookings.widget.HotelCollage.OnCollageImageClickedListener;

public class MiniDetailsFragment extends Fragment implements EventHandler {

	public static MiniDetailsFragment newInstance() {
		return new MiniDetailsFragment();
	}

	private TextView mNameTextView;
	private TextView mLocationTextView;
	private RatingBar mRatingBar;

	private HotelCollage mCollageHandler;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((SearchResultsFragmentActivity) getActivity()).mEventManager.registerEventHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mini_details, container, false);

		mNameTextView = (TextView) view.findViewById(R.id.name_text_view);
		mLocationTextView = (TextView) view.findViewById(R.id.location_text_view);
		mRatingBar = (RatingBar) view.findViewById(R.id.hotel_rating_bar);
		mCollageHandler = new HotelCollage(view, mOnImageClickedListener);

		updateViews(getInstance().mProperty, view);

		return view;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		((SearchResultsFragmentActivity) getActivity()).mEventManager.unregisterEventHandler(this);
	}

	//////////////////////////////////////////////////////////////////////////
	// Callbacks

	private OnCollageImageClickedListener mOnImageClickedListener = new OnCollageImageClickedListener() {
		public void onImageClicked(Media media) {
			((SearchResultsFragmentActivity) getActivity()).moreDetailsForPropertySelected();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Views

	private void updateViews(Property property) {
		updateViews(property, getView());
	}

	private void updateViews(Property property, View view) {
		// don't update views if there is no
		// view attached.
		if (view != null && property != null) {
			mNameTextView.setText(property.getName());
			mLocationTextView.setText(StrUtils.formatAddress(property.getLocation()).replace("\n", ", "));
			mRatingBar.setRating((float) property.getHotelRating());
			mCollageHandler.updateCollage(property);

			AvailabilitySummaryLayoutUtils.setupAvailabilitySummary(getActivity(), property, view);

			// update the summarized rates if they are available
			AvailabilityResponse availabilityResponse = ((SearchResultsFragmentActivity) getActivity())
					.getRoomsAndRatesAvailability();
			AvailabilitySummaryLayoutUtils.updateSummarizedRates(getActivity(), property, availabilityResponse, view,
					getString(R.string.see_details), seeDetailsOnClickListener,
					((SearchResultsFragmentActivity) getActivity()).mOnRateClickListener);
		}
	}

	private OnClickListener seeDetailsOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			((SearchResultsFragmentActivity) getActivity()).moreDetailsForPropertySelected();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case SearchResultsFragmentActivity.EVENT_PROPERTY_SELECTED:
			updateViews((Property) data);
			break;
		case SearchResultsFragmentActivity.EVENT_AVAILABILITY_SEARCH_STARTED:
			AvailabilitySummaryLayoutUtils.showLoadingForRates(getActivity(), getView());
			break;
		case SearchResultsFragmentActivity.EVENT_AVAILABILITY_SEARCH_ERROR:
			AvailabilitySummaryLayoutUtils.showErrorForRates(getView(), (String) data);
			break;
		case SearchResultsFragmentActivity.EVENT_AVAILABILITY_SEARCH_COMPLETE:
			AvailabilitySummaryLayoutUtils.showRatesContainer(getView());
			AvailabilitySummaryLayoutUtils.updateSummarizedRates(getActivity(), getInstance().mProperty,
					(AvailabilityResponse) data, getView(), getString(R.string.see_details), seeDetailsOnClickListener,
					((SearchResultsFragmentActivity) getActivity()).mOnRateClickListener);
			break;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience method

	public SearchResultsFragmentActivity.InstanceFragment getInstance() {
		return ((SearchResultsFragmentActivity) getActivity()).mInstance;
	}
}
