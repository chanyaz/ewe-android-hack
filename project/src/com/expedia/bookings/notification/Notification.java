package com.expedia.bookings.notification;

import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.expedia.bookings.R;
import com.mobiata.android.Log;

@Table(name = "Notifications")
public class Notification extends Model {

	/**
	 * NEW = This notification has never been displayed to the user.
	 * NOTIFIED = This notification has been displayed and is still visible/active.
	 * REMOVED = This notification has passed or been dismissed by the user.
	 */
	public enum StatusType {
		NEW,
		NOTIFIED,
		REMOVED;
	}

	/**
	 * RESOURCE = A resource must be specified in setImageResId.
	 * URL = A url must be specified in setImageValue, and a placeholder image
	 * must be specified in setImageResId.
	 * DESTINATION = Corresponds to ExpediaImageManager.ImageType.DESTINATION. An
	 * airport code must be specified in setImageValue, and a placeholder image must
	 * be specified in setImageResId.
	 * CAR = Corresponds to ExpediaImageManager.ImageType.CAR. A car type must be
	 * specified in setImageValue, and a placeholder image must be specified in
	 * setImageResId.
	 * ACTIVITY = Corresponds to ExpediaImageManager.ImageType.CAR. An activity
	 * type must be specified in setImageValue, and a placeholder image must be
	 * specified in setImageResId.
	 */
	public enum ImageType {
		RESOURCE,
		URL,
		DESTINATION,
		CAR,
		ACTIVITY;
	}

	@Column(name = "UniqueId")
	private String mUniqueId;

	@Column(name = "TriggerTimeMillis")
	private long mTriggerTimeMillis;

	@Column(name = "Ticker")
	private String mTicker;

	@Column(name = "Title")
	private String mTitle;

	@Column(name = "Body")
	private String mBody;

	@Column(name = "Status")
	private String mStatus;

	@Column(name = "ImageType")
	private String mImageType;

	@Column(name = "ImageResId")
	private int mImageResId;

	@Column(name = "ImageValue")
	private String mImageValue;

	public Notification() {
	}

	public Notification(String uniqueId, long triggerTimeMillis) {
		setUniqueId(uniqueId);
		setTriggerTimeMillis(triggerTimeMillis);
		setStatus(StatusType.NEW);
	}

	public String getUniqueId() {
		return mUniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.mUniqueId = uniqueId;
	}

	public long getTriggerTimeMillis() {
		return mTriggerTimeMillis;
	}

	public void setTriggerTimeMillis(long triggerTimeMillis) {
		this.mTriggerTimeMillis = triggerTimeMillis;
	}

	public String getTicker() {
		return mTicker;
	}

	public void setTicker(String ticker) {
		this.mTicker = ticker;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getBody() {
		return mBody;
	}

	public void setBody(String body) {
		this.mBody = body;
	}

	public StatusType getStatus() {
		return StatusType.valueOf(mStatus);
	}

	public void setStatus(StatusType statusType) {
		this.mStatus = statusType.toString();
	}

	public ImageType getImageType() {
		return ImageType.valueOf(mImageType);
	}

	public void setImageType(ImageType imageType) {
		this.mImageType = imageType.toString();
	}

	public int getImageResId() {
		return mImageResId;
	}

	public void setImageResId(int imageResId) {
		this.mImageResId = imageResId;
	}

	public String getImageValue() {
		return mImageValue;
	}

	public void setImageValue(String value) {
		this.mImageValue = value;
	}

	public void setImage(ImageType type, int resId, String value) {
		setImageType(type);
		setImageResId(resId);
		setImageValue(value);
	}

	/**
	 * Updates this Notification object with data from the other object (meanwhile
	 * not destroying this object's status or unique id).
	 * @param other
	 */
	public void updateFrom(Notification other) {
		setTriggerTimeMillis(other.mTriggerTimeMillis);
		setTicker(other.mTicker);
		setTitle(other.mTitle);
		setBody(other.mBody);
	}

	/**
	 * Schedule this notification with the OS AlarmManager. Multiple calls to this method
	 * will not result in multiple notifications, as long as the UniqueId remains the same.
	 *
	 * @param notification
	 */
	public void scheduleNotification(Context context) {
		Intent intent = new Intent(context, LocalNotificationReceiver.class);

		String uriString = "expedia://trip/component/" + mUniqueId;

		intent.setData(Uri.parse(uriString));

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		long triggerTimeMillis = mTriggerTimeMillis;

		//TODO: temporary ->
		triggerTimeMillis = System.currentTimeMillis() + 5000;
		//TODO: <-temporary

		mgr.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
	}

	//////////////////////////////////////////////////////////////////////////
	// Database type operations

	/**
	 * Returns a Notification, if found, from the table whose UniqueId matches the
	 * passed UniqueId.
	 * @param uniqueId
	 * @return
	 */
	public static Notification find(String uniqueId) {
		List<Notification> notifications = new Select().from(Notification.class)
				.where("UniqueId=?", uniqueId).limit("1").execute();
		if (notifications == null || notifications.size() == 0) {
			return null;
		}
		return notifications.get(0);
	}

	public static void scheduleAll(Context context) {
		long now = System.currentTimeMillis();
		List<Notification> notifications = new Select().from(Notification.class)
				.where("TriggerTimeMillis >= ? AND Status IN (?,?)", now, "NEW", "NOTIFIED")
				.orderBy("TriggerTimeMillis").execute();

		for (Notification notification : notifications) {
			notification.scheduleNotification(context);
		}
	}

	public static void updateStatus(String uniqueId, StatusType statusType) {
		Notification notification = find(uniqueId);
		if (notification != null) {
			notification.setStatus(statusType);
			notification.save();
		}
	}
}
