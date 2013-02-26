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

		boolean isPastCard = isItemInThePast(position);
		boolean isSummaryCard = isItemASummaryCard(position);

		card.setCardShaded(isPastCard);
		card.bind(getItem(position));
		card.setShowSummary(isSummaryCard);

		if (isSummaryCard) {
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
		Log.d("ItinCardDataAdapter - ItinerarayManager - onTripAdded");
	}

	@Override
	public void onTripUpdated(Trip trip) {
		Log.d("ItinCardDataAdapter - ItinerarayManager - onTripUpdated");
	}

	@Override
	public void onTripUpateFailed(Trip trip) {
		Log.d("ItinCardDataAdapter - ItinerarayManager - onTripUpateFailed");
	}

	@Override
	public void onTripRemoved(Trip trip) {
		Log.d("ItinCardDataAdapter - ItinerarayManager - onTripRemoved");
	}

	@Override
	public void onSyncFailure(SyncError error) {
		Log.d("ItinCardDataAdapter - ItinerarayManager - onSyncFailed");
	}

	@Override
	public void onSyncFinished(Collection<Trip> trips) {
		Log.d("ItinCardDataAdapter - ItinerarayManager - onSyncFinished");
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

	public synchronized int getMostRelevantCardPosition() {
		int retVal = mItinCardDatas.size() - 1;
		Calendar now = Calendar.getInstance();
		for (int i = 0; i < mItinCardDatas.size(); i++) {
			ItinCardData data = mItinCardDatas.get(i);
			if (data != null && data.getStartDate() != null && data.getStartDate().getCalendar() != null
					&& data.getStartDate().getCalendar().compareTo(now) >= 0) {
				// This is the first card that is after now
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

	private Type getItemViewCardType(int position) {
		int typeOrd = getItemViewType(position);
		typeOrd = typeOrd % TripComponent.Type.values().length;
		return Type.values()[typeOrd];
	}

	private boolean isItemInThePast(int position) {
		return position < getMostRelevantCardPosition();
	}

	private boolean isItemASummaryCard(int position) {
		if (!isItemInThePast(position)) {
			int[] sumCards = getSummaryCardPositions();
			for (int i = 0; i < sumCards.length; i++) {
				if (sumCards[i] == position) {
					return true;
				}
			}
		}
		return false;
	}

	private int[] getSummaryCardPositions() {
		int[] summaryCards = new int[1];
		summaryCards[0] = getMostRelevantCardPosition();
		return summaryCards;
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