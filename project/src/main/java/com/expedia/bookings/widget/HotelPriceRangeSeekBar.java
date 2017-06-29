package com.expedia.bookings.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;

public class HotelPriceRangeSeekBar extends FilterRangeSeekBar {
	public HotelPriceRangeSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		mTouchHelper = new HotelPriceFilterSeekBarTouchHelper(this);
		ViewCompat.setAccessibilityDelegate(this, mTouchHelper);

		a11yStartName = context.getString(R.string.price_range_minimum);
		a11yEndName = context.getString(R.string.price_range_maximum);
	}

	class HotelPriceFilterSeekBarTouchHelper extends FilterRangeSeekBarTouchHelper {
		HotelPriceFilterSeekBarTouchHelper(View forView) {
			super(forView);
		}

		@Override
		public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
			super.onInitializeAccessibilityNodeInfo(host, info);

			info.addAction(
				new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.price_range_increase_min_price,
					getContext().getString(R.string.increase_price_minimum_filter)));
			info.addAction(
				new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.price_range_decrease_min_price,
					getContext().getString(R.string.decrease_price_minimum_filter)));

			info.addAction(
				new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.price_range_increase_max_price,
					getContext().getString(R.string.increase_price_maximum_filter)));
			info.addAction(
				new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.price_range_decrease_max_price,
					getContext().getString(R.string.decrease_price_maximum_filter)));
		}

		@Override
		public boolean performAccessibilityAction(View host, int action, Bundle args) {
			switch (action) {
			case R.id.price_range_increase_min_price:
			case R.id.price_range_increase_max_price:
				updateValueBy(1, getThumb());
				return true;
			case R.id.price_range_decrease_min_price:
			case R.id.price_range_decrease_max_price:
				updateValueBy(-1, getThumb());
				return true;
			default:
				return super.performAccessibilityAction(host, action, args);
			}
		}
	}
}
