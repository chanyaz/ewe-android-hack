package com.expedia.bookings.test.robolectric;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;

@RunWith(RobolectricRunner.class)
public class PresenterTest {

	private class A {
		// ignore
	}

	private class B {
		// ignore
	}

	private class C extends Presenter {
		public C(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
	}

	private Presenter.Transition getBoringTransition(Class from, Class to) {
		return new Presenter.Transition(from, to, null, 100) {
			@Override
			public void startTransition(boolean forward) {
			}

			@Override
			public void updateTransition(float f, boolean forward) {
			}

			@Override
			public void finalizeTransition(boolean forward) {
			}
		};
	}

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
		root.addTransition(getBoringTransition(A.class, B.class));
		Assert.assertNotNull(root.getTransition(B.class.getName(), A.class.getName()));
	}

	@Test
	public void testTransitionsAndShowing() throws Throwable {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		Presenter.Transition aTob = getBoringTransition(A.class, B.class);
		root.addTransition(aTob);

		root.show(new A());
		Assert.assertEquals(root.getBackStack().size(), 1);

		Assert.assertTrue(root.getCurrentState().equals(A.class.getName()));
		Presenter.Transition t = root.getTransition(A.class.getName(), B.class.getName()).transition;
		Assert.assertNotNull(t);
		Assert.assertEquals(t, aTob);

		Assert.assertNotNull(root.getStateAnimator(new B()));
		Assert.assertTrue(t.state1.equals(A.class.getName()) && t.state2.equals(B.class.getName()));
		root.show(new B());
		Assert.assertEquals(root.getBackStack().size(), 2);
	}

	@Test
	public void testBack() throws Throwable {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addTransition(getBoringTransition(A.class, B.class));

		root.show(new A(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		Assert.assertTrue(root.getCurrentState().equals(A.class.getName()));
		Assert.assertEquals(1, root.getBackStack().size());

		root.show(new B(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		Assert.assertTrue(root.getCurrentState().equals(B.class.getName()));
		Assert.assertEquals(2, root.getBackStack().size());

		boolean backHandled = root.back(Presenter.TEST_FLAG_FORCE_NEW_STATE);
		Assert.assertTrue(backHandled);
		Assert.assertEquals(1, root.getBackStack().size());
		Assert.assertEquals(root.getCurrentState(), A.class.getName());
	}

	@Test
	public void testNestedBack() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addTransition(getBoringTransition(A.class, B.class));
		root.addTransition(new VisibilityTransition(root, B.class, C.class));
		Presenter node = new C(activity, null);
		root.show(new A(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		root.show(new B(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		root.show(node, Presenter.TEST_FLAG_FORCE_NEW_STATE);
		node.show(new B(), Presenter.TEST_FLAG_FORCE_NEW_STATE);

		Assert.assertEquals(root.getBackStack().size(), 3);
		Assert.assertTrue(root.getCurrentState().equals(C.class.getName()));
		Assert.assertEquals(node.getBackStack().size(), 1);
		Assert.assertTrue(node.getCurrentState().equals(B.class.getName()));

		root.back(Presenter.TEST_FLAG_FORCE_NEW_STATE);
		Assert.assertEquals(root.getBackStack().size(), 2);
		Assert.assertTrue(root.getCurrentState().equals(B.class.getName()));
		Assert.assertEquals(node.getBackStack().size(), 0);

		root.back(Presenter.TEST_FLAG_FORCE_NEW_STATE);
		Assert.assertEquals(root.getBackStack().size(), 1);
		Assert.assertTrue(root.getCurrentState().equals(A.class.getName()));

		root.back(Presenter.TEST_FLAG_FORCE_NEW_STATE);
		Assert.assertEquals(root.getBackStack().size(), 0);
		Assert.assertEquals(root.getCurrentState(), null);
	}

	@Test
	public void testClearBackStack() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addTransition(getBoringTransition(A.class, B.class));

		root.show(new A());
		Assert.assertEquals(1, root.getBackStack().size());
		root.show(new B(), Presenter.FLAG_CLEAR_BACKSTACK);
		Assert.assertEquals(1, root.getBackStack().size());
	}

	@Test
	public void testClearTop() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addTransition(new VisibilityTransition(root, A.class, B.class));
		root.show(new A(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		root.show(new B(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		root.show(new A(), Presenter.FLAG_CLEAR_TOP);
		Assert.assertEquals(1, root.getBackStack().size());
		Assert.assertEquals(A.class.getName(), root.getBackStack().pop().getClass().getName());
	}

	@Test
	public void testClearNestedBackstacks() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addTransition(new VisibilityTransition(root, A.class, C.class));
		Presenter node = new C(activity, null);
		root.show(new A());
		root.show(node);
		node.show(new A());
		root.clearBackStack();
		Assert.assertEquals(0, root.getBackStack().size());
		Assert.assertEquals(0, node.getBackStack().size());
	}

	@Test(expected = RuntimeException.class)
	public void testOneDefaultTransitionAllowed() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addDefaultTransition(new Presenter.DefaultTransition("FOO") {
			@Override
			public void finalizeTransition(boolean forward) {
			}
		});
		root.addDefaultTransition(new Presenter.DefaultTransition("BAR") {
			@Override
			public void finalizeTransition(boolean forward) {
			}
		});
	}
}
