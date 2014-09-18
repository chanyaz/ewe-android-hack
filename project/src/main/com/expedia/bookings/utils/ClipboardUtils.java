package com.expedia.bookings.utils;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;

public class ClipboardUtils {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE INTERFACES
	//////////////////////////////////////////////////////////////////////////////////////

	private static interface ClipboardWrapper {
		public void ensureClipboardManager(Context context);

		public boolean hasText();

		public void setText(CharSequence text);

		public void setText(CharSequence label, CharSequence text);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// STATIC CODE
	//////////////////////////////////////////////////////////////////////////////////////

	static {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			sClipboardWrapper = new HoneycombClipboardManager();
		}
		else {
			sClipboardWrapper = new BaseClipboardManager();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private static ClipboardWrapper sClipboardWrapper;

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static boolean hasText(Context context) {
		sClipboardWrapper.ensureClipboardManager(context);
		return sClipboardWrapper.hasText();
	}

	public static void setText(Context context, int resId) {
		setText(context, context.getText(resId));
	}

	public static void setText(Context context, CharSequence text) {
		sClipboardWrapper.ensureClipboardManager(context);
		sClipboardWrapper.setText(text);
	}

	public static void setText(Context context, int labelResId, int textResId) {
		setText(context, context.getText(labelResId), context.getText(textResId));
	}

	public static void setText(Context context, CharSequence label, CharSequence text) {
		sClipboardWrapper.ensureClipboardManager(context);
		sClipboardWrapper.setText(label, text);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CLASSES
	//////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("deprecation")
	private static class BaseClipboardManager implements ClipboardWrapper {
		private android.text.ClipboardManager mClipboardManager;

		public void ensureClipboardManager(Context context) {
			mClipboardManager = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		}

		@Override
		public boolean hasText() {
			return mClipboardManager.hasText();
		}

		public void setText(CharSequence text) {
			mClipboardManager.setText(text);
		}

		public void setText(CharSequence label, CharSequence text) {
			setText(text);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class HoneycombClipboardManager implements ClipboardWrapper {
		private ClipboardManager mClipboardManager;

		public void ensureClipboardManager(Context context) {
			mClipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		}

		@Override
		public boolean hasText() {
			return mClipboardManager.hasPrimaryClip();
		}

		public void setText(CharSequence text) {
			setText(null, text);
		}

		public void setText(CharSequence label, CharSequence text) {
			ClipData clip = ClipData.newPlainText(label, text);
			mClipboardManager.setPrimaryClip(clip);
		}
	}
}
