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
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.AvailabilitySummaryWidget;
import com.expedia.bookings.widget.AvailabilitySummaryWidget.AvailabilitySummaryListener;
import com.expedia.bookings.widget.HotelCollage;
import com.expedia.bookings.widget.HotelCollage.OnCollageImageClickedListener;

public class MiniDetailsFragment extends Fragment implements EventHandler, AvailabilitySummaryListener {

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
		((SearchResultsFragmentActivity) getActivity()).mEventManager.registerEventHandler(this);

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
			((SearchResultsFragmentActivity) getActivity()).startHotelGalleryActivity(media);
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

			AvailabilityResponse availabilityResponse = ((SearchResultsFragmentActivity) getActivity())
					.getRoomsAndRatesAvailability();

			if (availabilityResponse != null) {
				mAvailabilitySummary.showRates(availabilityResponse);
			}
		}

	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case SearchResultsFragmentActivity.EVENT_PROPERTY_SELECTED:
			updateViews((Property) data);
			break;
		case SearchResultsFragmentActivity.EVENT_AVAILABILITY_SEARCH_STARTED:
			mAvailabilitySummary.showProgressBar();
			break;
		case SearchResultsFragmentActivity.EVENT_AVAILABILITY_SEARCH_ERROR:
			mAvailabilitySummary.showError((String) data);
			break;
		case SearchResultsFragmentActivity.EVENT_AVAILABILITY_SEARCH_COMPLETE:
			mAvailabilitySummary.showRates((AvailabilityResponse) data);
			Property p = ((AvailabilityResponse) data).getProperty();
			getInstance().mProperty.setAmenityMask(p.getAmenityMask());
			getInstance().mProperty.setDescriptionText(p.getDescriptionText());
			getInstance().mProperty.setMediaList(p.getMediaList());
			updateViews(getInstance().mProperty);
			break;
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

	//////////////////////////////////////////////////////////////////////////
	// Convenience method

	public SearchResultsFragmentActivity.InstanceFragment getInstance() {
		return ((SearchResultsFragmentActivity) getActivity()).mInstance;
	}
}
