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
import com.expedia.bookings.services.SuggestionServices;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.LocationServices;

import rx.Observer;
import rx.Subscription;
import rx.exceptions.OnErrorNotImplementedException;

public abstract class SuggestionBaseAdapter extends BaseAdapter implements Filterable {

	// Implementing class decides how to use the suggestion service to provide suggestions
	protected abstract Subscription suggest(SuggestionServices suggestionServices,
			Observer<List<Suggestion>> suggestionsObserver, CharSequence query);

	private static final long MINIMUM_TIME_AGO = DateUtils.HOUR_IN_MILLIS;
	List<Suggestion> recentHistory = new ArrayList<Suggestion>();
	List<Suggestion> recentSuggestions = new ArrayList<Suggestion>();
	private boolean showRecentSearch = true;

	@Inject
	SuggestionServices suggestionServices;

	private List<Suggestion> suggestions = new ArrayList<>();
	private Subscription suggestSubscription;
	private final SuggestFilter filter = new SuggestFilter();

	@Override
	public int getCount() {
		return suggestions.size();
	}

	public void addAll(List<Suggestion> list, Context ctx) {
		recentHistory = list;
		getNearbyAirport(ctx);
	}

	public void getNearbyAirport(Context ctx) {
		long minTime = DateTime.now().getMillis() - MINIMUM_TIME_AGO;
		android.location.Location loc = LocationServices.getLastBestLocation(ctx, minTime);

		// just show the recent history items when there's no current loc
		if (loc != null) {
			String query = loc.getLatitude() + "|" + loc.getLongitude();
			suggestionServices.getNearbyAirportSuggestions(query, suggestionsObserver);
		}
		else {
			suggestions.addAll(recentHistory);
			filter.publishResults("", null);
		}
	}

	@Override
	public Suggestion getItem(int position) {
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
			}
			else {
				suggestions = recentSuggestions;
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
			throw new OnErrorNotImplementedException(e);
		}

		@Override
		public void onNext(List<Suggestion> suggestions) {
			if (showRecentSearch) {
				suggestions.addAll(recentHistory);
				recentSuggestions = suggestions;
			}
			SuggestionBaseAdapter.this.suggestions = suggestions;
		}
	};

	public void cleanup() {
		if (suggestSubscription != null) {
			suggestSubscription.unsubscribe();
			suggestSubscription = null;
		}
	}
}
