package com.expedia.bookings.widget;

import java.util.List;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.services.SuggestionServices;
import com.expedia.bookings.utils.StrUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class HotelSuggestionAdapter extends SuggestionBaseAdapter {

	protected Subscription getNearbySuggestions(String locale, String latLong, int siteId, Observer<List<Suggestion>> observer) {
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SuggestionViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_dropdown_item, parent, false);
			convertView.setTag(new SuggestionViewHolder(convertView));
		}

		holder = (SuggestionViewHolder) convertView.getTag();
		holder.bind(getItem(position));

		return convertView;
	}

	public static class SuggestionViewHolder {
		@InjectView(R.id.display_name_textView)
		TextView displayName;

		@InjectView(R.id.cars_dropdown_imageView)
		ImageView dropdownImage;

		public SuggestionViewHolder(View root) {
			ButterKnife.inject(this, root);
		}

		public void bind(Suggestion suggestion) {

			displayName.setText(Html.fromHtml(StrUtils.formatCityName(suggestion.displayName)));

			if (suggestion.iconType == Suggestion.IconType.HISTORY_ICON) {
				dropdownImage.setImageResource(R.drawable.recents);
			}
			else if (suggestion.iconType == Suggestion.IconType.CURRENT_LOCATION_ICON) {
				dropdownImage.setImageResource(R.drawable.ic_suggest_current_location);
			}
			else if (suggestion.type.equals("HOTEL")) {
				dropdownImage.setImageResource(R.drawable.hotel_suggest);
			}
			else if (suggestion.type.equals("AIRPORT")) {
				dropdownImage.setImageResource(R.drawable.airport_suggest);
			}
			else {
				dropdownImage.setImageResource(R.drawable.search_type_icon);
			}

			dropdownImage
				.setColorFilter(dropdownImage.getContext().getResources().getColor(R.color.hotels_primary_color));
		}
	}

	@Override
	protected Subscription suggest(SuggestionServices suggestionServices, Observer<List<Suggestion>> suggestionsObserver, CharSequence query) {
		return suggestionServices.getHotelSuggestions(query.toString(), suggestionsObserver);
	}
}
