package com.expedia.bookings.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;

/**
 * If we translate this container in the X direction, the child views appear fixed in place.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FixedTranslationFrameLayout extends FrameLayoutTouchController {

	public FixedTranslationFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * When we translate this container on the x axis, we do an equal
	 * oposite translation on the x axis for its children, causing the
	 * container bounds to be changed, but with the appearance that the
	 * container content has remained in place.
	 * @param translationX
	 */
	@Override
	public void setTranslationX(float translationX) {
		for (int i = 0; i < this.getChildCount(); i++) {
			getChildAt(i).setTranslationX(-translationX);
		}
		super.setTranslationX(translationX);
	}

	/**
	 * This method has been overriden by FixedTranslationFrameLayout such that only
	 * the child views will have their layerTypes changed.
	 */
	@Override
	public void setLayerType(int layerType, Paint paint) {
		for (int i = 0; i < this.getChildCount(); i++) {
			getChildAt(i).setLayerType(layerType, null);
		}

	}

}
