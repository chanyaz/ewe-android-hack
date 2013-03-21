package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

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
	private int mSummaryCardPosition;
	private int mAltSummaryCardPosition;
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
		// Add Items (we add to a new list so we can change the list if need be internally)
		mItinCardDatas.clear();
		mItinCardDatas.addAll(mItinManager.getItinCardData());

		// Do some calculations on the data
		organizeData();

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
		default:
			return State.NORMAL;
		}
	}

	private boolean isItemInThePast(int position) {
		ItinCardData data = mItinCardDatas.get(position);
		if (data == null || data.getEndDate() == null) {
			return false;
		}

		Calendar endCal = data.getEndDate().getCalendar();
		int endYear = endCal.get(Calendar.YEAR);
		int endDay = endCal.get(Calendar.DAY_OF_YEAR);

		Calendar now = Calendar.getInstance();
		int thisYear = now.get(Calendar.YEAR);
		int thisDay = now.get(Calendar.DAY_OF_YEAR);

		return (endYear == thisYear && endDay < thisDay) || endYear < thisYear;
	}

	private boolean isItemASummaryCard(int position) {
		return position == mSummaryCardPosition || position == mAltSummaryCardPosition;
	}

	private boolean isItemDetailCard(int position) {
		return (position == mDetailPosition);
	}

	// Assumes the list is sorted ahead of time
	private void organizeData() {
		// Reset calculated data
		mSummaryCardPosition = -1;
		mAltSummaryCardPosition = -1;

		// Nothing to do if there are no itineraries
		int len = mItinCardDatas.size();
		if (len == 0) {
			return;
		}

		// Calculate the summary (and possibly alternate) positions
		ItinCardData summaryCardData = null;
		ItinCardData firstInProgressCard = null;
		int firstInProgressCardPos = -1;
		Calendar now = Calendar.getInstance();
		long nowMillis = now.getTimeInMillis();
		int today = now.get(Calendar.DAY_OF_YEAR);
		for (int a = 0; a < len; a++) {
			boolean setAsSummaryCard = false;

			ItinCardData data = mItinCardDatas.get(a);
			Calendar startCal = data.getStartDate().getCalendar();

			if (data instanceof ItinCardDataFlight && ((ItinCardDataFlight) data).isEnRoute()) {
				setAsSummaryCard = true;
			}
			else if (data instanceof ItinCardDataHotel
					&& startCal.get(Calendar.DAY_OF_YEAR) == today) {
				if (summaryCardData instanceof ItinCardDataCar) {
					if (summaryCardData.getStartDate().getCalendar().before(now)) {
						setAsSummaryCard = true;
					}
				}
				else if (summaryCardData == null) {
					setAsSummaryCard = true;
				}
			}
			else if (startCal.after(now) && summaryCardData == null) {
				setAsSummaryCard = true;
			}

			if (setAsSummaryCard) {
				mSummaryCardPosition = a;
				summaryCardData = data;
			}

			if (firstInProgressCard == null && data.getEndDate().getCalendar().after(now)) {
				firstInProgressCardPos = a;
				firstInProgressCard = data;
			}
		}

		if (summaryCardData != null) {
			long threeHours = 1000 * 60 * 60 * 3;

			// If:
			// 1. The current summary card starts after the first in-progress card ends
			// 2. The current summary card is not happening in the next 3 hours
			// Use the first in-progress card as summary instead
			Calendar startDate = summaryCardData.getStartDate().getCalendar();
			if (firstInProgressCard.getEndDate().getCalendar().before(startDate)
					&& nowMillis < startDate.getTimeInMillis() - threeHours) {
				mSummaryCardPosition = firstInProgressCardPos;
				summaryCardData = firstInProgressCard;
			}

			// See if we have an alt summary card we want
			if (mSummaryCardPosition + 1 < len) {
				ItinCardData possibleAlt = mItinCardDatas.get(mSummaryCardPosition + 1);
				long startMillis = possibleAlt.getStartDate().getCalendar().getTimeInMillis();
				if (nowMillis > startMillis - threeHours) {
					mAltSummaryCardPosition = mSummaryCardPosition + 1;
				}
			}
		}
		else {
			// Check if last card hasn't ended; if so, make it the main summary card
			ItinCardData lastCard = mItinCardDatas.get(len - 1);
			if (lastCard.getEndDate().getCalendar().getTimeInMillis() > now.getTimeInMillis()) {
				mSummaryCardPosition = len - 1;
			}
		}
	}

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
