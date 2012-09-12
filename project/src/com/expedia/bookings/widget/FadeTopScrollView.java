package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Specialty ScrollView that only fades on the top edge.  It's used
 * for a screen where the ScrollView scrolls underneath something
 * above it, but not below.
 */
public class FadeTopScrollView extends ScrollView {

	public FadeTopScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected float getBottomFadingEdgeStrength() {
		return 0;
	}
}
