package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;

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
	private ArrayList<ChildTraveler> mChildren = new ArrayList<ChildTraveler>();

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

		// Click listeners
		mAdultMinus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				performHapticFeedback(view);
				if (mAdultCount > 1) {
					OmnitureTracking.trackRemoveTravelerLink(getContext(), OmnitureTracking.PICKER_ADULT);
				}
				removeAdult();
			}
		});

		mAdultPlus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				performHapticFeedback(view);
				if (canAddAnotherTraveler()) {
					OmnitureTracking.trackAddTravelerLink(getContext(), OmnitureTracking.PICKER_ADULT);
				}
				addAdult();
			}
		});

		mChildMinus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				performHapticFeedback(view);
				if (mChildren.size() > 0) {
					OmnitureTracking.trackRemoveTravelerLink(getContext(), OmnitureTracking.PICKER_CHILD);
				}
				removeChild(mChildren.size() - 1);
			}
		});

		mChildPlus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				performHapticFeedback(view);
				if (mChildren.size() < MAX_CHILDREN && canAddAnotherTraveler()) {
					OmnitureTracking.trackAddTravelerLink(getContext(), OmnitureTracking.PICKER_CHILD);
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

	public void initializeGuests(int initialAdultCount, List<ChildTraveler> initialChildren) {
		mAdultCount = initialAdultCount;
		if (initialChildren == null) {
			mChildren = new ArrayList<ChildTraveler>();
			return;
		}
		mChildren = new ArrayList<ChildTraveler>(initialChildren.size());
		for (ChildTraveler c : initialChildren) {
			mChildren.add(c);
		}
	}

	public boolean canAddAnotherTraveler() {
		return mAdultCount + mChildren.size() < MAX_TRAVELERS;
	}

	public void addAdult() {
		if (mAdultCount < MAX_ADULTS && canAddAnotherTraveler()) {
			mAdultCount++;
			mListener.onGuestsChanged(mAdultCount, mChildren);
			bind();
		}
	}

	public void removeAdult() {
		if (mAdultCount > 1) {
			mAdultCount--;
			mListener.onGuestsChanged(mAdultCount, mChildren);
			bind();
		}
	}

	public void addChild(int age) {
		if (mChildren.size() < MAX_CHILDREN && canAddAnotherTraveler()) {
			mChildren.add(new ChildTraveler(age, false));
			mListener.onGuestsChanged(mAdultCount, mChildren);
			bind();
		}
	}

	public void removeChild(int index) {
		if (index >= 0 && index < mChildren.size()) {
			mChildren.remove(index);
			mListener.onGuestsChanged(mAdultCount, mChildren);
			bind();
		}
	}

	public void bind() {
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

	//////////////////////////////////////////////////////////////////////////
	// Save state
	//
	// Inspiration from http://stackoverflow.com/questions/3542333/how-to-prevent-custom-views-from-losing-state-across-screen-orientation-changes/3542895#3542895

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		SavedState ss = new SavedState(superState);

		ss.mAdultCount = mAdultCount;
		ss.mChildren = mChildren;

		return ss;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof SavedState) {
			SavedState ss = (SavedState) state;
			super.onRestoreInstanceState(ss.getSuperState());
			mAdultCount = ss.mAdultCount;
			mChildren = ss.mChildren;
		} else {
			super.onRestoreInstanceState(state);
		}
	}

	private static class SavedState extends BaseSavedState {
		private int mAdultCount;
		private ArrayList<ChildTraveler> mChildren = new ArrayList<ChildTraveler>();

		private SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);

			mAdultCount = in.readInt();
			in.readTypedList(mChildren, ChildTraveler.CREATOR);
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);

			out.writeInt(mAdultCount);
			out.writeTypedList(mChildren);
		}

		// Required field that makes Parcelables from a Parcel
		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
	}

}
