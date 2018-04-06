package com.expedia.bookings.notification.carousel;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.net.URL;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import static android.graphics.BitmapFactory.decodeStream;

/**
 * Created by nbirla on 16/03/18.
 */

public class CarouselNotificationManager {
	private Context _oContext;
	public static final String NOTIF_MODEL = "notif_model";
	public static final String IS_LEFT = "is_left";
	public static final String CURRENT_POS = "current_pos";

	public interface ImageDownloadCallback{
		void onImageDownloadComplete(Bitmap b);
		void onError();
	}

	public CarouselNotificationManager(Context context){
		_oContext = context;
	}


	private int totalImageCount;
	private int totalDownloadCount;
	private List<String> removedUrls = new ArrayList<>();

	public void processGCMImageCarousel(final int notifId, final NotificationModel model){

		if(model.imageCarouselArray == null || model.imageCarouselArray.size() == 0)
			return;
//		PicassoHelper picassoHelper = new PicassoHelper.Builder(_oContext).setCallback(new Callback() {
//			@Override
//			public void onSuccess() {
//
//			}
//
//			@Override
//			public void onError() {
//
//			}
//		}).build();
//		picassoHelper.load(model.imageCarouselArray);

		totalImageCount = model.imageCarouselArray.size();
		publishCarouselNotification(model, notifId, 0);
	}

	public void publishCarouselNotification(NotificationModel model, final int id, int currentPos)
	{
		final NotificationCompat.Builder builder = CarouselNotificationManager.getNotification(_oContext, model);

		if(hasJellyBean()) {
			final RemoteViews remoteViews = new RemoteViews(_oContext.getPackageName(),
				R.layout.layout_geo_notification_big);

			final Intent leftButtonIntent = new Intent(_oContext, CarouselNotificationService.class);
			leftButtonIntent.putExtra(Intent.EXTRA_UID, id);
			leftButtonIntent.putExtra(NOTIF_MODEL, model);
			leftButtonIntent.putExtra(IS_LEFT, true);
			leftButtonIntent.putExtra(CURRENT_POS, currentPos);

			final Intent rightButtonIntent = new Intent(_oContext, CarouselNotificationService.class);
			rightButtonIntent.putExtra(Intent.EXTRA_UID, id);
			rightButtonIntent.putExtra(NOTIF_MODEL, model);
			rightButtonIntent.putExtra(IS_LEFT, false);
			rightButtonIntent.putExtra(CURRENT_POS, currentPos);

			try {
				URL url = new URL(model.imageCarouselArray.get(currentPos));
				remoteViews.setImageViewBitmap(R.id.image_view, decodeStream((InputStream)url.getContent()));
			}
			catch (Exception e) {
			}

			Intent newIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.deeplink));
			PendingIntent pi = PendingIntent.getActivity(_oContext, 0, newIntent, 0);


			remoteViews.setTextViewText(R.id.tv_title, model.title);
			remoteViews.setTextViewText(R.id.tv_desc, model.contentText);

			remoteViews.setOnClickPendingIntent(R.id.left,
				PendingIntent.getService(_oContext,10,leftButtonIntent,PendingIntent.FLAG_UPDATE_CURRENT));
			remoteViews.setOnClickPendingIntent(R.id.right,
				PendingIntent.getService(_oContext,15,rightButtonIntent,PendingIntent.FLAG_UPDATE_CURRENT));
			remoteViews.setOnClickPendingIntent(R.id.image_view, pi);

			builder.setCustomBigContentView(remoteViews);
			builder.setContentIntent(pi);

			buildNotification(builder,id);
		}else
		{
			buildNotification(builder,id);
		}

	}

	private void buildNotification(NotificationCompat.Builder builder, int id)
	{
		Notification notif = builder.build();
		notif.flags |= Notification.FLAG_AUTO_CANCEL;

		NotificationManager notificationManager = (NotificationManager) _oContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, notif);
	}


	public static NotificationCompat.Builder getNotification(Context context, NotificationModel model){

		Uri uri = null;
		if (model.doPlaySound == 1) {
			uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
			.setContentTitle(model.title)
			.setContentText(model.contentText)
			.setSmallIcon(R.drawable.ic_launcher);

		if(uri != null)
			builder.setSound(uri);

		return builder;
	}

	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}

}
