package com.expedia.bookings.utils;

import android.content.Context;
import android.graphics.Color;

import com.expedia.account.graphics.ArrowXDrawable;

public class ArrowXDrawableUtil {

	public static ArrowXDrawable getNavigationIconDrawable(Context context, ArrowDrawableType arrowDrawableType) {
		float upButtonSizePx = 24 * context.getResources().getDisplayMetrics().density;
		ArrowXDrawable mArrowXDrawable = new ArrowXDrawable(upButtonSizePx);
		mArrowXDrawable.setStrokeColor(Color.WHITE);
		mArrowXDrawable.setParameter(arrowDrawableType.getType());
		mArrowXDrawable.setFlip(true);
		return mArrowXDrawable;
	}

	public enum ArrowDrawableType {
		BACK(0),
		CLOSE(1);

		private final int type;

		ArrowDrawableType(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}
	}
}
