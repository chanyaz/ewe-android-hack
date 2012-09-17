package com.expedia.bookings.section;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler.SeatPreference;
import com.mobiata.android.util.Ui;

public class SeatPreferenceSpinnerAdapter extends ArrayAdapter<CharSequence> {
	class SeatPreferenceSpinnerHelper{
		SeatPreference mSeatPreference;
		String mSeatPreferenceStr;
		
		public SeatPreferenceSpinnerHelper(SeatPreference seatPreference, String seatPreferenceStr){
			setSeatPreference(seatPreference);
			setSeatPreferenceString(seatPreferenceStr);
		}
		
		public void setSeatPreferenceString(String seatPreference){
			mSeatPreferenceStr = seatPreference;
		}
		public String getSeatPreferenceString(){
			return mSeatPreferenceStr;
		}
		
		public void setSeatPreference(SeatPreference seatPreference){
			mSeatPreference = seatPreference;
		}
		public SeatPreference getSeatPreference(){
			return mSeatPreference;
		}
	}
	
	private ArrayList<SeatPreferenceSpinnerHelper> mSeatPreferences;
	private String mFormatString = "%s";
	

	public SeatPreferenceSpinnerAdapter(Context context) {
		super(context, R.layout.simple_spinner_traveler_item);
		setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		fillSeatPreferences(context);
	}
	
	public void setFormatString(String formatString){
		mFormatString = formatString;
	}

	@Override
	public int getCount() {
		return mSeatPreferences.size();
	}

	@Override
	public CharSequence getItem(int position) {
		return mSeatPreferences.get(position).getSeatPreferenceString();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View retView = super.getView(position, convertView, parent);
		TextView tv = Ui.findView(retView, android.R.id.text1);
		tv.setText(Html.fromHtml(String.format(mFormatString, getItem(position))));
		return retView;
	}
	
	public SeatPreference getSeatPreference(int position){
		return mSeatPreferences.get(position).getSeatPreference();
	}
	
	public int getSeatPreferencePosition(SeatPreference gender){
		if(gender == null){
			return -1;
		}
			
		for(int i = 0; i < mSeatPreferences.size(); i++){
			if(mSeatPreferences.get(i).getSeatPreference() == gender){
				return i;
			}
		}
		return -1;
	}

	private void fillSeatPreferences(Context context) {
		final Resources res = context.getResources();
		mSeatPreferences = new ArrayList<SeatPreferenceSpinnerHelper>();
		mSeatPreferences.add(new SeatPreferenceSpinnerHelper(SeatPreference.ANY, res.getString(R.string.any)));
		mSeatPreferences.add(new SeatPreferenceSpinnerHelper(SeatPreference.AISLE, res.getString(R.string.aisle)));
		mSeatPreferences.add(new SeatPreferenceSpinnerHelper(SeatPreference.WINDOW, res.getString(R.string.window)));
		
	}
}