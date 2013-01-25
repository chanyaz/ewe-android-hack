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
import com.expedia.bookings.widget.FlightItinCard;
import com.expedia.bookings.widget.HotelItinCard;
import com.expedia.bookings.widget.ItinCard;

public class TripComponentAdapter extends BaseAdapter implements ItinerarySyncListener {

	public enum TripComponentSortOrder {
		START_DATE
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Context mContext;
	private ItineraryManager mItinManager;
	private ArrayList<TripComponent> mTripComponents;
	private TripComponentSortOrder mSortOrder = TripComponentSortOrder.START_DATE;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	//////////////////////////////////////////////////////////////////////////////////////

	public TripComponentAdapter(Context context) {
		mContext = context;
		mItinManager = ItineraryManager.getInstance();
		mTripComponents = new ArrayList<TripComponent>();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// BaseAdapter methods
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public synchronized int getCount() {
		if (mTripComponents != null) {
			return mTripComponents.size();
		}

		return 0;
	}

	@Override
	public synchronized TripComponent getItem(int position) {
		if (mTripComponents != null) {
			return mTripComponents.get(position);
		}

		return null;
	}

	@Override
	public synchronized long getItemId(int position) {
		if (mTripComponents != null) {
			return position;
		}

		return -1;
	}

	@Override
	public synchronized View getView(final int position, View convertView, ViewGroup Parent) {
		ItinCard card;
		if (convertView != null && convertView instanceof ItinCard) {
			card = (ItinCard) convertView;
		}
		else {
			switch (getItem(position).getType()) {
			case FLIGHT: {
				card = new FlightItinCard(mContext);
				break;
			}
			default:
			case HOTEL: {
				card = new HotelItinCard(mContext);
				break;
			}
			}
		}

		//bind card and stuff...
		card.bind(getItem(position));
		card.showSummary(position == 0);

		return card;
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
	public void onSyncFinished(Collection<Trip> trips) {
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
		mTripComponents.clear();
		Collection<Trip> trips = mItinManager.getTrips();
		if (trips != null) {
			for (Trip trip : trips) {
				if (trip.getTripComponents() != null) {
					List<TripComponent> components = trip.getTripComponents();
					for (TripComponent comp : components) {
						mTripComponents.add(comp);
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
			Collections.sort(mTripComponents, mTripComponentStartDateComparator);
		}
	}

	Comparator<TripComponent> mTripComponentStartDateComparator = new Comparator<TripComponent>() {

		@Override
		public int compare(TripComponent compOne, TripComponent compTwo) {
			if (compOne.getStartDate() == null) {
				return -1;
			}
			if (compTwo.getStartDate() == null) {
				return 1;
			}
			return compOne.getStartDate().compareTo(compTwo.getStartDate());
		}
	};

}
