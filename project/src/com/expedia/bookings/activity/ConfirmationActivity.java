package com.expedia.bookings.activity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.SupportUtils;
import com.expedia.bookings.widget.RoomTypeHandler;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.MapUtils;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.hotellib.data.BillingInfo;
import com.mobiata.hotellib.data.BookingResponse;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Location;
import com.mobiata.hotellib.data.Money;
import com.mobiata.hotellib.data.Policy;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Rate;
import com.mobiata.hotellib.data.RateBreakdown;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.utils.JSONUtils;
import com.mobiata.hotellib.utils.StrUtils;
import com.mobiata.hotellib.widget.HotelItemizedOverlay;
import com.omniture.AppMeasurement;

public class ConfirmationActivity extends MapActivity {

	public static final String EXTRA_FINISH = "EXTRA_FINISH";

	private static final String CONFIRMATION_DATA_FILE = "confirmation.dat";

	private static final int INSTANCE_PROPERTY = 1;
	private static final int INSTANCE_SEARCH_PARAMS = 2;
	private static final int INSTANCE_RATE = 3;
	private static final int INSTANCE_BILLING_INFO = 4;
	private static final int INSTANCE_BOOKING_RESPONSE = 5;

	private Context mContext;

	private RoomTypeHandler mRoomTypeHandler;

	private SearchParams mSearchParams;
	private Property mProperty;
	private Rate mRate;
	private BillingInfo mBillingInfo;
	private BookingResponse mBookingResponse;

	private String mContactText;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		setContentView(R.layout.activity_confirmation);

		// Retrieve data to build this with
		boolean loadedData = false;
		SparseArray<Object> instance = (SparseArray<Object>) getLastNonConfigurationInstance();
		if (instance != null) {
			mSearchParams = (SearchParams) instance.get(INSTANCE_SEARCH_PARAMS);
			mProperty = (Property) instance.get(INSTANCE_PROPERTY);
			mRate = (Rate) instance.get(INSTANCE_RATE);
			mBillingInfo = (BillingInfo) instance.get(INSTANCE_BILLING_INFO);
			mBookingResponse = (BookingResponse) instance.get(INSTANCE_BOOKING_RESPONSE);
			loadedData = true;
		}
		else if (hasSavedConfirmationData(this)) {
			if (loadSavedConfirmationData()) {
				loadedData = true;
			}
			else {
				// If we failed to load the saved confirmation data, we should
				// delete the file and go back (since we are only here if we were called
				// directly from a startup).
				deleteSavedConfirmationData(this);
				finish();
			}
		}

		final Intent intent = getIntent();
		if (!loadedData) {
			mSearchParams = (SearchParams) JSONUtils.parseJSONableFromIntent(intent, Codes.SEARCH_PARAMS,
					SearchParams.class);
			mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY, Property.class);
			mRate = (Rate) JSONUtils.parseJSONableFromIntent(intent, Codes.RATE, Rate.class);
			mBillingInfo = (BillingInfo) JSONUtils.parseJSONableFromIntent(intent, Codes.BILLING_INFO,
					BillingInfo.class);
			mBookingResponse = (BookingResponse) JSONUtils.parseJSONableFromIntent(intent, Codes.BOOKING_RESPONSE,
					BookingResponse.class);

			// Start a background thread to save this data to the disk
			new Thread(new Runnable() {
				public void run() {
					saveConfirmationData();
				}
			}).start();
		}

		// TODO: Delete this once done testing
		// This code allows us to test the ConfirmationActivity standalone, for layout purposes.
		// Just point the default launcher activity towards this instead of SearchActivity
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
			try {
				mSearchParams = new SearchParams();
				mSearchParams.fillWithTestData();
				mProperty = new Property();
				mProperty.fillWithTestData();
				mRate = new Rate();
				mRate.fillWithTestData();
				mBookingResponse = new BookingResponse();
				mBookingResponse.fillWithTestData();

				mBillingInfo = new BillingInfo();
				mBillingInfo.setEmail("dan@mobiata.com");
				Location location = new Location();
				location.setCountryCode("US");
				location.setPostalCode("55408");
				mBillingInfo.setLocation(location);
			}
			catch (JSONException e) {
				Log.e("Couldn't create dummy data!", e);
			}
		}

		mRoomTypeHandler = new RoomTypeHandler(this, mProperty, mRate);
		mRoomTypeHandler.onCreate();

		//////////////////////////////////////////////////
		// Screen configuration

		// Thumbnail in the map
		ImageView thumbnail = (ImageView) findViewById(R.id.thumbnail_image_view);
		if (mProperty.getThumbnail() != null) {
			ImageCache.loadImage(mProperty.getThumbnail().getUrl(), thumbnail);
		}
		else {
			thumbnail.setVisibility(View.GONE);
		}

		// Show on the map where the hotel is
		MapView mapView = MapUtils.createMapView(this);
		ViewGroup mapContainer = (ViewGroup) findViewById(R.id.map_layout);
		mapContainer.addView(mapView);

		List<Property> properties = new ArrayList<Property>(1);
		properties.add(mProperty);
		List<Overlay> overlays = mapView.getOverlays();
		HotelItemizedOverlay overlay = new HotelItemizedOverlay(this, properties, false, mapView, null);
		overlays.add(overlay);
		MapController mc = mapView.getController();
		GeoPoint center = overlay.getCenter();
		GeoPoint offsetCenter = new GeoPoint(center.getLatitudeE6() + 1000, center.getLongitudeE6() - 8000);
		mc.setCenter(offsetCenter);
		mc.setZoom(15);

		// Overview of hotel
		TextView nameView = (TextView) findViewById(R.id.name_text_view);
		nameView.setText(mProperty.getName());
		Location location = mProperty.getLocation();
		TextView address1View = (TextView) findViewById(R.id.address1_text_view);
		address1View.setText(StrUtils.formatAddressStreet(location));
		TextView address2View = (TextView) findViewById(R.id.address2_text_view);
		address2View.setText(StrUtils.formatAddressCity(location));
		RatingBar hotelRating = (RatingBar) findViewById(R.id.hotel_rating_bar);
		hotelRating.setRating((float) mProperty.getHotelRating());
		RatingBar userRating = (RatingBar) findViewById(R.id.user_rating_bar);
		userRating.setRating((float) mProperty.getUserRating());

		// Reservation summary
		ViewGroup detailsLayout = (ViewGroup) findViewById(R.id.details_layout);
		LayoutUtils.addDetail(this, detailsLayout, R.string.confirmation_number, mBookingResponse.getConfNumber());
		LayoutUtils.addDetail(this, detailsLayout, R.string.itinerary_number, mBookingResponse.getItineraryId());
		LayoutUtils.addDetail(this, detailsLayout, R.string.confirmation_email, mBillingInfo.getEmail());
		mRoomTypeHandler.load(detailsLayout);
		LayoutUtils.addRateDetails(this, detailsLayout, mSearchParams, mProperty, mRate);

		// Total cost / cancellation policy at the bottom
		TextView totalCostView = (TextView) findViewById(R.id.total_cost_text_view);
		totalCostView.setText(mRate.getTotalAmountAfterTax().getFormattedMoney());
		TextView cancellationPolicyView = (TextView) findViewById(R.id.cancellation_policy_text_view);
		Policy cancellationPolicy = mRate.getRateRules().getPolicy(Policy.TYPE_CANCEL);
		if (cancellationPolicy != null) {
			cancellationPolicyView.setText(Html.fromHtml(cancellationPolicy.getDescription()));
		}
		else {
			cancellationPolicyView.setVisibility(View.GONE);
		}

		// Reservation support contact info
		TextView contactView = (TextView) findViewById(R.id.contact_text_view);
		if (Locale.getDefault().getCountry().toUpperCase().equals("CN")) {
			// Special case for China
			mContactText = getString(R.string.contact_phone_china_template, "10-800712-2608", "10-800120-2608");
		}
		else if (SupportUtils.hasConfSupportNumber()) {
			mContactText = getString(R.string.contact_phone_template, SupportUtils.getConfSupportNumber());
		}
		else {
			mContactText = getString(R.string.contact_phone_default_template, "1-800-780-5733", "00-800-11-20-11-40");
		}
		contactView.setText(mContactText);

		//////////////////////////////////////////////////
		// Button bar configuration
		ImageButton shareButton = (ImageButton) findViewById(R.id.share_button);
		shareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				share();
			}
		});

		ImageButton mapButton = (ImageButton) findViewById(R.id.map_button);
		mapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent newIntent = new Intent(Intent.ACTION_VIEW);
				String queryAddress = com.mobiata.hotellib.utils.StrUtils.formatAddress(mProperty.getLocation())
						.replace("\n", " ");
				newIntent.setData(Uri.parse("geo:0,0?q=" + queryAddress));
				startActivity(newIntent);
			}
		});

		Button newSearchButton = (Button) findViewById(R.id.new_search_button);
		newSearchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("User is initiating a new search.");

				onClickNewSearch();

				// Ensure we can't come back here again
				deleteSavedConfirmationData(mContext);

				Intent intent = new Intent(mContext, SearchActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

		// Tracking
		if (savedInstanceState == null) {
			onPageLoad();
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		SparseArray<Object> instance = new SparseArray<Object>();

		instance.put(INSTANCE_SEARCH_PARAMS, mSearchParams);
		instance.put(INSTANCE_PROPERTY, mProperty);
		instance.put(INSTANCE_RATE, mRate);
		instance.put(INSTANCE_BILLING_INFO, mBillingInfo);
		instance.put(INSTANCE_BOOKING_RESPONSE, mBookingResponse);

		mRoomTypeHandler.onRetainNonConfigurationInstance(instance);
		return instance;
	}

	@Override
	protected void onPause() {
		super.onPause();

		mRoomTypeHandler.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();

		mWasStopped = true;
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (mWasStopped) {
			onPageLoad();
			mWasStopped = false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mRoomTypeHandler.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// #7090: A user should remain on the confirmation page until they explicitly press the
		// "new search" key.  This is the easiest way to get out of this - send the user back
		// to the start, then finish that activity, when the user presses back.
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			Intent i = new Intent(mContext, SearchActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.putExtra("EXTRA_FINISH", true);
			startActivity(i);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void share() {
		Resources res = getResources();

		DateFormat dateFormatter = new SimpleDateFormat("MM/dd");
		DateFormat fullDateFormatter = android.text.format.DateFormat.getMediumDateFormat(this);
		DateFormat dayFormatter = new SimpleDateFormat("EEE");

		Date checkIn = mSearchParams.getCheckInDate().getTime();
		Date checkOut = mSearchParams.getCheckOutDate().getTime();

		// Create the subject
		String dateStart = dateFormatter.format(checkIn);
		String dateEnd = dateFormatter.format(checkOut);
		String subject = getString(R.string.share_subject_template, mProperty.getName(), dateStart, dateEnd);

		// Create the body 
		StringBuilder body = new StringBuilder();
		body.append(getString(R.string.share_body_start));
		body.append("\n\n");

		body.append(mProperty.getName());
		body.append("\n");
		body.append(StrUtils.formatAddress(mProperty.getLocation()));
		body.append("\n\n");

		appendLabelValue(body, R.string.confirmation_number, mBookingResponse.getConfNumber());
		body.append("\n");
		appendLabelValue(body, R.string.itinerary_number, mBookingResponse.getItineraryId());
		body.append("\n\n");

		appendLabelValue(body, R.string.name,
				getString(R.string.name_template, mBillingInfo.getFirstName(), mBillingInfo.getLastName()));
		body.append("\n");
		appendLabelValue(body, R.string.CheckIn,
				dayFormatter.format(checkIn) + ", " + fullDateFormatter.format(checkIn));
		body.append("\n");
		appendLabelValue(body, R.string.CheckOut,
				dayFormatter.format(checkOut) + ", " + fullDateFormatter.format(checkOut));
		body.append("\n");
		int numDays = mSearchParams.getStayDuration();
		appendLabelValue(body, R.string.stay_duration,
				res.getQuantityString(R.plurals.length_of_stay, numDays, numDays));
		body.append("\n\n");

		appendLabelValue(body, R.string.room_type, Html.fromHtml(mRate.getRoomDescription()).toString());
		body.append("\n");
		appendLabelValue(body, R.string.bed_type, mRate.getRatePlanName());
		body.append("\n");
		appendLabelValue(body, R.string.adults, mSearchParams.getNumAdults() + "");
		body.append("\n");
		appendLabelValue(body, R.string.children, mSearchParams.getNumChildren() + "");
		body.append("\n\n");

		if (mRate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : mRate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String dateStr = dayFormatter.format(date) + ", " + fullDateFormatter.format(date);
				Money amount = breakdown.getAmount();
				if (amount.getAmount() == 0) {
					appendLabelValue(body, getString(R.string.room_rate_template, dateStr), getString(R.string.free));
				}
				else {
					appendLabelValue(body, getString(R.string.room_rate_template, dateStr), amount.getFormattedMoney());
				}
				body.append("\n");
			}
			body.append("\n\n");
		}

		if (mRate.getTotalAmountBeforeTax() != null) {
			appendLabelValue(body, R.string.subtotal, mRate.getTotalAmountBeforeTax().getFormattedMoney());
			body.append("\n");
		}

		Money surcharge = mRate.getSurcharge();
		Money extraGuestFee = mRate.getExtraGuestFee();
		if (extraGuestFee != null) {
			appendLabelValue(body, R.string.extra_guest_charge, extraGuestFee.getFormattedMoney());
			if (surcharge != null) {
				surcharge = surcharge.copy();
				surcharge.subtract(extraGuestFee);
			}
		}
		if (surcharge != null) {
			appendLabelValue(body, R.string.TaxesAndFees, surcharge.getFormattedMoney());
			body.append("\n");
		}

		if (mRate.getTotalAmountAfterTax() != null) {
			body.append("\n");
			appendLabelValue(body, R.string.Total, mRate.getTotalAmountAfterTax().getFormattedMoney());
		}

		Policy cancellationPolicy = mRate.getRateRules().getPolicy(Policy.TYPE_CANCEL);
		if (cancellationPolicy != null) {
			body.append("\n\n");
			body.append(getString(R.string.cancellation_policy));
			body.append("\n");
			body.append(Html.fromHtml(cancellationPolicy.getDescription()));
		}

		body.append("\n\n");
		body.append(mContactText);

		SocialUtils.email(this, subject, body.toString());
	}

	private void appendLabelValue(StringBuilder sb, int labelStrId, String value) {
		appendLabelValue(sb, getString(labelStrId), value);
	}

	private void appendLabelValue(StringBuilder sb, String label, String value) {
		sb.append(label);
		sb.append(": ");
		sb.append(value);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Breadcrumb (reloading activity)

	public static boolean hasSavedConfirmationData(Context context) {
		File savedConfResults = context.getFileStreamPath(CONFIRMATION_DATA_FILE);
		return savedConfResults.exists();
	}

	public static boolean deleteSavedConfirmationData(Context context) {
		Log.i("Deleting saved confirmation data.");
		File savedConfResults = context.getFileStreamPath(CONFIRMATION_DATA_FILE);
		return savedConfResults.delete();
	}

	public boolean loadSavedConfirmationData() {
		Log.i("Loading saved confirmation data...");
		try {
			JSONObject data = new JSONObject(IoUtils.readStringFromFile(CONFIRMATION_DATA_FILE, this));
			mSearchParams = (SearchParams) JSONUtils.getJSONable(data, Codes.SEARCH_PARAMS, SearchParams.class);
			mProperty = (Property) JSONUtils.getJSONable(data, Codes.PROPERTY, Property.class);
			mRate = (Rate) JSONUtils.getJSONable(data, Codes.RATE, Rate.class);
			mBillingInfo = (BillingInfo) JSONUtils.getJSONable(data, Codes.BILLING_INFO, BillingInfo.class);
			mBookingResponse = (BookingResponse) JSONUtils.getJSONable(data, Codes.BOOKING_RESPONSE,
					BookingResponse.class);
			return true;
		}
		catch (Exception e) {
			Log.e("Could not load ConfirmationActivity state.", e);
			return false;
		}
	}

	public boolean saveConfirmationData() {
		Log.i("Saving confirmation data...");
		try {
			JSONObject data = new JSONObject();
			data.put(Codes.SEARCH_PARAMS, mSearchParams.toJson());
			data.put(Codes.PROPERTY, mProperty.toJson());
			data.put(Codes.RATE, mRate.toJson());
			data.put(Codes.BILLING_INFO, mBillingInfo.toJson());
			data.put(Codes.BOOKING_RESPONSE, mBookingResponse.toJson());

			IoUtils.writeStringToFile(CONFIRMATION_DATA_FILE, data.toString(0), this);

			return true;
		}
		catch (Exception e) {
			Log.e("Could not save ConfirmationActivity state.", e);
			return false;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	public void onPageLoad() {
		Log.d("Tracking \"App.Hotels.Checkout.Confirmation\" pageLoad");

		AppMeasurement s = new AppMeasurement(getApplication());

		TrackingUtils.addStandardFields(this, s);

		s.pageName = "App.Hotels.Checkout.Confirmation";

		s.events = "purchase";

		// Shopper/Confirmer
		s.eVar25 = s.prop25 = "Confirmer";

		// Product details
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String checkIn = df.format(mSearchParams.getCheckInDate().getTime());
		String checkOut = df.format(mSearchParams.getCheckOutDate().getTime());
		s.eVar30 = "Hotel:" + checkIn + "-" + checkOut + ":N";

		// Unique confirmation id
		s.prop15 = s.purchaseID = mBookingResponse.getConfNumber();

		// Billing country code
		s.prop46 = s.state = mBillingInfo.getLocation().getCountryCode();

		// Billing zip codes
		s.prop49 = s.zip = mBillingInfo.getLocation().getPostalCode();

		// Products
		int numDays = mSearchParams.getStayDuration();
		double totalCost = 0;
		if (mRate != null && mRate.getTotalAmountAfterTax() != null) {
			totalCost = mRate.getTotalAmountAfterTax().getAmount();
		}
		TrackingUtils.addProducts(s, mProperty, numDays, totalCost);

		// Currency code
		s.currencyCode = mRate.getTotalAmountAfterTax().getCurrency();

		// Send the tracking data
		s.track();
	}

	public void onClickNewSearch() {
		Log.d("Tracking \"new search\" onClick");
		TrackingUtils.trackSimpleEvent(this, null, null, "Shopper", "CKO.CP.StartNewSearch");
	}
}
