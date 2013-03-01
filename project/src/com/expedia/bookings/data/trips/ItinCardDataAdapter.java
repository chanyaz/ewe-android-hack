package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncListener;
import com.expedia.bookings.data.trips.ItineraryManager.SyncError;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.widget.ActivityItinCard;
import com.expedia.bookings.widget.CarItinCard;
import com.expedia.bookings.widget.CruiseItinCard;
import com.expedia.bookings.widget.FlightItinCard;
import com.expedia.bookings.widget.HotelItinCard;
import com.expedia.bookings.widget.ItinCard;
import com.expedia.bookings.widget.ItinCard.OnItinCardClickListener;
import com.mobiata.android.Log;

public class ItinCardDataAdapter extends BaseAdapter implements ItinerarySyncListener, OnItinCardClickListener {

	private static final int CUTOFF_HOURS = 48;

	public enum TripComponentSortOrder {
		START_DATE
	}

	private enum State {
		PAST,
		SUMMARY,
		NORMAL,
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Context mContext;
	private ItineraryManager mItinManager;
	private ArrayList<ItinCardData> mItinCardDatas;
	private TripComponentSortOrder mSortOrder = TripComponentSortOrder.START_DATE;

	private OnItinCardClickListener mOnItinCardClickListener;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinCardDataAdapter(Context context) {
		mContext = context;
		mItinManager = ItineraryManager.getInstance();
		mItinCardDatas = new ArrayList<ItinCardData>();
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
		if (mItinCardDatas != null) {
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
		ItinCard card = (ItinCard) convertView;
		if (card == null) {
			Type cardType = getItemViewCardType(position);
			switch (cardType) {
			case HOTEL: {
				card = new HotelItinCard(mContext);
				card.setOnItinCardClickListener(this);
				break;
			}
			case FLIGHT: {
				card = new FlightItinCard(mContext);
				card.setOnItinCardClickListener(this);
				break;
			}
			case CAR: {
				card = new CarItinCard(mContext);
				card.setOnItinCardClickListener(this);
				break;
			}
			case CRUISE: {
				card = new CruiseItinCard(mContext);
				card.setOnItinCardClickListener(this);
				break;
			}
			case ACTIVITY: {
				card = new ActivityItinCard(mContext);
				card.setOnItinCardClickListener(this);
				break;
			}
			default:
				throw new RuntimeException("The card type doesn't match any of our predefined types.");
			}
		}

		State state = getItemViewCardState(position);

		card.setCardShaded(state == State.PAST);
		card.bind(getItem(position));
		card.setShowSummary(state == State.SUMMARY);

		if (state == State.SUMMARY) {
			card.updateSummaryVisibility();
		}

		card.setShowExtraTopPadding(position == 0);
		card.setShowExtraBottomPadding(position == getCount() - 1);

		return card;
	}

	@Override
	public int getItemViewType(int position) {
		//Note: Our types are: 
		//normal cards: 0 <= TYPE < Type.values().length
		//shaded cards: Type.values().length <= TYPE < Type.values().length * 2
		//summary cards: Type.values().length * 2 <= TYPE < Type.values().length * 3
		Type type = getItem(position).getTripComponent().getType();
		int retVal = type.ordinal();
		boolean isInThePast = isItemInThePast(position);
		boolean isSumCard = isItemASummaryCard(position);
		if (isInThePast) {
			retVal += TripComponent.Type.values().length;
		}
		else if (isSumCard) {
			retVal += (TripComponent.Type.values().length * 2);
		}
		return retVal;
	}

	@Override
	public int getViewTypeCount() {
		//the *3 is so we have one for each type and one for each type that is shaded and one for each type in summary mode
		return TripComponent.Type.values().length * 3;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// ItinerarySyncListener
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onTripAdded(Trip trip) {
	}

	@Override
	public void onTripUpdated(Trip trip) {
	}

	@Override
	public void onTripUpateFailed(Trip trip) {
	}

	@Override
	public void onTripRemoved(Trip trip) {
	}

	@Override
	public void onSyncFailure(SyncError error) {
	}

	@Override
	public void onSyncFinished(Collection<Trip> trips) {
		Log.d("ItinCardDataAdapter - ItineraryManager - onSyncFinished");
		syncWithManager();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Sync the adapter data with the ItineraryManager
	 * 
	 * If enableSelfManagement() is used, the coder does not need to call this.
	 */
	public synchronized void syncWithManager() {
		//Add Items
		mItinCardDatas.clear();
		Collection<Trip> trips = mItinManager.getTrips();
		Calendar pastCutoffCal = Calendar.getInstance();
		pastCutoffCal.add(Calendar.HOUR_OF_DAY, -CUTOFF_HOURS);
		if (trips != null) {
			for (Trip trip : trips) {
				if (trip.getTripComponents() != null) {
					List<TripComponent> components = trip.getTripComponents();
					for (TripComponent comp : components) {
						List<ItinCardData> items = ItinCardDataFactory.generateCardData(comp);
						if (items != null) {
							for (ItinCardData item : items) {
								if (item.getEndDate() != null && item.getEndDate().getCalendar() != null
										&& item.getEndDate().getCalendar().compareTo(pastCutoffCal) >= 0) {
									this.mItinCardDatas.add(item);
								}
							}
						}
					}
				}
			}
		}
		//Sort Items
		sortItems();
		//Notify listeners
		notifyDataSetChanged();
	}

	/**
	 * Calling enableSelfManagement will cause this adapter to listen for changes in the
	 * ItineraryManager on its own, and thus should always provide views that have the most
	 * recent state according to the ItineraryManager.
	 * 
	 * If enableSelfManagement is called, the coder should call disableSelfManagement to
	 * avoid keeping listeners around beyond when they are useful
	 */
	public void enableSelfManagement() {
		mItinManager.addSyncListener(this);
		syncWithManager();
	}

	/**
	 * Disables self management. See enableSelfManagement();
	 */
	public void disableSelfManagement() {
		mItinManager.removeSyncListener(this);
	}

	/**
	 * Update the sort order of the list. The default is by start date.
	 * @param order - The order to sort the items in
	 */
	public synchronized void setSortOrder(TripComponentSortOrder order) {
		mSortOrder = order;
		sortItems();
		notifyDataSetChanged();
	}

	public void setOnItinCardClickListener(OnItinCardClickListener onItinCardClickListener) {
		mOnItinCardClickListener = onItinCardClickListener;
	}

	/**
	 * The first (and usually only) summary view card position
	 * @return
	 */
	public synchronized int getMostRelevantCardPosition() {
		int retVal = mItinCardDatas.size() - 1;
		Calendar now = Calendar.getInstance();
		for (int i = 0; i < mItinCardDatas.size(); i++) {
			ItinCardData data = mItinCardDatas.get(i);
			if (doesCardStartAfterCal(data, now)) {
				//The card with the next startTime
				retVal = i;
				break;
			}
		}
		//Return the last card otherwise, because if we got here, all our itins are in the past...
		return retVal;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	//are both start and end dates before cal
	private boolean isCardBeforeCal(ItinCardData data, Calendar cal) {
		if (data == null || data.getEndDate() == null || data.getStartDate() == null) {
			return false;
		}
		else {
			long calTime = cal.getTimeInMillis();
			long start = data.getStartDate().getCalendar().getTimeInMillis();
			long end = data.getEndDate().getCalendar().getTimeInMillis();

			if (start < calTime && end < calTime) {
				return true;
			}
			else {
				return false;
			}
		}
	}

	// start after cal
	private boolean doesCardStartAfterCal(ItinCardData data, Calendar cal) {
		if (data == null || data.getStartDate() == null) {
			return false;
		}
		else {
			long calTime = cal.getTimeInMillis();
			long start = data.getStartDate().getCalendar().getTimeInMillis();

			if (start > calTime) {
				return true;
			}
			else {
				return false;
			}
		}
	}

	//start date is before cal
	private boolean doesCardStartBeforeCal(ItinCardData data, Calendar cal) {
		if (data == null || data.getEndDate() == null || data.getStartDate() == null) {
			return false;
		}
		else {
			long calTime = cal.getTimeInMillis();
			long start = data.getStartDate().getCalendar().getTimeInMillis();

			if (start < calTime) {
				return true;
			}
			else {
				return false;
			}
		}
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
		default:
			return State.NORMAL;
		}
	}

	private boolean isItemInThePast(int position) {
		if (mItinCardDatas.size() <= position) {
			return false;
		}
		else {
			ItinCardData data = mItinCardDatas.get(position);
			return isCardBeforeCal(data, Calendar.getInstance());
		}
	}

	private boolean isItemASummaryCard(int position) {
		return isItemASummaryCard(position, getSummaryCardPositions());
	}

	private boolean isItemASummaryCard(int position, List<Integer> summaryCardPositions) {
		if (!isItemInThePast(position)) {
			return summaryCardPositions.contains(Integer.valueOf(position));
		}
		return false;
	}

	private List<Integer> getSummaryCardPositions() {
		ArrayList<Integer> sumCardPositions = new ArrayList<Integer>();

		Calendar now = Calendar.getInstance();
		Calendar futureThreshold = Calendar.getInstance();
		futureThreshold.add(Calendar.HOUR, 2);

		int firstCardPos = getMostRelevantCardPosition();
		sumCardPositions.add(firstCardPos);
		for (int i = firstCardPos + 1; i < mItinCardDatas.size(); i++) {
			ItinCardData data = mItinCardDatas.get(i);
			boolean afterNow = doesCardStartAfterCal(data, now);
			boolean startsBeforeThresh = doesCardStartBeforeCal(data, futureThreshold);
			if (afterNow && startsBeforeThresh) {
				sumCardPositions.add(i);
				continue;
			}
			if (doesCardStartAfterCal(data, futureThreshold)) {
				//They are in order so if we get to this point lets just be done
				break;
			}
		}
		return sumCardPositions;
	}

	private void sortItems() {
		if (mSortOrder.equals(TripComponentSortOrder.START_DATE)) {
			Collections.sort(mItinCardDatas, mItinCardDataStartDateComparator);
		}
	}

	Comparator<ItinCardData> mItinCardDataStartDateComparator = new Comparator<ItinCardData>() {
		@Override
		public int compare(ItinCardData dataOne, ItinCardData dataTwo) {
			if (dataOne.getStartDate() == null) {
				return -1;
			}
			if (dataTwo.getStartDate() == null) {
				return 1;
			}
			return dataOne.getStartDate().compareTo(dataTwo.getStartDate());
		}
	};

	@Override
	public void onCloseButtonClicked() {
		if (mOnItinCardClickListener != null) {
			mOnItinCardClickListener.onCloseButtonClicked();
		}
	}

	@Override
	public void onShareButtonClicked(String subject, String shortMessage, String longMessage) {
		if (mOnItinCardClickListener != null) {
			mOnItinCardClickListener.onShareButtonClicked(subject, shortMessage, longMessage);
		}
	}
}