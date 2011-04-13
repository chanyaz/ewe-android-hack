package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.model.Search;
import com.mobiata.hotellib.R;
import com.mobiata.hotellib.data.SearchParams;

public class SearchSuggestionAdapter extends BaseAdapter {
	private static final int TYPE_CURRENT_LOCATION = 0;
	private static final int TYPE_SEARCH_PARAM = 1;

	public Context mContext;
	public List<SearchParams> mSearchParams;

	public SearchSuggestionAdapter(Context context) {
		mContext = context;
		refreshData();
	}

	public void refreshData() {
		mSearchParams = Search.getAllSearchParams(mContext);
	}

	@Override
	public int getCount() {
		if (mSearchParams != null) {
			return mSearchParams.size() + 1;
		}
		return 1;
	}

	@Override
	public Object getItem(int position) {
		if (position == 0) {

		}
		return mSearchParams.get(position - 1);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return TYPE_CURRENT_LOCATION;
		}
		return TYPE_SEARCH_PARAM;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int viewType = getItemViewType(position);
		
		if (convertView == null) {
			convertView = new TextView(mContext);
		}
		
		if(viewType == TYPE_CURRENT_LOCATION) {
			((TextView) convertView).setText(R.string.current_location);
			
		}
		else if (viewType == TYPE_SEARCH_PARAM) {
			SearchParams searchParams = (SearchParams) getItem(position);
			((TextView) convertView).setText(searchParams.getFreeformLocation());
		}

		return convertView;
	}

	protected class TextRow {
		private String mText;

		public String getText() {
			return mText;
		}

		public TextRow(String text) {
			mText = text;
		}
	}
}
