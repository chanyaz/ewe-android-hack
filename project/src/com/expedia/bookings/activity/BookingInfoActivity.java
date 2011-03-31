package com.expedia.bookings.activity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.ImageCache;
import com.mobiata.hotellib.data.BillingInfo;
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

	private BillingInfo mBillingInfo;

	private LayoutInflater mInflater;

	// Cached data from arrays
	private String[] mStateCodes;
	private String[] mCountryCodes;

	// Cached views
	private EditText mFirstNameEditText;
	private EditText mLastNameEditText;
	private EditText mTelephoneEditText;
	private EditText mEmailEditText;
	private EditText mAddress1EditText;
	private EditText mAddress2EditText;
	private EditText mCityEditText;
	private EditText mPostalCodeEditText;
	private Spinner mStateSpinner;
	private EditText mStateEditText;
	private Spinner mCountrySpinner;
	private EditText mCardNumberEditText;
	private EditText mExpirationMonthEditText;
	private EditText mExpirationYearEditText;
	private EditText mSecurityCodeEditText;

	private TextView mSecurityCodeTipTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mInflater = getLayoutInflater();

		setContentView(R.layout.activity_booking_info);

		// Retrieve data to build this with
		final Intent intent = getIntent();
		mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY,
				Property.class);
		mSearchParams = (SearchParams) JSONUtils.parseJSONableFromIntent(intent,
				Codes.SEARCH_PARAMS,
				SearchParams.class);
		mRate = (Rate) JSONUtils.parseJSONableFromIntent(intent, Codes.RATE, Rate.class);

		// Retrieve some data we keep using
		Resources r = getResources();
		mStateCodes = r.getStringArray(R.array.state_codes);
		mCountryCodes = r.getStringArray(R.array.country_codes);

		// Retrieve views that we need for the form fields
		mFirstNameEditText = (EditText) findViewById(R.id.first_name_edit_text);
		mLastNameEditText = (EditText) findViewById(R.id.last_name_edit_text);
		mTelephoneEditText = (EditText) findViewById(R.id.telephone_edit_text);
		mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
		mAddress1EditText = (EditText) findViewById(R.id.address1_edit_text);
		mAddress2EditText = (EditText) findViewById(R.id.address2_edit_text);
		mCityEditText = (EditText) findViewById(R.id.city_edit_text);
		mPostalCodeEditText = (EditText) findViewById(R.id.postal_code_edit_text);
		mStateSpinner = (Spinner) findViewById(R.id.state_spinner);
		mStateEditText = (EditText) findViewById(R.id.state_edit_text);
		mCountrySpinner = (Spinner) findViewById(R.id.country_spinner);
		mCardNumberEditText = (EditText) findViewById(R.id.card_number_edit_text);
		mExpirationMonthEditText = (EditText) findViewById(R.id.expiration_month_edit_text);
		mExpirationYearEditText = (EditText) findViewById(R.id.expiration_year_edit_text);
		mSecurityCodeEditText = (EditText) findViewById(R.id.security_code_edit_text);

		// Other cached views
		mSecurityCodeTipTextView = (TextView) findViewById(R.id.security_code_tip_text_view);

		// Configure the layout
		configureTicket();
		configureForm();
		configureFooter();

		// Retrieve previous instance
		Instance lastInstance = (Instance) getLastNonConfigurationInstance();
		if (lastInstance != null) {
			this.mBillingInfo = lastInstance.mBillingInfo;
			syncFormFields();
		}
		else {
			mBillingInfo = new BillingInfo();
		}
	}

	private class Instance {
		public BillingInfo mBillingInfo;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		syncBillingInfo();

		Instance instance = new Instance();
		instance.mBillingInfo = this.mBillingInfo;
		return instance;
	}

	/**
	 * Syncs the local BillingInfo with data from the form fields.  Should be used before you want to access
	 * the local BillingInfo's data.
	 */
	private void syncBillingInfo() {
		// Start off with a clean slate
		mBillingInfo = new BillingInfo();

		mBillingInfo.setFirstName(mFirstNameEditText.getText().toString());
		mBillingInfo.setLastName(mLastNameEditText.getText().toString());
		mBillingInfo.setTelephone(mTelephoneEditText.getText().toString());
		mBillingInfo.setEmail(mEmailEditText.getText().toString());

		Location location = new Location();
		List<String> streetAddress = new ArrayList<String>();
		streetAddress.add(mAddress1EditText.getText().toString());
		String address2 = mAddress2EditText.getText().toString();
		if (address2 != null && address2.length() > 0) {
			streetAddress.add(address2);
		}
		location.setStreetAddress(streetAddress);
		location.setCity(mCityEditText.getText().toString());
		location.setPostalCode(mPostalCodeEditText.getText().toString());
		if (useStateSpinner()) {
			location.setStateCode(mStateCodes[mStateSpinner.getSelectedItemPosition()]);
		}
		else {
			location.setStateCode(mStateEditText.getText().toString());
		}
		location.setCountryCode(mCountryCodes[mCountrySpinner.getSelectedItemPosition()]);
		mBillingInfo.setLocation(location);

		mBillingInfo.setNumber(mCardNumberEditText.getText().toString());
		String expirationMonth = mExpirationMonthEditText.getText().toString();
		String expirationYear = mExpirationYearEditText.getText().toString();
		if (expirationMonth != null && expirationMonth.length() > 0 && expirationYear != null
				&& expirationYear.length() > 0) {
			Calendar cal = new GregorianCalendar(Integer.parseInt(expirationYear),
					Integer.parseInt(expirationMonth) - 1, 15);
			mBillingInfo.setExpirationDate(cal);
		}
		mBillingInfo.setSecurityCode(mSecurityCodeEditText.getText().toString());
	}

	/**
	 * Syncs the form fields with data from local BillingInfo.  Should be used when creating or
	 * restoring the Activity.
	 */
	private void syncFormFields() {
		mFirstNameEditText.setText(mBillingInfo.getFirstName());
		mLastNameEditText.setText(mBillingInfo.getLastName());
		mTelephoneEditText.setText(mBillingInfo.getTelephone());
		mEmailEditText.setText(mBillingInfo.getEmail());

		Location loc = mBillingInfo.getLocation();
		mAddress1EditText.setText(loc.getStreetAddress().get(0));
		if (loc.getStreetAddress().size() > 1) {
			mAddress2EditText.setText(loc.getStreetAddress().get(1));
		}
		mCityEditText.setText(loc.getCity());
		mPostalCodeEditText.setText(loc.getPostalCode());
		setSpinnerSelection(mCountrySpinner, mCountryCodes, loc.getCountryCode());
		if (useStateSpinner()) {
			setSpinnerSelection(mStateSpinner, mStateCodes, loc.getStateCode());
		}
		else {
			mStateEditText.setText(loc.getStateCode());
		}

		mCardNumberEditText.setText(mBillingInfo.getNumber());
		Calendar cal = mBillingInfo.getExpirationDate();
		if (cal != null) {
			mExpirationMonthEditText.setText((mBillingInfo.getExpirationDate().get(Calendar.MONTH) + 1) + "");
			mExpirationYearEditText.setText((mBillingInfo.getExpirationDate().get(Calendar.YEAR) - 2000) + "");
		}
		mSecurityCodeEditText.setText(mBillingInfo.getSecurityCode());
	}

	public boolean useStateSpinner() {
		String countryCode = mCountryCodes[mCountrySpinner.getSelectedItemPosition()];
		return countryCode.equals(getString(R.string.country_code_us))
				|| countryCode.equals(getString(R.string.country_code_ca));
	}

	private void configureTicket() {
		// Configure the booking summary at the top of the page
		ImageView thumbnailView = (ImageView) findViewById(R.id.thumbnail_image_view);
		if (mProperty.getThumbnail() != null) {
			ImageCache.getInstance().loadImage(mProperty.getThumbnail().getUrl(), thumbnailView);
		}
		else {
			thumbnailView.setVisibility(View.GONE);
		}

		TextView nameView = (TextView) findViewById(R.id.name_text_view);
		nameView.setText(mProperty.getName());

		Location location = mProperty.getLocation();
		TextView address1View = (TextView) findViewById(R.id.address1_text_view);
		address1View.setText(StrUtils.formatAddressStreet(location));
		TextView address2View = (TextView) findViewById(R.id.address2_text_view);
		address2View.setText(StrUtils.formatAddressCity(location));

		// Configure the details
		ViewGroup detailsLayout = (ViewGroup) findViewById(R.id.details_layout);
		addDetail(detailsLayout, R.string.room_type, mRate.getRoomDescription());

		addDetail(detailsLayout, R.string.GuestsLabel, StrUtils.formatGuests(this, mSearchParams));

		DateFormat medDf = android.text.format.DateFormat.getMediumDateFormat(this);
		String start = medDf.format(mSearchParams.getCheckInDate().getTime());
		String end = medDf.format(mSearchParams.getCheckOutDate().getTime());
		int numDays = (int) Math.round((mSearchParams.getCheckOutDate().getTimeInMillis() - mSearchParams
				.getCheckInDate().getTimeInMillis()) / (1000 * 60 * 60 * 24));
		String numNights = (numDays == 1) ? getString(R.string.stay_duration_one_night) : getString(
				R.string.stay_duration_template, numDays);
		addDetail(detailsLayout, R.string.CheckIn, start);
		addDetail(detailsLayout, R.string.CheckOut, end + "\n" + numNights);

		// If there's a breakdown list, show that; otherwise, show the nightly mRate
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
		if (mRate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : mRate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String dateStr = dateFormat.format(date);
				addDetail(detailsLayout, getString(R.string.room_rate_template, dateStr), breakdown.getAmount()
						.getFormattedMoney());
			}
		}
		else if (mRate.getDailyAmountBeforeTax() != null) {
			addDetail(detailsLayout, R.string.RatePerRoomPerNight, mRate.getDailyAmountBeforeTax().getFormattedMoney());
		}

		Money taxesAndFeesPerRoom = mRate.getTaxesAndFeesPerRoom();
		if (taxesAndFeesPerRoom != null && taxesAndFeesPerRoom.getFormattedMoney() != null
				&& taxesAndFeesPerRoom.getFormattedMoney().length() > 0) {
			addDetail(detailsLayout, R.string.TaxesAndFees, taxesAndFeesPerRoom.getFormattedMoney());
		}

		// Configure the total cost
		Money totalAmountAfterTax = mRate.getTotalAmountAfterTax();
		TextView totalView = (TextView) findViewById(R.id.total_cost_text_view);
		if (totalAmountAfterTax != null && totalAmountAfterTax.getFormattedMoney() != null
				&& totalAmountAfterTax.getFormattedMoney().length() > 0) {
			totalView.setText(totalAmountAfterTax.getFormattedMoney());
		}
		else {
			totalView.setText("Dan didn't account for no total info, tell him");
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

	private void configureForm() {
		// Set the default country as USA
		setSpinnerSelection(mCountrySpinner, getString(R.string.country_us));

		mCountrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (useStateSpinner()) {
					mStateSpinner.setVisibility(View.VISIBLE);
					mStateEditText.setVisibility(View.GONE);
				}
				else {
					mStateSpinner.setVisibility(View.GONE);
					mStateEditText.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Should not happen, do nothing
			}
		});
	}

	private void configureFooter() {
		// Configure the cancellation policy
		TextView cancellationPolicyView = (TextView) findViewById(R.id.cancellation_policy_text_view);
		boolean foundCancellationPolicy = false;
		for (Policy policy : mRate.getRateRules().getPolicies()) {
			if (policy.getType() == Policy.TYPE_CANCEL) {
				foundCancellationPolicy = true;
				cancellationPolicyView.setText(policy.getDescription());
			}
		}
		if (!foundCancellationPolicy) {
			cancellationPolicyView.setVisibility(View.GONE);
		}
	}

	private void setSpinnerSelection(Spinner spinner, String target) {
		spinner.setSelection(findAdapterIndex(spinner.getAdapter(), target));
	}

	private int findAdapterIndex(SpinnerAdapter adapter, String target) {
		int numItems = adapter.getCount();
		for (int n = 0; n < numItems; n++) {
			String name = (String) adapter.getItem(n);
			if (name.equalsIgnoreCase(target)) {
				return n;
			}
		}
		return -1;
	}

	private void setSpinnerSelection(Spinner spinner, String[] codes, String targetCode) {
		for (int n = 0; n < codes.length; n++) {
			if (targetCode.equals(codes[n])) {
				spinner.setSelection(n);
				return;
			}
		}
	}
}
