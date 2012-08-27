package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * This is a version of AutoCompleteTextView that will *always* filter.
 * 
 * It is necessary because otherwise AutoCompleteTextView will not filter
 * when there is no text entered into the field.
 */
public class AlwaysFilterAutoCompleteTextView extends AutoCompleteTextView {

	public AlwaysFilterAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean enoughToFilter() {
		return true;
	}
}
