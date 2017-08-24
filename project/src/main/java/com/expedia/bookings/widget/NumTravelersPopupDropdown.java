package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Ui;

public class NumTravelersPopupDropdown {

	public static PopupWindow newInstance(Context context) {
		NumTravelersAdapter adapter = new NumTravelersAdapter(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		LinearLayout numAdultsContent = (LinearLayout) inflater.inflate(R.layout.snippet_nav_dropdown, null);
		ListView lv = Ui.findView(numAdultsContent, R.id.nav_dropdown_list);
		lv.setAdapter(adapter);

		PopupWindow popup = new PopupWindow(numAdultsContent, ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				true);
		popup.setBackgroundDrawable(new BitmapDrawable());
		popup.setOutsideTouchable(true);
		popup.setTouchable(true);

		return popup;
	}

	static class NumTravelersAdapter extends BaseAdapter {

		private static final int MAX_NUM_ADULTS = 6;

		private final Context mContext;
		private final LayoutInflater mInflater;
		private List<String> mItems;

		public NumTravelersAdapter(Context context) {
			mContext = context;
			mInflater = LayoutInflater.from(context);
			init();
		}

		private void init() {
			mItems = new ArrayList<String>();

			Resources res = mContext.getResources();
			for (int i = 1; i < MAX_NUM_ADULTS + 1; i++) {
				mItems.add(res.getQuantityString(R.plurals.number_of_adults_TEMPLATE, i, i));
			}
		}

		@Override
		public int getCount() {
			if (mItems != null) {
				return mItems.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			if (mItems != null) {
				mItems.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.snippet_number_adults_text_view, parent, false);
			}

			TextView tv = Ui.findView(convertView, R.id.number_adults_text_view);
			FontCache.setTypeface(tv, FontCache.Font.ROBOTO_LIGHT);
			tv.setText(mItems.get(position));

			return convertView;
		}
	}

}
