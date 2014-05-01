package com.expedia.bookings.section;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

public class InfantSeatingOptionSpinnerAdapter extends BaseAdapter {

	private Context mContext;
	ArrayList<Boolean> mInfantCategories;

	public InfantSeatingOptionSpinnerAdapter(Context context) {
		mContext = context;
		fillInfantCategories();
	}

	@Override
	public int getCount() {
		return mInfantCategories.size();
	}

	@Override
	public Boolean getItem(int position) {
		return mInfantCategories.get(position);
	}

	@Override
	public long getItemId(int position) {
		// Don't need this.
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View retView;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			retView = inflater.inflate(R.layout.infant_toggle_spinner_item, null);
		}
		else {
			retView = convertView;
		}
		TextView tv = Ui.findView(retView, android.R.id.text1);
		tv.setText(getItem(position) ? R.string.infants_in_laps : R.string.infants_in_seats);
		return retView;
	}

	private void fillInfantCategories() {
		mInfantCategories = new ArrayList<Boolean>();
		// True means infants in seats. False means infants in laps.
		mInfantCategories.add(true);
		mInfantCategories.add(false);
	}
}
