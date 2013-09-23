package com.expedia.bookings.fragment;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelFilter.PriceRange;
import com.expedia.bookings.data.HotelFilter.SearchRadius;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.widget.SegmentedControlGroup;

public class FilterDialogFragment extends DialogFragment {

	private EditText mHotelNameEditText;;
	private SegmentedControlGroup mRadiusButtonGroup;
	private SegmentedControlGroup mRatingButtonGroup;
	private SegmentedControlGroup mPriceButtonGroup;
	private View mVipAccessButton;

	public static FilterDialogFragment newInstance() {
		return new FilterDialogFragment();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View view = inflater.inflate(R.layout.fragment_dialog_filter, null);
		builder.setView(view);

		mHotelNameEditText = (EditText) view.findViewById(R.id.filter_hotel_name_edit_text);
		mRadiusButtonGroup = (SegmentedControlGroup) view.findViewById(R.id.radius_filter_button_group);
		mRatingButtonGroup = (SegmentedControlGroup) view.findViewById(R.id.rating_filter_button_group);
		mPriceButtonGroup = (SegmentedControlGroup) view.findViewById(R.id.price_filter_button_group);
		mVipAccessButton = Ui.findView(view, R.id.filter_vip_access);
		if (PointOfSale.getPointOfSale().supportsVipAccess()) {
			mVipAccessButton.setVisibility(View.VISIBLE);
		}

		// Configure labels
		LayoutUtils.configureRadiusFilterLabels(getActivity(), mRadiusButtonGroup, Db.getFilter());

		// Need to set title in constructor or it will never show up
		builder.setTitle(getTitle());

		// Configure initial settings (based on the filter)
		HotelFilter filter = Db.getFilter();
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
		SearchType searchType = Db.getHotelSearch().getSearchParams().getSearchType();
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

		mVipAccessButton.setSelected(filter.isVipAccessOnly());

		// Configure functionality of each filter control
		mHotelNameEditText.addTextChangedListener(mHotelNameTextWatcher);
		mRadiusButtonGroup.setOnCheckedChangeListener(mRadiusCheckedChangeListener);
		mRatingButtonGroup.setOnCheckedChangeListener(mStarRatingCheckedChangeListener);
		mPriceButtonGroup.setOnCheckedChangeListener(mPriceCheckedChangeListener);
		mVipAccessButton.setOnClickListener(mVipAccessClickListener);

		// Add an "okay" button, even though it does nothing beside dismiss the dialog
		builder.setPositiveButton(android.R.string.ok, null);

		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);

		return dialog;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (isAdded()) {
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

			onFilterClosed();
		}

		super.onDismiss(dialog);
	}

	public CharSequence getTitle() {
		int count = Db.getHotelSearch().getSearchResponse().getFilteredPropertiesCount();
		return Html.fromHtml(getResources().getQuantityString(R.plurals.number_of_matching_hotels, count, count));
	}

	//////////////////////////////////////////////////////////////////////////
	// Listeners for filter form changes

	private final TextWatcher mHotelNameTextWatcher = new TextWatcher() {
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			HotelFilter filter = Db.getFilter();
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

			HotelFilter filter = Db.getFilter();
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

			HotelFilter filter = Db.getFilter();
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

			HotelFilter filter = Db.getFilter();
			filter.setPriceRange(priceRange);
			filter.notifyFilterChanged();

			onPriceFilterChanged();
		}
	};

	private final View.OnClickListener mVipAccessClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			boolean vipAccessOnly = !mVipAccessButton.isSelected();
			mVipAccessButton.setSelected(vipAccessOnly);

			HotelFilter filter = Db.getFilter();
			filter.setVipAccessOnly(vipAccessOnly);
			filter.notifyFilterChanged();

			OmnitureTracking.trackLinkHotelRefineVip(getActivity(), vipAccessOnly);
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	private void onFilterClosed() {
		Log.d("Tracking \"App.Hotels.Search.Refine.Name\" change...");
		OmnitureTracking.trackLinkHotelRefineName(getActivity(), mHotelNameEditText.getText().toString());
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

	//////////////////////////////////////////////////////////////////////////
	// Fragment control

	public void notifyFilterChanged() {
		getDialog().setTitle(getTitle());
	}
}
