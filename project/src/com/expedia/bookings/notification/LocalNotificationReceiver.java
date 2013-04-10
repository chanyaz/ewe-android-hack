package com.expedia.bookings.notification;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.support.v4.app.NotificationCompat;
import android.view.Display;
import android.view.WindowManager;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.ExpediaImageManager;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.TwoLevelImageCache;
import com.mobiata.android.bitmaps.TwoLevelImageCache.OnImageLoaded;
import com.mobiata.android.util.AndroidUtils;

public class LocalNotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String uniqueId = intent.getData().getLastPathSegment();
		Notification notification = Notification.find(uniqueId);

		if (notification == null) {
			Log.w("unable to find notification with unique id = " + uniqueId + ". Ignoring");
			return;
		}

		new NotificationScheduler(context, notification).start();
	}

	private static class NotificationScheduler {
		private Notification mNotification;
		private Context mContext;
		private Bitmap mBitmap;

		public NotificationScheduler(Context context, Notification notification) {
			mContext = context;
			mNotification = notification;
		}

		public void start() {
			Notification.ImageType imageType = mNotification.getImageType();
			switch (imageType) {
			case RESOURCE:
				mBitmap = BitmapFactory.decodeResource(mContext.getResources(), mNotification.getImageResId());
				scheduleNotification();
				break;
			case URL:
				TwoLevelImageCache.loadImage(mNotification.getImageValue(), mTwoLevelImageLoaded);
				break;
			case DESTINATION:
				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				bd.startDownload(mNotification.getUniqueId(), mDestinationImageUrlDownload,
						mDestinationImageUrlCallback);
				break;
			case CAR:
				break;
			case ACTIVITY:
				break;
			}
		}

		// For BackgroundDownloader, the object that will download the Destination Image
		private Download<BackgroundImageResponse> mDestinationImageUrlDownload = new Download<BackgroundImageResponse>() {
			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public BackgroundImageResponse doDownload() {
				String code = mNotification.getImageValue();

				int width, height;
				if (AndroidUtils.getSdkVersion() >= 13) {
					WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
					Display display = wm.getDefaultDisplay();
					Point size = new Point();
					display.getSize(size);
					width = size.x;
					height = size.y;
				}
				else {
					WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
					Display display = wm.getDefaultDisplay();
					width = display.getWidth();
					height = display.getHeight();
				}

				return ExpediaImageManager.getInstance().getDestinationImage(code, width, height, true);
			}
		};

		// Callback for BackgroundDownloader for the Destination Image
		private OnDownloadComplete<BackgroundImageResponse> mDestinationImageUrlCallback = new OnDownloadComplete<BackgroundImageResponse>() {
			@Override
			public void onDownload(BackgroundImageResponse results) {
				TwoLevelImageCache.loadImage(results.getImageUrl(), mTwoLevelImageLoaded);
			}
		};

		// Callbacks for TwoLevelImageCache image loader
		private OnImageLoaded mTwoLevelImageLoaded = new OnImageLoaded() {
			@Override
			public void onImageLoadFailed(String url) {
				mBitmap = BitmapFactory.decodeResource(mContext.getResources(), mNotification.getImageResId());
				scheduleNotification();
			}

			@Override
			public void onImageLoaded(String url, Bitmap bitmap) {
				mBitmap = bitmap;
				scheduleNotification();
			}
		};

		private void scheduleNotification() {
			PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext,
					SearchActivity.class), 0); //TODO
			PendingIntent directionsPendingIntent = pendingIntent; //TODO
			PendingIntent sharePendingIntent = pendingIntent; //TODO

			NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
					.bigPicture(Bitmap.createBitmap(mBitmap, 0, 96, mBitmap.getWidth(), 400, null, false))
					.setSummaryText(mNotification.getBody());

			String directions = mContext.getString(R.string.itin_action_directions);
			String share = mContext.getString(R.string.itin_action_share);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
					.setStyle(style)
					.setTicker(mNotification.getTitle())
					.setSmallIcon(R.drawable.ic_stat_expedia)
					.setContentTitle(mNotification.getTitle())
					.setContentText(mNotification.getBody())
					.setAutoCancel(true)
					.addAction(R.drawable.ic_direction, directions, directionsPendingIntent)
					.addAction(R.drawable.ic_social_share, share, sharePendingIntent)
					.setContentIntent(pendingIntent);

			String tag = mNotification.getUniqueId();

			NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

			//TODO: uncomment this if you want to be spammed with notifications
			//nm.notify(tag, 0, builder.build());
		}
	}

}
