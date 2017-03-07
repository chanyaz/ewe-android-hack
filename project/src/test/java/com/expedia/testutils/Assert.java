package com.expedia.testutils;

import android.view.View;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class Assert {

	public static void assertViewIsVisible(View view) {
		assertNotNull(view);
		assertThat(view.getVisibility(), equalTo(View.VISIBLE));
	}

	public static void assertViewIsNotVisible(View view) {
		assertNotNull(view);
		assertNotEquals(view.getVisibility(), equalTo(View.VISIBLE));
	}

}
