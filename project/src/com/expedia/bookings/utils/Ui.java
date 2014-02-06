package com.expedia.bookings.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.app.CompatFragmentActivity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewStub;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

/**
 * Adds compatibility library fragment support to Ui.
 */
public class Ui extends com.mobiata.android.util.Ui {

	@SuppressWarnings("unchecked")
	public static <T extends View> T inflateViewStub(Activity activity, int id) {
		ViewStub stub = findView(activity, id);
		if (stub != null) {
			return (T) stub.inflate();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends View> T inflateViewStub(View view, int id) {
		ViewStub stub = findView(view, id);
		if (stub != null) {
			return (T) stub.inflate();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Fragment> T findSupportFragment(CompatFragmentActivity activity, String tag) {
		return (T) activity.getSupportFragmentManager().findFragmentByTag(tag);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Fragment> T findSupportFragment(CompatFragmentActivity activity, int id) {
		return (T) activity.getSupportFragmentManager().findFragmentById(id);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Fragment> T findSupportFragment(Fragment fragment, String tag) {
		T childFragment = (T) fragment.getChildFragmentManager().findFragmentByTag(tag);
		if (childFragment != null) {
			return childFragment;
		}
		return (T) fragment.getFragmentManager().findFragmentByTag(tag);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Fragment> T findSupportFragment(Fragment fragment, int id) {
		T childFragment = (T) fragment.getChildFragmentManager().findFragmentById(id);
		if (childFragment != null) {
			return childFragment;
		}
		return (T) fragment.getFragmentManager().findFragmentById(id);
	}

	/**
	 * Even more convenient method for adding a single Fragment.
	 *
	 * Should only be used if there is a single Fragment that is in android.R.id.content.
	 */
	public static <T extends Fragment> T findOrAddSupportFragment(CompatFragmentActivity activity,
			Class<T> fragmentClass, String tag) {
		return findOrAddSupportFragment(activity, android.R.id.content, fragmentClass, tag);
	}

	/**
	 * Convenience method that either:
	 *
	 * 1. Finds and returns the Fragment if already exists in FragmentManager
	 * 2. Creates and adds the Fragment to containerViewId if doesn't exit
	 *
	 * Either way, it returns the Fragment, ready for use.
	 *
	 * Should only be used if there is a single Fragment that is in android.R.id.content.
	 */
	public static <T extends Fragment> T findOrAddSupportFragment(CompatFragmentActivity activity,
			int containerViewId, Class<T> fragmentClass, String tag) {
		T fragment = findSupportFragment(activity, tag);
		if (fragment == null) {
			try {
				fragment = fragmentClass.newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			activity.getSupportFragmentManager().beginTransaction().add(containerViewId, fragment, tag).commit();
		}
		return fragment;
	}

	/**
	 * Utility method to determine whether or not you can safely touch this Fragment.
	 */
	public static boolean isAdded(Fragment fragment) {
		return fragment != null && fragment.isAdded();
	}

	/**
	 * Convenience method when setting the enabled property of a View that may or may not be null
	 */
	public static void setEnabled(View view, boolean enabled) {
		if (view != null) {
			view.setEnabled(enabled);
		}
	}

	public static int[] measureRatio(int widthMeasureSpec, int heightMeasureSpec, double aspectRatio) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = widthMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : MeasureSpec
				.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = heightMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : MeasureSpec
				.getSize(heightMeasureSpec);

		int measuredWidth;
		int measuredHeight;

		if (heightMode == MeasureSpec.EXACTLY && widthMode == MeasureSpec.EXACTLY) {
			measuredWidth = widthSize;
			measuredHeight = heightSize;

		}
		else if (heightMode == MeasureSpec.EXACTLY) {
			measuredWidth = (int) Math.min(widthSize, heightSize * aspectRatio);
			measuredHeight = (int) (measuredWidth / aspectRatio);

		}
		else if (widthMode == MeasureSpec.EXACTLY) {
			measuredHeight = (int) Math.min(heightSize, widthSize / aspectRatio);
			measuredWidth = (int) (measuredHeight * aspectRatio);

		}
		else {
			if (widthSize > heightSize * aspectRatio) {
				measuredHeight = heightSize;
				measuredWidth = (int) (measuredHeight * aspectRatio);
			}
			else {
				measuredWidth = widthSize;
				measuredHeight = (int) (measuredWidth / aspectRatio);
			}

		}

		return new int[] { measuredWidth, measuredHeight };
	}

	/**
	 * Run code once, on the next layout pass of the given View. This implements the
	 * OnGlobalLayoutListener without having to worry about too much boilerplate.
	 * @param View
	 * @param Runnable
	 */
	public static void runOnNextLayout(final View view, final Runnable runnable) {
		view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				runnable.run();
			}
		});
	}

	/**
	 * Run code once, on the next layout pass of the fragment's getView(). This implements
	 * the OnGlobalLayoutListener without having to worry about too much boilerplate.
	 * @param Support Fragment
	 * @param Runnable
	 */
	public static void runOnNextLayout(Fragment fragment, Runnable runnable) {
		runOnNextLayout(fragment.getView(), runnable);
	}


	//
	// Be careful that the passed Context supports Theme information.
	//
	private static TypedArray obtainTypedArray(Context context, int attr) {
		TypedArray a = context.obtainStyledAttributes(new int[] {attr});
		if (!a.hasValue(0)) {
			throw new RuntimeException("Theme attribute not defined for attr=" + Integer.toHexString(attr));
		}
		return a;
	}

	public static int obtainThemeColor(Context context, int attr) {
		TypedArray a = obtainTypedArray(context, attr);
		int color = a.getColor(0, 0);
		a.recycle();

		return color;
	}

	public static Drawable obtainThemeDrawable(Context context, int attr) {
		TypedArray a = obtainTypedArray(context, attr);
		Drawable drawable = a.getDrawable(0);
		a.recycle();

		return drawable;
	}

	public static int obtainThemeResID(Context context, int attr) {
		TypedArray a = obtainTypedArray(context, attr);
		int resID = a.getResourceId(0, -1);
		a.recycle();

		return resID;
	}
}
