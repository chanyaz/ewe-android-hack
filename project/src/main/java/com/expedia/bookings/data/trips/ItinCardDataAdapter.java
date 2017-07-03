package com.expedia.bookings.data.trips;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.expedia.bookings.itin.data.ItinCardDataHotel;
import com.expedia.bookings.tracking.ItinPageUsableTrackingData;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.joda.time.DateTime;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.model.DismissedItinButton;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.widget.itin.FlightItinCard;
import com.expedia.bookings.widget.itin.HotelItinCard;
import com.expedia.bookings.widget.itin.ItinAirAttachCard;
import com.expedia.bookings.widget.itin.ItinButtonCard;
import com.expedia.bookings.widget.itin.ItinButtonCard.ItinButtonType;
import com.expedia.bookings.widget.itin.ItinButtonCard.OnHideListener;
import com.expedia.bookings.widget.itin.ItinCard;
import com.expedia.bookings.widget.itin.ItinCard.OnItinCardClickListener;
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
		BUTTON,
		AIR_ATTACH
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Context mContext;
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
		mItinCardDatas = new ArrayList<ItinCardData>();
		mItinCardDatasSync = new ArrayList<ItinCardData>();
		Ui.getApplication(context).defaultTripComponents();
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
	public synchronized View getView(final int position, View convertView, ViewGroup parent) {
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
		else if (isItemAnAirAttachCard(position)) {
			ItinAirAttachCard card;
			if (convertView instanceof ItinAirAttachCard) {
				card = (ItinAirAttachCard) convertView;
			}
			else {
				card = new ItinAirAttachCard(mContext);
				card.setOnHideListener(this);
			}

			card.bind((ItinCardDataAirAttach) data);

			return card;
		}
		else {
			ItinCard card;
			if (data instanceof ItinCardDataHotel) {
				if (convertView instanceof HotelItinCard) {
					card = (HotelItinCard) convertView;
				}
				else {
					card = new HotelItinCard(mContext, null);
					card.setOnItinCardClickListener(this);
				}
			}
			else if (data instanceof ItinCardDataFlight) {
				if (convertView instanceof FlightItinCard) {
					card = (FlightItinCard) convertView;
				}
				else {
					card = new FlightItinCard(mContext, null);
					card.setOnItinCardClickListener(this);
				}
			}
			else {
				if (convertView instanceof ItinCard) {
					card = (ItinCard) convertView;
				}
				else {
					card = new ItinCard(mContext);
					card.setOnItinCardClickListener(this);
				}
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
		boolean isAirAttachCard = isItemAnAirAttachCard(position);
		if (isDetailCard) {
			retVal += (TripComponent.Type.values().length * State.DETAIL.ordinal());
		}
		else if (isInThePast) {
			retVal += TripComponent.Type.values().length;
		}
		else if (isSumCard && !mSimpleMode) {
			retVal += (TripComponent.Type.values().length * State.NORMAL.ordinal());
		}
		else if (isButtonCard) {
			retVal += (TripComponent.Type.values().length * State.BUTTON.ordinal());
		}
		else if (isAirAttachCard) {
			retVal += (TripComponent.Type.values().length * State.AIR_ATTACH.ordinal());
		}

		return retVal;
	}

	@Override
	public int getViewTypeCount() {
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
		mItinCardDatasSync.addAll(getItineraryManagerInstance().getItinCardData());

		// Add air attach and hotel attach cards where applicable
		addAttachData(mItinCardDatasSync);

		// Add lx attach cards where applicable.
		addLXAttachData(mItinCardDatasSync);

		// Do some calculations on the data
		Pair<Integer, Integer> summaryCardPositions = calculateSummaryCardPositions(mItinCardDatasSync);

		// Add to actual data
		mItinCardDatas.clear();
		mItinCardDatas.addAll(mItinCardDatasSync);
		mSummaryCardPosition = summaryCardPositions.first;
		mAltSummaryCardPosition = summaryCardPositions.second;

		//Notify listeners
		notifyDataSetChanged();

		trackItinLoginPageUsable();

		// Reset state before next sync
		mItinCardDatasSync.clear();
	}

	@Nullable
	protected ItinPageUsableTrackingData getItinPageUsableTrackingDataModel() {
		com.expedia.bookings.dagger.TripComponent tripComponent = Ui.getApplication(mContext).tripComponent();
		if (tripComponent != null) {
			return tripComponent.itinPageUsablePerformanceModel();
		}
		else {
			return null;
		}
	}

	@VisibleForTesting
	public void trackItinLoginPageUsable() {
		ItinPageUsableTrackingData itinPageUsablePerformanceModel = getItinPageUsableTrackingDataModel();
		if (itinPageUsablePerformanceModel != null) {
			if ((getItineraryManagerInstance().getItinCardData() != null
				&& getItineraryManagerInstance().getItinCardData().size() > 0)) {
				if (itinPageUsablePerformanceModel.hasStartTime()) {
					itinPageUsablePerformanceModel.markTripResultsUsable(System.currentTimeMillis());
					OmnitureTracking.trackItin(itinPageUsablePerformanceModel.getTripLoadPageUsableData());
				}
			}
		}
	}

	/**
	 * Empty the Adapter and fire notifyDataSetChanged(). This does not remove any underlying data from ItineraryManager.
	 * <p>
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
	 *
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

	protected ItineraryManager getItineraryManagerInstance() {
		return ItineraryManager.getInstance();
	}

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
		case 5:
			return State.AIR_ATTACH;
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
		return (item instanceof ItinCardDataHotelAttach || item instanceof ItinCardDataLXAttach);
	}

	private boolean isItemAnAirAttachCard(int position) {
		return getItem(position) instanceof ItinCardDataAirAttach;
	}

	// Assumes the list is sorted ahead of time
	private Pair<Integer, Integer> calculateSummaryCardPositions(List<ItinCardData> itinCardDatas) {
		// Reset calculated data
		int summaryCardPosition = -1;
		int altSummaryCardPosition = -1;

		// Nothing to do if there are no itineraries
		int len = itinCardDatas.size();
		if (len == 0) {
			return new Pair<>(summaryCardPosition, altSummaryCardPosition);
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
			if (firstInProgressCard != null
				&& firstInProgressCard.getEndDate().isBefore(startDate)
				&& nowMillis < startDate.getMillis() - threeHours) {
				summaryCardPosition = firstInProgressCardPos;
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

		return new Pair<>(summaryCardPosition, altSummaryCardPosition);
	}

	private boolean isValidForSummary(ItinCardData data) {
		return data.hasSummaryData() && data.hasDetailData() && data.getStartDate() != null;
	}

	/**
	 * We add attach cross-sell message cards to the timeline after certain flights.
	 * All one-way flights should be followed by an attach card that links to a one-night hotel search in the destination city.
	 * Multi-leg trips should have attach cards between flight legs for the intervening dates.
	 * We do not show these cards when there is already a hotel in the timeline on the same day the flight lands.
	 */

	private void addAttachData(List<ItinCardData> itinCardDatas) {
		// Don't add attach cards if POS does not support hotel x-sell
		if (!PointOfSale.getPointOfSale().showHotelCrossSell()) {
			return;
		}
		// Nothing to do if there are no itineraries
		int len = itinCardDatas.size();
		if (len == 0) {
			return;
		}
		final HashSet<String> dismissedTripIds = getDismissedHotelAndFlightButtons();


		boolean isUserAirAttachQualified = Db.getTripBucket() != null &&
			Db.getTripBucket().isUserAirAttachQualified();

		for (int i = 0; i < len; i++) {
			ItinCardData data = itinCardDatas.get(i);
			DateTime start = data.getStartDate();
			DateTime currentDate = DateTime.now(start.getZone());

			if (ignoreDismissedTripIds(dismissedTripIds, data)) {
				continue;
			}

			if (ignoreItinCardDataFallback(data)) {
				continue;
			}

			if (ignorePastItineraries(start, currentDate)) {
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

			FlightLeg itinFlightLeg = ((ItinCardDataFlight) data).getFlightLeg();
			Waypoint itinDestination = itinFlightLeg.getLastWaypoint();
			TripFlight tripFlight = (TripFlight) data.getTripComponent();
			final int legCount = tripFlight.getFlightTrip().getLegCount();

			// Ignore last leg flights for a multi-leg trip
			if (legCount > 1 && ((ItinCardDataFlight) data).getLegNumber() == legCount - 1) {
				continue;
			}

			boolean insertButtonCard = false;
			FlightLeg nextFlightLeg = null;

			// Check if there is a next itin card to compare to
			if (i < len - 1) {
				ItinCardData nextData = itinCardDatas.get(i + 1);
				Type nextType = nextData.getTripComponentType();
				DateTime dateTimeOne = new DateTime(itinDestination.getMostRelevantDateTime());

				if (ignoreItinCardDataFallback(nextData)) {
					continue;
				}

				// Always add an attach button for one-way flights with no hotel
				if (legCount == 1 && !(nextType == Type.HOTEL)) {
					insertButtonCard = true;
				}

				// If the next itin is a flight
				else if (nextType == Type.FLIGHT) {
					nextFlightLeg = ((ItinCardDataFlight) nextData).getFlightLeg();
					Waypoint waypointTwo = nextFlightLeg.getFirstWaypoint();
					DateTime dateTimeTwo = new DateTime(waypointTwo.getMostRelevantDateTime());

					// Make sure there is more than 1 day between the two flights
					if (JodaUtils.daysBetween(dateTimeOne, dateTimeTwo) > 0) {
						insertButtonCard = true;

						// If the flights are not part of the same trip,
						// we won't use the next flight to generate search params
						if (!itinDestination.mAirportCode.equals(waypointTwo.mAirportCode) ||
							!(tripFlight.getParentTrip() == nextData.getTripComponent().getParentTrip())) {
							nextFlightLeg = null;
						}
					}
				}

				// If the next itin is a hotel
				else if (nextType == Type.HOTEL) {
					DateTime checkInDate = nextData.getStartDate();
					if (JodaUtils.daysBetween(dateTimeOne, checkInDate) > 0) {
						insertButtonCard = true;
					}
				}

				// If the next card in the timeline is neither a flight nor a hotel, show attach
				else {
					insertButtonCard = true;
				}
			}
			// The flight is the last itin
			else if (i == len - 1) {
				insertButtonCard = true;
			}

			if (insertButtonCard) {
				((ItinCardDataFlight) data).setShowAirAttach(true);
				((ItinCardDataFlight) data).setNextFlightLeg(nextFlightLeg);
				// Check if user qualifies for air attach
				if (isUserAirAttachQualified) {
					itinCardDatas
						.add(i + 1, new ItinCardDataAirAttach(tripFlight, itinFlightLeg, nextFlightLeg));
					len++;
					i++;
				}
				// Show default hotel cross-sell button
				else {
					itinCardDatas
						.add(i + 1, new ItinCardDataHotelAttach(tripFlight, itinFlightLeg, nextFlightLeg));
					len++;
					i++;
				}
			}
		}
	}

	@NonNull
	protected HashSet<String> getDismissedHotelAndFlightButtons() {
		// Get previously dismissed buttons
		final HashSet<String> dismissedTripIds = DismissedItinButton
			.getDismissedTripIds(ItinButtonType.HOTEL_ATTACH);
		final HashSet<String> dismissedAirAttach = DismissedItinButton
			.getDismissedTripIds(ItinButtonType.AIR_ATTACH);
		dismissedTripIds.addAll(dismissedAirAttach);
		return dismissedTripIds;
	}

	protected HashSet<String> getDismissedLXAttachButtons() {
		return DismissedItinButton
			.getDismissedTripIds(ItinButtonType.LX_ATTACH);
	}

	private void addLXAttachData(List<ItinCardData> itinCardDatas) {
		if (!PointOfSale.getPointOfSale().supports(LineOfBusiness.LX)) {
			return;
		}
		// Nothing to do if there are no itineraries
		int len = itinCardDatas.size();
		if (len == 0) {
			return;
		}
		// Get previously dismissed buttons
		final HashSet<String> dismissedTripIds = getDismissedLXAttachButtons();

		for (int i = 0; i < len; i++) {
			ItinCardData data = itinCardDatas.get(i);
			DateTime start = data.getStartDate();
			DateTime currentDate = DateTime.now(start.getZone());

			if (ignoreDismissedTripIds(dismissedTripIds, data)) {
				continue;
			}

			if (ignoreItinCardDataFallback(data)) {
				continue;
			}

			if (ignorePastItineraries(start, currentDate)) {
				continue;
			}

			if (ignoreNonHotelItineraries(data)) {
				continue;
			}

			TripHotel tripHotel = (TripHotel) data.getTripComponent();
			itinCardDatas
				.add(i + 1, new ItinCardDataLXAttach(tripHotel));
			len++;
			i++;

		}
	}

	private boolean ignoreNonHotelItineraries(ItinCardData data) {
		if (!data.getTripComponentType().equals(Type.HOTEL) || !(data instanceof ItinCardDataHotel)) {
			return true;
		}
		return false;
	}

	private boolean ignoreItinCardDataFallback(ItinCardData data) {
		if (data instanceof ItinCardDataFallback) {
			return true;
		}
		return false;
	}

	private boolean ignorePastItineraries(DateTime start, DateTime currentDate) {
		if (currentDate.isAfter(start) && currentDate.getDayOfYear() > start.getDayOfYear()) {
			return true;
		}
		return false;
	}

	private boolean ignoreDismissedTripIds(HashSet<String> dismissedTripIds, ItinCardData data) {
		if (dismissedTripIds.contains(data.getTripId())) {
			return true;
		}
		return false;
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
		for (ItinCardData itinCardData : mItinCardDatas) {
			if (itinCardData instanceof ItinCardDataHotelAttach && itinButtonType == ItinButtonType.HOTEL_ATTACH) {
				DismissedItinButton.dismiss(itinCardData.getTripId(), itinButtonType);
			}
			else if (itinCardData instanceof ItinCardDataAirAttach && itinButtonType == ItinButtonType.AIR_ATTACH) {
				DismissedItinButton.dismiss(itinCardData.getTripId(), itinButtonType);
			}
		}
		syncWithManager();
	}
}
