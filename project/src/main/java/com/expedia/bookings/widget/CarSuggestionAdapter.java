package com.expedia.bookings.widget;

import java.util.List;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.services.SuggestionServices;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class CarSuggestionAdapter extends SuggestionBaseAdapter {

	protected Subscription getNearbySuggestions(String locale, String latLong, int siteId, Observer<List<Suggestion>> observer) {
		return suggestionServices.getNearbyCarSuggestions(locale, latLong, siteId, observer);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CarSuggestionViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.car_dropdown_item, parent, false);
			convertView.setTag(new CarSuggestionViewHolder(convertView));
		}

		holder = (CarSuggestionViewHolder) convertView.getTag();
		holder.bind(getItem(position));

		return convertView;
	}

	public static class CarSuggestionViewHolder {
		@InjectView(R.id.location_title_textView)
		TextView locationTitle;

		@InjectView(R.id.location_subtitle_textView)
		TextView locationSubtitle;

		@InjectView(R.id.cars_dropdown_imageView)
		ImageView dropdownImage;

		public CarSuggestionViewHolder(View root) {
			ButterKnife.inject(this, root);
		}

		public void bind(Suggestion suggestion) {
			if (suggestion.isMajorAirport()) {
				locationTitle.setText(Html.fromHtml(StrUtils.formatCityName(suggestion.displayName)));
				locationSubtitle.setText(StrUtils.formatAirportName(suggestion.shortName));
			}
			else {
				locationTitle.setText(Html.fromHtml(StrUtils.formatCityName(suggestion.displayName)));
				locationSubtitle.setText(suggestion.shortName);
			}

			if (suggestion.iconType == Suggestion.IconType.HISTORY_ICON) {
				dropdownImage.setImageResource(R.drawable.recents);
			}
			else if (suggestion.iconType == Suggestion.IconType.CURRENT_LOCATION_ICON) {
				dropdownImage.setImageResource(R.drawable.ic_suggest_current_location);
			}
			else {
				dropdownImage.setImageResource(suggestion.isMajorAirport() ? R.drawable.ic_suggest_airport : R.drawable.search_type_icon);
			}
			dropdownImage
				.setColorFilter(dropdownImage.getContext().getResources()
					.getColor(Ui.obtainThemeResID(dropdownImage.getContext(), R.attr.skin_carsSecondaryColor)));
		}
	}

	@Override
	protected Subscription suggest(SuggestionServices suggestionServices, Observer<List<Suggestion>> suggestionsObserver, CharSequence query) {
		return suggestionServices.getCarSuggestions(query.toString(), PointOfSale.getSuggestLocaleIdentifier(), suggestionsObserver);
	}
}
