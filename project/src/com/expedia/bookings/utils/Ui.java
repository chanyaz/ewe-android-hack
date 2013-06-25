package com.expedia.bookings.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.app.CompatFragmentActivity;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;

/**
 * Adds compatibility library fragment support to Ui.
 */
public class Ui extends com.mobiata.android.util.Ui {

	@SuppressWarnings("unchecked")
	public static <T extends Fragment> T findSupportFragment(CompatFragmentActivity activity, String tag) {
		return (T) activity.getSupportFragmentManager().findFragmentByTag(tag);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Fragment> T findSupportFragment(CompatFragmentActivity activity, int id) {
		return (T) activity.getSupportFragmentManager().findFragmentById(id);
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

	public static int obtainStyledColor(Context context, int defColor, int... attrs) {
		if (attrs.length < 2) {
			return defColor;
		}

		TypedArray a = obtainTypedArray(context, attrs);
		int color = a.getColor(0, defColor);
		a.recycle();

		return color;
	}

	// Do not expose this method
	private static TypedArray obtainTypedArray(Context context, int... attrs) {
		int primaryId = 0;

		// Fetch the first attr out of the Theme
		TypedArray a = context.obtainStyledAttributes(new int[]{attrs[0]});
		primaryId = a.getResourceId(0, 0);
		a.recycle();

		// Now that we have a style to chase, we will follow it
		for (int i = 1; i < attrs.length - 1; i++) {
			a = context.obtainStyledAttributes(primaryId, new int[]{attrs[i]});
			primaryId = a.getResourceId(0, 0);
			a.recycle();
		}

		// Fetch the last guy in the chain
		a = context.obtainStyledAttributes(primaryId, new int[]{attrs[attrs.length - 1]});
		// Don't recycle, caller will be responsible
		return a;
	}
}
