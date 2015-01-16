package com.expedia.bookings.test.rules;

import android.content.Intent;
import android.support.annotation.LayoutRes;

import com.expedia.bookings.activity.PlaygroundActivity;

/**
 * This will automatically launch the PlaygroundActivity with the layout specified
 */
public class PlaygroundRule extends ActivityRule<PlaygroundActivity> {

	private @LayoutRes int mLayout;


	public PlaygroundRule(@LayoutRes int layout) {
		super(PlaygroundActivity.class);
		mLayout = layout;
	}

	@Override
	protected Intent getLaunchIntent(String targetPackage) {
		Intent intent = super.getLaunchIntent(targetPackage);
		PlaygroundActivity.addData(intent, mLayout);
		return intent;
	}
}
