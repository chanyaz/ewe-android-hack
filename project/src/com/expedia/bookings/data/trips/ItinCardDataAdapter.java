package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.LocalExpertSite.Destination;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.model.DismissedItinButton;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.widget.ItinCard;
import com.expedia.bookings.widget.ItinCard.OnItinCardClickListener;
import com.expedia.bookings.widget.itin.ItinButtonCard;
import com.expedia.bookings.widget.itin.ItinButtonCard.ItinButtonType;
import com.expedia.bookings.widget.itin.ItinButtonCard.OnHideListener;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.flightlib.data.Waypoint;

public class ItinCardDataAdapter extends BaseAdapter implements OnItinCardClickListener, OnHideListener {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE ENUMERATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	private enum State {
		PAST,
		SUMMARY,
		NORMAL,
		DETAIL,
		BUTTON
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Context mContext;
	private ItineraryManager mItinManager;
	private int mSummaryCardPosition;
	private int mAltSummaryCardPosition;
	private List<ItinCardData> mItinCardDatas;
	private int mDetailPosition = -1;
	private String mSelectedCardId;

	// This is used when we are syncing with the manager; that way we don't ever make
	// the adapter's data and the ListView's data go out of sync.
	private List<ItinCardData> mItinCardDatasSync;

	private boolean mSimpleMode = false;

	private OnItinCardClickListener mOnItinCardClickListener;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinCardDataAdapter(Context context) {
		mContext = context;
		mItinManager = ItineraryManager.getInstance();
		mItinCardDatas = new ArrayList<ItinCardData>();
		mItinCardDatasSync = new ArrayList<ItinCardData>();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// BaseAdapter methods
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public synchronized int getCount() {
		if (mItinCardDatas != null) {
			return mItinCardDatas.size();
		}

		return 0;
	}

	@Override
	public synchronized ItinCardData getItem(int position) {
		if (mItinCardDatas != null && position < mItinCardDatas.size()) {
			return mItinCardDatas.get(position);
		}

		return null;
	}

	@Override
	public synchronized long getItemId(int position) {
		if (mItinCardDatas != null) {
			return position;
		}

		return -1;
	}

	@Override
	public synchronized View getView(final int position, View convertView, ViewGroup Parent) {
		final ItinCardData data = getItem(position);
		if (isItemAButtonCard(position)) {
			ItinButtonCard card;
			if (convertView instanceof ItinButtonCard) {
				card = (ItinButtonCard) convertView;
			}
			else {
				card = new ItinButtonCard(mContext);
				card.setOnHideListener(this);
			}

			card.bind(data);

			return card;
		}
		else {
			ItinCard card;
			if (convertView instanceof ItinCard) {
				card = (ItinCard) convertView;
			}
			else {
				card = new ItinCard(mContext);
				card.setOnItinCardClickListener(this);
			}

			State state = getItemViewCardState(position);

			card.setCardSelected(mSimpleMode && data.getId().equals(mSelectedCardId));
			card.setCardShaded(state == State.PAST);
			card.setShowSummary(isItemASummaryCard(position));

			card.setShowExtraTopPadding(position == 0);
			card.setShowExtraBottomPadding(position == getCount() - 1);

			card.bind(data);

			return card;
		}
	}

	@Override
	public int getItemViewType(int position) {
		//Note: Our types are:
		//normal cards: 0 <= TYPE < Type.values().length
		//shaded cards: Type.values().length <= TYPE < Type.values().length * 2
		//summary cards: Type.values().length * 2 <= TYPE < Type.values().length * 3
		Type type = getItem(position).getTripComponentType();
		int retVal = type.ordinal();
		boolean isInThePast = isItemInThePast(position);
		boolean isSumCard = isItemASummaryCard(position);
		boolean isDetailCard = isItemDetailCard(position);
		boolean isButtonCard = isItemAButtonCard(position);
		if (isDetailCard) {
			retVal += (TripComponent.Type.values().length * 3);
		}
		else if (isInThePast) {
			retVal += TripComponent.Type.values().length;
		}
		else if (isSumCard && !mSimpleMode) {
			retVal += (TripComponent.Type.values().length * 2);
		}
		else if (isButtonCard) {
			retVal += (TripComponent.Type.values().length * 4);
		}

		return retVal;
	}

	@Override
	public int getViewTypeCount() {
		//the *3 is so we have one for each type and one for each type that is shaded and one for each type in summary mode
		// the +1 is for the button card type
		return TripComponent.Type.values().length * State.values().length;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void setDetailPosition(int position) {
		mDetailPosition = position;
	}

	public int getDetailPosition() {
		return mDetailPosition;
	}

	public void setSelectedCardId(String cardId) {
		mSelectedCardId = cardId;
	}

	/**
	 * Sync the adapter data with the ItineraryManager
	 */
	public synchronized void syncWithManager() {
		// Add Items
		mItinCardDatasSync.addAll(mItinManager.getItinCardData());

		// Add hotel attach cards
		addHotelAttachData(mItinCardDatasSync);

		// Add local expert cards
		if (ExpediaBookingApp.IS_EXPEDIA) {
			addLocalExpertData(mItinCardDatasSync);
		}

		// Do some calculations on the data
		Pair<Integer, Integer> summaryCardPositions = calculateSummaryCardPositions(mItinCardDatasSync);

		// Add to actual data
		mItinCardDatas.clear();
		mItinCardDatas.addAll(mItinCardDatasSync);
		mSummaryCardPosition = summaryCardPositions.first;
		mAltSummaryCardPosition = summaryCardPositions.second;

		//Notify listeners
		notifyDataSetChanged();

		// Reset state before next sync
		mItinCardDatasSync.clear();
	}

	/**
	 * Empty the Adapter and fire notifyDataSetChanged(). This does not remove any underlying data from ItineraryManager.
	 * 
	 * This method allows the adapter to be emptied without waiting for an ItineraryManager.sync operation to complete.
	 * It is useful for logging out a user, where we want the UI to reflect the user being logged out, but maybe itins have
	 * not all been cleared yet.
	 */
	public synchronized void clearAdapter() {
		mItinCardDatas.clear();

		//Notify listeners
		notifyDataSetChanged();
	}

	public void setSimpleMode(boolean enabled) {
		mSimpleMode = enabled;
	}

	public void setOnItinCardClickListener(OnItinCardClickListener onItinCardClickListener) {
		mOnItinCardClickListener = onItinCardClickListener;
	}

	/**
	 * The first (and usually only) summary view card position
	 * @return
	 */
	public synchronized int getMostRelevantCardPosition() {
		if (mSummaryCardPosition >= 0) {
			return mSummaryCardPosition;
		}
		else {
			return mItinCardDatas.size() - 1;
		}
	}

	public int getPosition(String itinCardId) {
		if (!TextUtils.isEmpty(itinCardId)) {
			// Possible TODO: Speed up via hash
			int len = mItinCardDatas.size();
			for (int a = 0; a < len; a++) {
				if (itinCardId.equals(mItinCardDatas.get(a).getId())) {
					return a;
				}
			}
		}

		return -1;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private Type getItemViewCardType(int position) {
		int typeOrd = getItemViewType(position);
		typeOrd = typeOrd % TripComponent.Type.values().length;
		return Type.values()[typeOrd];
	}

	private State getItemViewCardState(int position) {
		int typeOrd = getItemViewType(position) / TripComponent.Type.values().length;
		switch (typeOrd) {
		case 0:
			return State.NORMAL;
		case 1:
			return State.PAST;
		case 2:
			return State.SUMMARY;
		case 3:
			return State.DETAIL;
		case 4:
			return State.BUTTON;
		default:
			return State.NORMAL;
		}
	}

	private boolean isItemInThePast(int position) {
		ItinCardData data = mItinCardDatas.get(position);
		if (data == null || data.getEndDate() == null) {
			return false;
		}

		DateTime endDate = data.getEndDate();
		int endYear = endDate.getYear();
		int endDay = endDate.getDayOfYear();

		DateTime now = DateTime.now();
		int thisYear = now.getYear();
		int thisDay = now.getDayOfYear();

		return (endYear == thisYear && endDay < thisDay) || endYear < thisYear;
	}

	private boolean isItemASummaryCard(int position) {
		return position == mSummaryCardPosition || position == mAltSummaryCardPosition;
	}

	private boolean isItemDetailCard(int position) {
		return (position == mDetailPosition);
	}

	private boolean isItemAButtonCard(int position) {
		final ItinCardData item = getItem(position);
		return item instanceof ItinCardDataHotelAttach || item instanceof ItinCardDataLocalExpert;
	}

	// Assumes the list is sorted ahead of time
	private Pair<Integer, Integer> calculateSummaryCardPositions(List<ItinCardData> itinCardDatas) {
		// Reset calculated data
		int summaryCardPosition = -1;
		int altSummaryCardPosition = -1;

		// Nothing to do if there are no itineraries
		int len = itinCardDatas.size();
		if (len == 0) {
			return new Pair<Integer, Integer>(summaryCardPosition, altSummaryCardPosition);
		}

		// Calculate the summary (and possibly alternate) positions
		ItinCardData summaryCardData = null;
		ItinCardData firstInProgressCard = null;
		int firstInProgressCardPos = -1;
		DateTime now = DateTime.now();
		long nowMillis = now.getMillis();
		int today = now.getDayOfYear();
		for (int a = 0; a < len; a++) {
			boolean setAsSummaryCard = false;

			ItinCardData data = itinCardDatas.get(a);

			if (!isValidForSummary(data)) {
				continue;
			}

			DateTime startDate = data.getStartDate();

			if (data instanceof ItinCardDataFlight && ((ItinCardDataFlight) data).isEnRoute()) {
				setAsSummaryCard = true;
			}
			else if (data instanceof ItinCardDataHotel
					&& startDate.getDayOfYear() == today) {
				if (summaryCardData instanceof ItinCardDataCar) {
					if (summaryCardData.getStartDate().isBefore(now)) {
						setAsSummaryCard = true;
					}
				}
				else if (summaryCardData == null) {
					setAsSummaryCard = true;
				}
			}
			else if (startDate.isAfter(now) && summaryCardData == null) {
				setAsSummaryCard = true;
			}

			if (setAsSummaryCard) {
				summaryCardPosition = a;
				summaryCardData = data;
			}

			if (firstInProgressCard == null && data.getEndDate().isAfter(now)) {
				firstInProgressCardPos = a;
				firstInProgressCard = data;
			}
		}

		if (summaryCardData != null) {
			long threeHours = 3 * DateUtils.HOUR_IN_MILLIS;

			// If:
			// 1. The current summary card starts after the first in-progress card ends
			// 2. The current summary card is not happening in the next 3 hours
			// Use the first in-progress card as summary instead
			DateTime startDate = summaryCardData.getStartDate();
			if (firstInProgressCard.getEndDate().isBefore(startDate)
					&& nowMillis < startDate.getMillis() - threeHours) {
				summaryCardPosition = firstInProgressCardPos;
				summaryCardData = firstInProgressCard;
			}

			// See if we have an alt summary card we want
			if (summaryCardPosition + 1 < len) {
				ItinCardData possibleAlt = itinCardDatas.get(summaryCardPosition + 1);

				if (isValidForSummary(possibleAlt)) {
					long startMillis = possibleAlt.getStartDate().getMillis();
					if (possibleAlt.hasDetailData() && nowMillis > startMillis - threeHours) {
						altSummaryCardPosition = summaryCardPosition + 1;
					}
				}
			}
		}
		else {
			// Check if last card hasn't ended; if so, make it the main summary card
			ItinCardData lastCard = itinCardDatas.get(len - 1);
			if (lastCard.hasDetailData() && lastCard.getEndDate().isAfter(now)) {
				summaryCardPosition = len - 1;
			}
		}

		return new Pair<Integer, Integer>(summaryCardPosition, altSummaryCardPosition);
	}

	private boolean isValidForSummary(ItinCardData data) {
		return data.hasSummaryData() && data.hasDetailData() && data.getStartDate() != null;
	}

	private void addHotelAttachData(List<ItinCardData> itinCardDatas) {
		// Is Hotel Attach turned off?
		if (SettingUtils.get(mContext, R.string.setting_hide_hotel_attach, false)) {
			return;
		}

		// Nothing to do if there are no itineraries
		int len = itinCardDatas.size();
		if (len == 0) {
			return;
		}

		// Get previously dismissed buttons
		final HashSet<String> dismissedTripIds = DismissedItinButton
				.getDismissedTripIds(ItinButtonCard.ItinButtonType.HOTEL_ATTACH);

		for (int i = 0; i < len; i++) {
			ItinCardData data = itinCardDatas.get(i);
			DateTime start = data.getStartDate();
			DateTime now = DateTime.now(start.getZone());

			// Ignore dismissed buttons
			if (dismissedTripIds.contains(data.getTripId())) {
				continue;
			}

			// Ignore past itineraries
			if (now.isAfter(start) && now.getDayOfYear() > start.getDayOfYear()) {
				continue;
			}

			// Ignore non-flight itineraries
			if (!data.getTripComponentType().equals(Type.FLIGHT) || !(data instanceof ItinCardDataFlight)) {
				continue;
			}

			// Ignore shared itins
			if (data.getTripComponent().getParentTrip().isShared()) {
				continue;
			}

			// Ignore last leg flights
			TripFlight tripFlight = (TripFlight) data.getTripComponent();
			final int legCount = tripFlight.getFlightTrip().getLegCount();
			if (legCount > 0 && ((ItinCardDataFlight) data).getLegNumber() == legCount - 1) {
				continue;
			}

			// See if this flight is:
			// 1. Has an ending Flight from the same Trip, arriving and departing from same airport
			// 2. Has no hotels/flights in between now and the ending flight
			// 3. There is more than 1 day between this flight and the ending flight
			for (int j = i + 1; j < len; j++) {
				ItinCardData nextData = itinCardDatas.get(j);
				Type nextType = nextData.getTripComponentType();

				if (nextType == Type.HOTEL) {
					break;
				}
				else if (nextType == Type.FLIGHT) {
					if (data.getTripId().equals(nextData.getTripId()) && nextData instanceof ItinCardDataFlight) {
						FlightLeg firstLeg = ((ItinCardDataFlight) data).getFlightLeg();
						FlightLeg secondLeg = ((ItinCardDataFlight) nextData).getFlightLeg();
						Waypoint waypointOne = firstLeg.getLastWaypoint();
						Waypoint waypointTwo = secondLeg.getFirstWaypoint();

						if (waypointOne.mAirportCode.equals(waypointTwo.mAirportCode)) {
							DateTime dateTimeOne = new DateTime(waypointOne.getMostRelevantDateTime());
							DateTime dateTimeTwo = new DateTime(waypointTwo.getMostRelevantDateTime());
							if (JodaUtils.daysBetween(dateTimeOne, dateTimeTwo) != 0) {
								// Add HA button
								itinCardDatas.add(i + 1, new ItinCardDataHotelAttach(tripFlight, firstLeg, secondLeg));

								return;
							}
						}
					}

					// If we get to here, then this means it was a non-valid hotel attaching flight, so break
					break;
				}
			}
		}
	}

	private void addLocalExpertData(List<ItinCardData> itinCardDatas) {
		// Is Local Expert turned off?
		if (SettingUtils.get(mContext, R.string.setting_hide_local_expert, false)) {
			return;
		}

		// Nothing to do if there are no itineraries
		int len = itinCardDatas.size();
		if (len == 0) {
			return;
		}

		// Get previously dismissed buttons
		final HashSet<String> dismissedTripIds = DismissedItinButton
				.getDismissedTripIds(ItinButtonCard.ItinButtonType.LOCAL_EXPERT);

		ItinCardData data;
		for (int i = 0; i < len; i++) {
			data = itinCardDatas.get(i);

			// Ignore dismissed trips
			if (dismissedTripIds.contains(data.getTripId())) {
				continue;
			}

			// Only attach to hotels
			if (!data.getTripComponentType().equals(Type.HOTEL) || !(data instanceof ItinCardDataHotel)) {
				continue;
			}

			// Is this a valid location?
			if (!ItinCardDataLocalExpert.validLocation(((ItinCardDataHotel) data).getPropertyLocation())) {
				continue;
			}

			// Are we presently in the trip?
			if (!ItinCardDataLocalExpert.validDateTime(data.getStartDate(), data.getEndDate())) {
				continue;
			}

			// Ignore shared itins
			if (data.isSharedItin()) {
				continue;
			}

			// Add LE button
			itinCardDatas.add(i + 1, new ItinCardDataLocalExpert((TripHotel) data.getTripComponent()));

			return;
		}
	}

	// Used only for Omniture tracking
	//
	// Returns a delimited list of the local expert destinations, or the empty string
	// for none.
	public String getTrackingLocalExpertDestinations() {
		Set<String> dests = new HashSet<String>();
		for (ItinCardData data : mItinCardDatas) {
			if (data instanceof ItinCardDataLocalExpert) {
				Destination destination = ((ItinCardDataLocalExpert) data).getSiteDestination();
				dests.add(destination.getTrackingId());
			}
		}

		return TextUtils.join("|", dests);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// LISTENER IMPLEMENTATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	// ItinCard button click listener

	@Override
	public void onCloseButtonClicked() {
		// Pass the click event back up to the ListView to handle the closing of the card
		if (mOnItinCardClickListener != null) {
			mOnItinCardClickListener.onCloseButtonClicked();
		}
	}

	// ItinButtonCard hide listener

	@Override
	public void onHide(String tripId, ItinButtonType itinButtonType) {
		syncWithManager();
	}

	@Override
	public void onHideAll(ItinButtonType itinButtonType) {
		syncWithManager();
	}
}
