package com.expedia.bookings.test.espresso;

import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import com.expedia.bookings.content.SuggestionProvider;

public class SuggestionAdapterViewProtocol extends CursorAdapterViewProtocol {
	private static final SuggestionAdapterViewProtocol INSTANCE = new SuggestionAdapterViewProtocol();

	public static SuggestionAdapterViewProtocol getInstance() {
		return INSTANCE;
	}

	public Object getDataFromCursor(CursorAdapter cursorAdapter, Cursor cursor) {
		return Html.fromHtml(cursor.getString(SuggestionProvider.COL_DISPLAY_NAME)).toString();
	}
}
