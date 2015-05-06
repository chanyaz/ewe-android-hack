package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.mobiata.android.util.Ui;

public class GuestPicker extends LinearLayout {

	public interface GuestPickerListener {
		public void onGuestsChanged(int numAdults, List<ChildTraveler> children);
	}

	public static final int MAX_ADULTS = 6;
	public static final int MAX_CHILDREN = 4;
	public static final int MAX_TRAVELERS = 6;

	private TextView mAdultText;
	private TextView mChildText;
	private View mAdultMinus;
	private View mAdultPlus;
	private View mChildMinus;
	private View mChildPlus;
	private View mChildAgesLayout;

	private GuestPickerListener mListener;

	private int mAdultCount;
	private List<ChildTraveler> mChildren = new ArrayList<ChildTraveler>();

	public GuestPicker(Context context) {
		super(context, null);
	}

	public GuestPicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GuestPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setOrientation(VERTICAL);

		inflate(context, R.layout.guest_picker, this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// Cache Views
		mAdultText = Ui.findView(this, R.id.adult_count_text);
		mAdultMinus = Ui.findView(this, R.id.adults_minus);
		mAdultPlus = Ui.findView(this, R.id.adults_plus);

		mChildText = Ui.findView(this, R.id.child_count_text);
		mChildMinus = Ui.findView(this, R.id.children_minus);
		mChildPlus = Ui.findView(this, R.id.children_plus);

		mChildAgesLayout = Ui.findView(this, R.id.child_ages_layout);
		mChildAgesLayout.setVisibility(getChildAgesVisibility());

		boolean isTablet = getContext().getResources().getBoolean(R.bool.tablet);
		final String trackingBase = isTablet ? OmnitureTracking.PICKER_TRACKING_BASE_TABLET :
			OmnitureTracking.PICKER_TRACKING_BASE_FLIGHT;
		// Click listeners
		mAdultMinus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				performHapticFeedback(view);
				if (mAdultCount > 1) {
					OmnitureTracking.trackRemoveTravelerLink(getContext(), trackingBase, OmnitureTracking.PICKER_ADULT);
				}
				removeAdult();
			}
		});

		mAdultPlus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				performHapticFeedback(view);
				if (canAddAnotherTraveler()) {
					OmnitureTracking.trackAddTravelerLink(getContext(), trackingBase, OmnitureTracking.PICKER_ADULT);
				}
				addAdult();
			}
		});

		mChildMinus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				performHapticFeedback(view);
				if (mChildren.size() > 0) {
					OmnitureTracking.trackRemoveTravelerLink(getContext(), trackingBase, OmnitureTracking.PICKER_CHILD);
				}
				removeChild(mChildren.size() - 1);
			}
		});

		mChildPlus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				performHapticFeedback(view);
				if (mChildren.size() < MAX_CHILDREN && canAddAnotherTraveler()) {
					OmnitureTracking.trackAddTravelerLink(getContext(), trackingBase, OmnitureTracking.PICKER_CHILD);
					addChild(10);
				}
			}
		});
	}

	private void performHapticFeedback(View v) {
		v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	public void setListener(GuestPickerListener listener) {
		mListener = listener;
	}

	public boolean canAddAnotherTraveler() {
		return mAdultCount + mChildren.size() < MAX_TRAVELERS;
	}

	public void addAdult() {
		if (mAdultCount < MAX_ADULTS && canAddAnotherTraveler()) {
			mAdultCount++;
			mListener.onGuestsChanged(mAdultCount, mChildren);
			bindViews();
		}
	}

	public void removeAdult() {
		if (mAdultCount > 1) {
			mAdultCount--;
			mListener.onGuestsChanged(mAdultCount, mChildren);
			bindViews();
		}
	}

	public void addChild(int age) {
		if (mChildren.size() < MAX_CHILDREN && canAddAnotherTraveler()) {
			mChildren.add(new ChildTraveler(age, false));
			mListener.onGuestsChanged(mAdultCount, mChildren);
			bindViews();
		}
	}

	public void removeChild(int index) {
		if (index >= 0 && index < mChildren.size()) {
			mChildren.remove(index);
			mListener.onGuestsChanged(mAdultCount, mChildren);
			bindViews();
		}
	}

	public void bind(int adultCount, List<ChildTraveler> children) {
		mAdultCount = adultCount;
		mChildren = new ArrayList<>(children);
		bindViews();
	}

	private void bindViews() {
		mAdultText.setText(getResources().getQuantityString(R.plurals.number_of_adults, mAdultCount, mAdultCount));
		mChildText.setText(getResources().getQuantityString(R.plurals.number_of_children, mChildren.size(), mChildren.size()));
		GuestsPickerUtils.setChildSpinnerPositions(this, mChildren);
		GuestsPickerUtils.showOrHideChildAgeSpinners(getContext(), mChildren, this, mChildAgeSelectedListener);
		mChildAgesLayout.setVisibility(getChildAgesVisibility());
	}

	private int getChildAgesVisibility() {
		return mChildren.size() > 0 ? View.VISIBLE : View.INVISIBLE;
	}

	private final android.widget.AdapterView.OnItemSelectedListener mChildAgeSelectedListener = new android.widget.AdapterView.OnItemSelectedListener() {

		public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
			GuestsPickerUtils.setChildrenFromSpinners(getContext(), mChildAgesLayout, mChildren);
			GuestsPickerUtils.updateDefaultChildTravelers(getContext(), mChildren);
			mListener.onGuestsChanged(mAdultCount, mChildren);
		}

		public void onNothingSelected(android.widget.AdapterView<?> parent) {
			// Do nothing.
		}
	};

	public boolean moreInfantsThanAvailableLaps() {
		return GuestsPickerUtils.moreInfantsThanAvailableLaps(mAdultCount, mChildren);
	}

	public String getHeaderString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mAdultText.getText());
		if (mChildren.size() > 0) {
			sb.append(", ");
			sb.append(mChildText.getText());
		}
		return sb.toString();
	}

}
