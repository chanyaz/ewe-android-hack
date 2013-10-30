package com.expedia.bookings.utils;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.mobiata.android.util.ViewUtils;

public class HotelUtils {

	/**
	 * Tries to return the best "room" picture, but falls back to property
	 * images/thumbnails if none exists.  May return null if all fails.
	 */
	public static Media getRoomMedia(Property property, Rate rate) {
		if (rate != null && rate.getThumbnail() != null) {
			return rate.getThumbnail();
		}

		if (property != null) {
			if (property.getMediaCount() > 0) {
				return property.getMedia(0);
			}
			else {
				return property.getThumbnail();
			}
		}

		return null;
	}

	/**
	 * Sets up the "checkmark" action bar item
	 */
	public static Button setupActionBarCheckmark(final SherlockFragmentActivity activity, final MenuItem menuItem,
			boolean enabled) {
		Button tv = (Button) activity.getLayoutInflater().inflate(R.layout.actionbar_checkmark_item, null);
		ViewUtils.setAllCaps(tv);

		if (enabled) {
			tv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					activity.onOptionsItemSelected(menuItem);
				}
			});
		}
		else {
			tv.setClickable(false);
			tv.setFocusable(false);
			tv.setTextColor(activity.getResources().getColor(R.color.actionbar_text_disabled));
			tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_bar_checkmark_disabled, 0, 0, 0);
		}

		menuItem.setActionView(tv);

		return tv;
	}
}
