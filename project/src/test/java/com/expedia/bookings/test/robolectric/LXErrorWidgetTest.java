package com.expedia.bookings.test.robolectric;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.widget.LXErrorWidget;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class LXErrorWidgetTest {

	LXErrorWidget errorWidget;
	Activity activity;

	@Before
	public void before() {
		activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_LX);
		errorWidget = (LXErrorWidget) LayoutInflater.from(activity)
			.inflate(R.layout.lx_error_widget_test, null);
	}

	@Test
	public void testInvalidInput() {
		ApiError lxApiError = new ApiError(ApiError.Code.INVALID_INPUT);

		// Last name error
		ApiError.ErrorInfo info = new ApiError.ErrorInfo();
		info.field = "lastName";
		lxApiError.errorInfo = info;

		errorWidget.bind(lxApiError);

		Button errorButton = (Button) errorWidget.findViewById(R.id.error_action_button);
		TextView errorText = (TextView) errorWidget.findViewById(R.id.error_text);
		Toolbar errorToolbar = (Toolbar) errorWidget.findViewById(R.id.error_toolbar);

		String expectedToolbartitle = activity.getResources().getString(R.string.lx_invalid_input_text);
		String expectedButtonText = activity.getResources().getString(R.string.edit_info);

		assertEquals(expectedToolbartitle, errorToolbar.getTitle());
		assertEquals(activity.getResources().getString(R.string.reservation_invalid_name), errorText.getText());
		assertEquals(expectedButtonText, errorButton.getText());

		// First name error
		info.field = "firstName";
		errorWidget.bind(lxApiError);

		assertEquals(expectedToolbartitle, errorToolbar.getTitle());
		assertEquals(activity.getResources().getString(R.string.reservation_invalid_name), errorText.getText());
		assertEquals(expectedButtonText, errorButton.getText());

		// Phone number error
		info.field = "phone";
		errorWidget.bind(lxApiError);

		assertEquals(expectedToolbartitle, errorToolbar.getTitle());
		assertEquals(activity.getResources().getString(R.string.reservation_invalid_phone), errorText.getText());
		assertEquals(expectedButtonText, errorButton.getText());
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void testNoSearchResults() {
		ApiError lxApiError = new ApiError(ApiError.Code.LX_SEARCH_NO_RESULTS);

		errorWidget.bind(lxApiError);

		Button errorButton = (Button) errorWidget.findViewById(R.id.error_action_button);
		TextView errorText = (TextView) errorWidget.findViewById(R.id.error_text);
		Toolbar errorToolbar = (Toolbar) errorWidget.findViewById(R.id.error_toolbar);

		assertEquals(activity.getResources().getString(R.string.lx_error_text), errorToolbar.getTitle());
		assertEquals(activity.getResources().getString(R.string.error_lx_search_message), errorText.getText());
		assertEquals(activity.getResources().getString(R.string.edit_search), errorButton.getText());
	}
}
