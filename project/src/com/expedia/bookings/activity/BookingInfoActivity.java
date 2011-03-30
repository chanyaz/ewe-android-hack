package com.expedia.bookings.activity;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.ImageCache;
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

public class BookingInfoActivity extends Activity {

	private SearchParams mSearchParams;
	private Property mProperty;
	private Rate mRate;

	private LayoutInflater mInflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mInflater = getLayoutInflater();

		setContentView(R.layout.activity_booking_info);

		// Retrieve data to build this with
		final Intent intent = getIntent();
		Property property = mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY,
				Property.class);
		SearchParams searchParams = mSearchParams = (SearchParams) JSONUtils.parseJSONableFromIntent(intent,
				Codes.SEARCH_PARAMS,
				SearchParams.class);
		Rate rate = mRate = (Rate) JSONUtils.parseJSONableFromIntent(intent, Codes.RATE, Rate.class);

		// Configure the booking summary at the top of the page
		ImageView thumbnailView = (ImageView) findViewById(R.id.thumbnail_image_view);
		if (property.getThumbnail() != null) {
			ImageCache.getInstance().loadImage(property.getThumbnail().getUrl(), thumbnailView);
		}
		else {
			thumbnailView.setVisibility(View.GONE);
		}

		TextView nameView = (TextView) findViewById(R.id.name_text_view);
		nameView.setText(property.getName());

		Location location = property.getLocation();
		TextView address1View = (TextView) findViewById(R.id.address1_text_view);
		address1View.setText(StrUtils.formatAddressStreet(location));
		TextView address2View = (TextView) findViewById(R.id.address2_text_view);
		address2View.setText(StrUtils.formatAddressCity(location));

		// Configure the details
		ViewGroup detailsLayout = (ViewGroup) findViewById(R.id.details_layout);
		addDetail(detailsLayout, R.string.room_type, rate.getRoomDescription());

		addDetail(detailsLayout, R.string.GuestsLabel, StrUtils.formatGuests(this, searchParams));

		DateFormat medDf = android.text.format.DateFormat.getMediumDateFormat(this);
		String start = medDf.format(searchParams.getCheckInDate().getTime());
		String end = medDf.format(searchParams.getCheckOutDate().getTime());
		int numDays = (int) Math.round((searchParams.getCheckOutDate().getTimeInMillis() - searchParams
				.getCheckInDate().getTimeInMillis()) / (1000 * 60 * 60 * 24));
		String numNights = (numDays == 1) ? getString(R.string.stay_duration_one_night) : getString(
				R.string.stay_duration_template, numDays);
		addDetail(detailsLayout, R.string.CheckIn, start);
		addDetail(detailsLayout, R.string.CheckOut, end + "\n" + numNights);

		// If there's a breakdown list, show that; otherwise, show the nightly rate
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
		if (rate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : rate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String dateStr = dateFormat.format(date);
				addDetail(detailsLayout, getString(R.string.room_rate_template, dateStr), breakdown.getAmount()
						.getFormattedMoney());
			}
		}
		else if (rate.getDailyAmountBeforeTax() != null) {
			addDetail(detailsLayout, R.string.RatePerRoomPerNight, rate.getDailyAmountBeforeTax().getFormattedMoney());
		}

		Money taxesAndFeesPerRoom = rate.getTaxesAndFeesPerRoom();
		if (taxesAndFeesPerRoom != null && taxesAndFeesPerRoom.getFormattedMoney() != null
				&& taxesAndFeesPerRoom.getFormattedMoney().length() > 0) {
			addDetail(detailsLayout, R.string.TaxesAndFees, taxesAndFeesPerRoom.getFormattedMoney());
		}

		// Configure the total cost
		Money totalAmountAfterTax = rate.getTotalAmountAfterTax();
		TextView totalView = (TextView) findViewById(R.id.total_cost_text_view);
		if (totalAmountAfterTax != null && totalAmountAfterTax.getFormattedMoney() != null
				&& totalAmountAfterTax.getFormattedMoney().length() > 0) {
			totalView.setText(totalAmountAfterTax.getFormattedMoney());
		}
		else {
			totalView.setText("Dan didn't account for no total info, tell him");
		}

		// Configure the cancellation policy
		TextView cancellationPolicyView = (TextView) findViewById(R.id.cancellation_policy_text_view);
		boolean foundCancellationPolicy = false;
		for (Policy policy : rate.getRateRules().getPolicies()) {
			if (policy.getType() == Policy.TYPE_CANCEL) {
				foundCancellationPolicy = true;
				cancellationPolicyView.setText(policy.getDescription());
			}
		}
		if (!foundCancellationPolicy) {
			cancellationPolicyView.setVisibility(View.GONE);
		}
	}

	private void addDetail(ViewGroup parent, int labelStrId, String value) {
		addDetail(parent, getString(labelStrId), value);
	}

	private void addDetail(ViewGroup parent, String label, String value) {
		View detailRow = mInflater.inflate(R.layout.snippet_booking_detail, parent, false);
		TextView labelView = (TextView) detailRow.findViewById(R.id.label_text_view);
		labelView.setText(label);
		TextView valueView = (TextView) detailRow.findViewById(R.id.value_text_view);
		valueView.setText(value);
		parent.addView(detailRow);
	}
}
