package com.mobiata.android.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Job-simplifying UI tools.
 *
 * The find* methods reduce the need for casting in code - just kind of handy.
 *
 * The set* methods allow you to quickly set a single property on a View.
 * Do NOT use them if you're going to set multiple properties of a View -
 * it's much faster in those cases to just call findView() and then manipulate
 * from there!
 *
 * Also includes some simple Toast utils, for debugging purposes.
 *
 * Based on Code 42's utilities that they showed off at AUG.MN
 */
public class Ui {

	// Find Views

	@SuppressWarnings("unchecked")
	public static <T extends View> T findView(Activity activity, int id) {
		return (T) activity.findViewById(id);
	}

	@SuppressWarnings("unchecked")
	public static <T extends View> T findView(View view, int id) {
		return (T) view.findViewById(id);
	}

	@SuppressWarnings("unchecked")
	public static <T extends View> T findView(Dialog dialog, int id) {
		return (T) dialog.findViewById(id);
	}

	// Inflate Views

	/**
	 * Just like android.view.LayoutInflater.inflate(int resource, ViewGroup root), but with generics.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends View> T inflate(LayoutInflater inflater, int id, ViewGroup root) {
		return (T) inflater.inflate(id, root);
	}

	/**
	 * Just like android.view.LayoutInflater.inflate(int id, ViewGroup parent, boolean attachToRoot),
	 * but with generics.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends View> T inflate(LayoutInflater inflater, int id, ViewGroup parent, boolean attachToRoot) {
		return (T) inflater.inflate(id, parent, attachToRoot);
	}

	/**
	 * Just like android.view.LayoutInflater.inflate(int resource, ViewGroup root), but with generics
	 */
	@SuppressWarnings("unchecked")
	public static <T extends View> T inflate(Context context, int id, ViewGroup root) {
		return inflate(LayoutInflater.from(context), id, root);
	}

	/**
	 * Just like android.view.LayoutInflater.inflate(int resource, ViewGroup root), but with generics
	 */
	@SuppressWarnings("unchecked")
	public static <T extends View> T inflate(android.support.v4.app.Fragment fragment, int id, ViewGroup root) {
		return inflate(LayoutInflater.from(fragment.getActivity()), id, root);
	}

	/**
	 * Just like android.view.LayoutInflater.inflate(int id, ViewGroup parent, boolean attachToRoot),
	 * but with generics.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends View> T inflate(int id, ViewGroup parent, boolean attachToRoot) {
		return inflate(LayoutInflater.from(parent.getContext()), id, parent, attachToRoot);
	}

	@SuppressWarnings("unchecked")
	public static <T extends android.support.v4.app.Fragment> T findSupportFragment(FragmentActivity activity,
			String tag) {
		return (T) activity.getSupportFragmentManager().findFragmentByTag(tag);
	}

	// Find Fragment Listeners

	public static <T> T findFragmentListener(android.support.v4.app.Fragment fragment, Class<T> clz) {
		return findFragmentListener(fragment, clz, true);
	}

	@SuppressWarnings("unchecked")
	/**
	 * Finds a Fragment listener.  Prioritizes the parent Fragment before the grandParent fragment, which in turn has priority over Activity.
	 */
	public static <T> T findFragmentListener(android.support.v4.app.Fragment fragment, Class<T> clz, boolean mustBeFound) {
		android.support.v4.app.Fragment parentFragment = fragment.getParentFragment();
		if (clz.isInstance(parentFragment)) {
			return (T) parentFragment;
		}

		if (parentFragment != null) {
			android.support.v4.app.Fragment grandParentFragment = parentFragment.getParentFragment();
			if (clz.isInstance(grandParentFragment)) {
				return (T) grandParentFragment;
			}
		}

		Activity activity = fragment.getActivity();
		if (clz.isInstance(activity)) {
			return (T) activity;
		}

		if (mustBeFound) {
			throw new RuntimeException("Fragment (" + fragment.getClass().getName()
					+ ") could not find parent Fragment/Activity that implements listener (" + clz.getName() + ")");
		}

		return null;
	}

	public static void setOnClickListener(Activity activity, int id, OnClickListener onClickListener) {
		View v = findView(activity, id);
		v.setOnClickListener(onClickListener);
	}

	public static void setOnClickListener(View view, int id, OnClickListener onClickListener) {
		View v = findView(view, id);
		v.setOnClickListener(onClickListener);
	}

	/**
	 * Fast way to set text of a TextView.
	 *
	 * WARNING: If you're going to repeatedly manipulate a TextView, DO NOT
	 * USE THIS METHOD.  Instead, cache the TextView and manipulate it in
	 * code.  This method is only when you want to set the text of
	 * a TextView a single time.
	 *
	 * @param activity the activity
	 * @param id the id of a TextView
	 */
	public static void setText(Activity activity, int id, CharSequence text) {
		TextView tv = findView(activity, id);
		tv.setText(text);
	}

	/**
	 * @see #setText(Activity activity, int id, CharSequence text)
	 */
	public static void setText(Activity activity, int id, int textResId) {
		TextView tv = findView(activity, id);
		tv.setText(textResId);
	}

	/**
	 * @see #setText(Activity activity, int id, CharSequence text)
	 */
	public static void setText(View view, int id, CharSequence text) {
		TextView tv = findView(view, id);
		tv.setText(text);
	}

	/**
	 * @see #setText(Activity activity, int id, CharSequence text)
	 */
	public static void setText(View view, int id, int textResId) {
		TextView tv = findView(view, id);
		tv.setText(textResId);
	}

	public static void setImageResource(View view, int id, int resId) {
		ImageView imageView = findView(view, id);
		imageView.setImageResource(resId);
	}

	public static void showToast(Context context, CharSequence text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static void hideKeyboard(Activity activity) {
		hideKeyboard(activity, 0);
	}

	public static void hideKeyboard(View view) {
		hideKeyboard(view, 0);
	}

	public static void hideKeyboard(Activity activity, int flags) {
		hideKeyboard(activity == null ? null : activity.getCurrentFocus(), flags);
	}

	public static void hideKeyboard(View focused, int flags) {
		if (focused != null) {
			Context context = focused.getContext();
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(focused.getWindowToken(), flags);
		}
	}

	public static void showKeyboard(View view, ResultReceiver resultReceiver) {
		Context context = view.getContext();
		Configuration config = context.getResources().getConfiguration();
		if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
			// Show soft keyboard if physical keyboard is not open
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (resultReceiver != null) {
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT, resultReceiver);
			}
			else {
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
			}
		}
	}

	/**
	 * Run code once, on the next layout pass of the given View. This implements the
	 * OnGlobalLayoutListener without having to worry about too much boilerplate.
	 *
	 * @param View
	 * @param Runnable
	 */
	public static void runOnNextLayout(final View view, final Runnable runnable) {
		view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			public void onGlobalLayout() {
				view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				runnable.run();
			}
		});
	}

}
