package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.widget.ItinCard;
import com.expedia.bookings.widget.ItinCard.OnItinCardClickListener;
import com.expedia.bookings.widget.itin.ItinButtonCard;
import com.expedia.bookings.widget.itin.ItinContentGenerator;

public class ItinCardDataAdapter extends BaseAdapter implements OnItinCardClickListener {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE ENUMERATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	private enum State {
		PAST,
		SUMMARY,
		NORMAL,
		DETAIL
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final int TYPE_BUTTON_CARD = 0;
	private static final int TYPE_ITINERARY_CARD = 1;

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
		final ItinCardData data = getItem(position);
		final int type = getItemViewType(position);

		if (type == TYPE_BUTTON_CARD) {
			ItinButtonCard card;
			if (convertView instanceof ItinButtonCard) {
				card = (ItinButtonCard) convertView;
			}
			else {
				card = new ItinButtonCard(mContext);
				//card.setOnItinCardClickListener(this);
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
			card.bind(data);
			card.setShowSummary(state == State.SUMMARY);

			if (state == State.SUMMARY) {
				card.updateSummaryVisibility();
			}

			card.setShowExtraTopPadding(position == 0);
			card.setShowExtraBottomPadding(position == getCount() - 1);

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
		if (isButtonCard) {
			return TYPE_BUTTON_CARD;
		}
		else if (isDetailCard) {
			retVal += (TripComponent.Type.values().length * 3);
		}
		else if (isInThePast) {
			retVal += TripComponent.Type.values().length;
		}
		else if (isSumCard && !mSimpleMode) {
			retVal += (TripComponent.Type.values().length * 2);
		}

		retVal += TYPE_ITINERARY_CARD;

		return retVal;
	}

	@Override
	public int getViewTypeCount() {
		//the *3 is so we have one for each type and one for each type that is shaded and one for each type in summary mode
		// the +1 is for the button card type
		return TripComponent.Type.values().length * State.values().length + 1;
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
	 */
	public synchronized void syncWithManager() {
		// Add Items (we add to a new list so we can change the list if need be internally)
		mItinCardDatas.clear();
		mItinCardDatas.addAll(mItinManager.getItinCardData());

		// Do some calculations on the data
		organizeData();

		// Add attach cards
		addAttachData();

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
		int typeOrd = getItemViewType(position) - TYPE_ITINERARY_CARD;
		typeOrd = typeOrd % TripComponent.Type.values().length;
		return Type.values()[typeOrd];
	}

	private State getItemViewCardState(int position) {
		int typeOrd = (getItemViewType(position) - TYPE_ITINERARY_CARD) / TripComponent.Type.values().length;
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

	private boolean isItemAButtonCard(int position) {
		final ItinCardData item = getItem(position);
		return item instanceof ItinCardDataHotelAttach || item instanceof ItinCardDataLocalExpert;
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

			if (!data.hasDetailData()) {
				continue;
			}

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
				if (possibleAlt.hasDetailData() && nowMillis > startMillis - threeHours) {
					mAltSummaryCardPosition = mSummaryCardPosition + 1;
				}
			}
		}
		else {
			// Check if last card hasn't ended; if so, make it the main summary card
			ItinCardData lastCard = mItinCardDatas.get(len - 1);
			if (lastCard.hasDetailData()
					&& lastCard.getEndDate().getCalendar().getTimeInMillis() > now.getTimeInMillis()) {
				mSummaryCardPosition = len - 1;
			}
		}
	}

	private void addAttachData() {
		// Nothing to do if there are no itineraries
		int len = mItinCardDatas.size();
		if (len == 0) {
			return;
		}

		for (int i = 0; i < len; i++) {
			ItinCardData data = mItinCardDatas.get(i);
			Calendar start = data.getStartDate().getCalendar();
			Calendar now = Calendar.getInstance(start.getTimeZone());

			// Ignore past itineraries
			if (now.after(start) && now.get(Calendar.DAY_OF_YEAR) > start.get(Calendar.DAY_OF_YEAR)) {
				continue;
			}

			// Ignore non-flight itineraries
			if (!data.getTripComponentType().equals(Type.FLIGHT) || !(data instanceof ItinCardDataFlight)) {
				continue;
			}

			// Ignore last leg flights
			TripFlight tripFlight = (TripFlight) data.getTripComponent();
			final int legCount = tripFlight.getFlightTrip().getLegCount();
			if (legCount > 0 && ((ItinCardDataFlight) data).getLegNumber() == legCount - 1) {
				continue;
			}

			for (int j = i + 1; j < len; j++) {
				ItinCardData nextData = mItinCardDatas.get(j);
				Calendar end = nextData.getStartDate().getCalendar();

				if ((nextData.getTripComponentType().equals(Type.HOTEL) || nextData.getTripComponentType().equals(
						Type.FLIGHT)) && start.get(Calendar.DAY_OF_YEAR) == end.get(Calendar.DAY_OF_YEAR)) {
					break;
				}

				if (nextData.getTripComponentType().equals(Type.FLIGHT) && nextData instanceof ItinCardDataFlight) {
					// Attach hotel
					FlightLeg firstLeg = ((ItinCardDataFlight) data).getFlightLeg();
					FlightLeg secondLeg = ((ItinCardDataFlight) nextData).getFlightLeg();

					mItinCardDatas.add(i + 1, new ItinCardDataHotelAttach(tripFlight, firstLeg, secondLeg));

					return;
				}
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
