package com.expedia.bookings.notification;

import android.text.TextUtils;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.expedia.bookings.R;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Table(name = "Notifications")
public class Notification extends Model implements JSONable {

	public static final long FLAG_LOCAL = 0x01;
	public static final long FLAG_PUSH = 0x02;
	public static final long FLAG_DIRECTIONS = 0x4;
	public static final long FLAG_SHARE = 0x8;
	public static final long FLAG_CALL = 0x10;
	public static final long FLAG_REDEEM = 0x20;
	public static final long FLAG_VIEW = 0x40;

	public String getTemplateName() {
		return mTemplateName;
	}

	public void setTemplateName(String mTemplateName) {
		this.mTemplateName = mTemplateName;
	}

	/**
	 * NEW = This notification has never been displayed to the user.
	 * NOTIFIED = This notification has been displayed and is still visible/active.
	 * DISMISSED = This notification has passed or been dismissed by the user.
	 */
	public enum StatusType {
		NEW,
		NOTIFIED,
		DISMISSED
	}

	/**
	 * RESOURCE = A resource must be specified in setImageResId.
	 * URL = A url must be specified in setImageValue, and a placeholder image
	 * must be specified in setImageResId.
	 * URLS = A list of URLs. It will be stored as a JSONArray object.
	 * DESTINATION = An airport code must be specified in setImageValue, and a
	 * placeholder image must be specified in setImageResId.
	 * CAR = A car cetegory must be specified in setImageValue, and a placeholder
	 * image must be specified in setImageResId.
	 * ACTIVITY = An activitytype must be specified in setImageValue, and a
	 * placeholder image must be specified in setImageResId.
	 */
	public enum ImageType {
		RESOURCE,
		URL,
		URLS,
		DESTINATION,
		CAR,
		ACTIVITY,
		NONE
	}

	/**
	 * These correspond to the notification types we report through omniture.
	 * https://confluence/display/Omniture/App+Itinerary#AppItinerary-Version31
	 */
	public enum NotificationType {
		ACTIVITY_START,
		CAR_DROP_OFF,
		CAR_PICK_UP,
		FLIGHT_SHARE,
		FLIGHT_CHECK_IN,
		FLIGHT_CANCELLED,
		FLIGHT_GATE_TIME_CHANGE,
		FLIGHT_GATE_NUMBER_CHANGE,
		FLIGHT_DEPARTURE_REMINDER,
		FLIGHT_BAGGAGE_CLAIM,
		HOTEL_CHECK_IN,
		HOTEL_CHECK_OUT,
		DESKTOP_BOOKING,
		HOTEL_GET_READY,
		HOTEL_ACTIVITY_CROSSSEll,
		HOTEL_PRE_TRIP,
		HOTEL_ACTIVITY_IN_TRIP,
		FLIGHT_DELAYED,
		HOTEL_REVIEW
	}


	/**
	 * An array of valid resId's that can be used both for
	 * get/setImageResId and get/setIconResId.
	 * <p>
	 * Be careful when modifying this: only append to it and don't
	 * reorder anything. We'll store the INDEX of the resid from this array
	 * into the database. We won't store the resId's themselves because
	 * resId's are apt to change in a later build.
	 */
	private static final int[] sResIdMap = {
		0,
		R.drawable.ic_stat_expedia,
		R.drawable.ic_stat_flight,
		R.drawable.ic_stat_car,
		R.drawable.ic_stat_hotel,
		R.drawable.bg_itin_placeholder_cloud,
		R.drawable.ic_itin_ready,
	};

	@Column(name = "UniqueId")
	private String mUniqueId;

	@Column(name = "ItinId")
	private String mItinId;

	@Column(name = "TriggerTimeMillis")
	private long mTriggerTimeMillis;

	@Column(name = "ExpirationTimeMillis")
	private long mExpirationTimeMillis;

	// Oddly named for historical purposes
	@Column(name = "IconResId")
	private int mIconId;

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

	// Oddly named for historical purposes
	@Column(name = "ImageResId")
	private int mImageId;

	@Column(name = "ImageValue")
	private String mImageValue;

	@Column(name = "TemplateName")
	private String mTemplateName;

	@Column(name = "Flags")
	private long mFlags;

	// For Omniture tracking
	@Column(name = "NotificationType")
	private String mNotificationType;

	@Column(name = "DeepLink")
	private String mDeepLink;

	public Notification() {
	}

	/**
	 * Create a new com.expedia.bookings.notification.Notification object. If two
	 * notification objects share the same uniqueId, only the later one will be
	 * displayed.
	 *
	 * @param uniqueId - UniqueId to key the notification on
	 * @param itinId - Id of the Itin to open when clicked.
	 * @param triggerTimeMillis - when to display notification
	 */
	public Notification(String uniqueId, String itinId, long triggerTimeMillis) {
		setUniqueId(uniqueId);
		setItinId(itinId);
		setTriggerTimeMillis(triggerTimeMillis);

		// Defaults
		setExpirationTimeMillis(triggerTimeMillis + TimeUnit.DAYS.toMillis(1));
		setStatus(StatusType.NEW);
		setIconResId(R.drawable.ic_stat_expedia);
		setDeepLink("");
		setTemplateName("");
		setFlags(0);
	}

	//////////////////////////////////////////////////////////////////////////
	// Accessors

	public String getUniqueId() {
		return mUniqueId;
	}

	public void setUniqueId(String itinId) {
		this.mUniqueId = itinId;
	}

	public String getItinId() {
		return mItinId;
	}

	public void setItinId(String itinId) {
		this.mItinId = itinId;
	}

	public long getTriggerTimeMillis() {
		return mTriggerTimeMillis;
	}

	public void setTriggerTimeMillis(long triggerTimeMillis) {
		this.mTriggerTimeMillis = triggerTimeMillis;
	}

	public long getExpirationTimeMillis() {
		return mExpirationTimeMillis;
	}

	public void setExpirationTimeMillis(long expirationTimeMillis) {
		this.mExpirationTimeMillis = expirationTimeMillis;
	}

	public int getIconResId() {
		int resId = unmarshallResId(mIconId);

		if (resId == 0 || mIconId == 1) {
			resId = ProductFlavorFeatureConfiguration.getInstance().getNotificationIconResourceId();
		}

		return resId;
	}

	public void setIconResId(int iconResId) {
		this.mIconId = marshallResId(iconResId);
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
		int resId = unmarshallResId(mImageId);
		return resId == 0 ? R.drawable.bg_itin_placeholder : resId;
	}

	public void setImageResId(int imageResId) {
		this.mImageId = marshallResId(imageResId);
	}

	public String getImageValue() {
		return mImageValue;
	}

	public void setImageValue(String value) {
		this.mImageValue = value;
	}

	public void setImageCar(String url) {
		setImage(ImageType.CAR, 0, url);
	}

	public void setImageDestination(int placeholderResId, String value) {
		setImage(ImageType.DESTINATION, placeholderResId, value);
	}

	public void setImageUrls(List<String> urls) {
		if (urls != null && urls.size() > 0) {
			JSONArray arr = new JSONArray();
			for (String url : urls) {
				arr.put(url);
			}
			setImage(ImageType.URLS, 0, arr.toString());
		}
	}

	public List<String> getImageUrls() {
		ArrayList<String> urls = new ArrayList<String>();

		try {
			JSONArray arr = new JSONArray(getImageValue());
			for (int i = 0; i < arr.length(); i++) {
				urls.add(arr.getString(i));
			}
		}
		catch (JSONException e) {
			return null;
		}

		return urls;
	}

	public void didNotify() {
		this.setStatus(StatusType.NOTIFIED);
		this.save();
	}

	private void setImage(ImageType type, int resId, String value) {
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


	public String getDeepLink() {
		return mDeepLink;
	}

	public void setDeepLink(String mDeepLink) {
		this.mDeepLink = mDeepLink;
	}

	public NotificationType getNotificationType() {
		return NotificationType.valueOf(mNotificationType);
	}

	public void setNotificationType(NotificationType notificationType) {
		this.mNotificationType = notificationType.toString();
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("ItinId", mItinId);
			obj.put("UniqueId", mUniqueId);
			obj.put("TriggerTimeMillis", mTriggerTimeMillis);
			obj.put("ExpirationTimeMillis", mExpirationTimeMillis);
			obj.put("IconId", mIconId);
			obj.put("Ticker", mTicker);
			obj.put("Title", mTitle);
			obj.put("Body", mBody);
			obj.put("Status", mStatus);
			obj.put("ImageType", mImageType);
			obj.put("ImageId", mImageId);
			obj.put("ImageValue", mImageValue);
			obj.put("Flags", mFlags);
			obj.put("TemplateName", mTemplateName);
			obj.put("NotificationType", mNotificationType);
			obj.put("DeepLink", mDeepLink);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mItinId = obj.optString("ItinId");
		mUniqueId = obj.optString("UniqueId");
		mTriggerTimeMillis = obj.optLong("TriggerTimeMillis");
		mExpirationTimeMillis = obj.optLong("ExpirationTimeMillis");
		mIconId = obj.optInt("IconId");
		mTicker = obj.optString("Ticker");
		mTitle = obj.optString("Title");
		mBody = obj.optString("Body");
		mStatus = obj.optString("Status");
		mImageType = obj.optString("ImageType");
		mImageId = obj.optInt("ImageId");
		mImageValue = obj.optString("ImageValue");
		mFlags = obj.optLong("Flags");
		mNotificationType = obj.optString("NotificationType");
		mTemplateName = obj.optString("TemplateName");
		mDeepLink = obj.optString("DeepLink");
		return true;
	}

	//////////////////////////////////////////////////////////////////////////
	// Create instance from JSON

	/**
	 * Creates a Notification instance, given its JSON representation.
	 * @param json
	 * @return
	 */
	public static Notification getInstanceFromJsonString(String json) {
		Notification notification = new Notification();

		try {
			notification.fromJson(new JSONObject(json));
		}
		catch (JSONException e) {
			Log.e("Unable to parse notification.", e);
		}

		return notification;
	}

	//////////////////////////////////////////////////////////////////////////
	// Helpers

	/**
	 * Finds the index of resId in sResIdMap.
	 * @param resId
	 * @return index in sResIdMap
	 */
	private static int marshallResId(int resId) {
		for (int i = 0; i < sResIdMap.length; i++) {
			if (sResIdMap[i] == resId) {
				return i;
			}
		}
		Log.e("ResId not expected: " + resId);
		return 0;
	}

	/**
	 * Returns the resId, given its index in sResIdMap.
	 * In case of out of bounds, returns 0.
	 * @param index
	 * @return resId, or 0
	 */
	private static int unmarshallResId(int index) {
		if (index < 0 || index >= sResIdMap.length) {
			return 0;
		}
		return sResIdMap[index];
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj.getClass() != this.getClass()) {
			return false;
		}

		Notification other = (Notification) obj;

		if (!mUniqueId.equals(other.getUniqueId())) {
			return false;
		}

		if (mTriggerTimeMillis != other.getTriggerTimeMillis()) {
			return false;
		}

		if (!mTitle.equals(other.getTitle())) {
			return false;
		}

		if (!mBody.equals(other.getBody())) {
			return false;
		}

		if (!mTicker.equals(other.getTicker())) {
			return false;
		}

		if (mExpirationTimeMillis != other.getExpirationTimeMillis() && mExpirationTimeMillis > System
			.currentTimeMillis()) {
			return false;
		}

		return true;
	}
}
