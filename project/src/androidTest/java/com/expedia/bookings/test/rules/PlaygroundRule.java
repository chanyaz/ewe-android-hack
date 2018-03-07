package com.expedia.bookings.test.rules;

import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.StyleRes;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.widget.FrameLayout;

import com.expedia.bookings.activity.PlaygroundActivity;

/**
 * This will automatically launch the PlaygroundActivity with the layout specified
 */
public class PlaygroundRule extends ActivityTestRule<PlaygroundActivity> {

	private @LayoutRes int layout;
	private @StyleRes int style;

	public PlaygroundRule(@LayoutRes int layout) {
		super(PlaygroundActivity.class);
		this.layout = layout;
	}


	public PlaygroundRule(@LayoutRes int layout, @StyleRes int style) {
		super(PlaygroundActivity.class);
		this.layout = layout;
		this.style = style;
	}

	public View getRoot() {
		return ((FrameLayout) getActivity().findViewById(android.R.id.content)).getChildAt(0);
	}

	@Override
	protected Intent getActivityIntent() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		PlaygroundActivity.addData(intent, layout);
		PlaygroundActivity.addTheme(intent, style);
		return intent;
	}
}
