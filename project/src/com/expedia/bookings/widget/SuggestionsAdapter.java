package com.expedia.bookings.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.content.SuggestionProvider;
import com.expedia.bookings.data.SuggestionV2;
import com.mobiata.android.util.Ui;

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
		ViewHolder vh = (ViewHolder) view.getTag();

		int iconResId = cursor.getInt(SuggestionProvider.COL_ICON_1);
		if (iconResId == 0) {
			vh.mIcon1.setVisibility(View.GONE);
		}
		else {
			vh.mIcon1.setImageResource(iconResId);
			vh.mIcon1.setVisibility(View.VISIBLE);
		}

		vh.mTextView1.setText(cursor.getString(SuggestionProvider.COL_TEXT_1));
		vh.mTextView2.setText(cursor.getString(SuggestionProvider.COL_TEXT_2));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.simple_dropdown_item_2line, parent, false);

		ViewHolder vh = new ViewHolder();
		vh.mIcon1 = Ui.findView(v, android.R.id.icon1);
		vh.mTextView1 = Ui.findView(v, android.R.id.text1);
		vh.mTextView2 = Ui.findView(v, android.R.id.text2);
		v.setTag(vh);

		return v;
	}

	private static class ViewHolder {
		private ImageView mIcon1;
		private TextView mTextView1;
		private TextView mTextView2;
	}
}
