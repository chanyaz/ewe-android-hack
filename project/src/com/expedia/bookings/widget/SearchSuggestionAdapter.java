package com.expedia.bookings.widget;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.content.AutocompleteProvider;

public class SearchSuggestionAdapter extends CursorAdapter {
	private LayoutInflater mInflater;
	private String mCurrentLocationString;

	public SearchSuggestionAdapter(Context context) {
		super(context, null, 0);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mCurrentLocationString = context.getResources().getString(R.string.current_location);
	}

	static class SuggestionViewHolder {
		TextView locationTextView;
		ImageView iconImageView;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View convertView = mInflater.inflate(R.layout.row_suggestion, parent, false);

		SuggestionViewHolder holder = new SuggestionViewHolder();
		holder.locationTextView = (TextView) convertView.findViewById(R.id.location);
		holder.iconImageView = (ImageView) convertView.findViewById(R.id.icon);

		convertView.setTag(holder);

		return convertView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		SuggestionViewHolder holder = (SuggestionViewHolder) view.getTag();

		String searchText = cursor.getString(AutocompleteProvider.COLUMN_TEXT_INDEX);
		int iconResId = cursor.getInt(AutocompleteProvider.COLUMN_ICON_INDEX);

		holder.iconImageView.setImageResource(iconResId);
		holder.locationTextView.setText(searchText);

		if (mCurrentLocationString.equals(searchText)) {
			holder.locationTextView.setTypeface(null, Typeface.BOLD);
		}
		else {
			holder.locationTextView.setTypeface(null, Typeface.NORMAL);
		}
	}
}
