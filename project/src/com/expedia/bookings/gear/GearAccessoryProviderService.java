package com.expedia.bookings.gear;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

import org.joda.time.DateTime;

import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripFlight;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

public class GearAccessoryProviderService extends SAAgent {
	public static final String TAG = "GearAccessoryProviderService";

	public static final int SERVICE_CONNECTION_RESULT_OK = 0;

	public static final int EBACCESSORY_CHANNEL_ID = 104;

	private static SparseArray<EBAccessoryProviderConnection> mProviderConnections = null;

	private final IBinder mBinder = new LocalBinder();

	private ItineraryManager itineraryManager;

	private int mConnectionIdCounter = 0;

	public class LocalBinder extends Binder {
		public GearAccessoryProviderService getService() {
			return GearAccessoryProviderService.this;
		}
	}

	public GearAccessoryProviderService() {
		super(TAG, EBAccessoryProviderConnection.class);
	}

	public class EBAccessoryProviderConnection extends SASocket {
		private int mConnectionId;

		public EBAccessoryProviderConnection() {
			super(EBAccessoryProviderConnection.class.getName());
		}

		@Override
		public void onError(int channelId, String errorString, int error) {
			Log.e(TAG, "Connection is not alive ERROR: " + errorString + "  "
				+ error);
		}

		@Override
		public void onReceive(int channelId, byte[] data) {
			GearResponse response = null;
			try {
				Log.d(TAG, "Receive for " + channelId);
				Collection<Trip> trips = itineraryManager.getTrips();
				JSONArray responseArray = new JSONArray();
				List<ItinCardData> cards = itineraryManager.getItinCardData();
				ItinCardData itinCardData = null;
				for (ItinCardData cardData : cards) {
					Log.d(TAG, "cardData " + cardData);
					Log.d(TAG, cardData.getTripId() + "  test " + cardData.getTripComponentType());
					if (!hasExpired(cardData.getEndDate())) {

						itinCardData = cardData;
						TripComponent.Type tripType = itinCardData.getTripComponentType();

						if (tripType.equals(TripComponent.Type.FLIGHT)) {
							response = new FlightBookingResponse();
						}
						else if (tripType.equals(TripComponent.Type.HOTEL)) {
							response = new HotelBookingResponse();
						}
						else {
							continue;
						}
						break;
					}
				}

				if (response != null) {
					response.setTripComponent(itinCardData.getTripComponent());
					responseArray.put(response.getResponseForGear().toString());
				}

				final String responseData = responseArray.toString(2);
				Log.d(TAG, "OUR OUTPUT---" + responseData);
				final EBAccessoryProviderConnection providerConnection = mProviderConnections.get(mConnectionId);
				if (providerConnection == null) {
					Log.e(TAG, "Error, can not get EBAccessoryProviderConnection handler");
					return;
				}
				new Thread(new Runnable() {
					public void run() {
						try {
							providerConnection.send(EBACCESSORY_CHANNEL_ID, responseData.getBytes());
						} catch (IOException e) {
							Log.e(TAG, e.getMessage());
						}
					}
				}).start();
			} catch (Exception e) {
				Log.e(TAG, "Exception in Gear Service", e);
			}
		}

		@Override
		protected void onServiceConnectionLost(int errorCode) {
			Log.e(TAG, "onServiceConectionLost  for peer = " + mConnectionId
				+ "error code =" + errorCode);

			if (mProviderConnections != null) {
				mProviderConnections.remove(mConnectionId);
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate of Provider Service");

		SA mAccessory = new SA();
		try {
			mAccessory.initialize(this);
			itineraryManager = ItineraryManager.getInstance();
		} catch (SsdkUnsupportedException e) {
			Log.e(TAG, "Gear SDK not supported.");
		} catch (Exception e1) {
			Log.e(TAG, "Cannot initialize Accessory package.", e1);
			stopSelf();
		}

	}

	@Override
	protected void onServiceConnectionRequested(SAPeerAgent peerAgent) {
		Log.d(TAG, "Connection requested ");
		acceptServiceConnectionRequest(peerAgent);
	}

	@Override
	protected void onFindPeerAgentResponse(SAPeerAgent arg0, int arg1) {
		Log.d(TAG, "onFindPeerAgentResponse  arg1 =" + arg1);
	}

	@Override
	protected void onServiceConnectionResponse(SASocket thisConnection,
											   int result) {
		if (result == CONNECTION_SUCCESS) {
			if (thisConnection != null) {
				EBAccessoryProviderConnection providerConnection = (EBAccessoryProviderConnection) thisConnection;

				if (mProviderConnections == null) {
					mProviderConnections = new SparseArray<EBAccessoryProviderConnection>();
				}

				providerConnection.mConnectionId = ++mConnectionIdCounter;

				Log.d(TAG, "onServiceConnection connectionID = "
					+ providerConnection.mConnectionId);

				mProviderConnections.put(providerConnection.mConnectionId, providerConnection);

			}
			else {
				Log.e(TAG, "SASocket object is null");
			}
		}
		else if (result == CONNECTION_ALREADY_EXIST) {
			Log.e(TAG, "onServiceConnectionResponse, CONNECTION_ALREADY_EXIST");
		}
		else {
			Log.e(TAG, "onServiceConnectionResponse result error =" + result);
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "Called onBind");
		return mBinder;
	}

	public boolean hasExpired(DateTime dateTime) {
		DateTime pastCutOffDateTime = DateTime.now();
		return dateTime != null && dateTime.isBefore(pastCutOffDateTime);
	}
}