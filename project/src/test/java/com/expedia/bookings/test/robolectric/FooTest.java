package com.expedia.bookings.test.robolectric;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.TextView;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import com.expedia.bookings.R;

@RunWith(RobolectricRunner.class)
public class FooTest {
	@Test
	public void testSomething() throws Throwable {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		TextView textview = (TextView) LayoutInflater.from(activity).inflate(R.layout.header_done_btn, null);
		Assert.assertEquals("Done", textview.getText());
	}
}
