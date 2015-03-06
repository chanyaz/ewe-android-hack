package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.Log;

import rx.Observer;

public class CarSuggestionAdapter extends ArrayAdapter<Suggestion> implements Filterable {

	private List<Suggestion> mList;
	private LayoutInflater mInflater;


	public CarSuggestionAdapter(Context context, int resource) {
		super(context, resource);
		mInflater = LayoutInflater.from(context);
		mList = new ArrayList<>();
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	public void addAll(List<Suggestion> list) {
		mList.addAll(list);
		mFilter.publishResults("", null);
	}

	@Override
	public Suggestion getItem(int position) {
		return mList.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Suggestion suggestion = getItem(position);
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.cars_dropdown_item, parent, false);
			holder = new ViewHolder();
			holder.displayName = (TextView) convertView.findViewById(R.id.display_name_textView);
			holder.airportName = (TextView) convertView.findViewById(R.id.airport_name_textView);
			holder.dropdownImage = (ImageView) convertView.findViewById(R.id.cars_dropdown_imageView);
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
		TextView displayName;
		TextView airportName;
		ImageView dropdownImage;
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}

	private SuggestFilter mFilter = new SuggestFilter();


	private class SuggestFilter extends Filter {

		public SuggestFilter() {

		}

		@Override
		protected FilterResults performFiltering(CharSequence s) {
			FilterResults oReturn = new FilterResults();
			if (s != null && s.length() >= 3) {
				CarDb.getSuggestionServices()
					.getAirportSuggestions(s.toString(), mSuggestionsRequestObs);
			}
			oReturn.count = mList.size();
			oReturn.values = mList;
			return oReturn;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			notifyDataSetChanged();
		}
	}

	Observer<List<Suggestion>> mSuggestionsRequestObs = new Observer<List<Suggestion>>() {
		List<Suggestion> list;

		@Override
		public void onCompleted() {
			mList = list;
			mFilter.publishResults("", null);
		}

		@Override
		public void onError(Throwable e) {
			Log.d("ERROR", e);
		}

		@Override
		public void onNext(List<Suggestion> suggestions) {
			list = suggestions;
		}
	};
}
