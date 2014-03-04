package com.expedia.bookings.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.content.SuggestionProvider;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.otto.Events;

public class SuggestionsAdapter extends CursorAdapter {

	private ContentResolver mContent;

	public SuggestionsAdapter(Context context) {
		super(context, null, 0);

		mContent = context.getContentResolver();
	}

	public SuggestionV2 getSuggestion(int position) {
		Cursor c = getCursor();
		c.moveToPosition(position);
		return SuggestionProvider.rowToSuggestion(c);
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		Events.post(new Events.SuggestionResultsDelivered());
	}

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		Uri uri = Uri.withAppendedPath(
				SuggestionProvider.getContentFilterUri(mContext),
				Uri.encode(constraint.toString()));

		return mContent.query(uri, null, null, null, null);
	}

	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(SuggestionProvider.COL_FULL_NAME);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView textView = (TextView) view;

		int iconResId = cursor.getInt(SuggestionProvider.COL_ICON_1);
		textView.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);

		// We only want to show the bolded text (of the query) if there is actually a query
		String query = cursor.getString(SuggestionProvider.COL_QUERY);
		if (TextUtils.isEmpty(query)) {
			textView.setText(Html.fromHtml(cursor.getString(SuggestionProvider.COL_DISPLAY_NAME)).toString());
		}
		else {
			textView.setText(Html.fromHtml(cursor.getString(SuggestionProvider.COL_DISPLAY_NAME)));
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return inflater.inflate(R.layout.row_suggestion_dropdown, parent, false);
	}

}
