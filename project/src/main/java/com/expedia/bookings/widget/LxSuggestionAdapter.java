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

public class LxSuggestionAdapter extends SuggestionBaseAdapter {

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Suggestion suggestion = getItem(position);
		LxSuggestionViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.lx_dropdown_item, parent, false);
			LxSuggestionViewHolder suggestionViewHolder = new LxSuggestionViewHolder(convertView);
			convertView.setTag(suggestionViewHolder);
		}

		viewHolder = (LxSuggestionViewHolder) convertView.getTag();

		String name = StrUtils.formatCityName(suggestion.displayName);
		viewHolder.displayName.setText(Html.fromHtml(name));
		viewHolder.dropdownImage.setImageResource(
			suggestion.isHistory ? R.drawable.recents : R.drawable.ic_suggest_current_location);
		return convertView;
	}

	public static class LxSuggestionViewHolder {
		@InjectView(R.id.display_name_textView)
		TextView displayName;

		@InjectView(R.id.lx_dropdown_imageView)
		ImageView dropdownImage;

		public LxSuggestionViewHolder(View root) {
			ButterKnife.inject(this, root);
		}
	}

	@Override
	protected Subscription invokeSuggestionService(CharSequence query, SuggestionServices suggestionServices,
		Observer<List<Suggestion>> suggestionsObserver) {
		return suggestionServices.getLxSuggestions(query.toString(), suggestionsObserver);
	}
}
