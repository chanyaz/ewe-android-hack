package com.expedia.bookings.test.rules;

import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.StyleRes;

import com.expedia.bookings.activity.PlaygroundActivity;

/**
 * This will automatically launch the PlaygroundActivity with the layout specified
 */
public class PlaygroundRule extends ActivityRule<PlaygroundActivity> {

	private @LayoutRes int mLayout;
	private @StyleRes int mStyle;

	public PlaygroundRule(@LayoutRes int layout) {
		super(PlaygroundActivity.class);
		mLayout = layout;
	}


	public PlaygroundRule(@LayoutRes int layout, @StyleRes int style) {
		super(PlaygroundActivity.class);
		mLayout = layout;
		mStyle = style;
	}

	@Override
	protected Intent getLaunchIntent(String targetPackage) {
		Intent intent = super.getLaunchIntent(targetPackage);
		PlaygroundActivity.addData(intent, mLayout);
		PlaygroundActivity.addTheme(intent, mStyle);
		return intent;
	}
}
