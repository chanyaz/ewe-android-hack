package com.expedia.bookings.launch.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.ColorBuilder;
import com.expedia.bookings.utils.LayoutUtils;

/**
 * Defines common utilities for working with TVLY tablet home screen animations.
 */
public class LaunchScreenAnimationUtil {
	private LaunchScreenAnimationUtil() {
	}

	public static int getActionBarNavBarSize(Context context) {
		return LayoutUtils.getActionBarSize(context) + context.getResources()
			.getDimensionPixelSize(R.dimen.extra_status_bar_padding);
	}

	public static int getNavigationBarHeight(Context context) {
		Resources resources = context.getResources();
		int id = resources.getIdentifier(
			context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
				? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
		return id > 0 ? resources.getDimensionPixelSize(id) : 0;
	}

	public static int getMarginBottom(Context context) {
		return (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? context
			.getResources().getDimensionPixelSize(R.dimen.destination_tile_extra_bottom_padding) : 0;
	}

	public static void applyColorToOverlay(Activity activity, View... views) {
		ColorBuilder fullColorBuilder = new ColorBuilder(
			activity.getResources().getColor(R.color.collection_overlay_color));
		int textColor = fullColorBuilder
			.setOpacity(0.9f)
			.darkenBy(0.9f)
			.setAlpha(225)
			.build();
		GradientDrawable textViewBackground = new GradientDrawable();
		textViewBackground.setColor(textColor);
		if (Build.VERSION.SDK_INT < 16) {
			for (View view : views) {
				view.setBackgroundDrawable(textViewBackground);
			}
		}
		else {
			for (View view : views) {
				view.setBackground(textViewBackground);
			}
		}
	}
}
