package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

/**
 * A mega-search params object that can handle any search that
 * we do in the application.
 * <p/>
 * This object is purposefully dumb about validating its data; for example,
 * you could set an end date that is before a start date.  This is to avoid
 * issues that might otherwise arise when setting multiple fields at once.
 * <p/>
 * If you want to save/restore state (for example, during editing) then use
 * saveToMemento() to save a backup copy and restoreFromMemento() to pop back
 * to a previous state.  Note that Memento uses Parcels so it should not be
 * persisted.
 * <p/>
 * !!!NEW FIELD CHECKLIST!!!
 * 1. Add a getter/setter
 * 2. Add a setDefault() for that field (or set of fields, if it makes sense)
 * 3. Add setDefault() method to restoreToDefaults()
 * 4. Add validation for the field to isValid() (if applicable)
 * 5. Add the field to the Parcelable interface, i.e. SearchParams(Parcel in)
 * and writeToParcel()
 * 6. Add the field to restoreFromMemento()
 * 7. Add field to JSONable interface, i.e. toJson() and fromJson()
 * <p/>
 * Future ideas:
 * - canSearch(LineOfBusiness)
 * - Alternatively, convert ExpediaServices to use this, and add
 * method for converting others INTO a SearchParams object.
 */
public class SearchParams implements Parcelable, JSONable {

	private SuggestionV2 mOrigin;
	private SuggestionV2 mDestination;

	// In certain cases, the origin/destination may differ from the airport used to get there
	// (e.g., "current location" searches)
	private SuggestionV2 mOriginAirport;
	private SuggestionV2 mDestinationAirport;

	private LocalDate mStartDate;
	private LocalDate mEndDate;

	private int mNumAdults;
	private List<ChildTraveler> mChildTravelers;
	private boolean mInfantsInLaps;

	//This is intended to store the text manually entered by the user in the case of a manual query
	private String mCustomDestinationQryText;

	public SearchParams() {
		restoreToDefaults();
	}

	public SearchParams(SearchParams params) {
		//Simple copy constructor
		if (params != null) {
			fromJson(params.toJson());
		}
	}

	public SuggestionV2 getOrigin() {
		return mOrigin;
	}

	public Location getOriginLocation(boolean useAirport) {
		if (hasOrigin()) {
			SuggestionV2 origin = (useAirport && mOriginAirport != null ? mOriginAirport : mOrigin);
			if (origin != null && origin.getLocation() != null) {
				Location loc = new Location(origin.getLocation());
				loc.setDestinationId(origin.getAirportCode());
				loc.setDescription(origin.getFullName());
				return loc;
			}
		}
		return null;
	}

	public String getOriginAirportCode() {
		Location loc = getOriginLocation(true);
		if (loc == null) {
			return null;
		}

		return loc.getDestinationId();
	}

	public SearchParams setOrigin(SuggestionV2 origin) {
		mOrigin = origin;
		mOriginAirport = null; // Assume this is no longer valid with a new origin
		return this;
	}

	public boolean hasOrigin() {
		return mOrigin != null && !mOrigin.equals(new SuggestionV2());
	}

	public SuggestionV2 getDestination() {
		return mDestination;
	}

	public Location getDestinationLocation(boolean useAirport) {
		if (hasDestination()) {
			SuggestionV2 destination = (useAirport && mDestinationAirport != null ? mDestinationAirport : mDestination);
			if (destination != null) {
				Location loc = new Location(destination.getLocation());
				loc.setDestinationId(destination.getAirportCode());
				loc.setDescription(destination.getFullName());
				return loc;
			}
		}
		return null;
	}

	public SearchParams setDestination(SuggestionV2 destination) {
		mDestination = destination;
		mDestinationAirport = null; // Assume this is no longer valid with a new destination
		return this;
	}

	public boolean hasDestination() {
		return mDestination != null && !mDestination.equals(new SuggestionV2());
	}

	public SuggestionV2 getOriginAirport() {
		return mOriginAirport;
	}

	public SearchParams setOriginAirport(SuggestionV2 originAirport) {
		mOriginAirport = originAirport;
		return this;
	}

	public SuggestionV2 getDestinationAirport() {
		return mDestinationAirport;
	}

	public SearchParams setDestinationAirport(SuggestionV2 destinationAirport) {
		mDestinationAirport = destinationAirport;
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
		modifyDefaultInfantSeatingPreferenceAsNeeded();
		return this;
	}

	public List<Integer> getChildAges() {
		if (mChildTravelers == null) {
			mChildTravelers = new ArrayList<ChildTraveler>();
		}

		List<Integer> childAges = new ArrayList<Integer>();
		for (ChildTraveler c : mChildTravelers) {
			childAges.add(c.getAge());
		}

		return childAges;
	}

	public List<ChildTraveler> getChildTravelers() {
		if (mChildTravelers == null) {
			mChildTravelers = new ArrayList<ChildTraveler>();
		}
		return mChildTravelers;
	}

	public int getNumChildren() {
		return getChildAges().size();
	}

	public boolean getInfantsInLaps() {
		return mInfantsInLaps;
	}

	public String getCustomDestinationQryText() {
		return mCustomDestinationQryText;
	}

	public void setCustomDestinationQryText(String customDestinationQryText) {
		mCustomDestinationQryText = customDestinationQryText;
	}

	public SearchParams setChildTravelers(List<ChildTraveler> children) {
		mChildTravelers = children;
		modifyDefaultInfantSeatingPreferenceAsNeeded();
		return this;
	}

	public SearchParams setInfantsInLaps(boolean infantsInLaps) {
		mInfantsInLaps = infantsInLaps;
		return this;
	}

	private void modifyDefaultInfantSeatingPreferenceAsNeeded() {
		if (mChildTravelers != null) {
			if (GuestsPickerUtils.moreInfantsThanAvailableLaps(mNumAdults, mChildTravelers)) {
				mInfantsInLaps = false;
			}
		}
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
		setDefaultCustomDestinationQryText();
	}

	public void setDefaultLocations() {
		mOrigin = new SuggestionV2();
		mDestination = new SuggestionV2();
		mOriginAirport = null;
		mDestinationAirport = null;
	}

	public void setDefaultDuration() {
		mStartDate = null;
		mEndDate = null;
	}

	public void setDefaultGuests() {
		mNumAdults = 1;
		mChildTravelers = null;
		mInfantsInLaps = true;
	}

	public void setDefaultCustomDestinationQryText() {
		mCustomDestinationQryText = null;
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
		if (mStartDate != null && mEndDate != null && (mStartDate.isAfter(mEndDate) ||
			mStartDate.isBefore(org.joda.time.DateTime.now().toLocalDate()))) {
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

		for (ChildTraveler child : mChildTravelers) {
			if (child.getAge() > GuestsPickerUtils.MAX_CHILD_AGE || child.getAge() < GuestsPickerUtils.MIN_CHILD_AGE) {
				Log.w("SearchParams validation error: invalid child age (" + child.getAge() + ")");
				ret = false;
			}
		}

		if (GuestsPickerUtils.moreInfantsThanAvailableLaps(mNumAdults, mChildTravelers) && mInfantsInLaps) {
			ret = false;
		}

		return ret;
	}

	//////////////////////////////////////////////////////////////////////////
	// Conversion
	//
	// For the time being, convert into HotelSearchParams or
	// FlightSearchParams instead of trying to duplicate work for services.

	public boolean hasEnoughInfoForHotelsSearch() {
		return hasDestination();
	}

	public HotelSearchParams toHotelSearchParams() {
		HotelSearchParams params = new HotelSearchParams();

		if (mStartDate != null) {
			params.setCheckInDate(mStartDate);
		}
		else {
			// Default to "today" if not explicitly set
			params.setCheckInDate(LocalDate.now());
		}

		// Don't set the end date if it equals the start date, otherwise setCheckoutDate
		// validation will shift the days to the left one erroneously
		if (mEndDate != null && (mStartDate != null && !mStartDate.equals(mEndDate))) {
			params.setCheckOutDate(mEndDate);
		}
		else {
			// Default to check in +1 day if not explicitly set
			params.setCheckOutDate(params.getCheckInDate().plusDays(1));
		}

		params.setNumAdults(mNumAdults);
		params.setChildren(mChildTravelers);

		Location destLoc = getDestinationLocation(false);
		params.setCorrespondingAirportCode(destLoc.getDestinationId());

		// Map SuggestionV2.SearchType to HotelSearchParams.SearchType
		if (mDestination.getResultType() == SuggestionV2.ResultType.CURRENT_LOCATION && (destLoc.getLatitude() != 0
			|| destLoc.getLongitude() != 0)) {
			params.setSearchLatLon(destLoc.getLatitude(), destLoc.getLongitude());
			params.setSearchType(HotelSearchParams.SearchType.MY_LOCATION);
		}
		else if (!TextUtils.isEmpty(mCustomDestinationQryText)) {
			//If we have a custom query, we consider this freeform
			params.setUserQuery(mCustomDestinationQryText);
			params.setQuery(mCustomDestinationQryText);
			params.setSearchType(HotelSearchParams.SearchType.FREEFORM);
		}
		else {
			switch (mDestination.getSearchType()) {
			case ATTRACTION:
				params.setSearchType(HotelSearchParams.SearchType.POI);
				break;
			case AIRPORT:
				params.setSearchType(HotelSearchParams.SearchType.POI);
				break;
			case HOTEL:
				params.setSearchType(HotelSearchParams.SearchType.HOTEL);
				break;
			case CITY:
				params.setSearchType(HotelSearchParams.SearchType.CITY);
				break;
			default:
				params.setSearchType(HotelSearchParams.SearchType.FREEFORM);
				break;
			}

			// We prioritize regionId over lat/lng over query str. This is the same behavior in
			// ExpediaServices.search();
			if (mDestination.getRegionId() != 0) {
				params.setRegionId(Integer.toString(mDestination.getRegionId()));
			}
			else if (destLoc.getLatitude() != 0 || destLoc.getLongitude() != 0) {
				params.setSearchLatLon(destLoc.getLatitude(), destLoc.getLongitude());
			}
			else {
				params.setQuery(mDestination.getFullName());
			}
		}
		return params;
	}

	public boolean hasEnoughInfoForFlightsSearch() {
		return hasOrigin() && hasDestination() && getStartDate() != null;
	}

	public FlightSearchParams toFlightSearchParams() {
		FlightSearchParams params = new FlightSearchParams();

		Location origLoc = getOriginLocation(true);
		Location destLoc = getDestinationLocation(true);

		if (origLoc != null) {
			params.setDepartureLocation(origLoc);
		}
		if (destLoc != null) {
			params.setArrivalLocation(destLoc);
		}

		if (mStartDate != null) {
			params.setDepartureDate(mStartDate);
		}
		else {
			// Default to "today" if not explicitly set
			params.setDepartureDate(LocalDate.now());
		}

		if (mEndDate != null) {
			params.setReturnDate(mEndDate);
		}

		params.setOriginId(mOrigin == null ? 0 : mOrigin.getRegionId());
		params.setDestinationId(mDestination == null ? 0 : mDestination.getRegionId());

		params.setNumAdults(mNumAdults);
		params.setChildren(mChildTravelers);
		params.setInfantSeatingInLap(mInfantsInLaps);

		return params;
	}

	/**
	 * TODO support all field conversions
	 *
	 * @param hotelParams
	 * @return
	 */
	public static SearchParams fromHotelSearchParams(HotelSearchParams hotelParams) {
		SearchParams searchParams = new SearchParams();

		// Who
		searchParams.setNumAdults(hotelParams.getNumAdults());
		searchParams.setChildTravelers(hotelParams.getChildren());

		// Where
		SuggestionV2 destination = new SuggestionV2();

		switch (hotelParams.getSearchType()) {
		case CITY:
			destination.setSearchType(SuggestionV2.SearchType.CITY);
			destination.setFullName(hotelParams.getQuery());
			destination.setDisplayName(hotelParams.getQuery());
			destination.setRegionId(Integer.parseInt(hotelParams.getRegionId()));
			destination.setAirportCode(hotelParams.getCorrespondingAirportCode());
			break;
		case HOTEL:
			destination.setSearchType(SuggestionV2.SearchType.HOTEL);
			destination.setRegionId(Integer.parseInt(hotelParams.getRegionId()));
			destination.setDisplayName(hotelParams.getQuery());
			break;
		default:
			destination.setSearchType(SuggestionV2.SearchType.ATTRACTION);
			break;
		}
		if (hotelParams.getSearchLatitude() != 0 && hotelParams.getSearchLongitude() != 0) {
			Location loc = new Location();
			loc.setLatitude(hotelParams.getSearchLatitude());
			loc.setLongitude(hotelParams.getSearchLongitude());
			destination.setLocation(loc);
		}

		searchParams.setDestination(destination);

		// When
		if (hotelParams.getCheckInDate() != null) {
			searchParams.setStartDate(hotelParams.getCheckInDate());
		}
		if (hotelParams.getCheckOutDate() != null) {
			searchParams.setEndDate(hotelParams.getCheckOutDate());
		}

		return searchParams;
	}

	public static SearchParams fromFlightSearchParams(FlightSearchParams flightParams) {
		SearchParams searchParams = new SearchParams();

		// Who
		searchParams.setNumAdults(flightParams.getNumAdults());
		searchParams.setChildTravelers(flightParams.getChildren());

		// Where
		SuggestionV2 destination = new SuggestionV2();
		destination.setSearchType(SuggestionV2.SearchType.AIRPORT);
		destination.setRegionId(flightParams.getDestinationId());
		// Change the Display Name to some more relevant for flights?
		destination.setDisplayName(flightParams.getArrivalLocation().getDestinationId());
		searchParams.setDestination(destination);

		// When
		if (flightParams.getDepartureDate() != null) {
			searchParams.setStartDate(flightParams.getDepartureDate());
		}
		if (flightParams.getReturnDate() != null) {
			searchParams.setEndDate(flightParams.getReturnDate());
		}

		return searchParams;
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

		mOriginAirport = params.mOriginAirport;
		mDestinationAirport = params.mDestinationAirport;

		mStartDate = params.mStartDate;
		mEndDate = params.mEndDate;

		mNumAdults = params.mNumAdults;
		mChildTravelers = params.mChildTravelers;
		mInfantsInLaps = params.getInfantsInLaps();

		mCustomDestinationQryText = params.mCustomDestinationQryText;
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
		ClassLoader cl = SearchParams.class.getClassLoader();

		mOrigin = in.readParcelable(cl);
		mDestination = in.readParcelable(cl);

		mOriginAirport = in.readParcelable(cl);
		mDestinationAirport = in.readParcelable(cl);

		mStartDate = JodaUtils.readLocalDate(in);
		mEndDate = JodaUtils.readLocalDate(in);

		mNumAdults = in.readInt();
		mCustomDestinationQryText = in.readString();

		in.readList(getChildAges(), cl);
		mInfantsInLaps = in.readInt() > 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(mOrigin, flags);
		dest.writeParcelable(mDestination, flags);

		dest.writeParcelable(mOriginAirport, flags);
		dest.writeParcelable(mDestinationAirport, flags);

		JodaUtils.writeLocalDate(dest, mStartDate);
		JodaUtils.writeLocalDate(dest, mEndDate);

		dest.writeInt(mNumAdults);
		dest.writeList(getChildAges());
		dest.writeInt(mInfantsInLaps ? 0 : 1);

		dest.writeString(mCustomDestinationQryText == null ? "" : mCustomDestinationQryText);
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

			JSONUtils.putJSONable(obj, "originAirport", mOriginAirport);
			JSONUtils.putJSONable(obj, "destinationAirport", mDestinationAirport);

			JodaUtils.putLocalDateInJson(obj, "startDate", mStartDate);
			JodaUtils.putLocalDateInJson(obj, "endDate", mEndDate);

			obj.putOpt("numAdults", mNumAdults);
			JSONUtils.putJSONableList(obj, "children", mChildTravelers);
			obj.putOpt("infantsInLaps", mInfantsInLaps);
			obj.putOpt("customDestinationQryText", mCustomDestinationQryText);

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mOrigin = JSONUtils.getJSONable(obj, "origin", SuggestionV2.class);
		mDestination = JSONUtils.getJSONable(obj, "destination", SuggestionV2.class);

		mOriginAirport = JSONUtils.getJSONable(obj, "originAirport", SuggestionV2.class);
		mDestinationAirport = JSONUtils.getJSONable(obj, "destinationAirport", SuggestionV2.class);

		mStartDate = JodaUtils.getLocalDateFromJson(obj, "startDate");
		mEndDate = JodaUtils.getLocalDateFromJson(obj, "endDate");

		mNumAdults = obj.optInt("numAdults", 1);
		mChildTravelers = JSONUtils.getJSONableList(obj, "children", ChildTraveler.class);
		mInfantsInLaps = obj.optBoolean("infantsInLaps");
		mCustomDestinationQryText = obj.optString("customDestinationQryText");

		return true;
	}

	/*

	 */

	@Override
	public int hashCode() {
		try {
			return toJson().toString().hashCode();
		}
		catch (Exception ex) {
			Log.e("Exception generating hashcode.", ex);
		}
		return super.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (!(other instanceof SearchParams)) {
			return false;
		}
		if (other == this) {
			return true;
		}

		//TODO: This is the very lazy and very poor practice way to compare objects, we can do better
		try {
			String jsonStr = toJson().toString();
			String otherJsonStr = ((SearchParams) other).toJson().toString();
			return jsonStr.equals(otherJsonStr);
		}
		catch (Exception ex) {
			Log.e("Error in SearchParams.equals. toJson() likely failing.", ex);
		}
		return false;
	}
}
