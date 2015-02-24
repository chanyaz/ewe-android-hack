package com.expedia.bookings.widget;


import java.util.List;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.services.SuggestionServices;
import com.expedia.bookings.utils.StrUtils;


public class CarSuggestionAdapter extends SuggestionBaseAdapter {

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Suggestion suggestion = getItem(position);
		CarSuggestionViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cars_dropdown_item, parent, false);
			CarSuggestionViewHolder carSuggestionViewHolder = new CarSuggestionViewHolder(convertView);
			convertView.setTag(carSuggestionViewHolder);
		}

		viewHolder = (CarSuggestionViewHolder) convertView.getTag();

		viewHolder.airportName.setText(StrUtils.formatAirportName(suggestion.shortName));
		String name = StrUtils.formatCityName(suggestion.displayName);
		viewHolder.displayName.setText(Html.fromHtml(name));
		viewHolder.dropdownImage
			.setImageResource(suggestion.isHistory ? R.drawable.recents : R.drawable.ic_suggest_current_location);
		viewHolder.dropdownImage.setColorFilter(parent.getResources().getColor(R.color.cars_secondary_color));
		return convertView;
	}

	public static class CarSuggestionViewHolder {
		@InjectView(R.id.display_name_textView)
		TextView displayName;

		@InjectView(R.id.airport_name_textView)
		TextView airportName;

		@InjectView(R.id.cars_dropdown_imageView)
		ImageView dropdownImage;

		public CarSuggestionViewHolder(View root) {
			ButterKnife.inject(this, root);
		}
	}

	@Override
	protected Subscription invokeSuggestionService(CharSequence query, SuggestionServices suggestionServices,
		Observer<List<Suggestion>> suggestionsObserver) {
		return suggestionServices.getAirportSuggestions(query.toString(), suggestionsObserver);
	}
}
