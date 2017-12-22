package com.expedia.bookings.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

/**
 * Adds compatibility library fragment support to Ui.
 */
public class Ui extends com.mobiata.android.util.Ui {

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

	/**
	 * Even more convenient method for adding a single Fragment.
	 * <p/>
	 * Should only be used if there is a single Fragment that is in android.R.id.content.
	 */
	public static <T extends Fragment> T findOrAddSupportFragment(FragmentActivity activity, Class<T> fragmentClass,
		String tag) {
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
	public static <T extends Fragment> T findOrAddSupportFragment(FragmentActivity activity, int containerViewId,
		Class<T> fragmentClass, String tag) {
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

	/**
	 * Run code once, on the next layout pass of the given View. This implements the
	 * OnGlobalLayoutListener without having to worry about too much boilerplate.
	 */
	public static void runOnNextLayout(final View view, final Runnable runnable) {
		view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				runnable.run();
			}
		});
	}

	//
	// Be careful that the passed Context supports Theme information.
	//
	private static TypedArray obtainTypedArray(Context context, int attr) {
		TypedArray a = context.obtainStyledAttributes(new int[] { attr });
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

	public static void showTransparentStatusBar(Context ctx) {
		Window w = ((Activity) ctx).getWindow();
		w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
			WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
	}

	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	/**
	 * Sets the color for status bar when status bar is transparent and also give padding to container and toolbar.
	 *
	 * @param toolbar   toolbar of view
	 * @param viewGroup main container of layout not root container
	 * @param color     of status bar
	 */
	public static View setUpStatusBar(Context context, View toolbar,
		ViewGroup viewGroup, int color) {
		View v = new View(context);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT);
		int statusBarHeight = getStatusBarHeight(context);
		lp.height = statusBarHeight;
		v.setId(R.id.expedia_fake_status_bar);
		v.setLayoutParams(lp);
		v.setBackgroundColor(color);
		if (toolbar != null) {
			toolbar.setPadding(0, statusBarHeight, 0, 0);
		}
		int toolbarSize = getToolbarSize(context);
		if (viewGroup != null) {
			viewGroup.setPadding(0, toolbarSize + statusBarHeight, 0, 0);
		}
		return v;
	}

	// tweak logic to work when there's a toolbar + tabs
	public static View setUpStatusBarWithTabs(Context context, ViewGroup viewGroup, int color) {
		View v = new View(context);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT);
		int statusBarHeight = getStatusBarHeight(context);
		lp.height = statusBarHeight;
		v.setLayoutParams(lp);
		v.setBackgroundColor(color);
		if (viewGroup != null) {
			viewGroup.setPadding(0, statusBarHeight, 0, 0);
		}
		return v;
	}

	public static int getToolbarSize(Context context) {
		TypedValue typedValue = new TypedValue();
		int[] textSizeAttr = new int[] { android.R.attr.actionBarSize };
		TypedArray a = context.obtainStyledAttributes(typedValue.data, textSizeAttr);
		return (int) a.getDimension(0, 44);
	}

	public static int toolbarSizeWithStatusBar(Context context) {
		int toolbarSize = getToolbarSize(context);
		int statusBarHeight = getStatusBarHeight(context);

		return toolbarSize + statusBarHeight;
	}

	public static void setTextStyleBoldText(Spannable stringToSpan, int color, int startSpan, int endSpan) {
		setTextStyle(stringToSpan, color, startSpan, endSpan, true);
	}

	public static void setTextStyleNormalText(Spannable stringToSpan, int color, int startSpan, int endSpan) {
		setTextStyle(stringToSpan, color, startSpan, endSpan, false);
	}

	private static void setTextStyle(Spannable stringToSpan, int color, int startSpan, int endSpan, boolean isBold) {
		stringToSpan.setSpan(new ForegroundColorSpan(color),
			startSpan, endSpan,
			Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (isBold) {
			stringToSpan.setSpan(new StyleSpan(Typeface.BOLD), startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}
}
