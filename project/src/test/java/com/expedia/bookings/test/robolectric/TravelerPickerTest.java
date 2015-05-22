package com.expedia.bookings.test.robolectric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.TravelerPicker;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class TravelerPickerTest {

	@Test
	public void testDefaultValues() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		TravelerPicker travelerPicker = (TravelerPicker) LayoutInflater.from(activity)
			.inflate(R.layout.traveler_picker_test, null);

		TextView travelerInfoText = (TextView) travelerPicker.findViewById(R.id.num_guests);
		TextView adultText = (TextView) travelerPicker.findViewById(R.id.adult);
		TextView childText = (TextView) travelerPicker.findViewById(R.id.children);

		assertEquals("1 Adult", travelerInfoText.getText().toString());
		assertEquals("1 Adult", adultText.getText().toString());
		assertEquals("0 Children", childText.getText().toString());
	}

	@Test
	public void simpleClickAdultPlus() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		TravelerPicker travelerPicker = (TravelerPicker) LayoutInflater.from(activity)
			.inflate(R.layout.traveler_picker_test, null);

		TextView travelerInfoText = (TextView) travelerPicker.findViewById(R.id.num_guests);
		TextView adultText = (TextView) travelerPicker.findViewById(R.id.adult);
		TextView childText = (TextView) travelerPicker.findViewById(R.id.children);
		ImageButton adultPlus = (ImageButton) travelerPicker.findViewById(R.id.adults_plus);

		adultPlus.performClick();

		assertEquals("2 Adults", travelerInfoText.getText().toString());
		assertEquals("2 Adults", adultText.getText().toString());
	}

	@Test
	public void simpleClickAdultMinus() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		TravelerPicker travelerPicker = (TravelerPicker) LayoutInflater.from(activity)
			.inflate(R.layout.traveler_picker_test, null);

		TextView travelerInfoText = (TextView) travelerPicker.findViewById(R.id.num_guests);
		TextView adultText = (TextView) travelerPicker.findViewById(R.id.adult);
		TextView childText = (TextView) travelerPicker.findViewById(R.id.children);
		ImageButton adultPlus = (ImageButton) travelerPicker.findViewById(R.id.adults_plus);
		ImageButton adultMinus = (ImageButton) travelerPicker.findViewById(R.id.adults_minus);

		adultPlus.performClick();
		adultMinus.performClick();

		assertEquals("1 Adult", travelerInfoText.getText().toString());
		assertEquals("1 Adult", adultText.getText().toString());
	}

	@Test
	public void simpleClickChildPlus() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		TravelerPicker travelerPicker = (TravelerPicker) LayoutInflater.from(activity)
			.inflate(R.layout.traveler_picker_test, null);

		TextView travelerInfoText = (TextView) travelerPicker.findViewById(R.id.num_guests);
		TextView adultText = (TextView) travelerPicker.findViewById(R.id.adult);
		TextView childText = (TextView) travelerPicker.findViewById(R.id.children);
		ImageButton childPlus = (ImageButton) travelerPicker.findViewById(R.id.children_plus);
		TableRow childrenRow1 = (TableRow) travelerPicker.findViewById(R.id.children_row1);

		childPlus.performClick();

		assertEquals("1 Adult, 1 Child", travelerInfoText.getText().toString());
		assertEquals("1 Child", childText.getText().toString());
		assertEquals(1, childrenRow1.getChildCount());
	}

	@Test
	public void simpleClickChildMinus() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		TravelerPicker travelerPicker = (TravelerPicker) LayoutInflater.from(activity)
			.inflate(R.layout.traveler_picker_test, null);

		TextView travelerInfoText = (TextView) travelerPicker.findViewById(R.id.num_guests);
		TextView adultText = (TextView) travelerPicker.findViewById(R.id.adult);
		TextView childText = (TextView) travelerPicker.findViewById(R.id.children);
		ImageButton childPlus = (ImageButton) travelerPicker.findViewById(R.id.children_plus);
		ImageButton childMinus = (ImageButton) travelerPicker.findViewById(R.id.children_minus);
		TableRow childrenRow1 = (TableRow) travelerPicker.findViewById(R.id.children_row1);

		childPlus.performClick();
		childMinus.performClick();

		assertEquals("1 Adult", travelerInfoText.getText().toString());
		assertEquals("0 Children", childText.getText().toString());
		assertEquals(0, childrenRow1.getChildCount());
	}

	@Test
	public void minClickAdultMinus() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		TravelerPicker travelerPicker = (TravelerPicker) LayoutInflater.from(activity)
			.inflate(R.layout.traveler_picker_test, null);

		TextView travelerInfoText = (TextView) travelerPicker.findViewById(R.id.num_guests);
		TextView adultText = (TextView) travelerPicker.findViewById(R.id.adult);
		TextView childText = (TextView) travelerPicker.findViewById(R.id.children);
		ImageButton adultMinus = (ImageButton) travelerPicker.findViewById(R.id.adults_minus);

		adultMinus.performClick();

		assertEquals("1 Adult", travelerInfoText.getText().toString());
		assertEquals("1 Adult", adultText.getText().toString());
	}

	@Test
	public void maxClickAdultPlus() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		TravelerPicker travelerPicker = (TravelerPicker) LayoutInflater.from(activity)
			.inflate(R.layout.traveler_picker_test, null);

		TextView travelerInfoText = (TextView) travelerPicker.findViewById(R.id.num_guests);
		TextView adultText = (TextView) travelerPicker.findViewById(R.id.adult);
		TextView childText = (TextView) travelerPicker.findViewById(R.id.children);
		ImageButton adultPlus = (ImageButton) travelerPicker.findViewById(R.id.adults_plus);
		ImageButton adultMinus = (ImageButton) travelerPicker.findViewById(R.id.adults_minus);

		for (int i = 0; i < 6; i++) {
			adultPlus.performClick();
		}

		assertEquals("6 Adults", travelerInfoText.getText().toString());
		assertEquals("6 Adults", adultText.getText().toString());

		for (int i = 0; i < 6; i++) {
			adultMinus.performClick();
		}

		assertEquals("1 Adult", travelerInfoText.getText().toString());
		assertEquals("1 Adult", adultText.getText().toString());
	}

	@Test
	public void minClickChildMinus() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		TravelerPicker travelerPicker = (TravelerPicker) LayoutInflater.from(activity)
			.inflate(R.layout.traveler_picker_test, null);

		TextView travelerInfoText = (TextView) travelerPicker.findViewById(R.id.num_guests);
		TextView adultText = (TextView) travelerPicker.findViewById(R.id.adult);
		TextView childText = (TextView) travelerPicker.findViewById(R.id.children);
		ImageButton childMinus = (ImageButton) travelerPicker.findViewById(R.id.children_minus);
		TableRow childrenRow1 = (TableRow) travelerPicker.findViewById(R.id.children_row1);

		childMinus.performClick();

		assertEquals("1 Adult", travelerInfoText.getText().toString());
		assertEquals("0 Children", childText.getText().toString());
		assertEquals(0, childrenRow1.getChildCount());
	}

	@Test
	public void maxClickChildPlus() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		TravelerPicker travelerPicker = (TravelerPicker) LayoutInflater.from(activity)
			.inflate(R.layout.traveler_picker_test, null);

		TextView travelerInfoText = (TextView) travelerPicker.findViewById(R.id.num_guests);
		TextView adultText = (TextView) travelerPicker.findViewById(R.id.adult);
		TextView childText = (TextView) travelerPicker.findViewById(R.id.children);
		ImageButton childPlus = (ImageButton) travelerPicker.findViewById(R.id.children_plus);
		ImageButton childMinus = (ImageButton) travelerPicker.findViewById(R.id.children_minus);
		TableRow childrenRow1 = (TableRow) travelerPicker.findViewById(R.id.children_row1);
		TableRow childrenRow2 = (TableRow) travelerPicker.findViewById(R.id.children_row2);

		for (int i = 0; i < 6; i++) {
			childPlus.performClick();
		}

		assertEquals("1 Adult, 4 Children", travelerInfoText.getText().toString());
		assertEquals("4 Children", childText.getText().toString());
		assertEquals(2, childrenRow1.getChildCount());
		assertEquals(2, childrenRow2.getChildCount());

		for (int i = 0; i < 6; i++) {
			childMinus.performClick();
		}

		assertEquals("1 Adult", travelerInfoText.getText().toString());
		assertEquals("0 Children", childText.getText().toString());
		assertEquals(0, childrenRow1.getChildCount());
		assertEquals(0, childrenRow2.getChildCount());
	}
}
