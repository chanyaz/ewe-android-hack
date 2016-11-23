package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.expedia.bookings.R;
import com.expedia.bookings.content.SuggestionProvider;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.text.HtmlCompat;

public class SuggestionsAdapter extends ArrayAdapter<SuggestionV2> implements Filterable {

	private ArrayList<SuggestionV2> data = new ArrayList<>();
	private ContentResolver mContent;
	private SuggestFilter mFilter = new SuggestFilter();
	private LayoutInflater mInflater;

	public SuggestionsAdapter(Context context) {
		super(context, R.layout.row_suggestion_dropdown);
		mContent = context.getContentResolver();
		mInflater = LayoutInflater.from(getContext());
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		Events.post(new Events.SuggestionResultsDelivered());
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public SuggestionV2 getItem(int position) {
		return data.get(position);
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		SuggestionV2 suggestionV2 = data.get(position);
		ViewHolder viewHolder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_suggestion_dropdown, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.txtView = (TextView) convertView.findViewById(R.id.text1);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.txtView.setCompoundDrawablesWithIntrinsicBounds(suggestionV2.getIcon(), 0 ,0 ,0);
		viewHolder.txtView.setText(HtmlCompat.stripHtml(suggestionV2.getDisplayName()));

		return convertView;
	}

	private static class ViewHolder {
		TextView txtView;
	}

	@NonNull
	@Override
	public Filter getFilter() {
		return mFilter;
	}

	private class SuggestFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();

			final ArrayList<SuggestionV2> suggestionV2s = new ArrayList<>();

			Uri uri = Uri.withAppendedPath(
				SuggestionProvider.getContentFilterUri(getContext()),
				Uri.encode(constraint.toString()));
			Cursor c =  mContent.query(uri, null, null, null, null);
			while (c.moveToNext()) {
				suggestionV2s.add(SuggestionProvider.rowToSuggestion(c));
			}

			results.values = suggestionV2s;
			results.count = suggestionV2s.size();

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			data = (ArrayList<SuggestionV2>) results.values;
			notifyDataSetChanged();
		}

	}

}
