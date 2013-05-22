package com.expedia.bookings.notification;

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
import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.activity.StandaloneShareActivity;
import com.expedia.bookings.data.ExpediaImage;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.notification.Notification.StatusType;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.TwoLevelImageCache;
import com.mobiata.android.bitmaps.TwoLevelImageCache.OnImageLoaded;
import com.mobiata.android.util.AndroidUtils;

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
		String uriString = "expedia://notification/schedule/" + notification.getTag();
		intent.setData(Uri.parse(uriString));
		intent.putExtra(EXTRA_ACTION, ACTION_SCHEDULE);
		intent.putExtra(EXTRA_NOTIFICATION, notification.toJson().toString());
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}

	public static PendingIntent generateDismissPendingIntent(Context context, Notification notification) {
		Intent intent = new Intent(context, NotificationReceiver.class);
		String uriString = "expedia://notification/dismiss/" + notification.getTag();
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
			Log.w("JSONException, unable to create notification. Ignoring");
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
			new Notifier(context, notification).start();
			break;
		}
	}

	private static class Notifier {
		private Notification mNotification;
		private Context mContext;
		private Bitmap mBitmap;

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
				TwoLevelImageCache.loadImage(mNotification.getImageValue(), mTwoLevelImageLoaded);
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

		// Callbacks for TwoLevelImageCache image loader
		private OnImageLoaded mTwoLevelImageLoaded = new OnImageLoaded() {
			@Override
			public void onImageLoadFailed(String url) {
				mBitmap = null;
				display();
			}

			@Override
			public void onImageLoaded(String url, Bitmap bitmap) {
				mBitmap = bitmap;
				display();
			}
		};

		private void display() {
			NotificationCompat.Style style = null;

			//TODO: fix BigPictureStyle sometimes crashing
//			if (mBitmap != null) {
//				style = new NotificationCompat.BigPictureStyle()
//						.bigPicture(mBitmap)
//						.setSummaryText(mNotification.getBody());
//			}
//			else {
				style = new NotificationCompat.BigTextStyle()
						.bigText(mNotification.getBody());
//			}

			Intent clickIntent = LaunchActivity.createIntent(mContext, mNotification);
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
			if ((flags & Notification.FLAG_DIRECTIONS) != 0) {
				//TODO: directions
				Intent intent = LaunchActivity.createIntent(mContext, mNotification);
				PendingIntent directionsPendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

				String directions = mContext.getString(R.string.itin_action_directions);
				builder = builder.addAction(R.drawable.ic_direction, directions, directionsPendingIntent);
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
			nm.notify(mNotification.getTag(), 0, notif);
		}
	}
}
