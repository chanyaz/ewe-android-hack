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

public class LxSuggestionAdapter extends SuggestionBaseAdapter {

	protected Subscription getNearbySuggestions(String locale, String latLong, int siteId, Observer<List<Suggestion>> observer) {
		return suggestionServices.getNearbyLxSuggestions(locale, latLong, siteId, observer);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LxSuggestionViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.lx_dropdown_item, parent, false);
			convertView.setTag(new LxSuggestionViewHolder(convertView));
		}

		holder = (LxSuggestionViewHolder) convertView.getTag();
		holder.bind(getItem(position));

		return convertView;
	}

	public static class LxSuggestionViewHolder {
		@InjectView(R.id.title_textview)
		TextView displayName;

		@InjectView(R.id.lx_dropdown_imageView)
		ImageView dropdownImage;

		@InjectView(R.id.city_name_textView)
		TextView cityName;

		public LxSuggestionViewHolder(View root) {
			ButterKnife.inject(this, root);
		}

		public void bind(Suggestion suggestion) {
			cityName.setText(StrUtils.formatAirportName(suggestion.shortName));
			displayName.setText(Html.fromHtml(StrUtils.formatCityName(suggestion.displayName)));
			if (suggestion.iconType == Suggestion.IconType.HISTORY_ICON) {
				dropdownImage.setImageResource(R.drawable.recents);
			}
			else if (suggestion.iconType == Suggestion.IconType.CURRENT_LOCATION_ICON) {
				dropdownImage.setImageResource(R.drawable.ic_suggest_current_location);
			}
			else {
				dropdownImage.setImageResource(R.drawable.search_type_icon);
			}
			dropdownImage
				.setColorFilter(dropdownImage.getContext().getResources().getColor(
					Ui.obtainThemeResID(dropdownImage.getContext(), R.attr.skin_lxPrimaryColor)));
		}
	}

	@Override
	protected Subscription suggest(SuggestionServices service, Observer<List<Suggestion>> observer, CharSequence query) {
		return service.getLxSuggestions(query.toString(), PointOfSale.getSuggestLocaleIdentifier(), observer);
	}
}
