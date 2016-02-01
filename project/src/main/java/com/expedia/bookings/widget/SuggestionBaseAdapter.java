package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.joda.time.DateTime;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.services.SuggestionServices;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.SuggestionV4Utils;
import com.mobiata.android.LocationServices;

import rx.Observer;
import rx.Subscription;

public abstract class SuggestionBaseAdapter extends BaseAdapter implements Filterable {

	private static final int DEFAULT_AUTOFILL_ITEM_VIEW = 0;
	private static final int SUGGESTION_ITEM_VIEW = 1;
	private static final int ITEM_VIEW_TYPE_COUNT = 2;

	public static final String DEFAULT_AUTOFILL_ITEM_ID = "DEFAULT_AUTOFILL_ITEM";

	// Implementing class decides how to use the suggestion service to provide suggestions
	protected abstract Subscription suggest(SuggestionServices suggestionServices,
		Observer<List<SuggestionV4>> suggestionsObserver, CharSequence query, String clientId);
	protected abstract Subscription getNearbySuggestions(String locale, String latLong, int siteId, String clientId, Observer<List<SuggestionV4>> observer);

	private static final long MINIMUM_TIME_AGO = DateUtils.HOUR_IN_MILLIS;
	private boolean showRecentSearch = true;
	private boolean showNearby = false;

	private Context context;

	public SuggestionBaseAdapter(Context context) {
		this.context = context;
	}

	@Inject
	SuggestionServices suggestionServices;

	private List<SuggestionV4> recentHistory = new ArrayList<>();
	private List<SuggestionV4> nearbySuggestions = new ArrayList<>();
	private List<SuggestionV4> suggestions = new ArrayList<>();
	private Subscription suggestSubscription;
	private final SuggestFilter filter = new SuggestFilter();

	private void updateSuggestionsBackingList(List<SuggestionV4> suggestions) {
		this.suggestions = suggestions;
	}

	@Override
	public int getCount() {
		return suggestions.size();
	}

	@Override
	public int getViewTypeCount() {
		return ITEM_VIEW_TYPE_COUNT;
	}

	@Override
	public int getItemViewType (int position) {
		return position == 0 ? DEFAULT_AUTOFILL_ITEM_VIEW : SUGGESTION_ITEM_VIEW;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int itemViewType = getItemViewType(position);

		return itemViewType == DEFAULT_AUTOFILL_ITEM_VIEW ? getDefaultAutofillItemView(parent.getContext(), convertView) : null;
	}

	protected View getDefaultAutofillItemView(Context context, View convertView) {
		if (convertView == null) {
			convertView = new View(context);
			convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
		}

		return convertView;
	}

	public void addNearbyAndRecents(List<SuggestionV4> list, Context ctx) {
		recentHistory.addAll(list);

		long minTime = DateTime.now().getMillis() - MINIMUM_TIME_AGO;
		android.location.Location loc = LocationServices.getLastBestLocation(ctx, minTime);

		// just show the recent history items when there's no current loc
		if (loc != null) {
			showNearby = true;
			String latlong = loc.getLatitude() + "|" + loc.getLongitude();

			getNearbySuggestions(PointOfSale.getSuggestLocaleIdentifier(), latlong, PointOfSale.getPointOfSale().getSiteId(), ServicesUtil.generateClientId(ctx), suggestionsObserver);
		}
		else {
			suggestions.add(getDummySuggestionItem());
			suggestions.addAll(recentHistory);
			filter.publishResults("", null);
		}
	}

	public void updateRecentHistory(List<SuggestionV4> list) {
		suggestions.removeAll(recentHistory);
		recentHistory.clear();
		recentHistory.addAll(list);
		suggestions.addAll(recentHistory);
		filter.publishResults("", null);
	}

	@Override
	public SuggestionV4 getItem(int position) {
		if (suggestions == null || position >= suggestions.size()) {
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
			List<SuggestionV4> combinedSuggestionsList = new ArrayList<>();
			combinedSuggestionsList.add(getDummySuggestionItem());

			if (Strings.isNotEmpty(query) && query.length() >= SuggestionV4Utils.getMinSuggestQueryLength(context)) {
				cleanup();
				suggestSubscription = suggest(suggestionServices, suggestionsObserver, query, ServicesUtil.generateClientId(context));
				showRecentSearch = false;
				showNearby = false;
			}
			else {
				// Default to show nearby and recent history
				Set<SuggestionV4> suggestionsSet = new LinkedHashSet<SuggestionV4>();
				suggestionsSet.addAll(nearbySuggestions);
				suggestionsSet.addAll(recentHistory);
				combinedSuggestionsList.addAll(suggestionsSet);
			}

			results.count = combinedSuggestionsList.size();
			results.values = combinedSuggestionsList;
			updateSuggestionsBackingList(combinedSuggestionsList);
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			notifyDataSetChanged();
		}
	}

	private SuggestionV4 getDummySuggestionItem() {
		SuggestionV4 dummySuggestionForDefaultAutofill = new SuggestionV4();
		dummySuggestionForDefaultAutofill.gaiaId = DEFAULT_AUTOFILL_ITEM_ID;
		return dummySuggestionForDefaultAutofill;
	}

	private final Observer<List<SuggestionV4>> suggestionsObserver = new Observer<List<SuggestionV4>>() {
		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
		}

		@Override
		public void onNext(List<SuggestionV4> essSuggestions) {
			// Cache nearby
			if (showNearby) {
				nearbySuggestions.addAll(essSuggestions);
			}

			List<SuggestionV4> combinedSuggestionsList = new ArrayList<>();
			combinedSuggestionsList.add(getDummySuggestionItem());
			combinedSuggestionsList.addAll(essSuggestions);
			if (showRecentSearch) {
				combinedSuggestionsList.addAll(recentHistory);
			}

			updateSuggestionsBackingList(combinedSuggestionsList);
			filter.publishResults("", null);
		}
	};

	public void cleanup() {
		if (suggestSubscription != null) {
			suggestSubscription.unsubscribe();
			suggestSubscription = null;
		}
	}
}
