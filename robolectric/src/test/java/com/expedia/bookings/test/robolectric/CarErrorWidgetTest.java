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
import com.expedia.bookings.data.cars.CarApiError;
import com.expedia.bookings.widget.ErrorWidget;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class CarErrorWidgetTest {
	private CarApiError carApiError;

	@Before
	public void before() {
		carApiError = new CarApiError();
		carApiError.errorCode = CarApiError.Code.INVALID_INPUT;

		CarApiError.ErrorInfo info = new CarApiError.ErrorInfo();
		info.summary = "Invalid input error";
		info.field = "mainMobileTraveler.lastName";
		info.cause = "Please provide a name without numbers or special characters.";
		carApiError.errorInfo = info;
	}

	@Test
	public void testInvalidInput() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		ErrorWidget errorWidget = (ErrorWidget) LayoutInflater.from(activity)
			.inflate(R.layout.car_error_widget_test, null);

		errorWidget.bind(carApiError);

		Button errorButton = (Button) errorWidget.findViewById(R.id.error_action_button);
		TextView errorText = (TextView) errorWidget.findViewById(R.id.error_text);
		Toolbar errorToolbar = (Toolbar) errorWidget.findViewById(R.id.error_toolbar);

		assertEquals("Invalid Info", errorToolbar.getTitle());
		assertEquals("Names must not contain numbers or special characters.", errorText.getText());
		assertEquals("Edit Info", errorButton.getText());

		// testSessionTimeout
		carApiError = new CarApiError();
		carApiError.errorCode = CarApiError.Code.SESSION_TIMEOUT;

		errorWidget.bind(carApiError);

		assertEquals("Session Timeout", errorToolbar.getTitle());
		assertEquals("Still there? Your session has expired. Please try your search again.", errorText.getText());
		assertEquals("Edit Search", errorButton.getText());

	}
}
