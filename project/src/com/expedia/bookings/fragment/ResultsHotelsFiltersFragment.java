package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelFilter.PriceRange;
import com.expedia.bookings.data.HotelFilter.SearchRadius;
import com.expedia.bookings.data.HotelFilter.Sort;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.ResultsHotelListFragment.ISortAndFilterListener;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.HotelNeighborhoodLayout;
import com.expedia.bookings.widget.HotelNeighborhoodLayout.OnNeighborhoodsChangedListener;
import com.expedia.bookings.widget.SlidingRadioGroup;
import com.expedia.bookings.widget.Switch;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

/**
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
public class ResultsHotelsFiltersFragment extends Fragment {

	// TODO: a lot of this is copied straight from FilterDialogFragment. But will we
	// even need FilterDialogFragment after 4.0?

	private EditText mHotelNameEditText;
	private SlidingRadioGroup mSortByButtonGroup;
	private SlidingRadioGroup mRadiusButtonGroup;
	private SlidingRadioGroup mRatingButtonGroup;
	private SlidingRadioGroup mPriceButtonGroup;
	private View mVipAccessLabel;
	private Switch mVipAccessSwitch;
	private HotelNeighborhoodLayout mNeighborhoodLayout;
	private List<ISortAndFilterListener> mSortAndFilterListeners = new ArrayList<ResultsHotelListFragment.ISortAndFilterListener>();
	private ISortAndFilterListener mSortAndFilterListener;

	public static ResultsHotelsFiltersFragment newInstance() {
		ResultsHotelsFiltersFragment frag = new ResultsHotelsFiltersFragment();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mSortAndFilterListener = Ui.findFragmentListener(this, ISortAndFilterListener.class, true);
		mSortAndFilterListeners.add(mSortAndFilterListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_filters, null);

		Ui.findView(view, R.id.done_button).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onFilterClosed();
				for (ISortAndFilterListener sortAndFilterListener : mSortAndFilterListeners) {
					sortAndFilterListener.onSortAndFilterClicked();
				}
			}
		});

		mHotelNameEditText = Ui.findView(view, R.id.filter_hotel_name_edit_text);
		mSortByButtonGroup = Ui.findView(view, R.id.sort_by_button_group);
		mRadiusButtonGroup = Ui.findView(view, R.id.radius_filter_button_group);
		mRatingButtonGroup = Ui.findView(view, R.id.rating_filter_button_group);
		mPriceButtonGroup = Ui.findView(view, R.id.price_filter_button_group);
		mVipAccessLabel = Ui.findView(view, R.id.filter_vip_access_label);
		mVipAccessSwitch = Ui.findView(view, R.id.filter_vip_access_switch);

		mNeighborhoodLayout = Ui.findView(view, R.id.areas_layout);

		// Configure functionality of each filter control
		mHotelNameEditText.addTextChangedListener(mHotelNameTextWatcher);
		mSortByButtonGroup.setOnCheckedChangeListener(mSortCheckedChangeListener);
		mRadiusButtonGroup.setOnCheckedChangeListener(mRadiusCheckedChangeListener);
		mRatingButtonGroup.setOnCheckedChangeListener(mStarRatingCheckedChangeListener);
		mPriceButtonGroup.setOnCheckedChangeListener(mPriceCheckedChangeListener);
		mVipAccessSwitch.setOnCheckedChangeListener(mVipAccessCheckedListener);
		mNeighborhoodLayout.setOnNeighborhoodsChangedListener(mNeighborhoodsChangedListener);

		return view;
	}

	private void initializeViews(HotelSearch search, HotelFilter filter) {
		mHotelNameEditText.setText(filter.getHotelName());

		// Configure radius labels
		LayoutUtils.configureRadiusFilterLabels(getActivity(), mRadiusButtonGroup, filter);

		// Show/hide "sort by distance" depending on if this is a distance type search
		boolean showDistance = search != null
				&& search.getSearchParams() != null
				&& search.getSearchParams().getSearchType().shouldShowDistance();
		Ui.findView(getActivity(), R.id.sort_by_distance_button).setVisibility(showDistance ? View.VISIBLE : View.GONE);

		int checkId;
		switch (filter.getSort()) {
		case PRICE: {
			checkId = R.id.sort_by_price_button;
			break;
		}
		case RATING: {
			checkId = R.id.sort_by_rating_button;
			break;
		}
		case DISTANCE: {
			checkId = R.id.sort_by_distance_button;
			break;
		}
		case POPULAR:
		default: {
			checkId = R.id.sort_by_popular_button;
			break;
		}
		}
		mSortByButtonGroup.check(checkId);

		switch (filter.getSearchRadius()) {
		case SMALL: {
			checkId = R.id.radius_small_button;
			break;
		}
		case MEDIUM: {
			checkId = R.id.radius_medium_button;
			break;
		}
		case LARGE: {
			checkId = R.id.radius_large_button;
			break;
		}
		case ALL:
		default: {
			checkId = R.id.radius_all_button;
			break;
		}
		}
		mRadiusButtonGroup.check(checkId);
		SearchType searchType = search.getSearchParams().getSearchType();
		mRadiusButtonGroup.setVisibility(searchType == SearchType.ADDRESS || searchType == SearchType.MY_LOCATION
				|| searchType == SearchType.POI ? View.VISIBLE : View.GONE);

		double minStarRating = filter.getMinimumStarRating();
		if (minStarRating >= 5) {
			checkId = R.id.rating_high_button;
		}
		else if (minStarRating >= 4) {
			checkId = R.id.rating_medium_button;
		}
		else if (minStarRating >= 3) {
			checkId = R.id.rating_low_button;
		}
		else {
			checkId = R.id.rating_all_button;
		}
		mRatingButtonGroup.check(checkId);

		switch (filter.getPriceRange()) {
		case CHEAP: {
			checkId = R.id.price_cheap_button;
			break;
		}
		case MODERATE: {
			checkId = R.id.price_moderate_button;
			break;
		}
		case EXPENSIVE: {
			checkId = R.id.price_expensive_button;
			break;
		}
		case ALL:
		default: {
			checkId = R.id.price_all_button;
			break;
		}
		}
		mPriceButtonGroup.check(checkId);

		if (PointOfSale.getPointOfSale().supportsVipAccess()) {
			mVipAccessLabel.setVisibility(View.VISIBLE);
			mVipAccessSwitch.setVisibility(View.VISIBLE);
		}
		else {
			mVipAccessLabel.setVisibility(View.GONE);
			mVipAccessSwitch.setVisibility(View.GONE);
			filter.setVipAccessOnly(false);
		}

		// Configure Areas/Neighborhoods
		mNeighborhoodLayout.setNeighborhoods(search.getSearchResponse(), filter);
	}

	@Override
	public void onStart() {
		super.onStart();

		HotelSearch search = Db.getHotelSearch();
		if (search != null) {
			// Populate views based on the filter
			HotelFilter filter = Db.getFilter();
			initializeViews(search, filter);

			HotelSearchResponse response = search.getSearchResponse();
			if (response != null) {
				response.setFilter(filter);
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listeners for filter form changes

	private final TextWatcher mHotelNameTextWatcher = new TextWatcher() {
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			HotelFilter filter = Db.getFilter();
			filter.setHotelName(s.toString());
			filter.notifyFilterChanged();
			mNeighborhoodLayout.updateWidgets(Db.getHotelSearch().getSearchResponse());
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// Do nothing
		}

		public void afterTextChanged(Editable s) {
			// Do nothing
		}
	};

	private final OnCheckedChangeListener mSortCheckedChangeListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			Sort sort;
			switch (checkedId) {
			case R.id.sort_by_price_button: {
				sort = Sort.PRICE;
				break;
			}
			case R.id.sort_by_rating_button: {
				sort = Sort.RATING;
				break;
			}
			case R.id.sort_by_distance_button: {
				sort = Sort.DISTANCE;
				break;
			}
			case R.id.sort_by_popular_button:
			default: {
				sort = Sort.POPULAR;
				break;
			}
			}

			HotelFilter filter = Db.getFilter();
			filter.setSort(sort);
			filter.notifyFilterChanged();

			onSortChanged();
		}
	};

	private final OnCheckedChangeListener mRadiusCheckedChangeListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			SearchRadius searchRadius;
			switch (checkedId) {
			case R.id.radius_small_button: {
				searchRadius = SearchRadius.SMALL;
				break;
			}
			case R.id.radius_medium_button: {
				searchRadius = SearchRadius.MEDIUM;
				break;
			}
			case R.id.radius_large_button: {
				searchRadius = SearchRadius.LARGE;
				break;
			}
			case R.id.radius_all_button:
			default: {
				searchRadius = SearchRadius.ALL;
				break;
			}
			}

			HotelFilter filter = Db.getFilter();
			filter.setSearchRadius(searchRadius);
			filter.notifyFilterChanged();
			mNeighborhoodLayout.updateWidgets(Db.getHotelSearch().getSearchResponse());

			onRadiusFilterChanged();
		}
	};

	private final OnCheckedChangeListener mStarRatingCheckedChangeListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			double minStarRating;
			switch (checkedId) {
			case R.id.rating_low_button: {
				minStarRating = 3;
				break;
			}
			case R.id.rating_medium_button: {
				minStarRating = 4;
				break;
			}
			case R.id.rating_high_button: {
				minStarRating = 5;
				break;
			}
			case R.id.rating_all_button:
			default: {
				minStarRating = 0;
				break;
			}
			}

			HotelFilter filter = Db.getFilter();
			filter.setMinimumStarRating(minStarRating);
			filter.notifyFilterChanged();
			mNeighborhoodLayout.updateWidgets(Db.getHotelSearch().getSearchResponse());

			onRatingFilterChanged();
		}
	};

	private final OnCheckedChangeListener mPriceCheckedChangeListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			PriceRange priceRange;
			switch (checkedId) {
			case R.id.price_cheap_button: {
				priceRange = PriceRange.CHEAP;
				break;
			}
			case R.id.price_moderate_button: {
				priceRange = PriceRange.MODERATE;
				break;
			}
			case R.id.price_expensive_button: {
				priceRange = PriceRange.EXPENSIVE;
				break;
			}
			case R.id.price_all_button:
			default: {
				priceRange = PriceRange.ALL;
				break;
			}
			}

			HotelFilter filter = Db.getFilter();
			filter.setPriceRange(priceRange);
			filter.notifyFilterChanged();
			mNeighborhoodLayout.updateWidgets(Db.getHotelSearch().getSearchResponse());

			onPriceFilterChanged();
		}
	};

	private final CompoundButton.OnCheckedChangeListener mVipAccessCheckedListener
			= new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton button, boolean vipAccessOnly) {
			HotelFilter filter = Db.getFilter();
			filter.setVipAccessOnly(vipAccessOnly);
			filter.notifyFilterChanged();
			mNeighborhoodLayout.updateWidgets(Db.getHotelSearch().getSearchResponse());

			OmnitureTracking.trackLinkHotelRefineVip(getActivity(), vipAccessOnly);
		}
	};

	private final OnNeighborhoodsChangedListener mNeighborhoodsChangedListener = new OnNeighborhoodsChangedListener() {
		@Override
		public void onNeighborhoodsChanged(Set<Integer> neighborhoods) {
			HotelFilter filter = Db.getFilter();
			filter.setNeighborhoods(neighborhoods);
			filter.notifyFilterChanged();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	private void onFilterClosed() {
		Log.d("Tracking \"App.Hotels.Search.Refine.Name\" change...");
		OmnitureTracking.trackLinkHotelRefineName(getActivity(), mHotelNameEditText.getText().toString());
	}

	private void onSortChanged() {
		Log.d("Tracking \"App.Hotels.Search.Sort\" change...");

		switch (mSortByButtonGroup.getCheckedRadioButtonId()) {
		case R.id.sort_by_price_button: {
			OmnitureTracking.trackLinkHotelSort(getActivity(), OmnitureTracking.HOTELS_SEARCH_SORT_PRICE);
			break;
		}
		case R.id.sort_by_rating_button: {
			OmnitureTracking.trackLinkHotelSort(getActivity(), OmnitureTracking.HOTELS_SEARCH_SORT_RATING);
			break;
		}
		case R.id.sort_by_distance_button: {
			OmnitureTracking.trackLinkHotelSort(getActivity(), OmnitureTracking.HOTELS_SEARCH_SORT_DISTANCE);
			break;
		}
		case R.id.sort_by_popular_button:
		default: {
			OmnitureTracking.trackLinkHotelSort(getActivity(), OmnitureTracking.HOTELS_SEARCH_SORT_POPULAR);
			break;
		}
		}
	}

	private void onPriceFilterChanged() {
		Log.d("Tracking \"App.Hotels.Search.Refine.PriceRange\" change...");

		switch (mPriceButtonGroup.getCheckedRadioButtonId()) {
		case R.id.price_cheap_button: {
			OmnitureTracking.trackLinkHotelRefinePriceRange(getActivity(), PriceRange.CHEAP);
			break;
		}
		case R.id.price_moderate_button: {
			OmnitureTracking.trackLinkHotelRefinePriceRange(getActivity(), PriceRange.MODERATE);
			break;
		}
		case R.id.price_expensive_button: {
			OmnitureTracking.trackLinkHotelRefinePriceRange(getActivity(), PriceRange.EXPENSIVE);
			break;
		}
		case R.id.price_all_button:
		default: {
			OmnitureTracking.trackLinkHotelRefinePriceRange(getActivity(), PriceRange.ALL);
			break;
		}
		}
	}

	private void onRadiusFilterChanged() {
		Log.d("Tracking \"App.Hotels.Search.Refine.SearchRadius\" rating change...");

		switch (mRadiusButtonGroup.getCheckedRadioButtonId()) {
		case R.id.radius_small_button: {
			OmnitureTracking.trackLinkHotelRefineSearchRadius(getActivity(), SearchRadius.SMALL);
			break;
		}
		case R.id.radius_medium_button: {
			OmnitureTracking.trackLinkHotelRefineSearchRadius(getActivity(), SearchRadius.MEDIUM);
			break;
		}
		case R.id.radius_large_button: {
			OmnitureTracking.trackLinkHotelRefineSearchRadius(getActivity(), SearchRadius.LARGE);
			break;
		}
		case R.id.radius_all_button:
		default: {
			OmnitureTracking.trackLinkHotelRefineSearchRadius(getActivity(), SearchRadius.ALL);
			break;
		}
		}
	}

	private void onRatingFilterChanged() {
		Log.d("Tracking \"App.Hotels.Search.Refine\" rating change...");

		switch (mRatingButtonGroup.getCheckedRadioButtonId()) {
		case R.id.rating_low_button: {
			OmnitureTracking.trackLinkHotelRefineRating(getActivity(), "3Stars");
			break;
		}
		case R.id.rating_medium_button: {
			OmnitureTracking.trackLinkHotelRefineRating(getActivity(), "4Stars");
			break;
		}
		case R.id.rating_high_button: {
			OmnitureTracking.trackLinkHotelRefineRating(getActivity(), "5Stars");
			break;
		}
		case R.id.rating_all_button:
		default: {
			OmnitureTracking.trackLinkHotelRefineRating(getActivity(), "AllStars");
			break;
		}
		}
	}

	public void addSortAndFilterListener(ISortAndFilterListener sortAndFilterListener) {
		if (!mSortAndFilterListeners.contains(sortAndFilterListener)) {
			mSortAndFilterListeners.add(sortAndFilterListener);
		}
	}

}
