package com.expedia.bookings.fragment;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.services.GoogleServices;
import com.mobiata.android.services.GoogleServices.MapType;

public class QuickSearchFragment extends Fragment implements EventHandler {

	private static final int MAX_RECENT_SEARCHES = 3;

	public static QuickSearchFragment newInstance() {
		return new QuickSearchFragment();
	}

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
		View view = inflater.inflate(R.layout.fragment_quick_search, container, false);

		mRecentSearchesContainer = (ViewGroup) view.findViewById(R.id.recent_searches_container);
		mRecentSearchesLayout = (ViewGroup) view.findViewById(R.id.recent_searches_layout);
		mFeaturedDestinationsLayout = (ViewGroup) view.findViewById(R.id.featured_destinations_layout);

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
	// Adding quick searches

	public void addRecentSearch(final SearchParams searchParams) {
		mRecentSearchesContainer.setVisibility(View.VISIBLE);

		searchParams.ensureValidCheckInDate();

		String location = searchParams.getFreeformLocation();
		String thumbnailUrl = GoogleServices.getStaticMapUrl(300, 300, 12, MapType.ROADMAP, location);

		View destination = addDestination(thumbnailUrl, location);

		TextView paramsTextView = (TextView) destination.findViewById(R.id.params_text_view);
		String dateRange = DateUtils.formatDateRange(getActivity(), searchParams.getCheckInDate().getTimeInMillis(),
				searchParams.getCheckOutDate().getTimeInMillis() + 60000, DateUtils.FORMAT_NUMERIC_DATE);
		String guests = StrUtils.formatGuests(getActivity(), searchParams);
		paramsTextView.setText(dateRange + ", " + guests);

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
		// Do nothing for now
	}
}
