package com.expedia.bookings.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

public class FlightTravelerInfoOptionsActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {

	private static final int REQUEST_CODE_PICK_CONTACT = 1;

	private static final String CONTACT_URI = "CONTACT_URI";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flight_traveler_info_options);

		Button enterManually = Ui.findView(this, R.id.enter_info_manually_button);
		enterManually.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightTravelerInfoOptionsActivity.this,
						FlightTravelerInfoOneActivity.class);
				intent.fillIn(getIntent(), 0);
				startActivity(intent);
			}
		});

		Button fromContacts = Ui.findView(this, R.id.load_from_contacts_button);
		fromContacts.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent pickContactIntent = new Intent(Intent.ACTION_PICK);
				pickContactIntent.setData(ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(pickContactIntent, REQUEST_CODE_PICK_CONTACT);
			}
		});
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

		Ui.showToast(this, "Picked contact: " + data.getString(ContactQuery.DISPLAY_NAME));
		
		// TODO: Right now, we can't get much information about the person until we add
		// the READ_CONTACTS permission.  We need to have a discussion on whether we
		// want to add this permission at all to the app before moving forward on
		// this feature.

		// Shut down the query, now that we have the data
		getSupportLoaderManager().destroyLoader(ContactQuery._LOADER_ID);
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
