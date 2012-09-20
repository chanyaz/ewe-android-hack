package com.expedia.bookings.widget;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.content.AirportAutocompleteProvider;
import com.mobiata.android.util.Ui;

public class AirportDropDownAdapter extends CursorAdapter {

	private Map<String, String> mCountryCodeMap;

	private ContentResolver mContent;

	public AirportDropDownAdapter(Context context) {
		super(context, null, 0);

		mContent = context.getContentResolver();

		Resources r = context.getResources();
		mCountryCodeMap = new HashMap<String, String>();
		String[] countryCodes = r.getStringArray(R.array.country_codes);
		String[] countryNames = r.getStringArray(R.array.country_names);
		for (int a = 0; a < countryCodes.length; a++) {
			mCountryCodeMap.put(countryCodes[a], countryNames[a]);
		}
	}

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		Uri uri = Uri.withAppendedPath(
				AirportAutocompleteProvider.CONTENT_FILTER_URI,
				Uri.encode(constraint.toString()));

		return mContent.query(uri, null, null, null, null);
	}

	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(AirportAutocompleteProvider.COL_SUGGEST_COLUMN_QUERY);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder vh = (ViewHolder) view.getTag();
		vh.mTextView1.setText(cursor.getString(AirportAutocompleteProvider.COL_SUGGEST_COLUMN_TEXT_1));
		vh.mTextView2.setText(cursor.getString(AirportAutocompleteProvider.COL_SUGGEST_COLUMN_TEXT_2));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.simple_dropdown_item_2line, parent, false);

		ViewHolder vh = new ViewHolder();
		vh.mTextView1 = Ui.findView(v, android.R.id.text1);
		vh.mTextView2 = Ui.findView(v, android.R.id.text2);
		v.setTag(vh);

		// TODO: Style the rows
		// For now, we have to set this text black (for some reason it's white-on-white in some OSes)
		vh.mTextView1.setTextColor(Color.BLACK);
		vh.mTextView2.setTextColor(Color.BLACK);

		return v;
	}

	private static class ViewHolder {
		private TextView mTextView1;
		private TextView mTextView2;
	}

	//////////////////////////////////////////////////////////////////////////
	// RecentSearchList interaction

	public void onAirportSelected(String airportCode) {
		// TODO: Save airport code to recents?
	}

}
