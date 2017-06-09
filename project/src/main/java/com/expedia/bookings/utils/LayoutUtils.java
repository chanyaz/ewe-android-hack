package com.expedia.bookings.utils;

import org.jetbrains.annotations.NotNull;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.larvalabs.svgandroid.widget.SVGView;

public class LayoutUtils {

	public static void setSVG(@NotNull SVGView svgView, int svgRes) {
		if (!ExpediaBookingApp.isAutomation()) {
			svgView.setSVG(svgRes);
		}
	}
}
