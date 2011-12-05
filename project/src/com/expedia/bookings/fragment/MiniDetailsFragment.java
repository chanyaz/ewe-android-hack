package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
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
	boolean mDoesAvailabilityContainerExist = false;

	private HotelCollage mCollageHandler;

	private Handler mHandler = new Handler();

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

		// #11181: We make the root view clickable so that clicks don't bleed through to the underlying MapView
		view.setClickable(true);

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
			if (getInstance().mProperty.getMediaCount() > 0) {
				((SearchResultsFragmentActivity) getActivity()).startHotelGalleryActivity(media);
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

			// its possible for the summary container to not exist at all
			// in which case there's no setup to be done for this container
			if (mDoesAvailabilityContainerExist) {

				/*
				 * If the app is resumed, post the setup of the
				 * availability summary container to a runnable so that
				 * its only run after all the layout is complete.
				 * 
				 * The reason for posting to runnable instead of 
				 * just using a layoutChangedListener is because
				 * it seems like the system makes multiple passes
				 * to attempt to figure out the layout, therefore
				 * showing the intermediate steps of the layout
				 * to the user.
				 * 
				 * NOTE: The whole reason to post a runnable
				 * or wait for the layout to complete is because
				 * we need to know accurate dimensions of the views 
				 * to be able to determine whether or not the text
				 * is too wide for the container.
				 * 
				 * Another approach to this problem would be to have static
				 * set widths for the containers we care about, but this 
				 * provides for a more generic solution.
				 */
				if (isResumed()) {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							setupAvailabilityContainer(property, view);
						}
					});
				}
				/*
				 * This listener handles the case of orientation
				 * change as posting to the runnable during orientation
				 * change doesn't seem to do the trick since the accurate dimensions
				 * of views are not available yet.
				 * 
				 * If the app is not resumed, wait till the layout
				 * is complete to ensure that we setup the container
				 * appropriately on rotation. 
				 */
				else {
					final View availabilityContainer = view.findViewById(R.id.availability_summary_container);
					availabilityContainer.addOnLayoutChangeListener(new OnLayoutChangeListener() {

						@Override
						public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
								int oldTop, int oldRight, int oldBottom) {
							if (left == 0 && right == 0 && top == 0 && bottom == 0) {
								return;
							}
							setupAvailabilityContainer(property, view);
							availabilityContainer.removeOnLayoutChangeListener(this);
						}
					});

				}
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

		if (property.getLowestRate().isOnSale()) {
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
			
			
			// 11364: ensuring to specifically handle the case where the "from" word can be before
			// or after the min price
			if (startingIndexOfDisplayRate > 0) {
				str.setSpan(textBlackColorSpan, 0, startingIndexOfDisplayRate - 1, 0);
			}
			else if (startingIndexOfDisplayRate == 0) {
				str.setSpan(textBlackColorSpan, startingIndexOfDisplayRate + basePriceString.length(),
						salePriceString.length(), 0);
			}
			
			str.setSpan(textStyleSpan, 0, salePriceString.length(), 0);

			salePrice.setText(str);
			basePrice.setVisibility(View.GONE);
			perNightText.setTextColor(getResources().getColor(android.R.color.black));
		}

	}

	private OnClickListener seeDetailsOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			((SearchResultsFragmentActivity) getActivity())
					.moreDetailsForPropertySelected(SearchResultsFragmentActivity.SOURCE_MINI_DETAILS);
		}
	};

	private void setupAvailabilityContainer(final Property property, final View view) {
		AvailabilitySummaryLayoutUtils.setupAvailabilitySummary(getActivity(), property, view);
		// update the summarized rates if they are available
		AvailabilityResponse availabilityResponse = ((SearchResultsFragmentActivity) getActivity())
				.getRoomsAndRatesAvailability();
		AvailabilitySummaryLayoutUtils.updateSummarizedRates(getActivity(), property, availabilityResponse, view,
				getString(R.string.see_details), seeDetailsOnClickListener,
				((SearchResultsFragmentActivity) getActivity()).mOnRateClickListener);
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
				AvailabilitySummaryLayoutUtils
						.updateSummarizedRates(getActivity(), getInstance().mProperty, (AvailabilityResponse) data,
								getView(), getString(R.string.see_details), seeDetailsOnClickListener,
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
