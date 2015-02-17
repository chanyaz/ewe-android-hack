package com.expedia.bookings.test.robolectric;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;

import com.expedia.bookings.presenter.Presenter;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class PresenterTest {

	private class A {

	}

	private class B {

	}

	public Presenter.Transition boringTransition = new Presenter.Transition(A.class.getName(), B.class.getName(), null, 100) {
		@Override
		public void startTransition(boolean forward) {
		}

		@Override
		public void updateTransition(float f, boolean forward) {
		}

		@Override
		public void endTransition(boolean forward) {
		}

		@Override
		public void finalizeTransition(boolean forward) {
		}
	};


	@Test(expected = RuntimeException.class)
	public void testNoTransitionDefined() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		Object a = new Object();
		String b = new String();
		root.show(a);
		root.show(b);
	}

	@Test
	public void testReverseTransition() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addTransition(boringTransition);
		Assert.assertNotNull(root.getTransition(B.class.getName(), A.class.getName()));
	}

	@Test
	public void testTransitionsAndShowing() throws Throwable {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addTransition(boringTransition);
		Assert.assertTrue(root.getTransitions().size() == 1);

		root.show(new A());
		Assert.assertEquals(root.getBackStack().size(), 1);

		Assert.assertTrue(root.getCurrentState().equals(A.class.getName()));
		Presenter.Transition t = root.getTransition(A.class.getName(), B.class.getName());
		Assert.assertNotNull(t);
		Assert.assertEquals(t, boringTransition);

		Assert.assertNotNull(root.getStateAnimator(new B(), true));
		Assert.assertTrue(t.state1.equals(A.class.getName()) && t.state2.equals(B.class.getName()));
		root.show(new B());
		Assert.assertEquals(root.getBackStack().size(), 2);
	}

	@Test
	public void testClearBackStack() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addTransition(boringTransition);

		root.show(new A());
		Assert.assertEquals(1, root.getBackStack().size());
		root.show(new B(), true);
		Assert.assertEquals(1, root.getBackStack().size());
	}
}
