package com.expedia.bookings.fragment;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.services.GoogleServices;
import com.mobiata.android.services.GoogleServices.MapType;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.CalendarDatePicker.OnDateChangedListener;

public class SearchFragment extends Fragment implements EventHandler {

	private static final int MAX_RECENT_SEARCHES = 3;

	public static SearchFragment newInstance() {
		return new SearchFragment();
	}

	private EditText mLocationEditText;
	private CalendarDatePicker mCalendarDatePicker;
	private NumberPicker mAdultsNumberPicker;
	private NumberPicker mChildrenNumberPicker;
	private ViewGroup mRecentSearchesContainer;
	private ViewGroup mRecentSearchesLayout;
	private ViewGroup mFeaturedDestinationsLayout;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((TabletActivity) getActivity()).registerEventHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search, container, false);

		mLocationEditText = (EditText) view.findViewById(R.id.location_edit_text);
		mCalendarDatePicker = (CalendarDatePicker) view.findViewById(R.id.dates_date_picker);
		mAdultsNumberPicker = (NumberPicker) view.findViewById(R.id.adults_number_picker);
		mChildrenNumberPicker = (NumberPicker) view.findViewById(R.id.children_number_picker);
		mRecentSearchesContainer = (ViewGroup) view.findViewById(R.id.recent_searches_container);
		mRecentSearchesLayout = (ViewGroup) view.findViewById(R.id.recent_searches_layout);
		mFeaturedDestinationsLayout = (ViewGroup) view.findViewById(R.id.featured_destinations_layout);

		// Need to set temporary max values for number pickers, or updateViews() won't work (since a picker value
		// must be within its valid range to be set)
		mAdultsNumberPicker.setMaxValue(100);
		mChildrenNumberPicker.setMaxValue(100);

		updateViews();

		// Configure the location EditText
		mLocationEditText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!isHidden()) {
					String location = s.toString().trim();
					if (location.length() == 0) {
						((TabletActivity) getActivity()).setMyLocationSearch();
					}
					else {
						((TabletActivity) getActivity()).setFreeformLocation(mLocationEditText.getText().toString());
					}
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// Do nothing
			}

			public void afterTextChanged(Editable s) {
				// Do nothing
			}
		});

		// Configure the calendar
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker);
		mCalendarDatePicker.setOnDateChangedListener(new OnDateChangedListener() {
			public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
				if (!isHidden()) {
					Calendar checkIn = new GregorianCalendar(mCalendarDatePicker.getStartYear(), mCalendarDatePicker
							.getStartMonth(), mCalendarDatePicker.getStartDayOfMonth());
					Calendar checkOut = new GregorianCalendar(mCalendarDatePicker.getEndYear(), mCalendarDatePicker
							.getEndMonth(), mCalendarDatePicker.getEndDayOfMonth());

					((TabletActivity) getActivity()).setDates(checkIn, checkOut);
				}
			}
		});

		// Configure the number pickers
		GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);
		OnValueChangeListener valueChangeListener = new OnValueChangeListener() {
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				if (!isHidden()) {
					((TabletActivity) getActivity()).setGuests(mAdultsNumberPicker.getValue(),
							mChildrenNumberPicker.getValue());
				}

				GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);
			}
		};
		mAdultsNumberPicker.setOnValueChangedListener(valueChangeListener);
		mChildrenNumberPicker.setOnValueChangedListener(valueChangeListener);

		// Block NumberPickers from being editable
		mAdultsNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		mChildrenNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		// Configure the search button
		Button button = (Button) view.findViewById(R.id.search_button);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((TabletActivity) getActivity()).startSearch();
			}
		});

		// Get recent searches
		List<SearchParams> searches = Search.getRecentSearches(getActivity(), MAX_RECENT_SEARCHES);
		for (SearchParams params : searches) {
			addRecentSearch(params);
		}

		// Add some preset featured destinations
		addFeaturedDestination("http://www.destination360.com/north-america/us/massachusetts/images/s/boston.jpg",
				"Boston");
		addFeaturedDestination("http://sanfranciscoforyou.com/wp-content/uploads/2010/03/sf19.jpg", "San Francisco");
		addFeaturedDestination("http://www.traveladventures.org/continents/northamerica/images/minneapolis1.jpg",
				"Minneapolis");
		addFeaturedDestination(
				"http://wwp.greenwichmeantime.com/time-zone/usa/new-york/new-york-city/images/new-york-city.jpg",
				"New York");

		return view;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		((TabletActivity) getActivity()).unregisterEventHandler(this);
	}

	//////////////////////////////////////////////////////////////////////////
	// Views

	public void updateViews() {
		SearchParams params = ((TabletActivity) getActivity()).getSearchParams();

		mLocationEditText.setText(params.getSearchDisplayText(getActivity()));

		Calendar start = params.getCheckInDate();
		mCalendarDatePicker.updateStartDate(start.get(Calendar.YEAR), start.get(Calendar.MONTH),
				start.get(Calendar.DAY_OF_MONTH));
		Calendar end = params.getCheckOutDate();
		mCalendarDatePicker.updateEndDate(end.get(Calendar.YEAR), end.get(Calendar.MONTH),
				end.get(Calendar.DAY_OF_MONTH));

		mAdultsNumberPicker.setValue(params.getNumAdults());
		mChildrenNumberPicker.setValue(params.getNumChildren());
	}

	//////////////////////////////////////////////////////////////////////////
	// Recent searches/featured destinations

	public void addRecentSearch(final SearchParams searchParams) {
		mRecentSearchesContainer.setVisibility(View.VISIBLE);

		String location = searchParams.getFreeformLocation();
		String thumbnailUrl = GoogleServices.getStaticMapUrl(300, 300, 12, MapType.ROADMAP, location);

		View destination = addDestination(thumbnailUrl, location);
		destination.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				TabletActivity activity = (TabletActivity) getActivity();
				activity.setSearchParams(searchParams);
				activity.startSearch();
			}
		});

		mRecentSearchesLayout.addView(destination, mRecentSearchesLayout.getChildCount() - 1);
	}

	public void addFeaturedDestination(String thumbnailUrl, final String name) {
		View destination = addDestination(thumbnailUrl, name);
		destination.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				TabletActivity activity = (TabletActivity) getActivity();
				activity.setFreeformLocation(name);
				activity.startSearch();
			}
		});

		mFeaturedDestinationsLayout.addView(destination);
	}

	public View addDestination(String thumbnailUrl, String name) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View destination = inflater.inflate(R.layout.snippet_destination, null);

		ImageView thumbnail = (ImageView) destination.findViewById(R.id.thumbnail_image_view);
		ImageCache.loadImage(thumbnailUrl, thumbnail);

		TextView destinationTextView = (TextView) destination.findViewById(R.id.destination_text_view);
		destinationTextView.setText(name);

		return destination;
	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_SEARCH_PARAMS_CHANGED:
			if (isHidden()) {
				updateViews();
			}
			break;
		}
	}
}
