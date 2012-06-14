package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.DoubleKnobSlider;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.utils.DateTimeUtils;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.format.Time;
import android.widget.TextView;

public class DoubleKnobSliderTest extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_double_knob_slider_test);

		final DoubleKnobSlider dks = Ui.findView(this, R.id.double_knob);
		final TextView k1tv = Ui.findView(DoubleKnobSliderTest.this, R.id.knob_one_val);
		final TextView k2tv = Ui.findView(DoubleKnobSliderTest.this, R.id.knob_two_val);
		
		dks.setKnobOneChangeHandler(new Handler(){
			@Override
			public void handleMessage (Message msg){
				k1tv.setText(percentToTime(dks.getKnobOnePercentage()));
			}
		});
		dks.setKnobTwoChangeHandler(new Handler(){
			@Override
			public void handleMessage (Message msg){
				k2tv.setText(percentToTime(dks.getKnobTwoPercentage()));
			}
		});
	}
	
	public String percentToTime(double val){
		int dayMins = 60 * 24;
		int timeMins = (int) Math.round(val * dayMins);
		
		int hour = (int) Math.floor(timeMins/60);
		int min = timeMins%60;
		
		//round to 15
		min = min - min%15;
		
		String ampm = "am";
		
		if(hour > 12){
			hour = hour - 12;
			ampm = "pm";
		}
		
		if(hour == 0)
			hour = 12;
		
		return "" + hour + ":" + min + ampm + "  (" + val + ")";
		
		//DateTimeUtils.formatDuration(getResources(), timeMins);
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	private final int PICK_CONTACT = 1;
	private String poBox = "";
	private String name = "";
	private String phoneNumber = "";
	private String emailAddress = "";
	private String city = "";
	private String state = "";
	private String postalCode = "";
	private String country = "";
	private String type = "";
	private String street = "";

	public void printInfo() {
		Log.i("name:" + name + " poBox:" + poBox + " phNum:" + phoneNumber + " email:" + emailAddress + " city:" + city
				+ " type:" + type);
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {
				getContactInfo(data);
				//	        Uri contactData = data.getData();
				//	        Cursor c =  managedQuery(contactData, null, null, null, null);
				//	        if (c.moveToFirst()) {
				//	          String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				//	          Log.i("NAME:" + name);
				//	          ContactsContract.Contacts.
				//	          // TODO Whatever you want to do with the selected contact name.
				//	        }
			}
			break;
		}
	}

	protected void getContactInfo(Intent intent)
	{

		Cursor cursor = managedQuery(intent.getData(), null, null, null, null);
		
		for(int i =0; i < cursor.getColumnCount(); i++){
			Log.i("COL:" + cursor.getColumnName(i));
		}
		
		while (cursor.moveToNext())
		{
			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
			
			String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			
			
			
			if (hasPhone.equalsIgnoreCase("1"))
				hasPhone = "true";
			else
				hasPhone = "false";

			//	       if (Boolean.parseBoolean(hasPhone)) 
			//	       {
			//	        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,null, null);
			//	        while (phones.moveToNext()) 
			//	        {
			//	          phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			//	        }
			//	        phones.close();
			//	       }

			//	       // Find Email Addresses
			//	       Cursor emails = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,null, null);
			//	       while (emails.moveToNext()) 
			//	       {
			//	        emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
			//	       }
			//	       emails.close();
			//
			//	    Cursor address = getContentResolver().query(
			//	                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
			//	                null,
			//	                ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = " + contactId,
			//	                null, null);
			//	    while (address.moveToNext()) 
			//	    { 
			//	      // These are all private class variables, don't forget to create them.
			//	      poBox      = address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
			//	      street     = address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
			//	      city       = address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
			//	      state      = address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
			//	      postalCode = address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
			//	      country    = address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
			//	      type       = address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
			//	    }  //address.moveToNext()   
		} //while (cursor.moveToNext())        
		cursor.close();
		printInfo();
	}//getContactInfo

}
