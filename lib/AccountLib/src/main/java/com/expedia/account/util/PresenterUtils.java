package com.expedia.account.util;

import android.view.View;

public class PresenterUtils {

	public static float getTranslationXForCenter(View view, View parent) {
		return (parent.getWidth() / 2) - (view.getWidth() / 2) - view.getX();
	}

	public static float calculateStep(float start, float end, float percent) {
		return start + (percent * (end - start));
	}

}
