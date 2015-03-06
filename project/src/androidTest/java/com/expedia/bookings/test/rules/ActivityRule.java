package com.expedia.bookings.test.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.view.View;
import android.widget.FrameLayout;

/**
 * This will automatically launch the activity for each test method. Jacked from JakeWharton's gist.
 */
public class ActivityRule<T extends Activity> implements TestRule {
	private final Class<T> mActivityClass;

	public ActivityRule(Class<T> activityClass) {
		mActivityClass = activityClass;
	}

	private T mActivity;
	private Instrumentation mInstrumentation;

	public final T get() {
		launchActivity();
		return mActivity;
	}

	public final View getRoot() {
		launchActivity();
		return ((FrameLayout) mActivity.findViewById(android.R.id.content)).getChildAt(0);
	}

	public final Instrumentation instrumentation() {
		launchActivity();
		return mInstrumentation;
	}

	@Override
	public final Statement apply(final Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				launchActivity();

				base.evaluate();

				if (!mActivity.isFinishing()) {
					mActivity.finish();
				}
				mActivity = null; // Eager reference kill in case someone leaked our reference.
			}
		};
	}

	private Instrumentation setupInstrumentation() {
		if (mInstrumentation == null) {
			mInstrumentation = InstrumentationRegistry.getInstrumentation();
		}

		return mInstrumentation;
	}

	private void launchActivity() {
		if (mActivity != null) {
			return;
		}

		setupInstrumentation();

		final String targetPackage = mInstrumentation.getTargetContext().getPackageName();
		Intent intent = getLaunchIntent(targetPackage);

		Activity activity = mInstrumentation.startActivitySync(intent);
		if (mActivityClass.isAssignableFrom(activity.getClass())) {
			mActivity = (T) activity;
		}
		else {
			throw new RuntimeException("Got wrong activity type. Expected=" + mActivityClass.getName() + ", got=" + activity.getClass().getName());
		}
		mInstrumentation.waitForIdleSync();
	}

	protected Intent getLaunchIntent(String targetPackage) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClassName(targetPackage, mActivityClass.getName());
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}
}
