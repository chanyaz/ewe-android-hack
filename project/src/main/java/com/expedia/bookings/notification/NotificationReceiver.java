package com.expedia.bookings.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.NotificationCompat;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.StandaloneShareActivity;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.launch.activity.PhoneLaunchActivity;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.notification.Notification.StatusType;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.GoogleMapsUtil;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.navigation.NavUtils;
import com.expedia.bookings.widget.itin.FlightItinContentGenerator;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.flightlib.data.Airport;
import com.squareup.picasso.Picasso;

public class NotificationReceiver extends BroadcastReceiver {

	private static final String TAG = NotificationReceiver.class.getSimpleName();

	static final String EXTRA_ACTION = "EXTRA_ACTION";
	static final String EXTRA_NOTIFICATION = "EXTRA_NOTIFICATION";

	static final int ACTION_SCHEDULE = 0;
	private static final int ACTION_DISMISS = 1;

	//////////////////////////////////////////////////////////////////////////
	// Intent Generators

	public static PendingIntent generateSchedulePendingIntent(Context context, Notification notification) {
		Intent intent = new Intent(context, NotificationReceiver.class);
		String uriString = "expedia://notification/schedule/" + notification.getUniqueId();
		intent.setData(Uri.parse(uriString));
		intent.putExtra(EXTRA_ACTION, ACTION_SCHEDULE);
		intent.putExtra(EXTRA_NOTIFICATION, notification.toJson().toString());
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static PendingIntent generateDismissPendingIntent(Context context, Notification notification) {
		Intent intent = new Intent(context, NotificationReceiver.class);
		String uriString = "expedia://notification/dismiss/" + notification.getUniqueId();
		intent.setData(Uri.parse(uriString));
		intent.putExtra(EXTRA_ACTION, ACTION_DISMISS);
		intent.putExtra(EXTRA_NOTIFICATION, notification.toJson().toString());
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}

	//////////////////////////////////////////////////////////////////////////
	// Broadcast Receiver lifecycle events

	@Override
	public void onReceive(final Context context, Intent intent) {
		INotificationManager notificationManager = Ui.getApplication(context).appComponent().notificationManager();
		Notification notification = null;
		try {
			Notification deserialized = makeNotification();
			String jsonString = intent.getStringExtra(EXTRA_NOTIFICATION);
			deserialized.fromJson(new JSONObject(jsonString));
			notification = findExistingNotification(notificationManager, deserialized);
		}
		catch (JSONException e) {
			Log.w("JSONException, unable to create notification. Ignoring", e);
			return;
		}

		if (notification == null) {
			Log.w("Unable to find existing notification. Ignoring");
			return;
		}

		// Don't display old notifications
		if (notification.getExpirationTimeMillis() < System.currentTimeMillis()) {
			notificationManager.cancelAndDeleteNotification(notification);
		}

		int action = intent.getIntExtra(EXTRA_ACTION, ACTION_SCHEDULE);
		switch (action) {
		case ACTION_DISMISS:
			notification.setStatus(StatusType.DISMISSED);
			notification.save();
			break;
		case ACTION_SCHEDULE:
		default:
			checkTripValidAndShowNotification(context, notification);
			break;
		}
	}

	@NonNull
	protected Notification makeNotification() {
		return new Notification();
	}

	protected Notification findExistingNotification(INotificationManager notificationManager, Notification deserialized) {
		return notificationManager.findExisting(deserialized);
	}

	@VisibleForTesting
	protected void checkTripValidAndShowNotification(final Context context, final Notification finalNotification) {

		boolean isNotificationNotBooking = !finalNotification.getNotificationType()
			.equals(NotificationType.DESKTOP_BOOKING);

		if (isNotificationNotBooking) { // check trip is still valid (i.e. not cancelled)
			if (getItineraryManagerInstance().startSync(false)) {
				getItineraryManagerInstance()
					.addSyncListener(
						makeValidTripSyncListener(context, finalNotification, getItineraryManagerInstance()));
			}
			else {
				scheduleNotification(getItineraryManagerInstance().getTrips(), finalNotification, context);
			}
		}
		else {
			showNotification(finalNotification, context); //show booking notification
		}

	}

	@VisibleForTesting
	public ItineraryManager.ItinerarySyncAdapter makeValidTripSyncListener(final Context context,
		final Notification finalNotification, final ItineraryManager itineraryManager) {

		return new ItineraryManager.ItinerarySyncAdapter() {

			@Override
			public void onSyncFailure(ItineraryManager.SyncError error) {
				// Couldn't fetch trips. Show notification anyway

				itineraryManager.removeSyncListener(this);
				showNotification(finalNotification, context);
			}

			@Override
			public void onSyncFinished(Collection<Trip> trips) {
				scheduleNotification(trips, finalNotification, context);
				itineraryManager.removeSyncListener(this);
			}
		};
	}

	private boolean isValidTripForScheduledNotification(Collection<Trip> trips, final Notification finalNotification) {
		for (Trip trip : trips) {
			for (TripComponent tripComponent : trip.getTripComponents()) {
				boolean isPushNotification = PushNotificationUtils
					.isFlightAlertsNotification(finalNotification);
				boolean isValidTripForNotification = finalNotification.getUniqueId()
					.contains(tripComponent.getUniqueId());
				if (isValidTripForNotification || isPushNotification) {
					return true;
				}
			}
		}
		return false;
	}

	private void scheduleNotification(Collection<Trip> trips, final Notification finalNotification, Context context) {
		// Show notification only if trip exists
		INotificationManager notificationManager = Ui.getApplication(context)
			.appComponent().notificationManager();

		boolean validTripForScheduledNotification = isValidTripForScheduledNotification(trips, finalNotification);
		if (!validTripForScheduledNotification) {
			notificationManager.cancelNotificationIntent(finalNotification);
			notificationManager.dismissNotification(finalNotification);
		}
		else {
			notificationUpdateAndShow(notificationManager, finalNotification, context);
		}
	}

	private void notificationUpdateAndShow(INotificationManager notificationManager,
		Notification finalNotification,
		Context context) {
		Notification updatedNotification = findExistingNotification(notificationManager, finalNotification);
		if (updatedNotification != null) {
			showNotification(updatedNotification, context);
		}
	}

	protected ItineraryManager getItineraryManagerInstance() {
		return ItineraryManager.getInstance();
	}

	protected void showNotification(Notification finalNotification, Context context) {
		finalNotification.didNotify();
		new Notifier(context, finalNotification).start();
	}

	protected static class Notifier {
		private Notification mNotification;
		private Context mContext;
		private Bitmap mBitmap;
		private List<String> mUrls;

		public Notifier(Context context, Notification notification) {
			mContext = context;
			mNotification = notification;
		}

		public void start() {
			Notification.ImageType imageType = mNotification.getImageType();
			switch (imageType) {
			case RESOURCE:
				mBitmap = BitmapFactory.decodeResource(mContext.getResources(), mNotification.getImageResId());
				display();
				break;
			case URL:
				mUrls = new ArrayList<String>(1);
				mUrls.add(mNotification.getImageValue());
				loadNextUrl();
				break;
			case URLS:
				mUrls = mNotification.getImageUrls();
				loadNextUrl();
				break;
			case DESTINATION: {
				final String code = mNotification.getImageValue();
				Point screen = AndroidUtils.getScreenSize(mContext);
				int width = screen.x;
				int height = (int) (mContext.getResources().getDisplayMetrics().density * 256);
				final String url = new Akeakamai(Images.getFlightDestination(code)) //
					.resizeExactly(width, height) //
					.build();
				new PicassoHelper.Builder(mContext).setTarget(mDestinationImageLoaded).setCacheEnabled(true).build().load(url);
				break;
			}
			case CAR: {
				mUrls = new ArrayList<String>(1);
				mUrls.add(mNotification.getImageValue());
				loadNextUrl();
				break;
			}
			case ACTIVITY:
			case NONE:
				display();
				break;
			}
		}

		private PicassoTarget mDestinationImageLoaded = new PicassoTarget() {
			@Override
			public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
				super.onBitmapLoaded(bitmap, from);
				mBitmap = bitmap;
				display();
			}

			@Override
			public void onBitmapFailed(Drawable errorDrawable) {
				super.onBitmapFailed(errorDrawable);
				mBitmap = null;
				display();
			}

			@Override
			public void onPrepareLoad(Drawable placeHolderDrawable) {
				super.onPrepareLoad(placeHolderDrawable);
			}
		};

		private void loadNextUrl() {
			if (mUrls == null || mUrls.size() == 0) {
				mBitmap = null;
				display();
				return;
			}
			String url = mUrls.remove(0);
			new PicassoHelper.Builder(mContext).setTarget(mTwoLevelImageLoaded).setCacheEnabled(true).build().load(url);
		}

		// Callbacks for TwoLevelImageCache image loader
		private PicassoTarget mTwoLevelImageLoaded = new PicassoTarget() {
			@Override
			public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
				super.onBitmapLoaded(bitmap, from);
				try {
					// #1457 - We make a copy so that the TwoLevelImageCache can't recycle it from underneath us
					mBitmap = bitmap.copy(bitmap.getConfig(), false);
				}
				catch (OutOfMemoryError e) {
					// Gracefully handle out of memory here by just not displaying a big picture. NBD
					Log.w("Ran out of memory downloading a notification bitmap", e);
					mBitmap = null;
				}
				display();
			}

			@Override
			public void onBitmapFailed(Drawable errorDrawable) {
				super.onBitmapFailed(errorDrawable);
				loadNextUrl();
			}

			@Override
			public void onPrepareLoad(Drawable placeHolderDrawable) {
				super.onPrepareLoad(placeHolderDrawable);
			}
		};

		private void display() {
			NotificationCompat.Style style = null;

			if (mBitmap != null) {
				style = new NotificationCompat.BigPictureStyle()
					.bigPicture(mBitmap)
					.setSummaryText(mNotification.getBody());
			}
			else {
				style = new NotificationCompat.BigTextStyle()
					.bigText(mNotification.getBody());
			}

			Intent clickIntent = PhoneLaunchActivity.createIntent(mContext, mNotification);
			PendingIntent clickPendingIntent = PendingIntent.getActivity(mContext, 0, clickIntent, 0);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
				.setStyle(style)
				.setTicker(mNotification.getTitle())
				.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), mNotification.getIconResId()))
				.setContentTitle(mNotification.getTitle())
				.setContentText(mNotification.getBody())
				.setAutoCancel(true)
				.setDeleteIntent(generateDismissPendingIntent(mContext, mNotification))
				.setContentIntent(clickPendingIntent);

			int notificationIconResourceId = R.drawable.ic_stat;
			builder.setSmallIcon(notificationIconResourceId);

			int notificationIndicatorLEDColor = ProductFlavorFeatureConfiguration.getInstance()
				.getNotificationIndicatorLEDColor();
			builder.setLights(notificationIndicatorLEDColor, 200, 8000);

			long flags = mNotification.getFlags();
			ItinCardData data = ItineraryManager.getInstance().getItinCardDataFromItinId(mNotification.getItinId());

			if ((flags & Notification.FLAG_DIRECTIONS) != 0) {
				Intent intent = null;
				if (data instanceof ItinCardDataFlight) {
					Airport airport = ((ItinCardDataFlight) data).getFlightLeg().getFirstWaypoint().getAirport();
					intent = FlightItinContentGenerator.getAirportDirectionsIntent(airport);
				}
				else if (data instanceof ItinCardDataHotel) {
					intent = GoogleMapsUtil.getDirectionsIntent(((ItinCardDataHotel) data).getProperty().getLocation().toLongFormattedString());
				}
				else if (data instanceof ItinCardDataCar) {
					intent = mNotification.getNotificationType() == NotificationType.CAR_DROP_OFF
						? ((ItinCardDataCar) data).getDropOffDirectionsIntent()
						: ((ItinCardDataCar) data).getPickupDirectionsIntent();
				}

				// #1689: Ensure we have an activity that can handle the intent
				if (intent != null && NavUtils.canHandleIntent(mContext, intent)) {
					PendingIntent directionsPendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
					String directions = mContext.getString(R.string.itin_action_directions);
					builder = builder.addAction(R.drawable.ic_direction, directions, directionsPendingIntent);
				}
			}

			if ((flags & Notification.FLAG_REDEEM) != 0) {
				Intent intent = null;
				if (data instanceof ItinCardDataActivity) {
					intent = ((ItinCardDataActivity) data).buildRedeemIntent(mContext);
				}

				if (intent != null) {
					PendingIntent redeemPendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
					String redeem = mContext.getString(R.string.itin_action_redeem);
					builder = builder.addAction(R.drawable.ic_printer_redeem, redeem, redeemPendingIntent);
				}
			}

			if ((flags & Notification.FLAG_CALL) != 0) {
				String label = null;
				String phoneNumber = null;
				if (data instanceof ItinCardDataActivity) {
					phoneNumber = ((ItinCardDataActivity) data).getBestSupportPhoneNumber(mContext);
					label = mContext.getString(R.string.itin_action_support);
				}
				else if (data instanceof ItinCardDataCar) {
					phoneNumber = ((ItinCardDataCar) data).getRelevantVendorPhone();
					label = ((ItinCardDataCar) data).getVendorName();
				}
				else if (data instanceof ItinCardDataHotel) {
					phoneNumber = ((ItinCardDataHotel) data).getRelevantPhone();
					label = mContext.getString(R.string.itin_action_call_hotel);
				}

				if (phoneNumber != null) {
					Intent intent = SocialUtils.getCallIntent(mContext, phoneNumber);

					// #1689: Ensure we have an activity that can handle the intent
					if (NavUtils.canHandleIntent(mContext, intent)) {
						PendingIntent callPendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
						builder = builder.addAction(R.drawable.ic_phone, label, callPendingIntent);
					}
				}
			}

			if ((flags & Notification.FLAG_VIEW) != 0) {
				String view = mContext.getString(R.string.itin_action_view);
				builder = builder.addAction(R.drawable.ic_view_itin, view, clickPendingIntent);
			}

			if ((flags & Notification.FLAG_SHARE) != 0) {
				Intent intent = StandaloneShareActivity.createIntent(mContext, mNotification.getItinId());
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				PendingIntent sharePendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

				String share = mContext.getString(R.string.itin_action_share);
				builder = builder.addAction(R.drawable.ic_social_share, share, sharePendingIntent);
			}

			android.app.Notification notif = builder.build();
			NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(mNotification.getUniqueId(), 0, notif);
			OmnitureTracking.trackNotificationShown(mNotification);
		}
	}
}
