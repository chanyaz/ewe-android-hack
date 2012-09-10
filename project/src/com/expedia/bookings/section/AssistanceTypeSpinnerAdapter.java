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
import com.expedia.bookings.data.Traveler.AssistanceType;
import com.mobiata.android.util.Ui;

public class AssistanceTypeSpinnerAdapter extends ArrayAdapter<CharSequence> {
	
	class AssistanceSpinnerHelper{
		AssistanceType mAssistanceType;
		String mAssistanceTypeStr;
		
		public AssistanceSpinnerHelper(AssistanceType assistanceType, String assistanceTypeStr){
			setAssistanceType(assistanceType);
			setAssistanceTypeString(assistanceTypeStr);
		}
		
		public void setAssistanceTypeString(String assistanceType){
			mAssistanceTypeStr = assistanceType;
		}
		public String getAssistanceTypeString(){
			return mAssistanceTypeStr;
		}
		
		public void setAssistanceType(AssistanceType assistanceType){
			mAssistanceType = assistanceType;
		}
		public AssistanceType getAssistanceType(){
			return mAssistanceType;
		}
	}
	
	private ArrayList<AssistanceSpinnerHelper> mAssistanceTypes;
	private String mFormatString = "%s";
	

	public AssistanceTypeSpinnerAdapter(Context context) {
		super(context, R.layout.simple_spinner_traveler_item);
		setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		fillAssistanceTypes(context);
	}
	
	public void setFormatString(String formatString){
		mFormatString = formatString;
	}

	@Override
	public int getCount() {
		return mAssistanceTypes.size();
	}

	@Override
	public CharSequence getItem(int position) {
		return mAssistanceTypes.get(position).getAssistanceTypeString();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View retView = super.getView(position, convertView, parent);
		TextView tv = Ui.findView(retView, android.R.id.text1);
		tv.setText(Html.fromHtml(String.format(mFormatString, getItem(position))));
		return retView;
	}
	
	@Override 
	public View getDropDownView(int position, View convertView, ViewGroup parent){
		View retView = super.getDropDownView(position, convertView, parent);
		//We can set formatting here if we want to
		return retView;
	}
	
	public AssistanceType getAssistanceType(int position){
		return mAssistanceTypes.get(position).getAssistanceType();
	}
	
	public int getAssistanceTypePosition(AssistanceType gender){
		if(gender == null){
			return -1;
		}
			
		for(int i = 0; i < mAssistanceTypes.size(); i++){
			if(mAssistanceTypes.get(i).getAssistanceType() == gender){
				return i;
			}
		}
		return -1;
	}

	private void fillAssistanceTypes(Context context) {
		final Resources res = context.getResources();
		mAssistanceTypes = new ArrayList<AssistanceSpinnerHelper>();
		mAssistanceTypes.add(new AssistanceSpinnerHelper(AssistanceType.NONE, res.getString(R.string.no_assistance)));
		mAssistanceTypes.add(new AssistanceSpinnerHelper(AssistanceType.WHEELCHAIR, res.getString(R.string.wheelchair)));
		mAssistanceTypes.add(new AssistanceSpinnerHelper(AssistanceType.DEFIBRILLATOR, res.getString(R.string.defibrillator)));
		mAssistanceTypes.add(new AssistanceSpinnerHelper(AssistanceType.SUPER_LONG_ASSISTANCE_TYPE, res.getString(R.string.super_long_assistance)));
		
	}
}