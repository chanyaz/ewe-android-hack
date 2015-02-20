package com.expedia.bookings.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

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

	@SuppressWarnings("unchecked")
	public static <T extends android.support.v4.app.Fragment> T findChildSupportFragment(
		android.support.v4.app.Fragment fragment, String tag) {
		return (T) fragment.getChildFragmentManager().findFragmentByTag(tag);
	}

	@SuppressWarnings("unchecked")
	public static <T extends android.support.v4.app.Fragment> T findChildSupportFragment(
		android.support.v4.app.Fragment fragment, int id) {
		return (T) fragment.getChildFragmentManager().findFragmentById(id);
	}

	/**
	 * Even more convenient method for adding a single Fragment.
	 * <p/>
	 * Should only be used if there is a single Fragment that is in android.R.id.content.
	 */
	public static <T extends Fragment> T findOrAddSupportFragment(FragmentActivity activity, Class<T> fragmentClass, String tag) {
		return findOrAddSupportFragment(activity, android.R.id.content, fragmentClass, tag);
	}

	/**
	 * Convenience method that either:
	 * <p/>
	 * 1. Finds and returns the Fragment if already exists in FragmentManager
	 * 2. Creates and adds the Fragment to containerViewId if doesn't exit
	 * <p/>
	 * Either way, it returns the Fragment, ready for use.
	 * <p/>
	 * Should only be used if there is a single Fragment that is in android.R.id.content.
	 */
	public static <T extends Fragment> T findOrAddSupportFragment(FragmentActivity activity, int containerViewId, Class<T> fragmentClass, String tag) {
		T fragment = findSupportFragment(activity, tag);
		if (fragment == null) {
			try {
				fragment = fragmentClass.newInstance();
			}
			catch (Exception e) {
				Log.e("Caught exception trying to newInstance a fragment", e);
				throw new RuntimeException(e);
			}
			FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
			if (containerViewId == View.NO_ID) {
				transaction.add(fragment, tag);
			}
			else {
				transaction.add(containerViewId, fragment, tag);
			}
			transaction.commit();
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
	 * Utility method to get the application instance from the given context
	 */
	public static ExpediaBookingApp getApplication(Context context) {
		return (ExpediaBookingApp) context.getApplicationContext();
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

		return new int[] {measuredWidth, measuredHeight};
	}

	/**
	 * Run code once, on the next layout pass of the given View. This implements the
	 * OnGlobalLayoutListener without having to worry about too much boilerplate.
	 *
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
	 *
	 * @param Support  Fragment
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

	public static void addViewTreeObserverLogger(View v) {
		ViewTreeObserver vto = v.getViewTreeObserver();
		if (vto == null) {
			return;
		}

		vto.addOnGlobalFocusChangeListener(Api8ViewTreeObserverLogger.getInstance());
		vto.addOnGlobalLayoutListener(Api8ViewTreeObserverLogger.getInstance());
		vto.addOnPreDrawListener(Api8ViewTreeObserverLogger.getInstance());
		vto.addOnScrollChangedListener(Api8ViewTreeObserverLogger.getInstance());
		vto.addOnTouchModeChangeListener(Api8ViewTreeObserverLogger.getInstance());

		if (AndroidUtils.getSdkVersion() >= 16) {
			vto.addOnDrawListener(Api16ViewTreeObserverLogger.getInstance());
		}

		if (AndroidUtils.getSdkVersion() >= 18) {
			vto.addOnWindowAttachListener(Api18ViewTreeObserverLogger.getInstance());
			vto.addOnWindowFocusChangeListener(Api18ViewTreeObserverLogger.getInstance());
		}
	}

	public static void removeOnGlobalLayoutListener(View v, OnGlobalLayoutListener listener) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
		}
		else {
			v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
		}
	}

	public static void removeViewTreeObserverLogger(View v) {
		ViewTreeObserver vto = v.getViewTreeObserver();
		if (vto == null) {
			return;
		}

		vto.removeOnGlobalFocusChangeListener(Api8ViewTreeObserverLogger.getInstance());
		vto.removeOnGlobalLayoutListener(Api8ViewTreeObserverLogger.getInstance());
		vto.removeOnPreDrawListener(Api8ViewTreeObserverLogger.getInstance());
		vto.removeOnScrollChangedListener(Api8ViewTreeObserverLogger.getInstance());
		vto.removeOnTouchModeChangeListener(Api8ViewTreeObserverLogger.getInstance());

		if (AndroidUtils.getSdkVersion() >= 16) {
			vto.removeOnDrawListener(Api16ViewTreeObserverLogger.getInstance());
		}

		if (AndroidUtils.getSdkVersion() >= 18) {
			vto.removeOnWindowAttachListener(Api18ViewTreeObserverLogger.getInstance());
			vto.removeOnWindowFocusChangeListener(Api18ViewTreeObserverLogger.getInstance());
		}
	}

	private static class Api8ViewTreeObserverLogger implements ViewTreeObserver.OnGlobalFocusChangeListener,
		ViewTreeObserver.OnGlobalLayoutListener, ViewTreeObserver.OnPreDrawListener,
		ViewTreeObserver.OnScrollChangedListener, ViewTreeObserver.OnTouchModeChangeListener {

		private static final String TAG = "VTOEventLogger";

		private static class Holder {
			private static final Api8ViewTreeObserverLogger INSTANCE = new Api8ViewTreeObserverLogger();
		}

		private static Api8ViewTreeObserverLogger getInstance() {
			return Holder.INSTANCE;
		}

		@Override
		public void onGlobalFocusChanged(View oldFocus, View newFocus) {
			Log.v(TAG, "onGlobalFocusChanged");
		}

		@Override
		public void onGlobalLayout() {
			Log.v(TAG, "onGlobalLayout");
		}

		@Override
		public boolean onPreDraw() {
			Log.v(TAG, "onPreDraw");
			return true;
		}

		@Override
		public void onScrollChanged() {
			Log.v(TAG, "onScrollChanged");
		}

		@Override
		public void onTouchModeChanged(boolean isInTouchMode) {
			Log.v(TAG, "onTouchModeChanged(isInTouchMode=" + isInTouchMode + ")");
		}
	}

	private static class Api16ViewTreeObserverLogger implements ViewTreeObserver.OnDrawListener {
		private static final String TAG = "VTOEventLogger";

		private static class Holder {
			private static final Api16ViewTreeObserverLogger INSTANCE = new Api16ViewTreeObserverLogger();
		}

		private static Api16ViewTreeObserverLogger getInstance() {
			return Holder.INSTANCE;
		}

		@Override
		public void onDraw() {
			Log.v(TAG, "onDraw");
		}
	}

	private static class Api18ViewTreeObserverLogger implements ViewTreeObserver.OnWindowAttachListener, ViewTreeObserver.OnWindowFocusChangeListener {
		private static final String TAG = "VTOEventLogger";

		private static class Holder {
			private static final Api18ViewTreeObserverLogger INSTANCE = new Api18ViewTreeObserverLogger();
		}

		private static Api18ViewTreeObserverLogger getInstance() {
			return Holder.INSTANCE;
		}

		@Override
		public void onWindowAttached() {
			Log.v(TAG, "onWindowAttached");
		}

		@Override
		public void onWindowDetached() {
			Log.v(TAG, "onWindowDetached");
		}

		@Override
		public void onWindowFocusChanged(boolean hasFocus) {
			Log.v(TAG, "onWindowFocusChanged(hasFocus=" + hasFocus + ")");
		}
	}

	/**
	 * Convenience method to replace otherwise clunky code. Returns the Y coordinate of the
	 * absolute screen position of the passed view.
	 *
	 * @param view
	 * @return
	 */
	public static int getScreenLocationY(View view) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		return location[1];
	}

	public static Point getScreenSize(Context context) {
		Point screen = AndroidUtils.getDisplaySize(context);
		return screen;
	}

	public static Point getPortraitScreenSize(Context context) {
		Point screen = getScreenSize(context);

		if (screen.y < screen.x) {
			screen.set(screen.y, screen.x);
		}

		return screen;
	}

	public static Point getLandscapeScreenSize(Context context) {
		Point screen = getScreenSize(context);

		if (screen.y > screen.x) {
			screen.set(screen.y, screen.x);
		}

		return screen;
	}

	public static int getNavigationBarHeight(Context context) {
		Resources resources = context.getResources();
		int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0 && resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			return resources.getDimensionPixelSize(resourceId);
		}
		return 0;
	}

	public static Bitmap createBitmapFromView(View v) {
		Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
		v.draw(canvas);

		return bitmap;
	}
}
