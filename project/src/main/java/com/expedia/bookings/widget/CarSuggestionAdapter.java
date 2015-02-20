package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.services.SuggestionServices;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.Log;
import rx.Observer;

public class CarSuggestionAdapter extends ArrayAdapter<Suggestion> implements Filterable {

	@Inject
	SuggestionServices suggestionServices;

	private List<Suggestion> suggestions = new ArrayList<>();
	private SuggestFilter filter = new SuggestFilter();

	public CarSuggestionAdapter(Context context, int resource) {
		super(context, resource);
	}

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
	public View getView(int position, View convertView, ViewGroup parent) {
		Suggestion suggestion = getItem(position);
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cars_dropdown_item, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.airportName.setText(StrUtils.formatAirportName(suggestion.shortName));
		String name = StrUtils.formatCityName(suggestion.displayName);
		holder.displayName.setText(Html.fromHtml(name));
		holder.dropdownImage
			.setImageResource(suggestion.isHistory ? R.drawable.recents : R.drawable.ic_suggest_current_location);
		holder.dropdownImage.setColorFilter(parent.getResources().getColor(R.color.cars_secondary_color));
		return convertView;
	}

	public static class ViewHolder {
		@InjectView(R.id.display_name_textView)
		TextView displayName;

		@InjectView(R.id.airport_name_textView)
		TextView airportName;

		@InjectView(R.id.cars_dropdown_imageView)
		ImageView dropdownImage;

		public ViewHolder(View root) {
			ButterKnife.inject(this, root);
		}
	}

	@Override
	public Filter getFilter() {
		return filter;
	}

	private class SuggestFilter extends Filter {
		public SuggestFilter() {
			// ignore
		}

		@Override
		protected FilterResults performFiltering(CharSequence s) {
			FilterResults oReturn = new FilterResults();
			if (s != null && s.length() >= 3) {
				suggestionServices.getAirportSuggestions(s.toString(), suggestionsObserver);
			}
			oReturn.count = suggestions.size();
			oReturn.values = suggestions;
			return oReturn;
		}

		@SuppressWarnings("unchecked")
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
			Log.d("ERROR", e);
		}

		@Override
		public void onNext(List<Suggestion> suggestions) {
			CarSuggestionAdapter.this.suggestions = suggestions;
		}
	};
}
