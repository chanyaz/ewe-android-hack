package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelFilter.PriceRange;
import com.expedia.bookings.data.HotelFilter.SearchRadius;
import com.expedia.bookings.data.HotelFilter.Sort;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.ResultsHotelListFragment.ISortAndFilterListener;
import com.expedia.bookings.interfaces.IResultsFilterDoneClickedListener;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.HotelNeighborhoodLayout;
import com.expedia.bookings.widget.HotelNeighborhoodLayout.OnNeighborhoodsChangedListener;
import com.expedia.bookings.widget.ImageRadioButton;
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
	private Spinner mSortByButtonGroup;
	private SlidingRadioGroup mRadiusButtonGroup;
	private SlidingRadioGroup mRatingButtonGroup;
	private SlidingRadioGroup mPriceButtonGroup;
	private View mVipAccessContainer;
	private Switch mVipAccessSwitch;
	private HotelNeighborhoodLayout mNeighborhoodLayout;
	private List<ISortAndFilterListener> mSortAndFilterListeners = new ArrayList<ResultsHotelListFragment.ISortAndFilterListener>();
	private IResultsFilterDoneClickedListener mResultsFilterDoneClickedListener;

	// We don't want Omniture events being sent for sorts and filters
	// changing as their views are initialized.
	private boolean mAllowSortFilterOmnitureReporting;

	public static ResultsHotelsFiltersFragment newInstance() {
		return new ResultsHotelsFiltersFragment();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		ISortAndFilterListener sortAndFilterListener = Ui
			.findFragmentListener(this, ISortAndFilterListener.class, true);
		mSortAndFilterListeners.add(sortAndFilterListener);
		mResultsFilterDoneClickedListener = Ui.findFragmentListener(this, IResultsFilterDoneClickedListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_filters, null);

		Ui.findView(view, R.id.done_button).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onFilterClosed();
				mResultsFilterDoneClickedListener.onFilterDoneClicked();
				for (ISortAndFilterListener sortAndFilterListener : mSortAndFilterListeners) {
					sortAndFilterListener.onSortAndFilterClicked();
				}
			}
		});

		mHotelNameEditText = Ui.findView(view, R.id.filter_hotel_name_edit_text);
		mSortByButtonGroup = Ui.findView(view, R.id.sort_by_selection_spinner);
		mRadiusButtonGroup = Ui.findView(view, R.id.radius_filter_button_group);
		mRatingButtonGroup = Ui.findView(view, R.id.rating_filter_button_group);
		mPriceButtonGroup = Ui.findView(view, R.id.price_filter_button_group);
		mVipAccessContainer = Ui.findView(view, R.id.filter_vip_container);
		mVipAccessSwitch = Ui.findView(view, R.id.filter_vip_access_switch);

		mNeighborhoodLayout = Ui.findView(view, R.id.areas_layout);

		// Configure functionality of each filter control
		mHotelNameEditText.addTextChangedListener(mHotelNameTextWatcher);
		mSortByButtonGroup.setOnItemSelectedListener(mSortCheckedChangeListener);
		mRadiusButtonGroup.setOnCheckedChangeListener(mRadiusCheckedChangeListener);
		mRatingButtonGroup.setOnCheckedChangeListener(mStarRatingCheckedChangeListener);
		mPriceButtonGroup.setOnCheckedChangeListener(mPriceCheckedChangeListener);
		mVipAccessSwitch.setOnCheckedChangeListener(mVipAccessCheckedListener);
		mNeighborhoodLayout.setOnNeighborhoodsChangedListener(mNeighborhoodsChangedListener);

		return view;
	}

	private void initializeViews(HotelSearch search, HotelFilter filter) {
		mAllowSortFilterOmnitureReporting = false;
		mHotelNameEditText.setText(filter.getHotelName());

		// Configure radius labels
		LayoutUtils.configureRadiusFilterLabels(getActivity(), mRadiusButtonGroup, filter);

		// Show/hide "sort by distance" depending on if this is a distance type search
		boolean showDistance = search != null
			&& search.getSearchParams() != null
			&& search.getSearchParams().getSearchType().shouldShowDistance();

		List<String> sortOptions = new ArrayList<>();
		sortOptions.addAll(Arrays.asList(getResources().getStringArray(R.array.sort_options_hotels)));
		if (showDistance) {
			sortOptions.add(getString(R.string.distance));
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_sort_item, sortOptions);
		adapter.setDropDownViewResource(R.layout.spinner_sort_dropdown_item);

		mSortByButtonGroup.setAdapter(adapter);
		int selectedPosition = filter.getSort().ordinal();
		mSortByButtonGroup.setSelection(selectedPosition);

		int checkId;
		switch (filter.getSearchRadius()) {
		case SMALL:
			checkId = R.id.radius_small_button;
			break;
		case MEDIUM:
			checkId = R.id.radius_medium_button;
			break;
		case LARGE:
			checkId = R.id.radius_large_button;
			break;
		case ALL: //FALL THRU
		default:
			checkId = R.id.radius_all_button;
			break;
		}
		mRadiusButtonGroup.check(checkId);
		SearchType searchType = search.getSearchParams().getSearchType();
		// Visibility of SortBy & Filters for "distance" are declared in searchType.
		mRadiusButtonGroup.setVisibility(searchType.shouldShowDistance() ? View.VISIBLE : View.GONE);

		setDrawableForRatingRadioBtnBackground();

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
		case CHEAP:
			checkId = R.id.price_cheap_button;
			break;
		case MODERATE:
			checkId = R.id.price_moderate_button;
			break;
		case EXPENSIVE:
			checkId = R.id.price_expensive_button;
			break;
		case ALL: //FALL THRU
		default:
			checkId = R.id.price_all_button;
			break;
		}
		mPriceButtonGroup.check(checkId);

		if (PointOfSale.getPointOfSale().supportsVipAccess()) {
			mVipAccessContainer.setVisibility(View.VISIBLE);
			mVipAccessSwitch.setChecked(filter.isVipAccessOnly());
		}
		else {
			mVipAccessContainer.setVisibility(View.GONE);
			filter.setVipAccessOnly(false);
		}

		// Configure Areas/Neighborhoods
		mNeighborhoodLayout.setNeighborhoods(search.getSearchResponse(), filter);
		mAllowSortFilterOmnitureReporting = true;
	}

	@Override
	public void onStart() {
		super.onStart();

		HotelSearch search = Db.getHotelSearch();
		if (search != null) {
			initializeViews(search, Db.getFilter());
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

	private final AdapterView.OnItemSelectedListener mSortCheckedChangeListener = new AdapterView.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			Sort sort = Sort.values()[position];

			HotelFilter filter = Db.getFilter();
			filter.setSort(sort);
			filter.notifyFilterChanged();

			onSortChanged();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	private final OnCheckedChangeListener mRadiusCheckedChangeListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			SearchRadius searchRadius;
			switch (checkedId) {
			case R.id.radius_small_button:
				searchRadius = SearchRadius.SMALL;
				break;
			case R.id.radius_medium_button:
				searchRadius = SearchRadius.MEDIUM;
				break;
			case R.id.radius_large_button:
				searchRadius = SearchRadius.LARGE;
				break;
			case R.id.radius_all_button: //FALL THRU
			default:
				searchRadius = SearchRadius.ALL;
				break;
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
			case R.id.rating_low_button:
				minStarRating = 3;
				break;
			case R.id.rating_medium_button:
				minStarRating = 4;
				break;
			case R.id.rating_high_button:
				minStarRating = 5;
				break;
			case R.id.rating_all_button: //FALL THRU
			default:
				minStarRating = 0;
				break;
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
			case R.id.price_cheap_button:
				priceRange = PriceRange.CHEAP;
				break;
			case R.id.price_moderate_button:
				priceRange = PriceRange.MODERATE;
				break;
			case R.id.price_expensive_button:
				priceRange = PriceRange.EXPENSIVE;
				break;
			case R.id.price_all_button: //FALL THRU
			default:
				priceRange = PriceRange.ALL;
				break;
			}

			HotelFilter filter = Db.getFilter();
			filter.setPriceRange(priceRange);
			filter.notifyFilterChanged();
			mNeighborhoodLayout.updateWidgets(Db.getHotelSearch().getSearchResponse());

			onPriceFilterChanged();
		}
	};

	private final CompoundButton.OnCheckedChangeListener mVipAccessCheckedListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton button, boolean vipAccessOnly) {
			HotelFilter filter = Db.getFilter();
			filter.setVipAccessOnly(vipAccessOnly);
			filter.notifyFilterChanged();
			mNeighborhoodLayout.updateWidgets(Db.getHotelSearch().getSearchResponse());

			OmnitureTracking.trackLinkHotelRefineVip(vipAccessOnly);
		}
	};

	private final OnNeighborhoodsChangedListener mNeighborhoodsChangedListener = new OnNeighborhoodsChangedListener() {
		@Override
		public void onNeighborhoodsChanged(Set<Integer> neighborhoods) {
			HotelFilter filter = Db.getFilter();
			filter.setNeighborhoods(neighborhoods);
			filter.notifyFilterChanged();
			OmnitureTracking.trackTabletNeighborhoodFilter();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	private void onFilterClosed() {
		Log.d("Tracking \"App.Hotels.Search.Refine.Name\" change...");
		OmnitureTracking.trackLinkHotelRefineName(mHotelNameEditText.getText().toString());
	}

	private void onSortChanged() {
		if (mAllowSortFilterOmnitureReporting) {
			Log.d("Tracking \"App.Hotels.Search.Sort\" change...");
			Sort sort = Sort.values()[mSortByButtonGroup.getSelectedItemPosition()];
			switch (sort) {
			case PRICE:
				OmnitureTracking.trackLinkHotelSort(OmnitureTracking.HOTELS_SEARCH_SORT_PRICE);
				break;
			case RATING:
				OmnitureTracking.trackLinkHotelSort(OmnitureTracking.HOTELS_SEARCH_SORT_RATING);
				break;
			case DISTANCE:
				OmnitureTracking.trackLinkHotelSort(OmnitureTracking.HOTELS_SEARCH_SORT_DISTANCE);
				break;
			case DEALS:
				OmnitureTracking.trackLinkHotelSort(OmnitureTracking.HOTELS_SEARCH_SORT_DEALS);
				break;
			case RECOMMENDED: //FALL THRU
			default:
				OmnitureTracking.trackLinkHotelSort(OmnitureTracking.HOTELS_SEARCH_SORT_POPULAR);
				break;
			}
		}
	}

	private void onPriceFilterChanged() {
		if (mAllowSortFilterOmnitureReporting) {
			Log.d("Tracking \"App.Hotels.Search.Refine.PriceRange\" change...");

			switch (mPriceButtonGroup.getCheckedRadioButtonId()) {
			case R.id.price_cheap_button:
				OmnitureTracking.trackLinkHotelRefinePriceRange(PriceRange.CHEAP);
				break;
			case R.id.price_moderate_button:
				OmnitureTracking.trackLinkHotelRefinePriceRange(PriceRange.MODERATE);
				break;
			case R.id.price_expensive_button:
				OmnitureTracking.trackLinkHotelRefinePriceRange(PriceRange.EXPENSIVE);
				break;
			case R.id.price_all_button: //FALL THRU
			default:
				OmnitureTracking.trackLinkHotelRefinePriceRange(PriceRange.ALL);
				break;
			}
		}
	}

	private void onRadiusFilterChanged() {
		if (mAllowSortFilterOmnitureReporting) {
			Log.d("Tracking \"App.Hotels.Search.Refine.SearchRadius\" rating change...");

			switch (mRadiusButtonGroup.getCheckedRadioButtonId()) {
			case R.id.radius_small_button:
				OmnitureTracking.trackLinkHotelRefineSearchRadius(SearchRadius.SMALL);
				break;
			case R.id.radius_medium_button:
				OmnitureTracking.trackLinkHotelRefineSearchRadius(SearchRadius.MEDIUM);
				break;
			case R.id.radius_large_button:
				OmnitureTracking.trackLinkHotelRefineSearchRadius(SearchRadius.LARGE);
				break;
			case R.id.radius_all_button: //FALL THRU
			default:
				OmnitureTracking.trackLinkHotelRefineSearchRadius(SearchRadius.ALL);
				break;
			}
		}
	}

	private void onRatingFilterChanged() {
		if (mAllowSortFilterOmnitureReporting) {
			Log.d("Tracking \"App.Hotels.Search.Refine\" rating change...");
			switch (mRatingButtonGroup.getCheckedRadioButtonId()) {
			case R.id.rating_low_button:
				OmnitureTracking.trackLinkHotelRefineRating("3Stars");
				break;
			case R.id.rating_medium_button:
				OmnitureTracking.trackLinkHotelRefineRating("4Stars");
				break;
			case R.id.rating_high_button:
				OmnitureTracking.trackLinkHotelRefineRating("5Stars");
				break;
			case R.id.rating_all_button: //FALL THRU
			default:
				OmnitureTracking.trackLinkHotelRefineRating("AllStars");
				break;
			}
		}
	}

	private void setDrawableForRatingRadioBtnBackground() {
		boolean shouldShowCircleForRatings = PointOfSale.getPointOfSale().shouldShowCircleForRatings();

		ImageRadioButton ratingLowButton = (ImageRadioButton) mRatingButtonGroup.findViewById(R.id.rating_low_button);
		ImageRadioButton ratingMediumButton = (ImageRadioButton) mRatingButtonGroup
			.findViewById(R.id.rating_medium_button);
		ImageRadioButton ratingHighButton = (ImageRadioButton) mRatingButtonGroup.findViewById(R.id.rating_high_button);

		ratingLowButton.setDrawable(getResources()
			.getDrawable(shouldShowCircleForRatings ? R.drawable.btn_filter_3circle : R.drawable.btn_filter_3star));
		ratingMediumButton.setDrawable(getResources()
			.getDrawable(shouldShowCircleForRatings ? R.drawable.btn_filter_4circle : R.drawable.btn_filter_4star));
		ratingHighButton.setDrawable(getResources()
			.getDrawable(shouldShowCircleForRatings ? R.drawable.btn_filter_5circle : R.drawable.btn_filter_5star));
	}
}
