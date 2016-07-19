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
import com.expedia.bookings.widget.ErrorWidget;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class CarErrorWidgetTest {
	private ApiError apiError;

	@Before
	public void before() {
		apiError = new ApiError(ApiError.Code.INVALID_INPUT);

		ApiError.ErrorInfo info = new ApiError.ErrorInfo();
		info.summary = "Invalid input error";
		info.field = "mainMobileTraveler.lastName";
		info.cause = "Please provide a name without numbers or special characters.";
		apiError.errorInfo = info;
	}

	@Test
	public void testInvalidInput() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_Cars);
		ErrorWidget errorWidget = (ErrorWidget) LayoutInflater.from(activity)
			.inflate(R.layout.car_error_widget_test, null);

		errorWidget.bind(apiError);

		Button errorButton = (Button) errorWidget.findViewById(R.id.error_action_button);
		TextView errorText = (TextView) errorWidget.findViewById(R.id.error_text);
		Toolbar errorToolbar = (Toolbar) errorWidget.findViewById(R.id.error_toolbar);

		assertEquals("Invalid Info", errorToolbar.getTitle());
		assertEquals("Names must not contain numbers or special characters.", errorText.getText());
		assertEquals("Edit Info", errorButton.getText());

		// testSessionTimeout
		apiError = new ApiError(ApiError.Code.SESSION_TIMEOUT);

		errorWidget.bind(apiError);

		assertEquals("Session Expired", errorToolbar.getTitle());
		assertEquals("Still there? Your session has expired. Please try your search again.", errorText.getText());
		assertEquals("Edit Search", errorButton.getText());

	}
}
