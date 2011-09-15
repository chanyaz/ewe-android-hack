package com.expedia.bookings.activity;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Session;
import com.expedia.bookings.server.AvailabilityResponseHandler;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.RoomsAndRatesAdapter;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.FormatUtils.Conjunction;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.app.AsyncLoadListActivity;
import com.mobiata.android.json.JSONUtils;
import com.omniture.AppMeasurement;

public class RoomsAndRatesListActivity extends AsyncLoadListActivity {

	private Session mSession;
	private Property mProperty;
	private SearchParams mSearchParams;

	private RoomsAndRatesAdapter mAdapter;

	private ProgressBar mProgressBar;
	private TextView mEmptyTextView;
	private TextView mFooterTextView;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_rooms_and_rates);

		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mEmptyTextView = (TextView) findViewById(R.id.empty_text_view);

		// Retrieve data to build this with
		final Intent intent = getIntent();
		mSession = (Session) JSONUtils.parseJSONableFromIntent(intent, Codes.SESSION, Session.class);
		Property property = mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY,
				Property.class);
		mSearchParams = (SearchParams) JSONUtils.parseJSONableFromIntent(intent, Codes.SEARCH_PARAMS,
				SearchParams.class);

		// This code allows us to test the RoomsAndRatesListActivity standalone, for layout purposes.
		// Just point the default launcher activity towards this instead of SearchActivity
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
			try {
				property = mProperty = new Property();
				mProperty.fillWithTestData();
				mSearchParams = new SearchParams();
				mSearchParams.fillWithTestData();
			}
			catch (JSONException e) {
				Log.e("Couldn't create dummy data!", e);
			}
		}

		// Format the header
		ImageView thumbnailView = (ImageView) findViewById(R.id.thumbnail_image_view);
		if (property.getThumbnail() != null) {
			ImageCache.loadImage(property.getThumbnail().getUrl(), thumbnailView);
		}
		else {
			thumbnailView.setVisibility(View.GONE);
		}

		TextView nameView = (TextView) findViewById(R.id.name_text_view);
		nameView.setText(property.getName());

		TextView locationView = (TextView) findViewById(R.id.location_text_view);
		locationView.setText(StrUtils.formatAddressShort(property.getLocation()));

		RatingBar hotelRating = (RatingBar) findViewById(R.id.hotel_rating_bar);
		hotelRating.setRating((float) property.getHotelRating());

		// Setup the ListView
		View footer = getLayoutInflater().inflate(R.layout.footer_rooms_and_rates, null);
		mFooterTextView = (TextView) footer.findViewById(R.id.footer_text_view);
		getListView().addFooterView(footer, null, false);

		if (savedInstanceState == null) {
			onPageLoad();
		}
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Rate rate = (Rate) mAdapter.getItem(position);
		Intent intent = new Intent(this, BookingInfoActivity.class);
		intent.fillIn(getIntent(), 0);
		intent.putExtra(Codes.SESSION, mSession.toJson().toString());
		intent.putExtra(Codes.RATE, rate.toJson().toString());
		startActivity(intent);
	}

	@Override
	public String getUniqueKey() {
		return "com.expedia.bookings.roomsrates." + mProperty.getPropertyId();
	}

	@Override
	public void showProgress() {
		mProgressBar.setVisibility(View.VISIBLE);
		mEmptyTextView.setText(R.string.room_rates_loading);
	}

	@Override
	public Object downloadImpl() {
		Intent intent = getIntent();
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
			try {
				JSONObject obj = new JSONObject(getString(R.string.sample_availability_response));
				AvailabilityResponseHandler handler = new AvailabilityResponseHandler(this, mSearchParams, mProperty);
				return handler.handleJson(obj);
			}
			catch (JSONException e) {
				Log.w("Something bad happened", e);
				return null;
			}
		}
		else {
			ExpediaServices services = new ExpediaServices(this, mSession);
			return services.availability(mSearchParams, mProperty);
		}
	}

	@Override
	public void onResults(Object results) {
		mProgressBar.setVisibility(View.GONE);

		if (results == null) {
			TrackingUtils.trackErrorPage(this, "RatesListRequestFailed");
			mEmptyTextView.setText(R.string.error_no_response_room_rates);
			return;
		}

		AvailabilityResponse response = (AvailabilityResponse) results;

		mSession = response.getSession();

		if (response.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			for (ServerError error : response.getErrors()) {
				sb.append(error.getPresentableMessage(this));
				sb.append("\n");
			}
			mEmptyTextView.setText(sb.toString().trim());
			TrackingUtils.trackErrorPage(this, "RatesListRequestFailed");
			return;
		}

		mAdapter = new RoomsAndRatesAdapter(this, response);

		setListAdapter(mAdapter);

		List<String> commonValueAdds = response.getCommonValueAdds();
		if (commonValueAdds != null) {
			mFooterTextView.setText(getString(R.string.common_value_add_template,
					FormatUtils.series(this, commonValueAdds, ",", Conjunction.AND)));
			mFooterTextView.setVisibility(View.VISIBLE);
		}

		if (mAdapter.getCount() == 0) {
			TrackingUtils.trackErrorPage(this, "HotelHasNoRoomsAvailable");
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	public void onPageLoad() {
		Log.d("Tracking \"App.Hotels.RoomsRates\" event");

		AppMeasurement s = new AppMeasurement(getApplication());

		TrackingUtils.addStandardFields(this, s);

		s.pageName = "App.Hotels.RoomsRates";

		// Promo description
		s.eVar9 = mProperty.getLowestRate().getPromoDescription();

		// Shopper/Confirmer
		s.eVar25 = s.prop25 = "Shopper";

		// Rating or highly rated
		TrackingUtils.addHotelRating(s, mProperty);

		// Products
		TrackingUtils.addProducts(s, mProperty);

		// Send the tracking data
		s.track();
	}
}
