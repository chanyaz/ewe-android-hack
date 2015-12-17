package com.expedia.bookings.widget;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.LinearLayout;

import com.expedia.bookings.data.Db;
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
		void onNeighborhoodsChanged(Set<Integer> neighborhoods);
	}

	private OnNeighborhoodsChangedListener mListener;

	/**
	 * Maps locationId to a CheckBoxFilterWidget
	 */
	private SparseArray<CheckBoxFilterWidget> mWidgetMap = new SparseArray<CheckBoxFilterWidget>();

	private Set<Integer> mAllNeighborhoods;
	private Set<Integer> mCheckedNeighborhoods;

	private boolean mWidgetsBuilt = false;

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

	public void setNeighborhoods(HotelSearchResponse response, HotelFilter filter) {
		mCheckedNeighborhoods = filter.getNeighborhoods();
		if (response == null || response.getProperties() == null) {
			removeAllViews();
		}
		else {
			buildWidgets(response);
			updateWidgets(response);
		}
	}

	private void buildWidgets(HotelSearchResponse response) {
		// Keep track of all locationId's
		if (mAllNeighborhoods == null) {
			mAllNeighborhoods = new HashSet<Integer>();
			for (Property property : response.getProperties()) {
				int locationId = property.getLocation().getLocationId();
				mAllNeighborhoods.add(locationId);
			}
		}

		// If necessary, initialize mCheckedNeighborhoods with all neighborhoods checked
		if (mCheckedNeighborhoods == null) {
			mCheckedNeighborhoods = new HashSet<Integer>();
		}

		// Initialize all neighborhoods, disabled for now
		mWidgetMap.clear();
		for (Property property : response.getProperties()) {
			int locationId = property.getLocation().getLocationId();
			if (mWidgetMap.get(locationId) == null) {
				CheckBoxFilterWidget widget = new CheckBoxFilterWidget(getContext());
				widget.setTag(locationId);
				widget.setDescription(property.getLocation().getDescription());
				widget.setOnCheckedChangeListener(mCheckedChangedListener);
				mWidgetMap.put(locationId, widget);
			}
		}

		// Sort by CheckBoxFilterWidget.compareTo()
		TreeSet<CheckBoxFilterWidget> sorted = new TreeSet<CheckBoxFilterWidget>();
		for (int i = 0; i < mWidgetMap.size(); i++) {
			sorted.add(mWidgetMap.valueAt(i));
		}

		removeAllViews();
		for (CheckBoxFilterWidget view : sorted) {
			addView(view);
		}

		mWidgetsBuilt = true;
	}

	public void updateWidgets(HotelSearchResponse response) {
		if (!mWidgetsBuilt) {
			return;
		}

		for (Property property : response.getProperties()) {
			int locationId = property.getLocation().getLocationId();
			CheckBoxFilterWidget widget = mWidgetMap.get(locationId);
			widget.setEnabled(false);
			if (property.getLowestRate() != null) {
				widget.setPriceIfLower(property.getLowestRate().getDisplayPrice());
			}
		}

		// Look through the filtered properties, enable that filter widget
		// and modify its price if appropriate.
		for (Property property : response
			.getFilteredPropertiesIgnoringNeighborhood(Db.getHotelSearch().getSearchParams())) {
			int locationId = property.getLocation().getLocationId();
			CheckBoxFilterWidget widget = mWidgetMap.get(locationId);
			if (widget.isEnabled()) {
				widget.setPriceIfLower(property.getLowestRate().getDisplayPrice());
			}
			else {
				widget.setEnabled(true);
				if (property.getLowestRate() != null) {
					widget.setPrice(property.getLowestRate().getDisplayPrice());
				}
			}
		}

		mDisableCheckedListener = true;

		// Tick the neighborhoods according to the filter.
		for (int locationId : mAllNeighborhoods) {
			CheckBoxFilterWidget widget = mWidgetMap.get(locationId);
			widget.setChecked(mCheckedNeighborhoods.contains(locationId));
		}

		mDisableCheckedListener = false;
	}

	// Flag to disable the checked change listener when we're initializing the widget
	private boolean mDisableCheckedListener = false;

	private final OnCheckedChangeListener mCheckedChangedListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CheckBoxFilterWidget view, boolean isChecked) {
			if (mDisableCheckedListener) {
				return;
			}

			int locationId = (Integer) view.getTag();
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
		mListener.onNeighborhoodsChanged(
			areAllNeighborhoodsChecked() || mCheckedNeighborhoods.size() == 0 ? null : mCheckedNeighborhoods);
	}

	private boolean areAllNeighborhoodsChecked() {
		return mCheckedNeighborhoods.containsAll(mAllNeighborhoods);
	}
}
