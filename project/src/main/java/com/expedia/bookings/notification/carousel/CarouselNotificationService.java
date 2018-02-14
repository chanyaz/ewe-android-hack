package com.expedia.bookings.notification.carousel;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by nbirla on 16/03/18.
 */

public class CarouselNotificationService extends IntentService {

	private static final String TAG = "CarouselNotificationSer";

	public CarouselNotificationService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		int id = intent.getIntExtra(Intent.EXTRA_UID, -1);

		NotificationModel model = intent.getParcelableExtra(CarouselNotificationManager.NOTIF_MODEL);
		int currentPos = intent.getIntExtra(CarouselNotificationManager.CURRENT_POS, 0);
		boolean isLeft = intent.getBooleanExtra(CarouselNotificationManager.IS_LEFT, false);

		model.doPlaySound = 0;

		if (isLeft) {
			if (currentPos > 0) {
				currentPos--;
			} else {
				currentPos = model.imageCarouselArray.size() - 1;
			}
		} else {
			if (currentPos == model.imageCarouselArray.size() - 1) {
				currentPos = 0;
			} else {
				currentPos++;
			}
		}

		Log.d(TAG, "current position " + currentPos);

		CarouselNotificationManager carouselNotificationManager = new CarouselNotificationManager(this);
		carouselNotificationManager.publishCarouselNotification(model, id, currentPos);
	}
}
