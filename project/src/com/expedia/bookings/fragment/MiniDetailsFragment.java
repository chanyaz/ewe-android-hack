package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
import com.mobiata.android.text.StrikethroughTagHandler;

public class MiniDetailsFragment extends Fragment implements EventHandler {

	public static MiniDetailsFragment newInstance() {
		return new MiniDetailsFragment();
	}

	private TextView mNameTextView;
	private TextView mLocationTextView;
	private RatingBar mRatingBar;
	boolean mDoesAvailabilityContainerExist = false;

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

		mDoesAvailabilityContainerExist = (view.findViewById(R.id.availability_summary_container) != null);
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

			// its possible for the summary container to not exist at all
			// in which case there's no setup to be done for this container
			if (mDoesAvailabilityContainerExist) {
				AvailabilitySummaryLayoutUtils.setupAvailabilitySummary(getActivity(), property,  view);
				// update the summarized rates if they are available
				AvailabilityResponse availabilityResponse = ((SearchResultsFragmentActivity) getActivity())
						.getRoomsAndRatesAvailability();
				AvailabilitySummaryLayoutUtils.updateSummarizedRates(getActivity(), property, 
						availabilityResponse, view, getString(R.string.see_details), seeDetailsOnClickListener, ((SearchResultsFragmentActivity) getActivity()).mOnRateClickListener);
			}

			View seeDetailsButton = view.findViewById(R.id.see_details_button);
			if (seeDetailsButton != null) {
				seeDetailsButton.setOnClickListener(seeDetailsOnClickListener);
			}

			updateMinPrice(view, property);
		}

	}

	private void updateMinPrice(View view, Property property) {
		View minPriceContainer = view.findViewById(R.id.min_price_container);

		// nothing to do if the container does not exist
		if (minPriceContainer == null) {
			return;
		}

		TextView salePrice = (TextView) view.findViewById(R.id.min_price_text_view);
		TextView basePrice = (TextView) view.findViewById(R.id.base_price_text_view);
		TextView perNightText = (TextView) view.findViewById(R.id.per_night_text_view);
		StyleSpan textStyleSpan = new StyleSpan(Typeface.BOLD);

		if (property.getLowestRate().getSavingsPercent() > 0) {
			minPriceContainer.setBackgroundResource(R.drawable.sale_ribbon_large);
			basePrice.setVisibility(View.VISIBLE);

			String minPriceString = StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate());
			Spannable str = new SpannableString(minPriceString);
			str.setSpan(textStyleSpan, 0, minPriceString.length(), 0);
			salePrice.setText(str);

			basePrice.setText(StrUtils.getStrikedThroughSpanned(StrUtils.formatHotelPrice(property.getLowestRate()
					.getDisplayBaseRate())));

			salePrice.setTextColor(getResources().getColor(android.R.color.white));
			perNightText.setTextColor(getResources().getColor(android.R.color.white));
		}
		else {
			minPriceContainer.setBackgroundResource(R.drawable.normal_ribbon);
			String basePriceString = StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate());
			String salePriceString = getActivity().getString(R.string.min_room_price_template, basePriceString);
			int startingIndexOfDisplayRate = salePriceString.indexOf(basePriceString);

			ForegroundColorSpan textColorSpan = new ForegroundColorSpan(getActivity().getResources().getColor(
					R.color.hotel_price_text_color));
			ForegroundColorSpan textBlackColorSpan = new ForegroundColorSpan(getActivity().getResources().getColor(
					android.R.color.black));
			Spannable str = new SpannableString(salePriceString);
			str.setSpan(textColorSpan, startingIndexOfDisplayRate,
					startingIndexOfDisplayRate + basePriceString.length(), 0);
			str.setSpan(textBlackColorSpan, 0, startingIndexOfDisplayRate - 1, 0);
			str.setSpan(textStyleSpan, 0, salePriceString.length(), 0);

			salePrice.setText(str);
			basePrice.setVisibility(View.GONE);
			perNightText.setTextColor(getResources().getColor(android.R.color.black));
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
			if (mDoesAvailabilityContainerExist) {
				AvailabilitySummaryLayoutUtils.showLoadingForRates(getActivity(), getView());
			}
			break;
		case SearchResultsFragmentActivity.EVENT_AVAILABILITY_SEARCH_ERROR:
			if (mDoesAvailabilityContainerExist) {
				AvailabilitySummaryLayoutUtils.showErrorForRates(getView(), (String) data);
			}
			break;
		case SearchResultsFragmentActivity.EVENT_AVAILABILITY_SEARCH_COMPLETE:
			if (mDoesAvailabilityContainerExist) {
				AvailabilitySummaryLayoutUtils.showRatesContainer(getView());
				AvailabilitySummaryLayoutUtils.updateSummarizedRates(getActivity(), getInstance().mProperty,
						(AvailabilityResponse) data, getView(), getString(R.string.see_details), seeDetailsOnClickListener,
						((SearchResultsFragmentActivity) getActivity()).mOnRateClickListener);
			}
			break;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience method

	public SearchResultsFragmentActivity.InstanceFragment getInstance() {
		return ((SearchResultsFragmentActivity) getActivity()).mInstance;
	}
}
