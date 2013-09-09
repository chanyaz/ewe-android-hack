package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import android.os.Parcel;
import android.os.Parcelable;

import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.Log;

/**
 * A mega-search params object that can handle any search that
 * we do in the application.
 * 
 * This object is purposefully dumb about validating its data; for example,
 * you could set an end date that is before a start date.  This is to avoid
 * issues that might otherwise arise when setting multiple fields at once.
 * 
 * If you want to save/restore state (for example, during editing) then use
 * saveToMemento() to save a backup copy and restoreFromMemento() to pop back
 * to a previous state.  Note that Memento uses Parcels so it should not be
 * persisted.
 * 
 * !!!NEW FIELD CHECKLIST!!!
 * 1. Add a getter/setter
 * 2. Add a setDefault() for that field (or set of fields, if it makes sense)
 * 3. Add setDefault() method to restoreToDefaults()
 * 4. Add validation for the field to isValid() (if applicable)
 * 5. Add the field to the Parcelable interface, i.e. SearchParams(Parcel in)
 *    and writeToParcel()
 * 6. Add the field to restoreFromMemento()
 *
 * Future ideas:
 * - canSearch(LineOfBusiness)
 * - toHotelSearchParams() and toFlightSearchParams() (for compat 
 *   with ExpediaServices)
 * - Alternatively, convert ExpediaServices to use this, and add
 *   method for converting others INTO a SearchParams object.
 * 
 */
public class SearchParams implements Parcelable {

	private Location mOrigin;
	private Location mDestination;

	private LocalDate mStartDate;
	private LocalDate mEndDate;

	private int mNumAdults;
	private List<Integer> mChildAges;

	public SearchParams() {
		restoreToDefaults();
	}

	public Location getOrigin() {
		return mOrigin;
	}

	public SearchParams setOrigin(Location origin) {
		mOrigin = origin;
		return this;
	}

	public Location getDestination() {
		return mDestination;
	}

	public SearchParams setDestination(Location destination) {
		mDestination = destination;
		return this;
	}

	public LocalDate getStartDate() {
		return mStartDate;
	}

	public SearchParams setStartDate(LocalDate startDate) {
		mStartDate = startDate;
		return this;
	}

	public LocalDate getEndDate() {
		return mEndDate;
	}

	public SearchParams setEndDate(LocalDate endDate) {
		mEndDate = endDate;
		return this;
	}

	public int getNumAdults() {
		return mNumAdults;
	}

	public SearchParams setNumAdults(int numAdults) {
		mNumAdults = numAdults;
		return this;
	}

	public List<Integer> getChildAges() {
		if (mChildAges == null) {
			mChildAges = new ArrayList<Integer>();
		}

		return mChildAges;
	}

	public int getNumChildren() {
		return getChildAges().size();
	}

	public SearchParams setChildAges(List<Integer> childAges) {
		mChildAges = childAges;
		return this;
	}

	//////////////////////////////////////////////////////////////////////////
	// Defaults

	public void restoreToDefaults() {
		setDefaultLocations();
		setDefaultDuration();
		setDefaultGuests();
	}

	public void setDefaultLocations() {
		mOrigin = new Location();
		mDestination = new Location();
	}

	public void setDefaultDuration() {
		mStartDate = null;
		mEndDate = null;
	}

	public void setDefaultGuests() {
		mNumAdults = 1;
		mChildAges = null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Validation
	//
	// This is just as much documentation of the states the SearchParams can
	// be in as much as it is for validation purposes. 

	public boolean isValid() {
		return areLocationsValid() && isDurationValid() && areGuestsValid();
	}

	public boolean areLocationsValid() {
		// There is no way to create invalid locations at the moment
		return true;
	}

	public boolean isDurationValid() {
		if (mStartDate != null && mEndDate != null && mStartDate.isAfter(mEndDate)) {
			Log.w("SearchParams validation error: Start date is after end date");
			return false;
		}

		return true;
	}

	public boolean areGuestsValid() {
		boolean ret = true;

		int numChildren = getNumChildren();
		int maxAdults = GuestsPickerUtils.getMaxAdults(numChildren);
		int maxChildren = GuestsPickerUtils.getMaxChildren(mNumAdults);
		if (mNumAdults > maxAdults || numChildren > maxChildren) {
			Log.w("SearchParams validation error: too many adults or children.  numAdults=" + mNumAdults
					+ " maxAdults=" + maxAdults + " numChildren=" + numChildren + " maxChildren=" + maxChildren);
			ret = false;
		}

		for (int age : getChildAges()) {
			if (age > GuestsPickerUtils.MAX_CHILD_AGE || age < GuestsPickerUtils.MIN_CHILD_AGE) {
				Log.w("SearchParams validation error: invalid child age (" + age + ")");
				ret = false;
			}
		}

		return ret;
	}

	//////////////////////////////////////////////////////////////////////////
	// Memento

	public Memento saveToMemento() {
		return new Memento(this);
	}

	public void restoreFromMemento(Memento memento) {
		SearchParams params = memento.getSavedState();

		mOrigin = params.mOrigin;
		mDestination = params.mDestination;

		mStartDate = params.mStartDate;
		mEndDate = params.mEndDate;

		mNumAdults = params.mNumAdults;
		mChildAges = params.mChildAges;
	}

	public static final class Memento implements Parcelable {

		private final byte[] mState;

		private Memento(SearchParams stateToSave) {
			Parcel parcel = Parcel.obtain();
			stateToSave.writeToParcel(parcel, 0);
			mState = parcel.marshall();
			parcel.recycle();
		}

		public SearchParams getSavedState() {
			Parcel parcel = Parcel.obtain();
			try {
				parcel.unmarshall(mState, 0, mState.length);
				parcel.setDataPosition(0);
				return SearchParams.CREATOR.createFromParcel(parcel);
			}
			finally {
				parcel.recycle();
			}
		}

		// Parcelable

		private Memento(Parcel in) {
			mState = new byte[in.readInt()];
			in.readByteArray(mState);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(mState.length);
			dest.writeByteArray(mState);
		}

		@Override
		public int describeContents() {
			return 0;
		}

		public static final Parcelable.Creator<Memento> CREATOR = new Parcelable.Creator<Memento>() {
			public Memento createFromParcel(Parcel in) {
				return new Memento(in);
			}

			public Memento[] newArray(int size) {
				return new Memento[size];
			}
		};
	}

	//////////////////////////////////////////////////////////////////////////
	// Parcelable

	private SearchParams(Parcel in) {
		mOrigin = in.readParcelable(null);
		mDestination = in.readParcelable(null);

		mStartDate = JodaUtils.readLocalDate(in);
		mEndDate = JodaUtils.readLocalDate(in);

		mNumAdults = in.readInt();
		in.readList(getChildAges(), null);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(mOrigin, flags);
		dest.writeParcelable(mDestination, flags);

		JodaUtils.writeLocalDate(dest, mStartDate);
		JodaUtils.writeLocalDate(dest, mEndDate);

		dest.writeInt(mNumAdults);
		dest.writeList(getChildAges());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<SearchParams> CREATOR = new Parcelable.Creator<SearchParams>() {
		public SearchParams createFromParcel(Parcel in) {
			return new SearchParams(in);
		}

		public SearchParams[] newArray(int size) {
			return new SearchParams[size];
		}
	};

}
