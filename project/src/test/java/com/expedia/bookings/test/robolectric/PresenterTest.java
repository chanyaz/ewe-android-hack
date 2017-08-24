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

	private class StateA {
		// ignore
	}

	private class StateB {
		// ignore
	}

	private class StateC {
		// ignore
	}

	private class NestedPresenter extends Presenter {
		public NestedPresenter(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
	}

	private class CustomBackPresenter extends Presenter {
		final Presenter parentPresenter;

		public CustomBackPresenter(Context context, AttributeSet attrs, Presenter parent) {
			super(context, attrs);
			parentPresenter = parent;
		}

		public boolean back() {
			parentPresenter.show(new StateA(), Presenter.FLAG_CLEAR_BACKSTACK);
			return true;
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
			public void endTransition(boolean forward) {
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
		root.addTransition(getBoringTransition(StateA.class, StateB.class));
		Assert.assertNotNull(root.getTransition(StateB.class.getName(), StateA.class.getName()));
	}

	@Test
	public void testTransitionsAndShowing() throws Throwable {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		Presenter.Transition aTob = getBoringTransition(StateA.class, StateB.class);
		root.addTransition(aTob);

		root.show(new StateA());
		Assert.assertEquals(root.getBackStack().size(), 1);

		Assert.assertTrue(root.getCurrentState().equals(StateA.class.getName()));
		Presenter.Transition t = root.getTransition(StateA.class.getName(), StateB.class.getName()).transition;
		Assert.assertNotNull(t);
		Assert.assertEquals(t, aTob);

		Assert.assertNotNull(root.getStateAnimator(new StateB()));
		Assert.assertTrue(t.state1.equals(StateA.class.getName()) && t.state2.equals(StateB.class.getName()));
		root.show(new StateB());
		Assert.assertEquals(root.getBackStack().size(), 2);
	}

	@Test
	public void testBack() throws Throwable {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addTransition(getBoringTransition(StateA.class, StateB.class));

		root.show(new StateA(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		Assert.assertTrue(root.getCurrentState().equals(StateA.class.getName()));
		Assert.assertEquals(1, root.getBackStack().size());

		root.show(new StateB(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		Assert.assertTrue(root.getCurrentState().equals(StateB.class.getName()));
		Assert.assertEquals(2, root.getBackStack().size());

		boolean backHandled = root.back(Presenter.TEST_FLAG_FORCE_NEW_STATE);
		Assert.assertTrue(backHandled);
		Assert.assertEquals(1, root.getBackStack().size());
		Assert.assertEquals(root.getCurrentState(), StateA.class.getName());
	}

	@Test
	public void testPresenterShowDuringBack() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		Presenter customBackPresenter = new CustomBackPresenter(activity, null, root);

		root.addTransition(getBoringTransition(StateA.class, StateB.class));
		root.addTransition(getBoringTransition(StateB.class, StateC.class));
		root.addTransition(new VisibilityTransition(root, StateC.class, CustomBackPresenter.class));
		root.addTransition(new VisibilityTransition(root, StateA.class, CustomBackPresenter.class));

		root.show(new StateA(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		root.show(new StateB(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		root.show(new StateC(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		root.show(customBackPresenter, Presenter.TEST_FLAG_FORCE_NEW_STATE);
		customBackPresenter.show(new StateA(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		customBackPresenter.show(new StateB(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		customBackPresenter.show(new StateC(), Presenter.TEST_FLAG_FORCE_NEW_STATE);

		root.back();
		Assert.assertEquals(root.getBackStack().size(), 1);
		Assert.assertTrue(root.getCurrentState().equals(StateA.class.getName()));
	}

	@Test
	public void testNestedBack() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addTransition(getBoringTransition(StateA.class, StateB.class));
		root.addTransition(new VisibilityTransition(root, StateB.class, NestedPresenter.class));
		Presenter node = new NestedPresenter(activity, null);
		root.show(new StateA(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		root.show(new StateB(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		root.show(node, Presenter.TEST_FLAG_FORCE_NEW_STATE);
		node.show(new StateB(), Presenter.TEST_FLAG_FORCE_NEW_STATE);

		Assert.assertEquals(root.getBackStack().size(), 3);
		Assert.assertTrue(root.getCurrentState().equals(NestedPresenter.class.getName()));
		Assert.assertEquals(node.getBackStack().size(), 1);
		Assert.assertTrue(node.getCurrentState().equals(StateB.class.getName()));

		root.back(Presenter.TEST_FLAG_FORCE_NEW_STATE);
		Assert.assertEquals(root.getBackStack().size(), 2);
		Assert.assertTrue(root.getCurrentState().equals(StateB.class.getName()));
		Assert.assertEquals(node.getBackStack().size(), 0);

		root.back(Presenter.TEST_FLAG_FORCE_NEW_STATE);
		Assert.assertEquals(root.getBackStack().size(), 1);
		Assert.assertTrue(root.getCurrentState().equals(StateA.class.getName()));

		root.back(Presenter.TEST_FLAG_FORCE_NEW_STATE);
		Assert.assertEquals(root.getBackStack().size(), 0);
		Assert.assertEquals(root.getCurrentState(), null);
	}

	@Test
	public void testClearBackStack() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addTransition(getBoringTransition(StateA.class, StateB.class));

		root.show(new StateA());
		Assert.assertEquals(1, root.getBackStack().size());
		root.show(new StateB(), Presenter.FLAG_CLEAR_BACKSTACK);
		Assert.assertEquals(1, root.getBackStack().size());
	}

	@Test
	public void testClearTop() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addTransition(new VisibilityTransition(root, StateA.class, StateB.class));
		root.show(new StateA(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		root.show(new StateB(), Presenter.TEST_FLAG_FORCE_NEW_STATE);
		root.show(new StateA(), Presenter.FLAG_CLEAR_TOP);
		Assert.assertEquals(1, root.getBackStack().size());
		Assert.assertEquals(StateA.class.getName(), root.getBackStack().pop().getClass().getName());
	}

	@Test
	public void testClearNestedBackstacks() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addTransition(new VisibilityTransition(root, StateA.class, NestedPresenter.class));
		Presenter node = new NestedPresenter(activity, null);
		root.show(new StateA());
		root.show(node);
		node.show(new StateA());
		root.clearBackStack();
		Assert.assertEquals(0, root.getBackStack().size());
		Assert.assertEquals(0, node.getBackStack().size());
	}

	@Test(expected = RuntimeException.class)
	public void testOneDefaultTransitionAllowed() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Presenter root = new Presenter(activity, null);
		root.addDefaultTransition(new Presenter.DefaultTransition("FOO") { });
		root.addDefaultTransition(new Presenter.DefaultTransition("BAR") { });
	}
}
