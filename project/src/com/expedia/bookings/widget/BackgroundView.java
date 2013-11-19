package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * A simple version of View that indicates it does not have overlapping rendering.
 * This helps optimize performance a lot when we use a simple empty View + background
 * for some layouts.
 * 
 * Note that using LAYER_TYPE_HARDWARE with this View during an alpha fade will actually
 * HURT performance, for some reason.  It seems to ignore hasOverlappingRendering(),
 * which helps more than it hurts.
 */
public class BackgroundView extends View {

	public BackgroundView(Context context) {
		super(context);
	}

	public BackgroundView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BackgroundView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean hasOverlappingRendering() {
		return false;
	}

}
