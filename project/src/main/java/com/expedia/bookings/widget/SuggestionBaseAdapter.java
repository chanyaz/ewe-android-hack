package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;

import android.content.Context;
import android.text.format.DateUtils;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.services.SuggestionServices;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.LocationServices;

import rx.Observer;
import rx.Subscription;

public abstract class SuggestionBaseAdapter extends BaseAdapter implements Filterable {

	// Implementing class decides how to use the suggestion service to provide suggestions
	protected abstract Subscription suggest(SuggestionServices suggestionServices,
		Observer<List<Suggestion>> suggestionsObserver, CharSequence query);
	protected abstract Subscription getNearbySuggestions(String locale, String latLong, int siteId, Observer<List<Suggestion>> observer);

	private static final long MINIMUM_TIME_AGO = DateUtils.HOUR_IN_MILLIS;
	private boolean showRecentSearch = true;
	private boolean showNearby = false;

	@Inject
	SuggestionServices suggestionServices;

	private List<Suggestion> recentHistory = new ArrayList<>();
	private List<Suggestion> nearbySuggestions = new ArrayList<>();
	private List<Suggestion> suggestions = new ArrayList<>();
	private Subscription suggestSubscription;
	private final SuggestFilter filter = new SuggestFilter();

	@Override
	public int getCount() {
		return suggestions.size();
	}

	public void addNearbyAndRecents(List<Suggestion> list, Context ctx) {
		recentHistory.addAll(list);

		long minTime = DateTime.now().getMillis() - MINIMUM_TIME_AGO;
		android.location.Location loc = LocationServices.getLastBestLocation(ctx, minTime);

		// just show the recent history items when there's no current loc
		if (loc != null) {
			showNearby = true;
			String latlong = loc.getLatitude() + "|" + loc.getLongitude();

			getNearbySuggestions(PointOfSale.getSuggestLocaleIdentifier(), latlong, PointOfSale.getPointOfSale().getSiteId(), suggestionsObserver);
		}
		else {
			suggestions.addAll(recentHistory);
			filter.publishResults("", null);
		}
	}

	public void updateRecentHistory(List<Suggestion> list) {
		suggestions.removeAll(recentHistory);
		recentHistory.clear();
		recentHistory.addAll(list);
		suggestions.addAll(recentHistory);
		filter.publishResults("", null);
	}

	@Override
	public Suggestion getItem(int position) {
		if (suggestions == null || suggestions.size() == 0) {
			return null;
		}
		return suggestions.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public Filter getFilter() {
		return filter;
	}

	private class SuggestFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence query) {
			FilterResults results = new FilterResults();
			if (Strings.isNotEmpty(query) && query.length() >= 3) {
				cleanup();
				suggestSubscription = suggest(suggestionServices, suggestionsObserver, query);
				showRecentSearch = false;
				showNearby = false;
			}
			else {
				// Default to show nearby and recent history
				suggestions.clear();
				suggestions.addAll(nearbySuggestions);
				suggestions.addAll(recentHistory);
			}

			results.count = suggestions.size();
			results.values = suggestions;
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			notifyDataSetChanged();
		}
	}

	private final Observer<List<Suggestion>> suggestionsObserver = new Observer<List<Suggestion>>() {
		@Override
		public void onCompleted() {
			filter.publishResults("", null);
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
		}

		@Override
		public void onNext(List<Suggestion> suggests) {
			// Cache nearby
			if (showNearby) {
				nearbySuggestions.addAll(suggests);
			}

			suggestions.clear();
			suggestions.addAll(suggests);
			if (showRecentSearch) {
				suggestions.addAll(recentHistory);
			}
		}
	};

	public void cleanup() {
		if (suggestSubscription != null) {
			suggestSubscription.unsubscribe();
			suggestSubscription = null;
		}
	}
}
