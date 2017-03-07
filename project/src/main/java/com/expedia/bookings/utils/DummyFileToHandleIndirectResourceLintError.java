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
		R.bool.acct__isGoogleAccountChangeEnabled,
		R.string.rail_open_return_selected,
		R.string.rail_open_return_description,
		R.drawable.ic_left_disabled,
		R.drawable.ic_left_enabled,
		R.drawable.ic_right_disabled,
		R.drawable.ic_right_enabled,
		R.bool.tablet
	};
}
