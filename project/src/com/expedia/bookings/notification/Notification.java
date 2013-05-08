package com.expedia.bookings.notification;

import java.util.List;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.text.TextUtils;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.expedia.bookings.R;

@Table(name = "Notifications")
public class Notification extends Model {

	public static final long FLAG_LOCAL = 0x01;
	public static final long FLAG_PUSH = 0x02;
	public static final long FLAG_DIRECTIONS = 0x4;
	public static final long FLAG_SHARE = 0x8;

	/**
	 * NEW = This notification has never been displayed to the user.
	 * NOTIFIED = This notification has been displayed and is still visible/active.
	 * DISMISSED = This notification has passed or been dismissed by the user.
	 */
	public enum StatusType {
		NEW,
		NOTIFIED,
		DISMISSED;
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
		ACTIVITY,
		NONE;
	}

	/**
	 * These correspond to the notification types we report through omniture.
	 * https://confluence/display/Omniture/App+Itinerary#AppItinerary-Version31
	 */
	public enum NotificationType {
		ACTIVITY_START,
		CAR_DROP_OFF,
		CAR_PICK_UP,
		FLIGHT_CHECK_IN,
		FLIGHT_CANCELLED,
		FLIGHT_GATE_TIME_CHANGE,
		FLIGHT_GATE_NUMBER_CHANGE,
		FLIGHT_BAGGAGE_CLAIM,
		HOTEL_CHECK_IN,
		HOTEL_CHECK_OUT;
	}

	@Column(name = "UniqueId")
	private String mUniqueId;

	@Column(name = "TriggerTimeMillis")
	private long mTriggerTimeMillis;

	@Column(name = "IconResId")
	private int mIconResId;

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

	@Column(name = "Flags")
	private long mFlags;

	// For Omniture tracking
	@Column(name = "NotificationType")
	private String mNotificationType;

	public Notification() {
	}

	/**
	 * Create a new com.expedia.bookings.notification.Notification object. If two 
	 * notification objects share the same uniqueId, only the later one will be 
	 * displayed.
	 * @param uniqueId
	 * @param triggerTimeMillis
	 */
	public Notification(String uniqueId, long triggerTimeMillis) {
		setUniqueId(uniqueId);
		setTriggerTimeMillis(triggerTimeMillis);

		// Defaults
		setStatus(StatusType.NEW);
		setIconResId(R.drawable.ic_stat_expedia);
		setFlags(0);
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

	public int getIconResId() {
		return mIconResId;
	}

	public void setIconResId(int iconResId) {
		this.mIconResId = iconResId;
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
		if (TextUtils.isEmpty(mImageType)) {
			return ImageType.NONE;
		}
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

	public long getFlags() {
		return mFlags;
	}

	public void setFlags(long flags) {
		this.mFlags = flags;
	}

	public NotificationType getNotificationType() {
		return NotificationType.valueOf(mNotificationType);
	}

	public void setNotificationType(NotificationType notificationType) {
		this.mNotificationType = notificationType.toString();
	}

	/**
	 * Updates this Notification object with data from the other object (being very
	 * careful when changing its status).
	 * @param other
	 */
	public void updateFrom(Notification other) {
		// The idea here is that if there is a later notification about the same
		// thing, then we should display that later (and presumably more relevant)
		// notification.
		if (other.mTriggerTimeMillis > mTriggerTimeMillis) {
			setTriggerTimeMillis(other.mTriggerTimeMillis);
			setStatus(other.getStatus());
		}
		setIconResId(other.mIconResId);
		setTicker(other.mTicker);
		setTitle(other.mTitle);
		setBody(other.mBody);
		setImageType(other.getImageType());
		setImageResId(other.mImageResId);
		setImageValue(other.mImageValue);
		setFlags(other.mFlags);
	}

	/**
	 * Schedule this notification with the OS AlarmManager. Multiple calls to this method
	 * will not result in multiple notifications, as long as the UniqueId remains the same.
	 *
	 * @param context
	 */
	public void scheduleNotification(Context context) {
		PendingIntent pendingIntent = NotificationReceiver.generateSchedulePendingIntent(context, mUniqueId);
		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC_WAKEUP, mTriggerTimeMillis, pendingIntent);
	}

	/**
	 * Cancel a previously scheduled notification with the OS AlarmManager.
	 *
	 * @param context
	 */
	public void cancelNotification(Context context) {
		PendingIntent pendingIntent = NotificationReceiver.generateSchedulePendingIntent(context, mUniqueId);

		// Cancel if in the future
		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		mgr.cancel(pendingIntent);

		// Dismiss a possibly displayed notification
		String tag = getUniqueId();
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(tag, 0);

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
				.where("Status IN (?,?)", StatusType.NEW.name(), StatusType.NOTIFIED.name())
				.orderBy("TriggerTimeMillis").execute();

		for (Notification notification : notifications) {
			notification.scheduleNotification(context);
		}
	}

	public static void deleteAll(Context context) {
		List<Notification> notifications = new Select().from(Notification.class).execute();

		for (Notification notification : notifications) {
			notification.cancelNotification(context);
		}

		// Delete all here instead of individually in the loop, for efficiency.
		new Delete().from(Notification.class).execute();
	}

	public static void updateStatus(String uniqueId, StatusType statusType) {
		Notification notification = find(uniqueId);
		if (notification != null) {
			notification.setStatus(statusType);
			notification.save();
		}
	}
}
