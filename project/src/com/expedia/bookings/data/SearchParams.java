package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

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
 * 7. Add field to JSONable interface, i.e. toJson() and fromJson()
 *
 * Future ideas:
 * - canSearch(LineOfBusiness)
 * - Alternatively, convert ExpediaServices to use this, and add
 *   method for converting others INTO a SearchParams object.
 * 
 */
public class SearchParams implements Parcelable, JSONable {

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

	public boolean hasOrigin() {
		return mOrigin != null && !mOrigin.equals(new Location());
	}

	public Location getDestination() {
		return mDestination;
	}

	public SearchParams setDestination(Location destination) {
		mDestination = destination;
		return this;
	}

	public boolean hasDestination() {
		return mDestination != null && !mDestination.equals(new Location());
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Origin=");
		sb.append(mOrigin == null ? "null" : mOrigin.toJson().toString());
		sb.append(" ");
		sb.append("Destination=");
		sb.append(mDestination == null ? "null" : mDestination.toJson().toString());
		sb.append(" ");
		sb.append("Start=");
		sb.append(mStartDate == null ? "null" : mStartDate.toString());
		sb.append(" ");
		sb.append("End=");
		sb.append(mEndDate == null ? "null" : mEndDate.toString());
		sb.append(" ");
		sb.append("Adults=" + mNumAdults);
		sb.append(" ");
		sb.append("Children=" + Arrays.toString(getChildAges().toArray()));

		return sb.toString();
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
	// Conversion
	//
	// For the time being, convert into HotelSearchParams or
	// FlightSearchParams instead of trying to duplicate work for services.

	public HotelSearchParams toHotelSearchParams() {
		HotelSearchParams params = new HotelSearchParams();

		if (!mDestination.equals(new Location())) {
			// TODO: This is temporary code; the suggestions are all based off FS.db, which doesn't
			// give us good hotel search data excepting lat/lng.  So we use the airport's lat/lng
			// for the search for now - once we are stabilized a bit we should switch to basing
			// data off mOrigin.
			Airport airport = FlightStatsDbUtils.getAirport(mDestination.getDestinationId());

			if (airport != null && airport.getLatitude() != 0 || airport.getLongitude() != 0) {
				params.setSearchLatLon(airport.getLatitude(), airport.getLongitude());
			}
			else {
				params.setQuery(mDestination.getCity());
			}
		}

		if (mStartDate != null) {
			params.setCheckInDate(mStartDate);
		}

		if (mEndDate != null) {
			params.setCheckOutDate(mEndDate);
		}

		params.setNumAdults(mNumAdults);
		params.setChildren(getChildAges());

		return params;
	}

	public FlightSearchParams toFlightSearchParams() {
		FlightSearchParams params = new FlightSearchParams();

		params.setDepartureLocation(mOrigin);
		params.setArrivalLocation(mDestination);

		if (mStartDate != null) {
			params.setDepartureDate(mStartDate);
		}

		if (mEndDate != null) {
			params.setReturnDate(mEndDate);
		}

		params.setNumAdults(mNumAdults);
		params.setChildren(getChildAges());

		return params;
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

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();

			JSONUtils.putJSONable(obj, "origin", mOrigin);
			JSONUtils.putJSONable(obj, "destination", mDestination);

			JodaUtils.putLocalDateInJson(obj, "startDate", mStartDate);
			JodaUtils.putLocalDateInJson(obj, "endDate", mEndDate);

			obj.putOpt("numAdults", mNumAdults);
			JSONUtils.putIntList(obj, "childAges", mChildAges);

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mOrigin = JSONUtils.getJSONable(obj, "origin", Location.class);
		mDestination = JSONUtils.getJSONable(obj, "destination", Location.class);

		mStartDate = JodaUtils.getLocalDateFromJsonBackCompat(obj, "startDate", null);
		mEndDate = JodaUtils.getLocalDateFromJsonBackCompat(obj, "endDate", null);

		mNumAdults = obj.optInt("numAdults", 1);
		mChildAges = JSONUtils.getIntList(obj, "childAges");

		return true;
	}
}
