package com.expedia.bookings.widget;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.LinearLayout;

import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.widget.CheckBoxFilterWidget.OnCheckedChangeListener;

public class HotelNeighborhoodLayout extends LinearLayout {

	public interface OnNeighborhoodsChangedListener {
		/**
		 * If onNeighborhoodsChanged is called with *null*, that means do not filter by
		 * neighborhood (i.e. all neighborhoods are selected).
		 */
		public void onNeighborhoodsChanged(Set<Integer> neighborhoods);
	}

	private OnNeighborhoodsChangedListener mListener;
	private Set<Integer> mAllNeighborhoods;
	private Set<Integer> mCheckedNeighborhoods;

	public HotelNeighborhoodLayout(Context context) {
		super(context);
	}

	public HotelNeighborhoodLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(11)
	public HotelNeighborhoodLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setOnNeighborhoodsChangedListener(OnNeighborhoodsChangedListener listener) {
		mListener = listener;
	}

	public void setNeighborhoods(HotelSearchResponse response, HotelFilter filter) {
		mCheckedNeighborhoods = filter.getNeighborhoods();
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
			// Prepopulate mCheckedNeighborhoods with all neighborhoods checked
			if (mCheckedNeighborhoods == null) {
				mCheckedNeighborhoods = new HashSet<Integer>();
				for (Property property : sorted) {
					mCheckedNeighborhoods.add(property.getLocation().getLocationId());
				}
			}

			mAllNeighborhoods = new HashSet<Integer>();
			for (Property property : sorted) {
				int locationId = property.getLocation().getLocationId();
				boolean isChecked = mCheckedNeighborhoods.contains(locationId);

				mAllNeighborhoods.add(locationId);

				CheckBoxFilterWidget filterWidget = new CheckBoxFilterWidget(getContext());
				filterWidget.bindHotel(property);
				filterWidget.setTag(property);
				filterWidget.setChecked(isChecked);
				filterWidget.setOnCheckedChangeListener(mCheckedChangedListener);
				addView(filterWidget);
			}
		}
	}

	private final OnCheckedChangeListener mCheckedChangedListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CheckBoxFilterWidget view, boolean isChecked) {
			Property property = (Property) view.getTag();
			int locationId = property.getLocation().getLocationId();
			if (isChecked) {
				mCheckedNeighborhoods.add(locationId);
			}
			else {
				mCheckedNeighborhoods.remove(locationId);
			}
			triggerOnNeighborhoodsChanged();
		}
	};

	private void triggerOnNeighborhoodsChanged() {
		mListener.onNeighborhoodsChanged(areAllNeighborhoodsChecked() ? null : mCheckedNeighborhoods);
	}

	private boolean areAllNeighborhoodsChecked() {
		return mCheckedNeighborhoods.containsAll(mAllNeighborhoods);
	}
}
