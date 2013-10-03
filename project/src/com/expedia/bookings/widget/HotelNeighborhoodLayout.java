package com.expedia.bookings.widget;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
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

	@TargetApi(11)
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
				mNeighborhoods.add(property.getLocation().getLocationId());

				CheckBoxFilterWidget filterWidget = new CheckBoxFilterWidget(getContext());
				filterWidget.setOnCheckedChangeListener(mCheckedChangedListener);
				filterWidget.bindHotel(property);
				filterWidget.setTag(property);

				addView(filterWidget);
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
			ViewGroup row = (ViewGroup) getChildAt(i);
			CheckBox box = (CheckBox) row.getChildAt(0);
			if (!box.isChecked()) {
				return false;
			}
		}
		return true;
	}
}
