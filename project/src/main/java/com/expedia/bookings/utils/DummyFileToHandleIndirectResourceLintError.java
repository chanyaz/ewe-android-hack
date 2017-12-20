package com.expedia.bookings.utils;

import com.expedia.bookings.R;

/*
 * In some part of the Studio 2.0 toolset, lint seems to have regressed to mark indirectly referenced resources
 * as unused. https://code.google.com/p/android/issues/detail?id=225951
 *
 * This file makes lint stop complaining about those resources. Must delete this file once the lint defect is fixed.
 */
public class DummyFileToHandleIndirectResourceLintError {

	private static final int[] USED_RESOURCES = {
		R.style.AccountSectionLabel,
		R.style.AccountPrimaryText,
		R.style.AccountSecondaryText,
		R.style.BundleWidgetTop,
		R.bool.acct__isGoogleAccountChangeEnabled,
		R.bool.signInCardUseCompatPadding,
		R.bool.abs__split_action_bar_is_narrow,
		R.string.rail_open_return_selected,
		R.string.rail_open_return_description,
		R.drawable.ic_left_disabled,
		R.drawable.ic_left_enabled,
		R.drawable.ic_right_disabled,
		R.drawable.ic_right_enabled,
		R.drawable.itin_website_icon,
		R.color.btn_sign_in_pressed,
		R.color.btn_sign_in_focused,
		R.color.blue7,
		R.color.bundleTextGray,
		R.color.docketOutboundWidgetGray,
		R.drawable.flight_upsell_cross_icon,
		R.drawable.flight_upsell_oval_icon,
		R.drawable.flight_upsell_tick_icon,
		R.string.roundtrip,
		R.style.FlightFareFamilyTextStyle,
		R.style.FlightFareFamilyAmenityText,
		R.style.FareFamilyHeader,
		R.style.FareFamilyHeaderSubtitle,
		R.style.FlightFareFamilyAmenityIcon,
		R.string.roundtrip_with_brackets,
		R.string.flight_outbound_label,
		R.bool.tablet,
		R.string.recent_searched_location,
		R.string.suggested_location,
		R.string.suggested_location_list
	};
}
