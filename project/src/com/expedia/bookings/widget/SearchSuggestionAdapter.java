package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
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
	protected LayoutInflater mInflater;
	public List<SearchParams> mSearchParams;

	public SearchSuggestionAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		refreshData();
	}

	public void refreshData() {
		mSearchParams = Search.getAllSearchParams(mContext);
		notifyDataSetChanged();
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
		
		SuggestionViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_suggestion, parent, false);

			holder = new SuggestionViewHolder();
			holder.suggestionTextView = (TextView) convertView.findViewById(R.id.suggestion_text_view);

			convertView.setTag(holder);
		}
		else {
			holder = (SuggestionViewHolder) convertView.getTag();
		}
		
		if(viewType == TYPE_CURRENT_LOCATION) {
			holder.suggestionTextView.setText(R.string.current_location);
			
		}
		else if (viewType == TYPE_SEARCH_PARAM) {
			SearchParams searchParams = (SearchParams) getItem(position);
			holder.suggestionTextView.setText(searchParams.getFreeformLocation());
		}

		return convertView;
	}

	static class SuggestionViewHolder {
		TextView suggestionTextView;
	}
}
