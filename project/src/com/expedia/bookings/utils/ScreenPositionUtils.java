package com.expedia.bookings.utils;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.mobiata.android.Log;

/**
 * This class helps us figure out where things are on screen,
 * and helps us to figure out how that translates to local coordinates.
 * 
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ScreenPositionUtils {

	/**
	 * This will return the global position/size rect of the provided view.
	 * 
	 * Keep in mind that the returned position will include things like the actionbar height,
	 * so typically a call to translateGlobalPositionToLocalPosition is required to get back
	 * useful coordinates.
	 * 
	 * @param view
	 * @return
	 */
	public static Rect getGlobalScreenPosition(View view) {
		return getGlobalScreenPosition(view, false, false);
	}

	/**
	 * Sometimes views are translated, but you want to know where they are in layout.
	 * 
	 * This will traverse the supplied view's parent tree and determine global position,
	 * as though the view (and all of its parents) are NOT translated at all.
	 * 
	 * @param view
	 * @return
	 */
	public static Rect getGlobalScreenPositionWithoutTranslations(View view) {
		return getGlobalScreenPosition(view, true, true);
	}

	/**
	 * This will return the global position/size rect of the provided view.
	 * 
	 * Keep in mind that the returned position will include things like the actionbar height,
	 * so typically a call to translateGlobalPositionToLocalPosition is required to get back
	 * useful coordinates.
	 * 
	 * If we would like to get the actual position in layout (without translations) we can set the 
	 * values of offsetTranslationX and offsetTranslationY to true.
	 * 
	 * @param view - view to get global screen position for
	 * @param offsetTranslationX - if true, we get the views position disregarding any translationX for the whole view tree
	 * @param offsetTranslationY - if true, we get the views position disregarding any translationY for the whole view tree
	 * @return
	 */
	public static Rect getGlobalScreenPosition(View view, boolean offsetTranslationX, boolean offsetTranslationY) {
		int[] currentGlobalLocation = new int[2];
		view.getLocationOnScreen(currentGlobalLocation);

		Rect retRect = new Rect();
		retRect.left = currentGlobalLocation[0];
		retRect.right = retRect.left + view.getWidth();
		retRect.top = currentGlobalLocation[1];
		retRect.bottom = retRect.top + view.getHeight();

		if (offsetTranslationX || offsetTranslationY) {
			int totalTranslationX = 0;
			int totalTranslationY = 0;

			View v = view;
			while (v != null && v instanceof View) {
				totalTranslationX += v.getTranslationX();
				totalTranslationY += v.getTranslationY();
				if (v.getParent() instanceof View) {
					v = (View) v.getParent();
				}
				else {
					v = null;
				}
			}

			if (offsetTranslationX) {
				retRect.left -= totalTranslationX;
				retRect.right -= totalTranslationX;
			}

			if (offsetTranslationY) {
				retRect.top -= totalTranslationY;
				retRect.bottom -= totalTranslationY;
			}
		}

		Log.d("ScreenPositionUtils - getGlobalScreenPosition() - locOnScreen[0]" + currentGlobalLocation[0]
				+ " locOnScreen[1]:" + currentGlobalLocation[1] + " output:" + retRect + " offsetTranslationX:"
				+ offsetTranslationX + " offsetTranslationY:" + offsetTranslationY);

		return retRect;
	}

	/**
	 * This method returns the local position of global coordinates as they are generated by
	 * our getGlobalScreenPosition methods.
	 * 
	 * Typically global coordinates are not useful because they take into account things like
	 * the actionbar.This makes those global coordinates useful by spitting out a position
	 * in relation to the passed in ViewGroup vg.
	 * 
	 * @param globalPos - The global position
	 * @param vg - the ViewGroup we want our return value to be relative to.
	 * @return
	 */
	public static Rect translateGlobalPositionToLocalPosition(Rect globalPos, ViewGroup vg) {
		int[] vgLocation = new int[2];
		vg.getLocationOnScreen(vgLocation);

		Rect rect = new Rect();
		rect.left = globalPos.left - vgLocation[0];
		rect.top = globalPos.top - vgLocation[1];
		rect.right = rect.left + (globalPos.right - globalPos.left);
		rect.bottom = rect.top + (globalPos.bottom - globalPos.top);

		Log.d("ScreenPositionUtils - translateGlobalPositionToLocalPosition() - globalPos:" + globalPos + " output:"
				+ rect + " vg.locOnScreen[0]" + vgLocation[0] + " vg.locOnScreen[1]:" + vgLocation[1]);

		return rect;
	}

}
