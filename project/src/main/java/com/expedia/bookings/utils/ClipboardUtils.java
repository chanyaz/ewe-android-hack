package com.expedia.bookings.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardUtils {

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static boolean hasText(Context context) {
		ClipboardManager board = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		return board.hasPrimaryClip();
	}

	public static void setText(Context context, int resId) {
		setText(context, context.getText(resId));
	}

	public static void setText(Context context, CharSequence text) {
		setText(context, null, text);
	}

	public static void setText(Context context, int labelResId, int textResId) {
		setText(context, context.getText(labelResId), context.getText(textResId));
	}

	public static void setText(Context context, CharSequence label, CharSequence text) {
		ClipboardManager board = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText(label, text);
		board.setPrimaryClip(clip);
	}

	public static String getText(Context context) {
		ClipboardManager board = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		return board.getPrimaryClip().getItemAt(0).getText().toString();
	}
}
