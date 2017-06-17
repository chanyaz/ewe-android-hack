package com.expedia.bookings.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.hotel.PriceRange;
import com.squareup.phrase.Phrase;

public class HotelPriceRangeSeekBar extends FilterRangeSeekBar {
	private PriceRange priceRange;

	private String a11yMinPriceText;
	private String a11yMaxPriceText;

	public HotelPriceRangeSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		ViewCompat.setAccessibilityDelegate(this, new RangeBarAllyDelegate());
		//TODO Hotels should also use the same accessibility, if Accessibility team approves
		mTouchHelper = null;
	}

	public void setPriceRange(PriceRange range) {
		this.priceRange = range;
		setUpperLimit(priceRange.getNotches());
		a11yMinPriceText = priceRange.formatValue(getMinValue());
		a11yMaxPriceText = priceRange.formatValue(getMaxValue());
	}

	private String getContentDesc() {
		if (a11yMinPriceText != null && a11yMaxPriceText != null) {
			return Phrase.from(getContext().getString(R.string.price_range_filter_cont_desc_TEMPLATE))
				.put("min_price", a11yMinPriceText)
				.put("max_price", a11yMaxPriceText)
				.format().toString();
		}
		return "";
	}

	private class RangeBarAllyDelegate extends AccessibilityDelegateCompat {

		@Override
		public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
			super.onInitializeAccessibilityNodeInfo(host, info);

			info.addAction(new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.price_range_increase_min_price, getContext().getString(R.string.increase_price_minimum_filter)));
			info.addAction(new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.price_range_decrease_min_price, getContext().getString(R.string.decrease_price_minimum_filter)));

			info.addAction(new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.price_range_increase_max_price, getContext().getString(R.string.increase_price_maximum_filter)));
			info.addAction(new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.price_range_decrease_max_price, getContext().getString(R.string.decrease_price_maximum_filter)));

			if (a11yMinPriceText != null && a11yMaxPriceText != null) {
				String infoText = Phrase.from(getContext().getString(R.string.price_range_filter_cont_desc_TEMPLATE))
					.put("min_price", a11yMinPriceText)
					.put("max_price", a11yMaxPriceText)
					.format().toString();
				info.setText(infoText);
			}
		}

		@Override
		public boolean performAccessibilityAction(View host, int action, Bundle args) {
			FilterRangeSeekBar seekBar = (FilterRangeSeekBar) host;
			switch (action) {
			/*case R.id.price_range_increase_min_price:
				seekBar.setMinValue(seekBar.getMinValue() + 1);
				a11yMinPriceText = priceRange.formatValue(seekBar.getMinValue());
				seekBar.listener.onRangeSeekBarValuesChanged(seekBar, seekBar.getMinValue(), seekBar.getMaxValue(), Thumb.MIN);
				seekBar.setContentDescription(getContentDesc());
				return true;
			case R.id.price_range_decrease_min_price:
				seekBar.setMinValue(seekBar.getMinValue() - 1);
				a11yMinPriceText = priceRange.formatValue(seekBar.getMinValue());
				seekBar.listener.onRangeSeekBarValuesChanged(seekBar, seekBar.getMinValue(), seekBar.getMaxValue(), Thumb.MIN);
				seekBar.setContentDescription(getContentDesc());
				return true;
			case R.id.price_range_increase_max_price:
				seekBar.setMaxValue(seekBar.getMaxValue() + 1);
				a11yMaxPriceText = priceRange.formatValue(seekBar.getMaxValue());
				seekBar.listener.onRangeSeekBarValuesChanged(seekBar, seekBar.getMinValue(), seekBar.getMaxValue(), Thumb.MAX);
				seekBar.setContentDescription(getContentDesc());
				return true;
			case R.id.price_range_decrease_max_price:
				seekBar.setMaxValue(seekBar.getMaxValue() - 1);
				a11yMaxPriceText = priceRange.formatValue(seekBar.getMaxValue());
				seekBar.listener.onRangeSeekBarValuesChanged(seekBar, seekBar.getMinValue(), seekBar.getMaxValue(), Thumb.MAX);
				seekBar.setContentDescription(getContentDesc());
				return true;*/
			default:
				return super.performAccessibilityAction(host, action, args);
			}
		}
	}
}
