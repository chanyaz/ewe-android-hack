package com.expedia.bookings.widget;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.appwidget.ExpediaBookingsService;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

/**
 * This class is responsible for managing the 
 * widget preview that appears on the widget 
 * configuration activity and the widget dialog in the app.
 *
 */
public class WidgetPreviewHandler {

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// VIEWS
	//----------------------------------

	private ImageButton mNextHotelButton;
	private ImageButton mPrevHotelButton;
	private TextView mHotelNameTextView;
	private TextView mHotelLocationTextView;
	private TextView mHotelPriceTextView;
	private TextView mSaleTextView;
	private TextView mHighlyRatedTextView;
	private ImageView mHotelThumbnailView;
	private ViewGroup mNavigationContainer;
	private ViewGroup mPricePerNightContainer;
	private ViewGroup mWidgetContentsContainer;
	private View mRootView;
	private EditText mSpecifyLocationEditText;

	//----------------------------------
	// OTHERS
	//----------------------------------

	private List<Property> mProperties;
	private int mCurrentPosition;
	private Activity mActivity;
	private TimerTask mTimerTask;
	private Timer mTimer;

	/**
	 * Permantly cached properties and location
	 * to preview in the widget. The image is loaded
	 * from over the network, which is the only piece of information
	 * that is not permanently cached for this widget.
	 */
	private static final String PROPERTIES_IN_JSON = "{\"properties\":[{\"lowestRate\":{\"numberOfNights\":1,\"rateType\":0,\"averageRate\":{\"amount\":71.99,\"currency\":\"USD\"},\"rateChange\":false,\"averageBaseRate\":{\"amount\":79.99,\"currency\":\"USD\"},\"surcharge\":{\"amount\":8.32,\"currency\":\"USD\"},\"valueAdds\":[],\"numRoomsLeft\":0,\"promoDescription\":\"Sale! Save 10% on this Stay.\"},\"location\":{\"countryCode\":\"US\",\"streetAddress\":[\"16838 International Boulevard\"],\"stateCode\":\"WA\",\"longitude\":-122.29576,\"latitude\":47.45145,\"postalCode\":\"98188\",\"city\":\"SeaTac\"},\"averageExpediaRating\":3.9,\"totalReviews\":248,\"available\":true,\"supplierType\":\"E\",\"distanceFromUser\":{\"unit\":\"MILES\",\"distance\":13.055667},\"lowRate\":{\"amount\":71.99,\"currency\":\"USD\"},\"thumbnail\":{\"url\":\"http://media.expedia.com/mobiata/hotels/153008_180.jpg\",\"width\":0,\"height\":0},\"propertyId\":\"153008\",\"totalRecommendations\":211,\"description\":\"<p><b>Location. </b> <br />Located in SeaTac, Red Roof Inn Seattle Airport is near the airport and close to Westfield Southcenter, Hydroplane and Raceboat Museum, and Starfire Sports Complex. Other regional attractions include Pike Place Market and Space Needle. </p><p><b>Hotel Features. </b><br />Recreational amenities include a fitness facility. Complimentary wireless Internet access is available in public areas. The property has an airport shuttle, which is complimentary. Guest parking is complimentary. Additional property amenities include laundry facilities. Extended parking privileges may be offered to guests after check out (surcharge). A total renovation of this property was completed in 2001. </p><p><b>Guestrooms. </b> <br /> There are 152 guestrooms at Red Roof Inn Seattle Airport. Bathrooms feature shower/tub combinations and hair dryers. Wireless Internet access is complimentary. In addition to complimentary newspapers and in room safes, guestrooms offer speakerphones with voice mail as well as complimentary local calls (restrictions may apply). Televisions have premium cable channels, first run movies, and free movie channels. Rooms also include blackout drapes/curtains, electronic/magnetic keys, irons/ironing boards, and clock radios. Guests may request extra towels/bedding and wake up calls. Housekeeping is available daily. Cribs (infant beds) are available on request. </p> <br /> <p><b>Notifications and Fees</b><br /></p><p>The following fees and deposits are charged by the property at time of service, check in, or check out.  <ul><li>Safe: US$ 1.50 per night</li> </ul></p><p>The above list may not be comprehensive. Fees and deposits may not include tax and are subject to change. </p>\",\"name\":\"Red Roof Inn Seattle Airport\",\"expediaPropertyId\":14917,\"hotelRating\":2,\"media\":[{\"url\":\"http://media.expedia.com/hotels/1000000/20000/15000/14917/14917_29_b.jpg\",\"width\":0,\"height\":0},{\"url\":\"http://media.expedia.com/hotels/1000000/20000/15000/14917/14917_28_b.jpg\",\"width\":0,\"height\":0},{\"url\":\"http://media.expedia.com/hotels/1000000/20000/15000/14917/14917_30_b.jpg\",\"width\":0,\"height\":0},{\"url\":\"http://media.expedia.com/hotels/1000000/20000/15000/14917/14917_31_b.jpg\",\"width\":0,\"height\":0},{\"url\":\"http://media.expedia.com/hotels/1000000/20000/15000/14917/14917_32_b.jpg\",\"width\":0,\"height\":0},{\"url\":\"http://media.expedia.com/hotels/1000000/20000/15000/14917/14917_33_b.jpg\",\"width\":0,\"height\":0},{\"url\":\"http://media.expedia.com/hotels/1000000/20000/15000/14917/14917_34_b.jpg\",\"width\":0,\"height\":0}],\"amenityMask\":150945866}]}";

	public WidgetPreviewHandler(Activity activity) {
		mActivity = activity;
		mTimer = new Timer();
		mTimerTask = getTimerTask();

		ViewGroup widgetContentsRootView = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.widget_contents,
				null);
		ViewGroup hotelInfoContents = (ViewGroup) mActivity.findViewById(R.id.hotel_info_contents);
		hotelInfoContents.addView(widgetContentsRootView);

		mNextHotelButton = (ImageButton) mActivity.findViewById(R.id.next_hotel_btn);
		mPrevHotelButton = (ImageButton) mActivity.findViewById(R.id.prev_hotel_btn);
		mNavigationContainer = (ViewGroup) mActivity.findViewById(R.id.navigation_container);
		mHotelNameTextView = (TextView) mActivity.findViewById(R.id.hotel_name_text_view);
		mHotelLocationTextView = (TextView) mActivity.findViewById(R.id.location_text_view);
		mSaleTextView = (TextView) mActivity.findViewById(R.id.sale_text_view);
		mHighlyRatedTextView = (TextView) mActivity.findViewById(R.id.highly_rated_text_view);
		mHotelPriceTextView = (TextView) mActivity.findViewById(R.id.price_text_view);
		mHotelThumbnailView = (ImageView) mActivity.findViewById(R.id.hotel_image_view);
		mPricePerNightContainer = (ViewGroup) mActivity.findViewById(R.id.price_per_night_container);
		mWidgetContentsContainer = (ViewGroup) mActivity.findViewById(R.id.widget_contents_container);

		mRootView = mActivity.findViewById(R.id.root_widget_config_view);
		mSpecifyLocationEditText = (EditText) mActivity.findViewById(R.id.location_option_text_view);

		/*
		 * Capture the touch event on the root view to 
		 * clear focus off the location edit text when the user clicks
		 * anywhere outside of the edit text
		 */
		mRootView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mSpecifyLocationEditText.clearFocus();
				// return false so that the root view
				// still manages the touch event, even though 
				// it was used to clear the state of the edit text
				return false;
			}
		});

		mNextHotelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loadNextProperty();
			}
		});

		mPrevHotelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loadPrevProperty();
			}
		});

		mNavigationContainer.setVisibility(View.INVISIBLE);
	}

	@SuppressWarnings("unchecked")
	public void loadPreviewHotels() {
		mCurrentPosition = 0;

		try {
			JSONObject obj = new JSONObject(PROPERTIES_IN_JSON);
			mProperties = (List<Property>) JSONUtils.getJSONableList(obj, "properties", Property.class);
		}
		catch (JSONException e) {
			Log.i("Error parsing properties list: ", e);
		}

		String[] hotelNames = mActivity.getResources().getStringArray(R.array.preview_hotels);
		int i = 0;
		for(String hotelName : hotelNames) {
			if(i > (mProperties.size() -1)) {
				Property newProperty = new Property();
				newProperty.fromJson(mProperties.get(0).toJson());
				newProperty.setName(hotelName);
				mProperties.add(newProperty);
			} else {
				mProperties.get(i).setName(hotelName);
			}
			i++;
		}
		showProperty(mProperties.get(mCurrentPosition));
	}

	private void showProperty(Property property) {

		mNavigationContainer.setVisibility(View.VISIBLE);
		mWidgetContentsContainer.setVisibility(View.VISIBLE);

		mHotelNameTextView.setText(property.getName());
		mHotelLocationTextView.setText(mActivity.getString(R.string.preview_hotel_location));
		mHotelPriceTextView.setText(StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate()));

		if (property.getLowestRate().getSavingsPercent() > 0) {
			mSaleTextView.setText(mActivity.getString(R.string.widget_savings_template, property.getLowestRate()
					.getSavingsPercent() * 100));
			mPricePerNightContainer.setBackgroundResource(R.drawable.widget_price_bg);
			mSaleTextView.setVisibility(View.VISIBLE);
			mHighlyRatedTextView.setVisibility(View.GONE);
		}
		else if (property.getLowestRate().getSavingsPercent() == 0 && property.isHighlyRated()) {
			mSaleTextView.setVisibility(View.GONE);
			mHighlyRatedTextView.setVisibility(View.VISIBLE);
		}
		else {
			mSaleTextView.setVisibility(View.GONE);
			mHighlyRatedTextView.setVisibility(View.GONE);
			mPricePerNightContainer.setBackgroundResource(R.drawable.widget_price_bg_no_sale);
		}

		mActivity.findViewById(R.id.loading_text_container).setVisibility(View.GONE);
		mActivity.findViewById(R.id.loading_text_view).setVisibility(View.GONE);
		mActivity.findViewById(R.id.refresh_text_view).setVisibility(View.GONE);

		mHotelThumbnailView.setImageResource(R.drawable.widget_thumbnail_background);
		ImageCache.loadImage(property.getThumbnail().getUrl(), mHotelThumbnailView);

		mWidgetContentsContainer.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.fade_in));
	}

	private void loadNextProperty() {
		mCurrentPosition = ((mCurrentPosition + 1) >= mProperties.size()) ? 0 : mCurrentPosition + 1;
		showProperty(mProperties.get(mCurrentPosition));
		scheduleRotation();
	}

	private void loadPrevProperty() {
		mCurrentPosition = ((mCurrentPosition - 1) < 0) ? (mProperties.size() - 1) : (mCurrentPosition - 1);
		showProperty(mProperties.get(mCurrentPosition));
	}

	private void scheduleRotation() {
		mTimer.cancel();
		mTimerTask.cancel();
		mTimer = new Timer();
		mTimerTask = getTimerTask();
		mTimer.schedule(mTimerTask, ExpediaBookingsService.ROTATE_INTERVAL, ExpediaBookingsService.ROTATE_INTERVAL);
	}

	private TimerTask getTimerTask() {
		return new TimerTask() {

			@Override
			public void run() {
				mActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						loadNextProperty();
					}
				});
			}
		};
	}
}
