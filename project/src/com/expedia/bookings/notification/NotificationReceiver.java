package com.expedia.bookings.notification;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.ItineraryActivity;
import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.activity.StandaloneShareActivity;
import com.expedia.bookings.data.ExpediaImage;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.notification.Notification.StatusType;
import com.expedia.bookings.widget.itin.FlightItinContentGenerator;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.bitmaps.TwoLevelImageCache;
import com.mobiata.android.bitmaps.TwoLevelImageCache.OnImageLoaded;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.flightlib.data.Airport;

public class NotificationReceiver extends BroadcastReceiver {

	private static final String TAG = NotificationReceiver.class.getSimpleName();

	private static final String EXTRA_ACTION = "EXTRA_ACTION";
	private static final String EXTRA_NOTIFICATION = "EXTRA_NOTIFICATION";

	private static final int ACTION_SCHEDULE = 0;
	private static final int ACTION_DISMISS = 1;

	//////////////////////////////////////////////////////////////////////////
	// Intent Generators

	public static PendingIntent generateSchedulePendingIntent(Context context, Notification notification) {
		Intent intent = new Intent(context, NotificationReceiver.class);
		String uriString = "expedia://notification/schedule/" + notification.getUniqueId();
		intent.setData(Uri.parse(uriString));
		intent.putExtra(EXTRA_ACTION, ACTION_SCHEDULE);
		intent.putExtra(EXTRA_NOTIFICATION, notification.toJson().toString());
		return PendingIntent.getBroadcast(context, 0, intent, 0);
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
	public void onReceive(Context context, Intent intent) {
		Notification notification = null;
		try {
			Notification deserialized = new Notification();
			String jsonString = intent.getStringExtra(EXTRA_NOTIFICATION);
			deserialized.fromJson(new JSONObject(jsonString));
			notification = Notification.findExisting(deserialized);
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
			notification.setStatus(StatusType.EXPIRED);
			notification.save();

			// Just in case it's still showing up
			notification.cancelNotification(context);
			return;
		}

		int action = intent.getIntExtra(EXTRA_ACTION, ACTION_SCHEDULE);
		switch (action) {
		case ACTION_DISMISS:
			notification.setStatus(StatusType.DISMISSED);
			notification.save();
			break;
		case ACTION_SCHEDULE:
		default:
			notification.setStatus(StatusType.NOTIFIED);
			notification.save();
			new Notifier(context, notification).start();
			break;
		}
	}

	private static class Notifier {
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
			case DESTINATION:
				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				bd.startDownload(mNotification.getItinId(), mDestinationImageUrlDownload,
						mDestinationImageUrlCallback);
				break;
			case CAR:
			case ACTIVITY:
			case NONE:
				display();
				break;
			}
		}

		// For BackgroundDownloader, the object that will download the Destination Image
		private Download<ExpediaImage> mDestinationImageUrlDownload = new Download<ExpediaImage>() {
			@SuppressLint("NewApi")
			@Override
			public ExpediaImage doDownload() {
				String code = mNotification.getImageValue();
				Point size = AndroidUtils.getScreenSize(mContext);
				return ExpediaImageManager.getInstance().getDestinationImage(code, size.x, size.y, true);
			}
		};

		// Callback for BackgroundDownloader for the Destination Image
		private OnDownloadComplete<ExpediaImage> mDestinationImageUrlCallback = new OnDownloadComplete<ExpediaImage>() {
			@Override
			public void onDownload(ExpediaImage image) {
				TwoLevelImageCache.loadImage(image.getUrl(), mDestinationImageLoaded);
			}
		};

		// Callbacks for TwoLevelImageCache image loader. Special for Destination Image because
		// we have to crop the returned bitmap.
		private OnImageLoaded mDestinationImageLoaded = new OnImageLoaded() {
			@Override
			public void onImageLoadFailed(String url) {
				mBitmap = null;
				display();
			}

			@Override
			public void onImageLoaded(String url, Bitmap bitmap) {
				// These are tailored to the specific size of our destination images (720x1140 on xhdpi).
				// They don't need to be exact anyway.
				int left = 0;
				int top = (int) (bitmap.getHeight() * 0.1);
				int width = bitmap.getWidth();
				int height = (int) (bitmap.getHeight() * 0.35);
				mBitmap = Bitmap.createBitmap(bitmap, left, top, width, height, null, false);
				display();
			}
		};

		private void loadNextUrl() {
			if (mUrls == null || mUrls.size() == 0) {
				mBitmap = null;
				display();
				return;
			}
			String url = mUrls.remove(0);
			TwoLevelImageCache.loadImage(url, mTwoLevelImageLoaded);
		}

		// Callbacks for TwoLevelImageCache image loader
		private OnImageLoaded mTwoLevelImageLoaded = new OnImageLoaded() {
			@Override
			public void onImageLoadFailed(String url) {
				loadNextUrl();
			}

			@Override
			public void onImageLoaded(String url, Bitmap bitmap) {
				// #1457 - We make a copy so that the TwoLevelImageCache can't recycle it from underneath us
				mBitmap = bitmap.copy(bitmap.getConfig(), false);
				display();
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

			Intent clickIntent;
			if (ExpediaBookingApp.useTabletInterface(mContext)) {
				clickIntent = ItineraryActivity.createIntent(mContext, mNotification.getItinId());
			}
			else {
				clickIntent = LaunchActivity.createIntent(mContext, mNotification);
			}
			PendingIntent clickPendingIntent = PendingIntent.getActivity(mContext, 0, clickIntent, 0);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
					.setStyle(style)
					.setTicker(mNotification.getTitle())
					.setSmallIcon(R.drawable.ic_stat_expedia)
					.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), mNotification.getIconResId()))
					.setContentTitle(mNotification.getTitle())
					.setContentText(mNotification.getBody())
					.setAutoCancel(true)
					.setDeleteIntent(generateDismissPendingIntent(mContext, mNotification))
					.setContentIntent(clickPendingIntent)
					.setLights(0xfbc51e, 200, 8000); // Expedia suitcase color

			long flags = mNotification.getFlags();
			ItinCardData data = ItineraryManager.getInstance().getItinCardDataFromItinId(mNotification.getItinId());

			if ((flags & Notification.FLAG_DIRECTIONS) != 0) {
				Intent intent = null;
				if (data instanceof ItinCardDataFlight) {
					Airport airport = ((ItinCardDataFlight) data).getFlightLeg().getFirstWaypoint().getAirport();
					intent = FlightItinContentGenerator.getAirportDirectionsIntent(airport);
				}
				else if (data instanceof ItinCardDataHotel) {
					intent = ((ItinCardDataHotel) data).getDirectionsIntent();
				}
				else if (data instanceof ItinCardDataCar) {
					intent = mNotification.getNotificationType() == NotificationType.CAR_DROP_OFF
							? ((ItinCardDataCar) data).getDropOffDirectionsIntent()
							: ((ItinCardDataCar) data).getPickupDirectionsIntent();
				}

				if (intent != null) {
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
					PendingIntent callPendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
					builder = builder.addAction(R.drawable.ic_phone, label, callPendingIntent);
				}
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
		}
	}
}
