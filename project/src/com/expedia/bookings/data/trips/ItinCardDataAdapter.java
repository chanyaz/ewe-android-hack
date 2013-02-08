package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncListener;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.widget.ActivityItinCard;
import com.expedia.bookings.widget.CarItinCard;
import com.expedia.bookings.widget.CruiseItinCard;
import com.expedia.bookings.widget.FlightItinCard;
import com.expedia.bookings.widget.HotelItinCard;
import com.expedia.bookings.widget.ItinCard;
import com.mobiata.android.Log;

public class ItinCardDataAdapter extends BaseAdapter implements ItinerarySyncListener {

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
			Type cardType = Type.values()[getItemViewType(position)];
			switch (cardType) {
			case HOTEL: {
				card = new HotelItinCard(mContext);
				break;
			}
			case FLIGHT: {
				card = new FlightItinCard(mContext);
				break;
			}
			case CAR: {
				card = new CarItinCard(mContext);
				break;
			}
			case CRUISE: {
				card = new CruiseItinCard(mContext);
				break;
			}
			case ACTIVITY: {
				card = new ActivityItinCard(mContext);
				break;
			}
			default:
				throw new RuntimeException("The card type doesn't match any of our predefined types.");
			}
		}

		card.bind(getItem(position));
		card.setShowSummary(position == 0);

		if (position == 0) {
			card.updateSummaryVisibility();
		}

		return card;
	}

	@Override
	public int getItemViewType(int position) {
		Type type = getItem(position).getTripComponent().getType();
		return type.ordinal();
	}

	@Override
	public int getViewTypeCount() {
		return TripComponent.Type.values().length;
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
		if (trips != null) {
			for (Trip trip : trips) {
				if (trip.getTripComponents() != null) {
					List<TripComponent> components = trip.getTripComponents();
					for (TripComponent comp : components) {
						//mItinCardDatas.add(comp);
						List<ItinCardData> items = ItinCardDataFactory.generateCardData(comp);
						if (items != null) {
							this.mItinCardDatas.addAll(items);
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

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

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
}
