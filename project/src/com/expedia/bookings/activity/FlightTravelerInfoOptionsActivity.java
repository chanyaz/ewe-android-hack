package com.expedia.bookings.activity;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.data.User;
import com.expedia.bookings.model.YoYo;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.mobiata.android.util.Ui;

public class FlightTravelerInfoOptionsActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {

	private static final int REQUEST_CODE_PICK_CONTACT = 1;
	private static final String CONTACT_URI = "CONTACT_URI";

	View mOverviewBtn;
	View mEnterManuallyBtn;
	View mFromContactsBtn;

	TextView mEditTravelerLabel;
	ViewGroup mEditTravelerContainer;
	ViewGroup mAssociatedTravelersContainer;

	int mCurrentPassengerIndex;
	FlightPassenger mCurrentPassenger;

	SectionTravelerInfo mPassengerContact;
	SectionTravelerInfo mPassengerPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flight_traveler_info_options);

		mCurrentPassengerIndex = getIntent().getIntExtra(Codes.PASSENGER_INDEX, 0);

		mEditTravelerContainer = Ui.findView(this, R.id.edit_traveler_container);
		mEditTravelerLabel = Ui.findView(this, R.id.edit_traveler_label);
		mAssociatedTravelersContainer = Ui.findView(this, R.id.associated_travelers_container);

		mOverviewBtn = Ui.findView(this, R.id.overview_btn);
		mOverviewBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightTravelerInfoOptionsActivity.this, FlightCheckoutActivity.class);
				startActivity(intent);
			}
		});

		mEnterManuallyBtn = Ui.findView(this, R.id.enter_info_manually_button);
		mEnterManuallyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Db.getFlightPassengers().set(mCurrentPassengerIndex, new FlightPassenger());
				gotoFirstDataEntryPage();
			}
		});

		mFromContactsBtn = Ui.findView(this, R.id.load_from_contacts_button);
		mFromContactsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent pickContactIntent = new Intent(Intent.ACTION_PICK);
				pickContactIntent.setData(ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(pickContactIntent, REQUEST_CODE_PICK_CONTACT);
			}
		});

		//Associated Travelers (From Expedia Account)
		mAssociatedTravelersContainer.removeAllViews();
		if (User.isLoggedIn(this)) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Service.LAYOUT_INFLATER_SERVICE);
			Resources res = getResources();
			for (int i = 0; i < Db.getUser().getAssociatedTravelers().size(); i++) {
				final FlightPassenger passenger = Db.getUser().getAssociatedTravelers().get(i);
				SectionTravelerInfo travelerInfo = (SectionTravelerInfo) inflater.inflate(
						R.layout.section_display_traveler_info_name, null);
				travelerInfo.bind(passenger);
				travelerInfo.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mCurrentPassenger = passenger;
						Db.getFlightPassengers().set(mCurrentPassengerIndex, passenger);
						//TODO: In the future we hope that stored travelers will have all the traveler data required
						//At that time we will not need to go to the entry pages at all
						gotoFirstDataEntryPage();
					}
				});

				mAssociatedTravelersContainer.addView(travelerInfo);
				
				//Add divider
				View divider = new View(this);
				LinearLayout.LayoutParams divLayoutParams = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, res.getDimensionPixelSize(R.dimen.simple_grey_divider_height));
				divLayoutParams.setMargins(0, res.getDimensionPixelSize(R.dimen.simple_grey_divider_margin_top), 0,
						res.getDimensionPixelSize(R.dimen.simple_grey_divider_margin_bottom));
				divider.setLayoutParams(divLayoutParams);
				divider.setBackgroundColor(res.getColor(R.color.divider_grey));
				mAssociatedTravelersContainer.addView(divider);
			}
		}

		//Selected traveler
		mCurrentPassenger = Db.getFlightPassengers().get(mCurrentPassengerIndex);

		mPassengerContact = Ui.findView(this, R.id.current_traveler_contact);
		mPassengerPrefs = Ui.findView(this, R.id.current_traveler_prefs);

		mPassengerContact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightTravelerInfoOptionsActivity.this,
						FlightTravelerInfoOneActivity.class);
				YoYo yoyo = new YoYo();
				yoyo.addYoYoTrick(FlightTravelerInfoOptionsActivity.class);
				intent.putExtra(Codes.PASSENGER_INDEX, mCurrentPassengerIndex);
				intent.putExtra(YoYo.TAG_YOYO, yoyo);
				startActivity(intent);
			}
		});

		mPassengerPrefs.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightTravelerInfoOptionsActivity.this,
						FlightTravelerInfoTwoActivity.class);
				YoYo yoyo = new YoYo();
				yoyo.addYoYoTrick(FlightTravelerInfoOptionsActivity.class);
				intent.putExtra(Codes.PASSENGER_INDEX, mCurrentPassengerIndex);
				intent.putExtra(YoYo.TAG_YOYO, yoyo);
				startActivity(intent);
			}
		});

		refreshCurrentPassenger();
	}

	public void refreshCurrentPassenger() {
		if (!mCurrentPassenger.hasName()) {
			mEditTravelerContainer.setVisibility(View.GONE);
			mEditTravelerLabel.setVisibility(View.GONE);
		}
		else {
			mEditTravelerContainer.setVisibility(View.VISIBLE);
			mEditTravelerLabel.setVisibility(View.VISIBLE);
		}

		mPassengerContact.bind(mCurrentPassenger);
		mPassengerPrefs.bind(mCurrentPassenger);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_PICK_CONTACT) {
			if (resultCode == RESULT_OK && data.getData() != null) {
				Bundle args = new Bundle();
				args.putParcelable(CONTACT_URI, data.getData());
				getSupportLoaderManager().initLoader(ContactQuery._LOADER_ID, args, this);
			}
		}
	}

	protected void gotoFirstDataEntryPage() {
		Intent intent = new Intent(FlightTravelerInfoOptionsActivity.this, FlightTravelerInfoOneActivity.class);
		YoYo yoyo = new YoYo();
		yoyo.addYoYoTrick(FlightTravelerInfoTwoActivity.class);
		yoyo.addYoYoTrick(FlightCheckoutActivity.class);
		intent.putExtra(Codes.PASSENGER_INDEX, mCurrentPassengerIndex);
		intent.putExtra(YoYo.TAG_YOYO, yoyo);
		startActivity(intent);
	}

	//////////////////////////////////////////////////////////////////////////
	// Implementation of LoaderCallbacks<Cursor>

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = args.getParcelable(CONTACT_URI);
		return new CursorLoader(this, uri, ContactQuery.PROJECTION, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		data.moveToFirst();

		ArrayList<String> validNameParts = new ArrayList<String>();
		String[] nameparts = data.getString(ContactQuery.DISPLAY_NAME).split(" ");
		for (int i = 0; i < nameparts.length; i++) {
			nameparts[i] = nameparts[i].trim();
			if (!TextUtils.isEmpty(nameparts[i])) {
				validNameParts.add(nameparts[i]);
			}
		}

		FlightPassenger passenger = new FlightPassenger();
		if (validNameParts.size() > 0) {
			passenger.setFirstName(validNameParts.get(0));
		}
		if (validNameParts.size() == 2) {
			passenger.setLastName(validNameParts.get(1));
		}
		if (validNameParts.size() >= 3) {
			passenger.setMiddleName(validNameParts.get(1));
			passenger.setLastName(validNameParts.get(2));
		}

		
		Db.getFlightPassengers().set(mCurrentPassengerIndex, passenger);
		

		// Shut down the query, now that we have the data
		getSupportLoaderManager().destroyLoader(ContactQuery._LOADER_ID);

		gotoFirstDataEntryPage();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Do nothing, should never happen
	}

	//////////////////////////////////////////////////////////////////////////
	// Query definition

	public interface ContactQuery {
		int _LOADER_ID = 0x01;

		String[] PROJECTION = {
				Contacts._ID,
				Contacts.DISPLAY_NAME,
		};

		int _ID = 0;
		int DISPLAY_NAME = 1;
	}
}
