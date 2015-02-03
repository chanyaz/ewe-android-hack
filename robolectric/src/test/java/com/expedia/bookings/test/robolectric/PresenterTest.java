package com.expedia.bookings.test.robolectric;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.presenter.Presenter;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class PresenterTest {
	@Test
	public void testShowHideBackStack() throws Throwable {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		Assert.assertNotNull(root);

		TextView textview1 = new TextView(activity);
		TextView textview2 = new TextView(activity);
		textview1.setVisibility(View.GONE);
		textview2.setVisibility(View.GONE);

		root.addView(textview1);
		root.addView(textview2);

		root.show(textview1);
		Assert.assertTrue(textview1.getVisibility() == View.VISIBLE);
		Assert.assertTrue(textview2.getVisibility() == View.GONE);
		Assert.assertFalse(root.back());
		Assert.assertTrue(textview1.getVisibility() == View.GONE);
		Assert.assertTrue(textview2.getVisibility() == View.GONE);

		root.show(textview1);
		root.show(textview2);
		Assert.assertTrue(textview1.getVisibility() == View.GONE);
		Assert.assertTrue(textview2.getVisibility() == View.VISIBLE);
		Assert.assertTrue(root.back());
		Assert.assertTrue(textview1.getVisibility() == View.VISIBLE);
		Assert.assertTrue(textview2.getVisibility() == View.GONE);
		Assert.assertFalse(root.back());
		Assert.assertTrue(textview1.getVisibility() == View.GONE);
		Assert.assertTrue(textview2.getVisibility() == View.GONE);

		// Test hide stack
		root.show(textview1);
		root.show(textview2);
		root.hide(textview1);

		Assert.assertTrue(textview1.getVisibility() == View.GONE);
		Assert.assertTrue(textview2.getVisibility() == View.VISIBLE);
		Assert.assertFalse(root.back());
		Assert.assertTrue(textview1.getVisibility() == View.GONE);
		Assert.assertTrue(textview2.getVisibility() == View.GONE);
	}

	@Test
	public void testNestedPresenter() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		Assert.assertNotNull(root);

		TextView textView1 = new TextView(activity);
		TextView textView2 = new TextView(activity);
		Presenter child1 = new Presenter(activity, null);
		child1.addView(textView1);
		child1.addView(textView2);

		child1.show(textView1);

		root.addView(child1);
		root.show(child1);

		Assert.assertTrue(textView1.getVisibility() == View.VISIBLE);
		Assert.assertTrue(textView2.getVisibility() == View.GONE);

		Assert.assertFalse(root.back());
		Assert.assertTrue(textView1.getVisibility() == View.GONE);
		Assert.assertTrue(textView2.getVisibility() == View.GONE);

		Presenter child2 = new Presenter(activity, null);
		TextView textView3 = new TextView(activity);
		child2.addView(textView3);

		child1.show(textView1);
		child2.show(textView3);

		root.show(child1);
		root.show(child2);

		Assert.assertTrue(child1.getVisibility() == View.GONE);
		Assert.assertTrue(textView3.getVisibility() == View.VISIBLE);

		Assert.assertTrue(root.back());
		Assert.assertTrue(child2.getVisibility() == View.GONE);
		Assert.assertTrue(child1.getVisibility() == View.VISIBLE);
		Assert.assertTrue(textView1.getVisibility() == View.VISIBLE);

		Assert.assertFalse(root.back());
		Assert.assertTrue(child1.getVisibility() == View.GONE);
		Assert.assertTrue(child2.getVisibility() == View.GONE);
	}
}
