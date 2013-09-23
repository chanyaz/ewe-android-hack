package com.expedia.bookings.widget;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.StrUtils;

public class HotelNeighborhoodLayout extends LinearLayout {

	public interface OnNeighborhoodsChangedListener {
		public void onNeighborhoodsChanged(Set<Integer> neighborhoods, boolean areAllChecked);
	}

	private OnNeighborhoodsChangedListener mListener;
	private Set<Integer> mNeighborhoods;

	public HotelNeighborhoodLayout(Context context) {
		super(context);
	}

	public HotelNeighborhoodLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HotelNeighborhoodLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setOnNeighborhoodsChangedListener(OnNeighborhoodsChangedListener listener) {
		mListener = listener;
	}

	public void setNeighborhoods(HotelSearchResponse response) {
		if (response == null) {
			removeAllViews();
		}
		else {
			setNeighborhoods(response.getProperties());
		}
	}

	public void setNeighborhoods(List<Property> properties) {

		SparseArray<Property> neighborhoods = new SparseArray<Property>();

		if (properties != null) {
			// Find property in each location with the lowest price
			for (Property property : properties) {
				int locationId = property.getLocation().getLocationId();
				Property lowest = neighborhoods.get(locationId);
				if (lowest == null || lowest.getLowestRate().compareTo(property.getLowestRate()) > 0) {
					neighborhoods.put(locationId, property);
				}
			}
		}

		// Sort by location name
		TreeSet<Property> sorted = new TreeSet<Property>(new Comparator<Property>() {
			public int compare(Property lhs, Property rhs) {
				String a = lhs.getLocation() == null || lhs.getLocation().getDescription() == null ? ""
						: lhs.getLocation().getDescription();
				String b = rhs.getLocation() == null || rhs.getLocation().getDescription() == null ? ""
						: rhs.getLocation().getDescription();
				return a.compareTo(b);
			}
		});
		for (int i = 0; i < neighborhoods.size(); i++) {
			sorted.add(neighborhoods.valueAt(i));
		}

		removeAllViews();
		if (sorted.size() == 0) {
			// TODO: DESIGN: Do we want to show "no neighborhoods" or just make this GONE?
		}
		else {
			mNeighborhoods = new HashSet<Integer>();
			for (Property property : sorted) {
				String description = property.getLocation().getDescription();
				Rate rate = property.getLowestRate();
				String hotelPrice = StrUtils.formatHotelPrice(rate.getDisplayRate());
				mNeighborhoods.add(property.getLocation().getLocationId());

				// TODO: This is temporary. We'll want to inflate a layout here once design is ready.
				CheckBox row = new CheckBox(getContext());
				row.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, 56));
				row.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				row.setText(description + " - " + hotelPrice);
				row.setChecked(true);
				row.setTag(property);
				row.setOnCheckedChangeListener(mCheckedChangedListener);
				addView(row);
			}
		}
	}

	private final OnCheckedChangeListener mCheckedChangedListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Property property = (Property) buttonView.getTag();
			int locationId = property.getLocation().getLocationId();
			if (isChecked) {
				mNeighborhoods.add(locationId);
			}
			else {
				mNeighborhoods.remove(locationId);
			}
			triggerOnNeighborhoodsChanged();
		}
	};

	private void triggerOnNeighborhoodsChanged() {
		mListener.onNeighborhoodsChanged(mNeighborhoods, areAllNeighborhoodsChecked());
	}

	private boolean areAllNeighborhoodsChecked() {
		for (int i = 0; i < getChildCount(); i++) {
			CheckBox row = (CheckBox) getChildAt(i);
			if (!row.isChecked()) {
				return false;
			}
		}
		return true;
	}
}
