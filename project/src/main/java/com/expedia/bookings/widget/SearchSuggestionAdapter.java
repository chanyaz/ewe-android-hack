package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.content.AutocompleteProvider;
import com.expedia.bookings.data.AutocompleteSuggestion;

public class SearchSuggestionAdapter extends ArrayAdapter<AutocompleteSuggestion> implements Filterable {
	private final LayoutInflater mInflater;
	private final String mCurrentLocationString;
	private final ArrayList<AutocompleteSuggestion> data = new ArrayList<>();

	public SearchSuggestionAdapter(Context context) {
		super(context, R.layout.row_suggestion);
		mInflater = LayoutInflater.from(context);
		mCurrentLocationString = context.getResources().getString(R.string.current_location);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public AutocompleteSuggestion getItem(int position) {
		return data.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AutocompleteSuggestion suggestionV2 = data.get(position);
		SuggestionViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_suggestion, parent, false);
			holder = new SuggestionViewHolder();
			holder.locationTextView = (TextView) convertView.findViewById(R.id.location);
			holder.iconImageView = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
		}
		else {
			holder = (SuggestionViewHolder) convertView.getTag();
		}

		String searchText = suggestionV2.getText();
		int iconResId = suggestionV2.getIcon();

		holder.iconImageView.setImageResource(iconResId);
		holder.locationTextView.setText(searchText);

		if (mCurrentLocationString.equals(searchText)) {
			holder.locationTextView.setTypeface(null, Typeface.BOLD);
		}
		else {
			holder.locationTextView.setTypeface(null, Typeface.NORMAL);
		}

		return convertView;
	}

	static class SuggestionViewHolder {
		TextView locationTextView;
		ImageView iconImageView;
	}

	public void updateData(Cursor c) {
		data.clear();
		while (c != null && c.moveToNext()) {
			data.add(AutocompleteProvider.rowToSuggestion(c));
		}
		notifyDataSetChanged();
	}
}
