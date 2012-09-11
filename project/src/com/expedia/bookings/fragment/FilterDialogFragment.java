package com.expedia.bookings.fragment;

import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.Filter;
import com.expedia.bookings.data.Filter.PriceRange;
import com.expedia.bookings.data.Filter.SearchRadius;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.mobiata.android.Log;
import com.mobiata.android.widget.SegmentedControlGroup;

public class FilterDialogFragment extends DialogFragment {

	private EditText mHotelNameEditText;;
	private SegmentedControlGroup mRadiusButtonGroup;
	private SegmentedControlGroup mRatingButtonGroup;
	private SegmentedControlGroup mPriceButtonGroup;

	public static FilterDialogFragment newInstance() {
		return new FilterDialogFragment();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.fragment_dialog_filter, null);
		builder.setView(view);

		mHotelNameEditText = (EditText) view.findViewById(R.id.filter_hotel_name_edit_text);
		mRadiusButtonGroup = (SegmentedControlGroup) view.findViewById(R.id.radius_filter_button_group);
		mRatingButtonGroup = (SegmentedControlGroup) view.findViewById(R.id.rating_filter_button_group);
		mPriceButtonGroup = (SegmentedControlGroup) view.findViewById(R.id.price_filter_button_group);

		// Configure labels
		LayoutUtils.configureRadiusFilterLabels(getActivity(), mRadiusButtonGroup, Db.getSearchResponse().getFilter());

		// Need to set title in constructor or it will never show up
		builder.setTitle(getTitle());

		// Configure initial settings (based on the filter)
		Filter filter = Db.getSearchResponse().getFilter();
		mHotelNameEditText.setText(filter.getHotelName());

		int checkId;
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
		SearchType searchType = Db.getSearchParams().getSearchType();
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

		// Configure functionality of each filter control
		mHotelNameEditText.addTextChangedListener(mHotelNameTextWatcher);
		mRadiusButtonGroup.setOnCheckedChangeListener(mRadiusCheckedChangeListener);
		mRatingButtonGroup.setOnCheckedChangeListener(mStarRatingCheckedChangeListener);
		mPriceButtonGroup.setOnCheckedChangeListener(mPriceCheckedChangeListener);

		// Add an "okay" button, even though it does nothing beside dismiss the dialog
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Hide the IME
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mHotelNameEditText.getWindowToken(), 0);
			}
		});

		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);

		return dialog;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		onFilterClosed();
		super.onDismiss(dialog);
	}

	public CharSequence getTitle() {
		Property[] properties = Db.getSearchResponse().getFilteredAndSortedProperties();
		int count = properties == null ? 0 : properties.length;
		return Html.fromHtml(getResources().getQuantityString(R.plurals.number_of_matching_hotels, count, count));
	}

	//////////////////////////////////////////////////////////////////////////
	// Listeners for filter form changes

	private final TextWatcher mHotelNameTextWatcher = new TextWatcher() {
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			Filter filter = Db.getSearchResponse().getFilter();
			filter.setHotelName(s.toString());
			filter.notifyFilterChanged();
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// Do nothing
		}

		public void afterTextChanged(Editable s) {
			// Do nothing
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

			Filter filter = Db.getSearchResponse().getFilter();
			filter.setSearchRadius(searchRadius);
			filter.notifyFilterChanged();

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

			Filter filter = Db.getSearchResponse().getFilter();
			filter.setMinimumStarRating(minStarRating);
			filter.notifyFilterChanged();

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

			Filter filter = Db.getSearchResponse().getFilter();
			filter.setPriceRange(priceRange);
			filter.notifyFilterChanged();

			onPriceFilterChanged();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	private void onFilterClosed() {
		Log.d("Tracking \"App.Hotels.Search.Refine.Name\" change...");
		String pageName = "App.Hotels.Search.Refine.Name." + mHotelNameEditText.getText().toString();
		TrackingUtils.trackSimpleEvent(getActivity(), pageName, null, "Shopper", null);
	}

	private void onPriceFilterChanged() {
		Log.d("Tracking \"App.Hotels.Search.Refine.PriceRange\" change...");

		String refinement;

		switch (mPriceButtonGroup.getCheckedRadioButtonId()) {
		case R.id.price_cheap_button: {
			refinement = "App.Hotels.Search.Refine.PriceRange.1$";
			break;
		}
		case R.id.price_moderate_button: {
			refinement = "App.Hotels.Search.Refine.PriceRange.2$";
			break;
		}
		case R.id.price_expensive_button: {
			refinement = "App.Hotels.Search.Refine.PriceRange.3$";
			break;
		}
		case R.id.price_all_button:
		default: {
			refinement = "App.Hotels.Search.Refine.PriceRange.All";
			break;
		}
		}

		TrackingUtils.trackSimpleEvent(getActivity(), refinement, null, "Shopper", null);
	}

	private void onRadiusFilterChanged() {
		Log.d("Tracking \"App.Hotels.Search.Refine.SearchRadius\" rating change...");

		String refinement = "App.Hotels.Search.Refine.SearchRadius";
		SearchRadius searchRadius;

		switch (mRadiusButtonGroup.getCheckedRadioButtonId()) {
		case R.id.radius_small_button: {
			searchRadius = Filter.SearchRadius.SMALL;
			break;
		}
		case R.id.radius_medium_button: {
			searchRadius = Filter.SearchRadius.MEDIUM;
			break;
		}
		case R.id.radius_large_button: {
			searchRadius = Filter.SearchRadius.LARGE;
			break;
		}
		case R.id.radius_all_button:
		default: {
			searchRadius = Filter.SearchRadius.ALL;
			break;
		}
		}

		if (searchRadius != Filter.SearchRadius.ALL) {
			final DistanceUnit distanceUnit = DistanceUnit.getDefaultDistanceUnit();
			final String unitString = distanceUnit.equals(DistanceUnit.MILES) ? "mi" : "km";

			refinement += "." + new DecimalFormat("##.#").format(searchRadius.getRadius(distanceUnit)) + unitString;
		}
		else {
			refinement += ".All";
		}

		TrackingUtils.trackSimpleEvent(getActivity(), refinement, null, "Shopper", null);
	}

	private void onRatingFilterChanged() {
		Log.d("Tracking \"App.Hotels.Search.Refine\" rating change...");

		String refinement;

		switch (mRatingButtonGroup.getCheckedRadioButtonId()) {
		case R.id.rating_low_button: {
			refinement = "App.Hotels.Search.Refine.3Stars";
			break;
		}
		case R.id.rating_medium_button: {
			refinement = "App.Hotels.Search.Refine.4Stars";
			break;
		}
		case R.id.rating_high_button: {
			refinement = "App.Hotels.Search.Refine.5Stars";
			break;
		}
		case R.id.rating_all_button:
		default: {
			refinement = "App.Hotels.Search.Refine.AllStars";
			break;
		}
		}

		TrackingUtils.trackSimpleEvent(getActivity(), refinement, null, "Shopper", null);
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment control

	public void notifyFilterChanged() {
		getDialog().setTitle(getTitle());
	}
}
