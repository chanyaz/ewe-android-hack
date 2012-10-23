package com.expedia.bookings.widget;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * This class implements a basically infinitely scrolling list.
 * 
 * http://stackoverflow.com/questions/2332847/how-to-create-a-closed-circular-listview
 * 
 * @author doug
 *
 * @param <T>
 */
public class CircularArrayAdapter<T> extends ArrayAdapter<T> {
	public static final int HALF_MAX_VALUE = Integer.MAX_VALUE / 2;

	public CircularArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public CircularArrayAdapter(Context context, int textViewResourceId, T[] objects) {
		super(context, textViewResourceId, objects);
	}

	@Override
	public int getCount() {
		return Integer.MAX_VALUE;
	}

	@Override
	public T getItem(int position) {
		if (super.getCount() == 0) {
			return null;
		}
		return super.getItem(position % super.getCount());
	}

	public int getMiddle() {
		return HALF_MAX_VALUE - (super.getCount() == 0 ? 0 : HALF_MAX_VALUE % super.getCount());
	}
}
