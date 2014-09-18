package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * This is a special version of ListView that is for handling an Adapter that dynamically loads thumbnails
 * from the internet.
 */
public class MeasureListView extends ListView {

	public MeasureListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Object adapter = getAdapter();
		if (adapter instanceof OnMeasureListener) {
			OnMeasureListener listener = (OnMeasureListener) adapter;
			listener.onStartMeasure();
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			listener.onStopMeasure();
		}
		else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
