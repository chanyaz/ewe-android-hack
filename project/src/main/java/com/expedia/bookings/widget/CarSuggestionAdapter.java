package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.services.SuggestionServices;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class CarSuggestionAdapter extends SuggestionBaseAdapter {

	public CarSuggestionAdapter(Context context) {
		super(context);
	}

	protected Subscription getNearbySuggestions(String locale, String latLong, int siteId, String clientId, Observer<List<SuggestionV4>> observer) {
		return suggestionServices.getNearbyCarSuggestions(locale, latLong, siteId, clientId, observer);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View itemView = super.getView(position, convertView, parent);
		if (itemView != null) {
			return itemView;
		}

		CarSuggestionViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.car_dropdown_item, parent, false);
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

		public void bind(SuggestionV4 suggestion) {
			updateLocationTitleAndSubTitle(suggestion);

			updateDropdownImage(suggestion);
		}

		private void updateLocationTitleAndSubTitle(SuggestionV4 suggestion) {
			if (SuggestionV4.CURRENT_LOCATION_ID.equalsIgnoreCase(suggestion.gaiaId)) {
				locationTitle.setText(locationTitle.getContext().getString(R.string.current_location));
				locationSubtitle.setVisibility(View.GONE);
			}
			else if (suggestion.isMajorAirport()) {
				locationTitle.setText(Html.fromHtml(StrUtils.formatCityName(suggestion.regionNames.displayName)));
				locationSubtitle.setText(StrUtils.formatAirportName(suggestion.regionNames.shortName));
			}
			else {
				locationTitle.setText(Html.fromHtml(StrUtils.formatCityName(suggestion.regionNames.displayName)));
				locationSubtitle.setText(suggestion.regionNames.shortName);
			}
		}

		private void updateDropdownImage(SuggestionV4 suggestion) {
			if (suggestion.iconType == SuggestionV4.IconType.HISTORY_ICON) {
				dropdownImage.setImageResource(R.drawable.recents);
			}
			else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
				dropdownImage.setImageResource(R.drawable.ic_suggest_current_location);
			}
			else {
				dropdownImage.setImageResource(
					suggestion.isMajorAirport() ? R.drawable.ic_suggest_airport : R.drawable.search_type_icon);
			}
			dropdownImage
				.setColorFilter(dropdownImage.getContext().getResources()
					.getColor(Ui.obtainThemeResID(dropdownImage.getContext(), R.attr.skin_carsSecondaryColor)));
		}
	}

	@Override
	protected Subscription suggest(SuggestionServices suggestionServices, Observer<List<SuggestionV4>> suggestionsObserver, CharSequence query, String clientId) {
		return suggestionServices.getCarSuggestions(query.toString(), PointOfSale.getSuggestLocaleIdentifier(), clientId, suggestionsObserver);
	}
}
