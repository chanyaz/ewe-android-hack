package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

//TODO - Should be moved out of Cars
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.services.SuggestionServices;
import com.expedia.bookings.utils.Strings;

import rx.Observer;
import rx.Subscription;
import rx.exceptions.OnErrorNotImplementedException;

public abstract class SuggestionBaseAdapter extends BaseAdapter implements Filterable {

	@Inject
	SuggestionServices suggestionServices;

	private List<Suggestion> suggestions = new ArrayList<>();

	//Abstract Methods to be implemented by child classes
	protected abstract Subscription invokeSuggestionService(CharSequence query, SuggestionServices suggestionServices,
		Observer<List<Suggestion>> suggestionsObserver);

	@Override
	public int getCount() {
		return suggestions.size();
	}

	public void addAll(List<Suggestion> list) {
		suggestions.addAll(list);
		filter.publishResults("", null);
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

	private SuggestFilter filter = new SuggestFilter();

	private Subscription suggestSubscription;
	private class SuggestFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence s) {
			FilterResults oReturn = new FilterResults();
			if (Strings.isNotEmpty(s) && s.length() >= 3) {
				cleanup();
				suggestSubscription = invokeSuggestionService(s, suggestionServices, suggestionsObserver);
			}
			oReturn.count = suggestions.size();
			oReturn.values = suggestions;
			return oReturn;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			notifyDataSetChanged();
		}
	}

	private Observer<List<Suggestion>> suggestionsObserver = new Observer<List<Suggestion>>() {
		@Override
		public void onCompleted() {
			filter.publishResults("", null);
		}

		@Override
		public void onError(Throwable e) {
			throw new OnErrorNotImplementedException(e);
		}

		@Override
		public void onNext(List<Suggestion> suggestions) {
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
