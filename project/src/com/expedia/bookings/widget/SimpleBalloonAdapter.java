package com.expedia.bookings.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.google.android.maps.OverlayItem;
import com.mobiata.android.widget.BalloonItemizedOverlay.BalloonAdapter;

public class SimpleBalloonAdapter implements BalloonAdapter {

	private Context mContext;
	private TextView mTextView;

	public SimpleBalloonAdapter(Context context) {
		mContext = context;
	}

	@Override
	public void bindView(View view, OverlayItem item, int index) {
		mTextView.setText(item.getTitle());
	}

	@Override
	public View newView(ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTextView = (TextView) inflater.inflate(R.layout.balloon_map_simple, parent, false);
		return mTextView;
	}

}
