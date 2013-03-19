package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncListener;
import com.expedia.bookings.data.trips.ItineraryManager.SyncError;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.widget.ItinCard;
import com.expedia.bookings.widget.ItinCard.OnItinCardClickListener;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.Log;

public class ItinCardDataAdapter extends BaseAdapter implements ItinerarySyncListener, OnItinCardClickListener {

	private static final int CUTOFF_HOURS = 48;

	private enum State {
		PAST,
		SUMMARY,
		NORMAL,
		DETAIL
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Context mContext;
	private ItineraryManager mItinManager;
	private ArrayList<ItinCardData> mItinCardDatas;
	private int mDetailPosition = -1;
	private String mSelectedCardId;

	private boolean mSimpleMode = false;

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
		ItinCard card = (ItinCard) convertView;
		if (card == null) {
			card = new ItinCard(mContext);
			card.setOnItinCardClickListener(this);
		}

		State state = getItemViewCardState(position);

		ItinCardData data = getItem(position);

		card.setCardSelected(mSimpleMode && data.getId().equals(mSelectedCardId));
		card.setCardShaded(state == State.PAST);
		card.bind(data);
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
		Type type = getItem(position).getTripComponentType();
		int retVal = type.ordinal();
		boolean isInThePast = isItemInThePast(position);
		boolean isSumCard = isItemASummaryCard(position);
		boolean isDetailCard = isItemDetailCard(position);
		if (isDetailCard) {
			retVal += (TripComponent.Type.values().length * 3);
		}
		else if (isInThePast) {
			retVal += TripComponent.Type.values().length;
		}
		else if (isSumCard && !mSimpleMode) {
			retVal += (TripComponent.Type.values().length * 2);
		}
		return retVal;
	}

	@Override
	public int getViewTypeCount() {
		//the *3 is so we have one for each type and one for each type that is shaded and one for each type in summary mode
		return TripComponent.Type.values().length * State.values().length;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// ItinerarySyncListener
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onTripAdded(Trip trip) {
	}

	@Override
	public void onTripUpdated(Trip trip) {
		Log.d("ItinCardDataAdapter - onTripUpdated " + trip.getTripId());
		ItineraryManager.broadcastTripRefresh(mContext, trip);

	}

	@Override
	public void onTripUpdateFailed(Trip trip) {
		Log.d("ItinCardDataAdapter - onTripUpdateFailed " + trip.getTripId());
		// Note: Must broadcast that an update failed so that the ItinCard gets notified to redraw (and thus remove the
		// ProgressBar from being present on the screen).
		ItineraryManager.broadcastTripRefresh(mContext, trip);
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

	public void setDetailPosition(int position) {
		mDetailPosition = position;
	}

	public void setSelectedCardId(String cardId) {
		mSelectedCardId = cardId;
	}

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
					List<TripComponent> components = trip.getTripComponents(true);
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
		int retVal = mItinCardDatas.size() - 1;
		Calendar now = Calendar.getInstance();
		for (int i = 0; i < mItinCardDatas.size(); i++) {
			ItinCardData data = mItinCardDatas.get(i);
			// IN PROGRESS flights are relevant, most def.
			if (doesCardStartAfterCal(data, now)
					|| data.getTripComponentType() == TripComponent.Type.FLIGHT
					&& isCardInProgressAtCal(data, now)) {
				//The card with the next startTime
				retVal = i;
				break;
			}
		}
		//Return the last card otherwise, because if we got here, all our itins are in the past...
		return retVal;
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

	// Cal is after start time but before finish time
	private boolean isCardInProgressAtCal(ItinCardData data, Calendar cal) {
		if (data == null || data.getEndDate() == null || data.getStartDate() == null) {
			return false;
		}

		long calTime = cal.getTimeInMillis();
		long start = data.getStartDate().getCalendar().getTimeInMillis();

		if (calTime < start) {
			return false;
		}

		long end = data.getEndDate().getCalendar().getTimeInMillis();
		if (calTime > end) {
			return false;
		}

		return true;
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
		case 3:
			return State.DETAIL;
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

	private boolean isItemDetailCard(int position) {
		return (position == mDetailPosition);
	}

	private List<Integer> getSummaryCardPositions() {
		ArrayList<Integer> sumCardPositions = new ArrayList<Integer>();

		Calendar now = Calendar.getInstance();
		Calendar futureThreshold = Calendar.getInstance();
		futureThreshold.add(Calendar.HOUR, 2);

		int firstCardPos = getMostRelevantCardPosition();
		for (int i = firstCardPos; i < mItinCardDatas.size(); i++) {
			ItinCardData data = mItinCardDatas.get(i);
			if (!data.hasSummaryData()) {
				continue;
			}
			if (i == firstCardPos) {
				sumCardPositions.add(i);
				continue;
			}
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
		Collections.sort(mItinCardDatas, mItinCardDataComparator);
	}

	private Comparator<ItinCardData> mItinCardDataComparator = new Comparator<ItinCardData>() {
		@Override
		public int compare(ItinCardData dataOne, ItinCardData dataTwo) {
			// Sort by:
			// 1. "checkInDate" (but ignoring the time)
			// 2. Type (flight < car < activity < hotel < cruise)
			// 3. "checkInDate" (including time)
			// 4. Unique ID

			// 1
			int comparison = dataOne.getStartDateSerialized() - dataTwo.getStartDateSerialized();
			if (comparison != 0) {
				return comparison;
			}

			// 2
			comparison = dataOne.getTripComponentType().ordinal() - dataTwo.getTripComponentType().ordinal();
			if (comparison != 0) {
				return comparison;
			}

			// 3
			comparison = (int) (dataOne.getStartDate().getMillisFromEpoch() - dataTwo.getStartDate()
					.getMillisFromEpoch());
			if (comparison != 0) {
				return comparison;
			}

			// 4
			comparison = dataOne.getId().compareTo(dataTwo.getId());

			return comparison;
		}
	};

	@Override
	public void onCloseButtonClicked() {
		if (mOnItinCardClickListener != null) {
			mOnItinCardClickListener.onCloseButtonClicked();
		}
	}

	@Override
	public void onShareButtonClicked(ItinContentGenerator<?> generator) {
		if (mOnItinCardClickListener != null) {
			mOnItinCardClickListener.onShareButtonClicked(generator);
		}
	}
}
