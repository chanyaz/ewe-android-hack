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

public class LxSuggestionAdapter extends SuggestionBaseAdapter {

	public LxSuggestionAdapter(Context context) {
		super(context);
	}

	protected Subscription getNearbySuggestions(String locale, String latLong, int siteId, String clientId, Observer<List<SuggestionV4>> observer) {
		return suggestionServices.getNearbyLxSuggestions(locale, latLong, siteId, clientId, observer);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View itemView = super.getView(position, convertView, parent);
		if (itemView != null) {
			return itemView;
		}

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

		public void bind(SuggestionV4 suggestion) {
			cityName.setText(StrUtils.formatAirportName(suggestion.regionNames.shortName));
			displayName.setText(Html.fromHtml(StrUtils.formatCityName(suggestion.regionNames.displayName)));
			if (suggestion.iconType == SuggestionV4.IconType.HISTORY_ICON) {
				dropdownImage.setImageResource(R.drawable.recents);
			}
			else if (SuggestionV4.CURRENT_LOCATION_ID.equalsIgnoreCase(suggestion.gaiaId)) {
				dropdownImage.setImageResource(R.drawable.ic_suggest_current_location);
				displayName.setText(displayName.getContext().getString(R.string.current_location));
				cityName.setVisibility(View.GONE);
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
	protected Subscription suggest(SuggestionServices service, Observer<List<SuggestionV4>> observer, CharSequence query, String clientId) {
		return service.getLxSuggestions(query.toString(), PointOfSale.getSuggestLocaleIdentifier(), clientId, observer);
	}
}
