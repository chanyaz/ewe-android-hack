package com.expedia.bookings.widget;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class AirportDropDownAdapter extends CursorAdapter implements FilterQueryProvider {

	private Map<String, String> mCountryCodeMap;

	public AirportDropDownAdapter(Context context) {
		super(context, null, 0);

		Resources r = context.getResources();
		mCountryCodeMap = new HashMap<String, String>();
		String[] countryCodes = r.getStringArray(R.array.country_codes);
		String[] countryNames = r.getStringArray(R.array.country_names);
		for (int a = 0; a < countryCodes.length; a++) {
			mCountryCodeMap.put(countryCodes[a], countryNames[a]);
		}

		setFilterQueryProvider(this);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String code = cursor.getString(1);
		String city = cursor.getString(3);
		String countryCode = cursor.getString(5);

		String lastPart;
		if (countryCode.equals("US")) {
			lastPart = cursor.getString(4);
		}
		else {
			lastPart = mCountryCodeMap.get(countryCode);
		}

		ViewHolder vh = (ViewHolder) view.getTag();
		String source = context.getString(R.string.search_airport_autocomplete_TEMPLATE, code, city, lastPart);
		vh.mTextView.setText(Html.fromHtml(source));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);

		ViewHolder vh = new ViewHolder();
		vh.mTextView = Ui.findView(v, android.R.id.text1);
		v.setTag(vh);

		// TODO: Style the rows
		// For now, we have to set this text black (for some reason it's white-on-white in some OSes)
		vh.mTextView.setTextColor(Color.BLACK);

		return v;
	}

	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(1);
	}

	private static class ViewHolder {
		private TextView mTextView;
	}

	//////////////////////////////////////////////////////////////////////////
	// FilterQueryProvider

	private SQLiteDatabase mDb;

	public void openDb() {
		if (mDb == null || !mDb.isOpen()) {
			mDb = FlightStatsDbUtils.getStaticDb();
		}
	}

	public void closeDb() {
		if (mDb != null && mDb.isOpen()) {
			mDb.close();
			mDb = null;
		}
	}

	@Override
	public Cursor runQuery(CharSequence constraint) {
		if (mDb == null || !mDb.isOpen() || TextUtils.isEmpty(constraint)) {
			return null;
		}

		String exact = constraint.toString();
		Cursor c = runQuery(exact);
		if (c.getCount() > 0) {
			return c;
		}

		// If we came up with no results, do a query without spaces
		return runQuery(exact.replaceAll(" ", ""));
	}

	private Cursor runQuery(String exact) {
		String like = exact + "%";
		exact = exact.toUpperCase();
		String sql = "SELECT a._id, a.code, a.name, a.city, a.stateCode, c.countryCode, (a.code = ?) AS 'exactMatch' "
				+ "FROM airports a "
				+ "JOIN countries c ON c._id = a.countryId "
				+ "WHERE (code like ? or name like ? or city like ?) ORDER BY exactMatch DESC, classification, city";

		return mDb.rawQuery(sql, new String[] { exact, like, like, like });
	}
}
